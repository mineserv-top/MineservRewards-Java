package io.greitan.mineserv.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import io.greitan.mineserv.MineservRewards;
import io.greitan.mineserv.utils.Language;
import io.greitan.mineserv.utils.Logger;

public class MineservCommand implements CommandExecutor, TabCompleter {

    private final MineservRewards plugin;
    private final String lang;

    // Get the plugin and lang interfaces.
    public MineservCommand(MineservRewards plugin, String lang)
    {
        this.plugin = plugin;
        this.lang = lang;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length >= 1)
            {
                // Setup command - setup the configuration.
                if (args[0].equalsIgnoreCase("setup") && player.hasPermission("voice.setup"))
                {
                    String newHost = args[1];
                    String newPort = args[2];
                    String newKey = args[3];

                    if(Objects.nonNull(newHost) && Objects.nonNull(newPort) && Objects.nonNull(newKey)){
                        plugin.getConfig().set("config.host", newHost);
                        plugin.getConfig().set("config.port", newPort);
                        plugin.getConfig().set("config.secret-key", newKey);
                        plugin.saveConfig();
                        plugin.reloadConfig();
                        plugin.reload();

                        player.sendMessage(Component.text(Language.getMessage(lang, "cmd-setup-success")).color(NamedTextColor.AQUA));
                    } else {
                        player.sendMessage(Component.text(Language.getMessage(lang, "cmd-setup-invalid-data")).color(NamedTextColor.RED));
                    }
                }
                // Reload command - reload the configs.
                else if (args[0].equalsIgnoreCase("reload") && player.hasPermission("voice.reload"))
                {
                    plugin.reload();
                    player.sendMessage(Component.text(Language.getMessage(lang, "cmd-reload")).color(NamedTextColor.GREEN));
                }
                // Command select invalid.
                else
                {
                    player.sendMessage(Component.text(Language.getMessage(lang, "cmd-invalid-args")).color(NamedTextColor.RED));
                }
            }
        }
        // Commands runned by console.
        else if(args.length >= 1) 
        {
            // Reload command - reload the configs.
            if (args[0].equalsIgnoreCase("reload"))
            {
                plugin.reload();
                Logger.log(Component.text(Language.getMessage(lang, "cmd-reload")).color(NamedTextColor.GREEN));
            } 
            // Command not for console.
            else
            {
                sender.sendMessage(Component.text(Language.getMessage(lang, "cmd-not-player")).color(NamedTextColor.RED));
            }
        }
        // Invalid command arguments.
        else
        {
            sender.sendMessage(Component.text(Language.getMessage(lang, "cmd-invalid-args")).color(NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
        {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
    
        // Main command arguments.
        if (args.length == 1)
        {
            List<String> options = Arrays.asList( "setup", "reload");
            StringUtil.copyPartialMatches(args[0], options, completions);
        }
    
        // Setup command arguments.
        if (args.length == 2 && args[0].equalsIgnoreCase("setup"))
        {
            List<String> options = Arrays.asList("host port key");
            StringUtil.copyPartialMatches(args[1], options, completions);
        }
    
        Collections.sort(completions);
        return completions;
    }
    
}
