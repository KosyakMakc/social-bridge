package io.github.kosyakmakc.socialBridge.Commands.Arguments;

import java.util.concurrent.CompletableFuture;

public interface ICommandArgumentSuggestions {

    public CompletableFuture<String[]> getSuggestions();
}
