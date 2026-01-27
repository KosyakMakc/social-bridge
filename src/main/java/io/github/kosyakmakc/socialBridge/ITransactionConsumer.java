package io.github.kosyakmakc.socialBridge;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface ITransactionConsumer<T> {
    CompletableFuture<T> accept(ITransaction transaction);
}
