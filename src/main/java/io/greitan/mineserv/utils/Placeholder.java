package io.greitan.mineserv.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import io.greitan.mineserv.MineservRewards;

public class Placeholder extends PlaceholderExpansion  {
    private final MineservRewards plugin;

    // Get the plugin interface.
    public Placeholder(MineservRewards plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "mineserv";
    }

    @Override
    public String getAuthor() {
        return "Mineserv";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        // Voice icon placeholder "%mineserv_votes%"
        if (identifier.equalsIgnoreCase("votes")) {
        }
        return null;
    }
}
