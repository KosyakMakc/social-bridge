package io.github.kosyakmakc.socialBridge.paper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PaperEventListener implements Listener {
    public PaperEventListener(SocialBridgePaper minecraftPlatform) {
        minecraftPlatform.getServer().getPluginManager().registerEvents(this, minecraftPlatform);
    }

    @EventHandler
    public void OnPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().updateCommands();
    }
}
