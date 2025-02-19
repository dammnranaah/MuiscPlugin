package com.devranaah.musicplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class MusicManager {
    private final MusicPlugin plugin;
    private final Map<String, MusicZone> musicZones;
    private final Map<String, List<String>> playlists;
    private final Map<UUID, Note> playerNotes;

    public MusicManager(MusicPlugin plugin) {
        this.plugin = plugin;
        this.musicZones = new ConcurrentHashMap<>();
        this.playlists = new ConcurrentHashMap<>();
        this.playerNotes = new HashMap<>();
        loadPlaylists();
    }

    private void loadPlaylists() {
        ConfigurationSection playlistSection = plugin.getConfig().getConfigurationSection("playlists");
        if (playlistSection != null) {
            for (String key : playlistSection.getKeys(false)) {
                List<String> tracks = playlistSection.getStringList(key);
                playlists.put(key, new ArrayList<>(tracks));
            }
        }
    }

    public void createMusicZone(String id, Location location, String playlist, int radius) {
        if (!playlists.containsKey(playlist)) {
            throw new IllegalArgumentException("Playlist " + playlist + " does not exist!");
        }
        
        MusicZone zone = new MusicZone(id, location, playlist, radius);
        musicZones.put(id, zone);
    }

    public void removeMusicZone(String id) {
        musicZones.remove(id);
    }

    public void updatePlayerMusic(Player player) {
        if (plugin.getPlayerSettings(player).isMuted()) {
            return;
        }

        Location playerLoc = player.getLocation();
        for (MusicZone zone : musicZones.values()) {
            double distance = playerLoc.distance(zone.getLocation());
            
            if (distance <= zone.getRadius()) {
                float volume = calculateVolume(distance, zone.getRadius());
                playMusic(player, zone, volume);
            }
        }
    }

    private float calculateVolume(double distance, int radius) {
        int fadeDistance = plugin.getConfig().getInt("defaults.fade_distance", 3);
        if (distance >= radius - fadeDistance) {
            return (float) ((radius - distance) / fadeDistance);
        }
        return 1.0f;
    }

    private void playMusic(Player player, MusicZone zone, float volume) {
        String currentTrack = zone.getCurrentTrack();
        if (currentTrack == null) return;

        // Adjust volume based on player settings
        volume *= plugin.getPlayerSettings(player).getVolume() / 100f;
        
        // Use ItemsAdder to play the custom sound
        player.playSound(player.getLocation(), currentTrack, volume, 1.0f);
    }

    public void stopAllMusic() {
        musicZones.clear();
    }

    public void reload() {
        stopAllMusic();
        playlists.clear();
        loadPlaylists();
    }

    public void playNote(Player player, Note note) {
        player.playNote(player.getLocation(), Instrument.PIANO, note);
        playerNotes.put(player.getUniqueId(), note);
    }

    public void stopAll() {
        playerNotes.clear();
    }

    public Note getPlayerNote(Player player) {
        return playerNotes.get(player.getUniqueId());
    }

    private static class MusicZone {
        private final String id;
        private final Location location;
        private final String playlist;
        private final int radius;
        private int currentTrackIndex;
        private long lastPlayTime;

        public MusicZone(String id, Location location, String playlist, int radius) {
            this.id = id;
            this.location = location;
            this.playlist = playlist;
            this.radius = radius;
            this.currentTrackIndex = 0;
            this.lastPlayTime = 0;
        }

        public Location getLocation() {
            return location;
        }

        public int getRadius() {
            return radius;
        }

        public String getCurrentTrack() {
            List<String> tracks = MusicPlugin.getInstance().getMusicManager().playlists.get(playlist);
            if (tracks == null || tracks.isEmpty()) return null;
            
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastPlayTime > 1000) { // Basic track switching logic
                currentTrackIndex = (currentTrackIndex + 1) % tracks.size();
                lastPlayTime = currentTime;
            }
            
            return tracks.get(currentTrackIndex);
        }
    }
} 