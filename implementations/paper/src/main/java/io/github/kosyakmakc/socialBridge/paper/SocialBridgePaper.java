package io.github.kosyakmakc.socialBridge.paper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.github.kosyakmakc.socialBridge.Commands.ICommandWithArguments;
import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.Commands.Arguments.ICommandArgumentNumeric;
import io.github.kosyakmakc.socialBridge.Commands.Arguments.ICommandArgumentSuggestions;
import io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands.IMinecraftCommand;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.LocalizationService;
import io.github.kosyakmakc.socialBridge.DefaultModule;
import io.github.kosyakmakc.socialBridge.ITransaction;
import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.MinecraftUser;
import io.github.kosyakmakc.socialBridge.Modules.IModuleBase;
import io.github.kosyakmakc.socialBridge.Modules.IMinecraftModule;
import io.github.kosyakmakc.socialBridge.SocialBridge;
import io.github.kosyakmakc.socialBridge.Utils.Version;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public final class SocialBridgePaper extends JavaPlugin implements IMinecraftPlatform {
    public static final String PLATFORM_NAME = "paper";
    public static final UUID PLATFORM_ID = UUID.fromString("2a461647-2958-4e61-9429-12f0bb5c8d3c");

    private final Version socialBridgVersion;
    private final ISocialBridge socialBridge;
    private final UUID instanceId;


    public SocialBridgePaper() {
        try {
            this.saveDefaultConfig();
            socialBridgVersion = new Version(this.getPluginMeta().getVersion());

            UUID localInstanceId;
            try {
                localInstanceId = UUID.fromString(this.get(DefaultModule.MODULE_ID, "instanceID", "", null).join());
            }
            catch (IllegalArgumentException err) {
                localInstanceId = new UUID(0L, 0L);
            }
            if (localInstanceId.compareTo(new UUID(0L, 0L)) == 0) {
                localInstanceId = UUID.randomUUID();
                this.set(DefaultModule.MODULE_ID, "instanceID", localInstanceId.toString(), null).join();
            }

            instanceId = localInstanceId;

            SocialBridge.Init(this);
            socialBridge = SocialBridge.INSTANCE;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }

    @Override
    public UUID getId() {
        return PLATFORM_ID;
    }

    @Override
    public UUID getInstanceId() {
        return instanceId;
    }

    @Override
    public void onEnable() {
        new PaperEventListener(this);
    }

    @Override
    public void onDisable() {
        var socialPlatforms = List.copyOf(socialBridge.getSocialPlatforms());
        var modules = List.copyOf(socialBridge.getModules());

        CompletableFuture
            .allOf(socialPlatforms.stream().map(x -> socialBridge.disconnectSocialPlatform(x)).toArray(CompletableFuture[]::new))
            .thenCompose(Void -> CompletableFuture.allOf(modules.stream().map(x -> socialBridge.disconnectModule(x)).toArray(CompletableFuture[]::new)))
            .join();
    }

    @Override
    public CompletableFuture<Void> connectModule(IMinecraftModule module) {
        return CompletableFuture.runAsync(() -> {
            if (module.getLoader() instanceof JavaPlugin externalPlugin) {
                externalPlugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
                    var mcCommands = module.getMinecraftCommands();
                    if (mcCommands.isEmpty()) {
                        return;
                    }
                    
                    var rootLiteral = Commands.literal(module.getName());
                    
                    for(var bridgeCommand : mcCommands) {
                        var handler = HandleCommand(bridgeCommand);
                        
                        var cmd = Commands
                            .literal(bridgeCommand.getLiteral())
                            .executes(handler);
                        
                        var permission = bridgeCommand.getPermission();
                        if (!permission.isEmpty()) {
                            cmd.requires(sender -> sender.getSender().hasPermission(bridgeCommand.getPermission()));
                        }
                        
                        if (bridgeCommand instanceof ICommandWithArguments commandWithArguments) {
                            // Registering singleton handler on all command phase, bridge-command will be can handle invalid calls and then notice user
                            RequiredArgumentBuilder<CommandSourceStack, ?> prev = null;
                            for (var argument : commandWithArguments.getArgumentDefinitions()) {
                                var argumentNode = BuildArgumentNode(argument).executes(handler);
                                
                                if (prev == null) {
                                    cmd.then(argumentNode);
                                }
                                else {
                                    prev.then(argumentNode);
                                }
                                
                                prev = argumentNode;
                            }
                        }
                        
                        rootLiteral.then(cmd);
                    }
                    commands.registrar().register(rootLiteral.build());
                });
            }
            else {
                getLogger().warning("Detected not supported module loader for '" + module.getName() + "' module.");
            }
        })
        .thenRun(() -> {
            this.getServer().getOnlinePlayers().forEach(player -> player.updateCommands());
        });
    }
    
    private Command<CommandSourceStack> HandleCommand(IMinecraftCommand bridgeCommand) {
        return ctx -> {
            var sender = ctx.getSource().getSender();

            var mcPlatformUser = sender instanceof Player player ? new BukkitMinecraftUser(player, this) : null;
            // TODO what about another CommandSender?

            try {
                var commandContext = new PaperMinecraftCommandExecutionContext(mcPlatformUser, ctx.getInput());
                bridgeCommand.handle(commandContext);
            } catch (ArgumentFormatException e) {
                if (mcPlatformUser != null) {
                    socialBridge.getLocalizationService().getMessage(
                        socialBridge.getModule(DefaultModule.class),
                        mcPlatformUser.getLocale(),
                        e.getMessageKey(),
                        null
                    )
                    .thenAccept(msgTemplate -> 
                        mcPlatformUser.sendMessage(msgTemplate, new HashMap<>()));
                }
                else {
                    socialBridge.getLocalizationService().getMessage(
                        socialBridge.getModule(DefaultModule.class),
                        LocalizationService.defaultLocale,
                        e.getMessageKey(),
                        null
                    )
                    .thenAccept(msgTemplate -> 
                        getLogger().warning(msgTemplate));
                }
            }
            catch (Exception err) {
                err.printStackTrace();
            }
            return SINGLE_SUCCESS;
        };
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private RequiredArgumentBuilder<CommandSourceStack, ?> BuildArgumentNode(CommandArgument argument) {
        var commandName = argument.getName();
        var dataType = argument.getDataType();

        return switch (dataType) {
            case Boolean -> Commands
                    .argument(commandName, BoolArgumentType.bool())
                    .suggests(new BridgeCommandSuggestionProvider((ICommandArgumentSuggestions) argument));
            case Integer -> Commands
                    .argument(commandName, IntegerArgumentType.integer(((ICommandArgumentNumeric<Integer>) argument).getMin(), ((ICommandArgumentNumeric<Integer>) argument).getMax()));
            case Long -> Commands
                    .argument(commandName, LongArgumentType.longArg(((ICommandArgumentNumeric<Long>) argument).getMin(), ((ICommandArgumentNumeric<Long>) argument).getMax()));
            case Float -> Commands
                    .argument(commandName, FloatArgumentType.floatArg(((ICommandArgumentNumeric<Float>) argument).getMin(), ((ICommandArgumentNumeric<Float>) argument).getMax()));
            case Double -> Commands
                    .argument(commandName, DoubleArgumentType.doubleArg(((ICommandArgumentNumeric<Double>) argument).getMin(), ((ICommandArgumentNumeric<Double>) argument).getMax()));
            case Word -> Commands
                    .argument(commandName, StringArgumentType.word())
                    .suggests(new BridgeCommandSuggestionProvider((ICommandArgumentSuggestions) argument));
            case String -> Commands
                    .argument(commandName, StringArgumentType.string())
                    .suggests(new BridgeCommandSuggestionProvider((ICommandArgumentSuggestions) argument));
            case GreedyString -> Commands
                    .argument(commandName, StringArgumentType.greedyString())
                    .suggests(new BridgeCommandSuggestionProvider((ICommandArgumentSuggestions) argument));
        };
    }

    @Override
    public @NotNull Path getDataDirectory() throws IOException {
        var dataPath = super.getDataPath();
        Files.createDirectories(dataPath);
        return dataPath;
    }

    @Override
    public @NotNull Logger getLogger() {
        return super.getLogger();
    }

    @Override
    public CompletableFuture<MinecraftUser> tryGetUser(UUID minecraftId) {
        return CompletableFuture.supplyAsync(() -> {
            var onlinePlayer = getServer().getPlayer(minecraftId);
            if (onlinePlayer != null) {
                return new BukkitMinecraftUser(onlinePlayer, this);
            }
            else {
                return null;
            }
        })
        .thenCompose(bukkitUser -> {
            if (bukkitUser == null) {
                var fakeProfile = getServer()
                       .getOfflinePlayer(minecraftId)
                       .getPlayerProfile();

                return fakeProfile
                       .update()
                       .thenApply(profile -> fakeProfile == profile ? null : new OfflineBukkitMinecraftUser(profile, this));
            }
            else {
                return CompletableFuture.completedStage(bukkitUser);
            }
        });
    }

    @Override
    public CompletableFuture<String> get(IModuleBase module, String parameter, String defaultValue, ITransaction transaction) {
        return get(module.getId(), parameter, defaultValue, transaction);
    }

    @Override
    public CompletableFuture<String> get(UUID moduleId, String parameter, String defaultValue, ITransaction transaction) {
        return getFromConfig(moduleId, parameter, defaultValue, transaction);
    }

    private CompletableFuture<String> getFromConfig(UUID moduleId, String parameter, String defaultValue, ITransaction transaction) {
        return CompletableFuture.supplyAsync(() -> {
            var config = this.getConfig();

            var moduleSection = config.getConfigurationSection("module-" +  moduleId.toString());
            if (moduleSection == null) {
                return defaultValue;
            }

            return moduleSection.getString(parameter, defaultValue);
        });
    }

    @Override
    public CompletableFuture<Boolean> set(IModuleBase module, String parameter, String value, ITransaction transaction) {
        return set(module.getId(), parameter, value, transaction);
    }

    @Override
    public CompletableFuture<Boolean> set(UUID moduleId, String parameter, String value, ITransaction transaction) {
        return setToConfig(moduleId, parameter, value);
    }

    private CompletableFuture<Boolean> setToConfig(UUID moduleId, String parameter, String value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var config = this.getConfig();

                var moduleSection = config.getConfigurationSection("module-" + moduleId.toString());
                if (moduleSection == null) {
                    moduleSection = config.createSection("module-" + moduleId.toString());
                }

                moduleSection.set(parameter, value);
                this.saveConfig();

                getLogger().info("plugin configuration change: " + parameter + "=" + value);
                return true;
            }
            catch (Exception err) {
                this.getLogger().log(Level.SEVERE, "Failed to set parameter(" + parameter + "=" + value + ")", err);
                return false;
            }
        });
    }

    @Override
    public Version getSocialBridgeVersion() {
        return socialBridgVersion;
    }

    @Override
    public CompletableFuture<List<MinecraftUser>> getOnlineUsers() {
        var users = getServer()
                    .getOnlinePlayers()
                    .stream()
                    .map(player -> (MinecraftUser) new BukkitMinecraftUser(player, this))
                    .toList();
        return CompletableFuture.completedFuture(users);
    }
}
