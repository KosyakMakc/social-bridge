package io.github.kosyakmakc.socialBridge.DatabasePlatform;

@FunctionalInterface
public interface IDatabaseConsumer<T> {
    T accept(DatabaseContext databaseContext);
}
