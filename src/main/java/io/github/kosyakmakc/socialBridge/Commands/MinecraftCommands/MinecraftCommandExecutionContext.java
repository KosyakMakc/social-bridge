package io.github.kosyakmakc.socialBridge.Commands.MinecraftCommands;

import io.github.kosyakmakc.socialBridge.MinecraftPlatform.MinecraftUser;

public abstract class MinecraftCommandExecutionContext {
    private final MinecraftUser sender;

    public MinecraftCommandExecutionContext(MinecraftUser sender) {
        this.sender = sender;
    }

    public MinecraftUser getSender() {
        return sender;
    }

    public abstract String getMessage();

    public abstract String getFullMessage();
}
