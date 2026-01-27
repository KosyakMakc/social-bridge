package io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.ArgumentFormatException;
import io.github.kosyakmakc.socialBridge.Commands.ICommand;

public interface IMinecraftCommand extends ICommand {
    String getPermission();

    void handle(MinecraftCommandExecutionContext context) throws ArgumentFormatException;
}
