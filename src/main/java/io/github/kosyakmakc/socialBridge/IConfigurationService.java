package io.github.kosyakmakc.socialBridge;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IConfigurationService {
    CompletableFuture<String> get(IBridgeModule module, String parameter, String defaultValue);
    CompletableFuture<Boolean> set(IBridgeModule module, String parameter, String value);

    CompletableFuture<String> get(UUID moduleId, String parameter, String defaultValue);
    CompletableFuture<Boolean> set(UUID moduleId, String parameter, String value);
}
