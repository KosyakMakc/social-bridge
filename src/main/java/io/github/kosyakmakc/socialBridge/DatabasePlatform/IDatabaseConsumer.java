package io.github.kosyakmakc.socialBridge.DatabasePlatform;

import java.util.concurrent.CompletableFuture;

import io.github.kosyakmakc.socialBridge.ITransaction;

@FunctionalInterface
public interface IDatabaseConsumer<T> {
    CompletableFuture<T> accept(ITransaction transaction);
}
