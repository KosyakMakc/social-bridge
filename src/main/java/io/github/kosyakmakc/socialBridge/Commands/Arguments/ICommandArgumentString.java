package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import java.util.concurrent.CompletableFuture;

public interface ICommandArgumentString {

    public CompletableFuture<String[]> getAutoCompletes();
}
