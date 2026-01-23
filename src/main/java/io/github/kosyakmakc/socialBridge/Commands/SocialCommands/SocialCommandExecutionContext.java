package io.github.kosyakmakc.socialBridge.Commands.SocialCommands;

import io.github.kosyakmakc.socialBridge.SocialPlatforms.ISocialMessage;
import io.github.kosyakmakc.socialBridge.SocialPlatforms.SocialUser;

public class SocialCommandExecutionContext {
    private ISocialMessage message;

    public SocialCommandExecutionContext (ISocialMessage message) {
        this.message = message;
    }

    public ISocialMessage getMessage() {
        return message;
    }

    public SocialUser getSender() {
        return message.getAuthor();
    }
}
