package io.github.kosyakmakc.socialBridge.Commands;

import java.util.List;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;

public interface ICommandWithArguments extends ICommand {
    @SuppressWarnings("rawtypes")
    List<CommandArgument> getArgumentDefinitions();
}
