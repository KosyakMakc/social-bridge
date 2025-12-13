package io.github.kosyakmakc.socialBridge.Commands.SocialCommands;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.IBridgeModule;
import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.SocialUser;
import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public abstract class SocialCommandBase implements ISocialCommand {
    private final String commandName;
    private final MessageKey description;
    @SuppressWarnings("rawtypes")
    private final List<CommandArgument> argumentDefinition;
    private ISocialBridge bridge = null;
    private Logger logger = null;

    public SocialCommandBase(String commandName, MessageKey description) {
        this(commandName, description, new ArrayList<>());
    }

    @SuppressWarnings("rawtypes")
    public SocialCommandBase(String commandName, MessageKey description, List<CommandArgument> argumentDefinition) {
        this.commandName = commandName;
        this.description = description;
        this.argumentDefinition = Collections.unmodifiableList(argumentDefinition);
    }

    @Override
    public String getLiteral() {
        return commandName;
    }

    @Override
    public MessageKey getDescription() {
        return description;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<CommandArgument> getArgumentDefinitions() {
        return argumentDefinition;
    }

    @Override
    public CompletableFuture<Void> enable(IBridgeModule module) {
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

    public abstract void execute(SocialUser sender, List<Object> args);

    @Override
    public void handle(SocialUser sender, StringReader argsReader) throws ArgumentFormatException {
        if (bridge == null) {
            Logger.getGlobal().info(this.getClass().getName() + " - initialization failed, skip handling");
            return;
        }

        var arguments = new LinkedList<>();

        for (var argument : getArgumentDefinitions()) {
            var valueItem = argument.getValue(argsReader);
            arguments.add(valueItem);
        }

        execute(sender, arguments);
    }

    protected ISocialBridge getBridge() {
        return bridge;
    }
}
