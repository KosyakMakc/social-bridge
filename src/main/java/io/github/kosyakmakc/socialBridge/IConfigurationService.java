package io.github.kosyakmakc.socialBridge;

import java.util.concurrent.CompletableFuture;

public interface IConfigurationService {
    CompletableFuture<String> get(IBridgeModule module, String parameter, String defaultValue);
    CompletableFuture<Boolean> set(IBridgeModule module, String parameter, String value);
}
