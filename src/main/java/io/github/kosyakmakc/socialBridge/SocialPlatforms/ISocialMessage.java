package io.github.kosyakmakc.socialBridge.SocialPlatforms;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public interface ISocialMessage {
    Identifier getChannelId(); // how to mark private\public single\group chats?
    Identifier getId();

    SocialUser getAuthor();
    String getStringMessage();
    Collection<ISocialAttachment> getAttachments();

    CompletableFuture<Boolean> sendReply(String message, HashMap<String, String> placeholders);
}
