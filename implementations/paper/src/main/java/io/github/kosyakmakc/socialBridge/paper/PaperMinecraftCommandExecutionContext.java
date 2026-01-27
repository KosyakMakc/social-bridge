package io.github.kosyakmakc.socialBridge.paper;

import java.io.StringReader;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands.MinecraftCommandExecutionContext;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.MinecraftUser;

public class PaperMinecraftCommandExecutionContext extends MinecraftCommandExecutionContext {
    private static final CommandArgument<String> systemWordArgument = CommandArgument.ofWord("/{pluginSuffix} {commandLiteral} [arguments, ...]");
    private static final CommandArgument<String> systemGreedyStringArgument = CommandArgument.ofGreedyString("[arguments, ...]");

    private final String fullMessage;
    private final String message;

    public PaperMinecraftCommandExecutionContext(MinecraftUser player, String fullMessage) {
        super(player);
        this.fullMessage = fullMessage;

        var argsReader = new StringReader(fullMessage);

        // pumping "/{moduleSuffix}" in reader, but result is ignored - Paper performs command routing.
        try {
            systemWordArgument.getValue(argsReader);

            // pumping {commandLiteral} in reader, but result is ignored - Paper performs command routing.
            systemWordArgument.getValue(argsReader);

            message = systemGreedyStringArgument.getValue(argsReader);
        } catch (ArgumentFormatException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFullMessage() {
        return fullMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
