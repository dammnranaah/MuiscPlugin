package com.devranaah.musicplugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class MusicPlugin extends JavaPlugin {
    private static MusicPlugin instance;
    private MusicManager musicManager;
    private FileConfiguration config;
    private Map<UUID, PlayerSettings> playerSettings;
    private BukkitTask updateTask;

    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config if it doesn't exist
        saveDefaultConfig();
        config = getConfig();
        
        // Initialize managers and settings
        playerSettings = new ConcurrentHashMap<>();
        musicManager = new MusicManager(this);
        
        // Register commands
        getCommand("music").setExecutor(new MusicCommand(this));
        
        // Register events
        getServer().getPluginManager().registerEvents(new MusicListener(this), this);
        
        // Start update task
        startUpdateTask();
        
        getLogger().info("MusicPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Stop all music
        musicManager.stopAllMusic();
        
        getLogger().info("MusicPlugin has been disabled!");
    }

    private void startUpdateTask() {
        int updateInterval = config.getInt("performance.update_interval", 20);
        updateTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                musicManager.updatePlayerMusic(player);
            }
        }, updateInterval, updateInterval);
    }

    public static MusicPlugin getInstance() {
        return instance;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public PlayerSettings getPlayerSettings(Player player) {
        return playerSettings.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerSettings());
    }

    public void reload() {
        reloadConfig();
        config = getConfig();
        musicManager.reload();
    }
}

class PlayerSettings {
    private boolean muted;
    private int volume;

    public PlayerSettings() {
        this.muted = false;
        this.volume = 100;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
    }
} 