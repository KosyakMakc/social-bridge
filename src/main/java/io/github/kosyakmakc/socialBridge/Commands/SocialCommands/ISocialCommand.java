package io.github.kosyakmakc.socialBridge.Commands.SocialCommands;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.Commands.ICommand;

public interface ISocialCommand extends ICommand {
    void handle(SocialCommandExecutionContext context) throws ArgumentFormatException;
}
