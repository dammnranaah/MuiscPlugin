package com.devranaah.musicplugin;

import org.bukkit.ChatColor;
import org.bukkit.Note;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MusicCommand implements CommandExecutor {
    private final MusicPlugin plugin;

    public MusicCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("musicplugin.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /music <note>");
            return true;
        }

        try {
            Note note = Note.natural(1, Note.Tone.valueOf(args[0].toUpperCase()));
            plugin.getMusicManager().playNote(player, note);
            player.sendMessage(ChatColor.GREEN + "Playing note: " + args[0]);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid note! Valid notes are: A, B, C, D, E, F, G");
        }

        return true;
    }
} 