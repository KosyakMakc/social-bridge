package io.github.kosyakmakc.socialBridge.Utils;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class AsyncEvent<T> {
    private final Set<Consumer<T>> handlers = new CopyOnWriteArraySet<Consumer<T>>();

    public void addHandler(Consumer<T> handler) {
        handlers.add(handler);
    }

    public void removeHandler(Consumer<T> handler) {
        handlers.remove(handler);
    }

    public CompletableFuture<Void> invoke(T event) {
        return CompletableFuture.allOf(
            handlers
            .stream()
            .map(consumer -> CompletableFuture.runAsync(() -> {
                try {
                    consumer.accept(event);
                }
                catch (Exception err) {
                    err.printStackTrace();
                }
            }))
            .toArray(CompletableFuture[]::new)
        );
    }
}
