package io.github.kosyakmakc.socialBridge.Commands.SocialCommands;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.Commands.ICommand;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.SocialUser;

import java.io.StringReader;

public interface ISocialCommand extends ICommand {
    void handle(SocialUser sender, StringReader argsReader) throws ArgumentFormatException;
}
