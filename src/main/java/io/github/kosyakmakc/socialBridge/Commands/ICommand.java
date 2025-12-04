package io.github.kosyakmakc.socialBridge.Commands;

import io.github.kosyakmakc.socialBridge.Commands.Arguments.CommandArgument;
import io.github.kosyakmakc.socialBridge.IBridgeModule;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ICommand {
    CompletableFuture<Void> enable(IBridgeModule bridge);
    CompletableFuture<Void> disable();

    @SuppressWarnings("rawtypes")
    List<CommandArgument> getArgumentDefinitions();

    String getLiteral();
}
