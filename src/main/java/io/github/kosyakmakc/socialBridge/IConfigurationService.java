package io.github.kosyakmakc.socialBridge;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IConfigurationService {
    CompletableFuture<String> get(ISocialModule module, String parameter, String defaultValue);
    CompletableFuture<String> get(ISocialModule module, String parameter, String defaultValue, ITransaction transaction);
    CompletableFuture<Boolean> set(ISocialModule module, String parameter, String value);
    CompletableFuture<Boolean> set(ISocialModule module, String parameter, String value, ITransaction transaction);

    CompletableFuture<String> get(UUID moduleId, String parameter, String defaultValue);
    CompletableFuture<String> get(UUID moduleId, String parameter, String defaultValue, ITransaction transaction);
    CompletableFuture<Boolean> set(UUID moduleId, String parameter, String value);
    CompletableFuture<Boolean> set(UUID moduleId, String parameter, String value, ITransaction transaction);
}
