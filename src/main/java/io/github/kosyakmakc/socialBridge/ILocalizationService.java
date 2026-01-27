package io.github.kosyakmakc.socialBridge;

import java.util.concurrent.CompletableFuture;

import io.github.kosyakmakc.socialBridge.Modules.IModuleBase;
import io.github.kosyakmakc.socialBridge.Modules.ITranslationsModule;
import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

public interface ILocalizationService {
    CompletableFuture<String> getMessage(IModuleBase module, String locale, MessageKey key, ITransaction transaction);
    CompletableFuture<Boolean> setMessage(IModuleBase module, String locale, MessageKey key, String localization, ITransaction transaction);
    CompletableFuture<Void> restoreLocalizationsOfModule(ITranslationsModule module);
}