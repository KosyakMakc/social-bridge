package io.github.kosyakmakc.socialBridge.SocialPlatforms;

import io.github.kosyakmakc.socialBridge.IBridgeModule;
import io.github.kosyakmakc.socialBridge.ISocialBridge;
import io.github.kosyakmakc.socialBridge.Utils.Version;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public interface ISocialPlatform {
    String getPlatformName();
    Version getCompabilityVersion();

    CompletableFuture<Void> connectModule(IBridgeModule module);
    CompletableFuture<Void> disconnectModule(IBridgeModule module);

    CompletableFuture<Boolean> sendMessage(SocialUser telegramUser, String message, HashMap<String, String> placeholders);
    CompletableFuture<SocialUser> tryGetUser(Identifier id);

    CompletableFuture<Boolean> enable(ISocialBridge authBridge);
    CompletableFuture<Void> disable();
}
