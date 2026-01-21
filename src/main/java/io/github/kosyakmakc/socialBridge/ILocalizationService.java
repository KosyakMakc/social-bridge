package io.github.kosyakmakc.socialBridge;

import java.util.concurrent.CompletableFuture;

import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

public interface ILocalizationService {
    CompletableFuture<String> getMessage(ISocialModule module, String locale, MessageKey key, ITransaction transaction);
    CompletableFuture<Boolean> setMessage(ISocialModule module, String locale, MessageKey key, String localization, ITransaction transaction);
    CompletableFuture<Void> restoreLocalizationsOfModule(ISocialModule module);
}