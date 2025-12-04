package io.github.kosyakmakc.socialBridge.paper;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

import com.destroystokyo.paper.profile.PlayerProfile;

import io.github.kosyakmakc.socialBridge.DatabasePlatform.LocalizationService;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.IMinecraftPlatform;
import io.github.kosyakmakc.socialBridge.MinecraftPlatform.MinecraftUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

public class OfflineBukkitMinecraftUser extends MinecraftUser {
    private final IMinecraftPlatform platform;
    private final Logger logger;

    private final UUID playerId;
    private final String playerName;

    public OfflineBukkitMinecraftUser(PlayerProfile player, IMinecraftPlatform platform) {
        super();
        this.platform = platform;
        this.logger = Logger.getLogger(platform.getLogger().getName() + '.' + BukkitMinecraftUser.class.getSimpleName());

        this.playerId = player.getId();
        this.playerName = player.getName();
    }

    public String getName() {
        return playerName;
    }

    @Override
    public UUID getId() {
        return playerId;
    }

    public String getLocale() {
        return LocalizationService.defaultLocale;
    }

    @Override
    public boolean HasPermission(String permission) {
        return false;
    }

    @Override
    public void sendMessage(String message, HashMap<String, String> placeholders) {
        var builder = MiniMessage.builder()
                                 .tags(TagResolver.builder()
                                                  .resolver(StandardTags.defaults())
                                                  .build());

        for (var placeholderKey : placeholders.keySet()) {
            builder.editTags(x -> x.resolver(Placeholder.component(placeholderKey, Component.text(placeholders.get(placeholderKey)))));
        }

        var builtMessage = builder.build().deserialize(message).toString();
        logger.info("message to '" + this.getName() + "' - " + builtMessage);
    }
}
