package io.github.kosyakmakc.socialBridge.paper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands.IMinecraftCommand;
import io.github.kosyakmakc.socialBridge.DatabasePlatform.LocalizationService;
import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.MinecraftUser;
import io.github.kosyakmakc.socialBridge.SocialBridge;
import io.github.kosyakmakc.socialBridge.Utils.Version;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public final class AuthBridgePaper extends JavaPlugin implements IMinecraftPlatform {
    private static final CommandArgument<String> systemWordArgument = CommandArgument.ofWord("/{pluginSuffix} {commandLiteral} [arguments, ...]");

    private ISocialBridge authBridge;
    private final Version socialBridgVersion;

    public AuthBridgePaper() {
        try {
            socialBridgVersion = new Version(this.getPluginMeta().getVersion());
            SocialBridge.Init(this);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEnable() {
        SocialBridge.INSTANCE.Start();
        new PaperEventListener(this);

        SetupCommands();
    }

    private void SetupCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {

            for (var module : authBridge.getModules()) {
                var mcCommands = module.getMinecraftCommands();
                if (mcCommands.isEmpty()) {
                    continue;
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

                    // Registering singleton handler on all command phase, bridge-command will be can handle invalid calls and then notice user
                    RequiredArgumentBuilder<CommandSourceStack, ?> prev = null;
                    for (var argument : bridgeCommand.getArgumentDefinitions()) {
                        var argumentNode = BuildArgumentNode(argument).executes(handler);

                        if (prev == null) {
                            cmd.then(argumentNode);
                        }
                        else {
                            prev.then(argumentNode);
                        }

                        prev = argumentNode;
                    }

                    rootLiteral.then(cmd);
                }
                commands.registrar().register(rootLiteral.build());
            }
        });
    }

    private Command<CommandSourceStack> HandleCommand(IMinecraftCommand bridgeCommand) {
        return ctx -> {
            var sender = ctx.getSource().getSender();

            var mcPlatformUser = sender instanceof Player player ? new BukkitMinecraftUser(player) : null;
            try {
                var args = ctx.getInput();
                var reader = new StringReader(args);

                // pumping "/{moduleSuffix}" in reader
                systemWordArgument.getValue(reader);

                // pumping {commandLiteral} in reader
                systemWordArgument.getValue(reader);

                bridgeCommand.handle(mcPlatformUser, reader);
            } catch (ArgumentFormatException e) {
                if (mcPlatformUser != null) {
                    mcPlatformUser.sendMessage(authBridge.getLocalizationService().getMessage(mcPlatformUser.getLocale(), e.getMessageKey()), new HashMap<>());
                }
                else {
                    getLogger().warning(authBridge.getLocalizationService().getMessage(LocalizationService.defaultLocale, e.getMessageKey()));
                }
            }
            return SINGLE_SUCCESS;
        };
    }

    @SuppressWarnings("rawtypes")
    private RequiredArgumentBuilder<CommandSourceStack, ?> BuildArgumentNode(CommandArgument argument) {
        var commandName = argument.getName();
        var dataType = argument.getDataType();

        return switch (dataType) {
            case Boolean -> Commands
                    .argument(commandName, BoolArgumentType.bool())
                    .suggests(new BridgeCommandSuggestionProvider(argument));
            case Integer -> Commands
                    .argument(commandName, IntegerArgumentType.integer())
                    .suggests(new BridgeCommandSuggestionProvider(argument));
            case Long -> Commands
                    .argument(commandName, LongArgumentType.longArg())
                    .suggests(new BridgeCommandSuggestionProvider(argument));
            case Float -> Commands
                    .argument(commandName, FloatArgumentType.floatArg())
                    .suggests(new BridgeCommandSuggestionProvider(argument));
            case Double -> Commands
                    .argument(commandName, DoubleArgumentType.doubleArg())
                    .suggests(new BridgeCommandSuggestionProvider(argument));
            case Word -> Commands
                    .argument(commandName, StringArgumentType.word())
                    .suggests(new BridgeCommandSuggestionProvider(argument));
            case String -> Commands
                    .argument(commandName, StringArgumentType.string())
                    .suggests(new BridgeCommandSuggestionProvider(argument));
            case GreedyString -> Commands
                    .argument(commandName, StringArgumentType.greedyString())
                    .suggests(new BridgeCommandSuggestionProvider(argument));
        };
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void setAuthBridge(ISocialBridge authBridge) {
        this.authBridge = authBridge;
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
    public MinecraftUser getUser(UUID minecraftId) {
        return new BukkitMinecraftUser(Bukkit.getPlayer(minecraftId));
    }

    @Override
    public String get(String parameter, String defaultValue) {
        if (Objects.equals(parameter, "connectionString")) {
            try {
                return "jdbc:sqlite:" + Path.of(getDataDirectory().toAbsolutePath().toString(), "social-bridge.sqlite");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.getConfig().getString(parameter, defaultValue);
    }

    @Override
    public boolean set(String parameter, String value) {
        try {
            this.getConfig().set(parameter, value);
            return true;
        }
        catch (Exception err) {
            this.getLogger().log(Level.SEVERE, "Failed to set parameter(" + parameter + "=" + value + ")", err);
            return false;
        }
    }

    @Override
    public Version getSocialBridgeVersion() {
        return socialBridgVersion;
    }
}
