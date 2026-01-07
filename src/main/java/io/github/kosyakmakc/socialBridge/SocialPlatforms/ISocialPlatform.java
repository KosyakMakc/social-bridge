package io.github.kosyakmakc.socialBridge.SocialPlatforms;

import io.github.kosyakmakc.socialBridge.ISocialModule;
import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.Utils.Version;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ISocialPlatform {
    String getPlatformName();
    UUID getId();
    Version getCompabilityVersion();

    CompletableFuture<Void> connectModule(ISocialModule module);
    CompletableFuture<Void> disconnectModule(ISocialModule module);

    CompletableFuture<Boolean> sendMessage(SocialUser telegramUser, String message, HashMap<String, String> placeholders);
    CompletableFuture<SocialUser> tryGetUser(Identifier id);

    CompletableFuture<Boolean> enable(ISocialBridge authBridge);
    CompletableFuture<Void> disable();
}
