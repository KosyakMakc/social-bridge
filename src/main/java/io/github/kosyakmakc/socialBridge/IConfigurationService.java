package io.github.kosyakmakc.socialBridge;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.github.kosyakmakc.socialBridge.Modules.IModuleBase;

public interface IConfigurationService {
    CompletableFuture<String> get(IModuleBase module, String parameter, String defaultValue, ITransaction transaction);
    CompletableFuture<Boolean> set(IModuleBase module, String parameter, String value, ITransaction transaction);

    CompletableFuture<String> get(UUID moduleId, String parameter, String defaultValue, ITransaction transaction);
    CompletableFuture<Boolean> set(UUID moduleId, String parameter, String value, ITransaction transaction);
}
