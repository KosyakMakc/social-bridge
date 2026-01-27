package io.github.kosyakmakc.socialBridge.TestEnvironment;

import java.io.StringReader;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.Commands.SocialCommands.SocialCommandExecutionContext;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.ISocialMessage;

public class HeadlessSocialCommandExecutionContext extends SocialCommandExecutionContext {
    private static final CommandArgument<String> systemWordArgument = CommandArgument.ofWord("/{pluginSuffix_commandLiteral} [arguments, ...]");
    private static final CommandArgument<String> systemGreedyStringArgument = CommandArgument.ofGreedyString("[arguments, ...]");

    private final String commandMessage;

    public HeadlessSocialCommandExecutionContext(ISocialMessage message) {
        super(message);
        var fullMessage = message.getStringMessage();

        var argsReader = new StringReader(fullMessage);

        try {
            // pumping "/{moduleSuffix_commandLiteral}" in reader, but result is ignored - Paper performs command routing.
            systemWordArgument.getValue(argsReader);

            commandMessage = systemGreedyStringArgument.getValue(argsReader);
        } catch (ArgumentFormatException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFullMessage() {
        return getSocialMessage().getStringMessage();
    }

    @Override
    public String getMessage() {
        return commandMessage;
    }

}
