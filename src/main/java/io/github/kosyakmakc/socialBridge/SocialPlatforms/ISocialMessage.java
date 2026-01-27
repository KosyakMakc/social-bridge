package io.github.kosyakmakc.socialBridge.SocialPlatforms;

import java.util.Collection;

public interface ISocialMessage {
    Identifier getChannelId(); // how to mark private\public single\group chats?
    Identifier getId();

    SocialUser getAuthor();
    String getStringMessage();
    Collection<ISocialAttachment> getAttachments();
}
