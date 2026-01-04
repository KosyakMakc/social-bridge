package io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.ISocialModule;
import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.MinecraftUser;
import io.github.kosyakmakc.socialBridge.Utils.MessageKey;
import io.github.kosyakmakc.socialBridge.Utils.Permissions;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public abstract class MinecraftCommandBase implements IMinecraftCommand {
    private final String literal;
    private final MessageKey description;
    private final String permission;
    @SuppressWarnings("rawtypes")
    private final List<CommandArgument> argumentDefinition;
    private ISocialBridge bridge = null;
    private Logger logger = null;

    public MinecraftCommandBase(String literal, MessageKey description) {
        this(literal, description, Permissions.NO_PERMISSION);
    }

    public MinecraftCommandBase(String literal, MessageKey description, String permission) {
        this(literal, description, permission, new ArrayList<>());
    }

    @SuppressWarnings("rawtypes")
    public MinecraftCommandBase(String literal, MessageKey description, List<CommandArgument> argumentDefinition) {
        this(literal, description, Permissions.NO_PERMISSION, argumentDefinition);
    }

    @SuppressWarnings("rawtypes")
    public MinecraftCommandBase(String literal, MessageKey description, String permission, List<CommandArgument> argumentDefinition) {
        this.literal = literal;
        this.description = description;
        this.permission = permission;
        this.argumentDefinition = Collections.unmodifiableList(argumentDefinition);
    }

    @Override
    public CompletableFuture<Void> enable(ISocialModule module) {
        bridge = module.getBridge();
        logger = Logger.getLogger(bridge.getLogger().getName() + '.' + module.getName() + '.' + getLiteral());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> disable() {
        bridge = null;
        logger = null;
        return CompletableFuture.completedFuture(null);
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getLiteral() {
        return literal;
    }

    @Override
    public MessageKey getDescription() {
        return description;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<CommandArgument> getArgumentDefinitions() {
        return argumentDefinition;
    }

    public abstract void execute(MinecraftUser sender, List<Object> args);

    @Override
    public void handle(MinecraftUser sender, StringReader argsReader) throws ArgumentFormatException {
        if (bridge == null) {
            Logger.getGlobal().warning(this.getClass().getName() + " - initialization failed, skip handling");
            return;
        }

        var arguments = new LinkedList<>();

        for (var argument : getArgumentDefinitions()) {
            var valueItem = argument.getValue(argsReader);
            arguments.add(valueItem);
        }

        var permissionNode = getPermission();
        if (permissionNode.isEmpty()) {
            execute(sender, arguments);
        }
        else {
            if (sender == null) {
                return;
            }

            sender.hasPermission(permissionNode).thenAccept(hasPermission -> {
                if (hasPermission) {
                    execute(sender, arguments);
                }
            });
        }
    }

    protected ISocialBridge getBridge() {
        return bridge;
    }
}
