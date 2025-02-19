package com.devranaah.musicplugin;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class MusicListener implements Listener {
    private final MusicPlugin plugin;

    public MusicListener(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBossSpawn(EntitySpawnEvent event) {
        if (!plugin.getConfig().getBoolean("events.enabled", true)) {
            return;
        }

        if (isBossEntity(event.getEntityType())) {
            ConfigurationSection bossSection = plugin.getConfig().getConfigurationSection("events.triggers.boss_spawn");
            if (bossSection != null) {
                String playlist = bossSection.getString("playlist");
                int radius = bossSection.getInt("radius", 40);
                
                Location loc = event.getLocation();
                String zoneId = "boss_" + UUID.randomUUID().toString();
                plugin.getMusicManager().createMusicZone(zoneId, loc, playlist, radius);
            }
        }
    }

    @EventHandler
    public void onTimeChange(TimeSkipEvent event) {
        if (!plugin.getConfig().getBoolean("events.enabled", true)) {
            return;
        }

        if (event.getWorld().getTime() >= 13000 && event.getWorld().getTime() <= 13100) {
            // Night is starting
            ConfigurationSection nightSection = plugin.getConfig().getConfigurationSection("events.triggers.night_time");
            if (nightSection != null) {
                String playlist = nightSection.getString("playlist");
                int radius = nightSection.getInt("radius", 25);
                
                // Create ambient music zones at spawn
                Location spawnLoc = event.getWorld().getSpawnLocation();
                String zoneId = "night_" + UUID.randomUUID().toString();
                plugin.getMusicManager().createMusicZone(zoneId, spawnLoc, playlist, radius);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Clean up any music-related data when player leaves
        plugin.getMusicManager().stopAll();
    }

    private boolean isBossEntity(EntityType type) {
        return type == EntityType.ENDER_DRAGON || 
               type == EntityType.WITHER || 
               type == EntityType.ELDER_GUARDIAN ||
               type == EntityType.WARDEN;
    }
} 