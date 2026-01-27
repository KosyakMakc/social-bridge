package io.github.kosyakmakc.socialBridge.Commands;

import io.github.kosyakmakc.socialBridge.Modules.IModuleBase;
import io.github.kosyakmakc.socialBridge.Utils.MessageKey;

import java.util.concurrent.CompletableFuture;

public interface ICommand {
    CompletableFuture<Void> enable(IModuleBase bridge);
    CompletableFuture<Void> disable();

    String getLiteral();
    MessageKey getDescription();
}
