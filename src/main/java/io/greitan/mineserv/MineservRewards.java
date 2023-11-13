package io.greitan.mineserv;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import io.greitan.mineserv.commands.MineservCommand;
import io.greitan.mineserv.methods.Methods;
import io.greitan.mineserv.network.Network;
import io.greitan.mineserv.utils.*;
import java.io.IOException;
import java.util.Objects;

import com.vexsoftware.votifier.VoteHandler;
import com.vexsoftware.votifier.NuVotifierBukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class MineservRewards extends JavaPlugin {
    private static @Getter MineservRewards instance;
    private @Getter boolean isRunned = false;
    private @Getter String host = "";
    private @Getter int port = 0;
    private @Getter String secretKey = "";
    private @Getter VoteHandler VoteHandler;

    private String lang;

    @Override
    public void onEnable() {
        instance = this;
        lang = "ru";
        Language.init(this);
    
        MineservCommand voiceCommand = new MineservCommand(this, lang);
        getCommand("mineserv").setExecutor(voiceCommand);
        getCommand("mineserv").setTabCompleter(voiceCommand);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholder(this).register();
        }

        new Methods(this);
        VoteHandler = createVoteHandler();
        this.reload();
    }

    @Override
    public void onDisable() {
        Network.stopWebServer();
    }

    public void reload() {
        saveDefaultConfig();
        reloadConfig();
        Logger.info(Language.getMessage(lang, "plugin-config-loaded"));
        Logger.info(Language.getMessage(lang, "plugin-command-execuror"));

        host = getConfig().getString("config.host");
        port = getConfig().getInt("config.port");
        secretKey = getConfig().getString("config.secret-key");

        isRunned = connect(host, port, secretKey);
    }

    public Boolean connect(String host, int port, String secretKey) {
        if (Objects.nonNull(host) && Objects.nonNull(secretKey))
        {
            Network.stopWebServer();
            try {
                return Network.startWebServer(host, port, secretKey);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else return false;
    }

    public NuVotifierBukkit createVoteHandler() {
        Server server = getServer();
        PluginManager pluginManager = server.getPluginManager();
        Plugin votifierPlugin = pluginManager.getPlugin("Votifier");

        if (votifierPlugin instanceof NuVotifierBukkit) {
            return (NuVotifierBukkit) votifierPlugin;
        } else {
            return null;
        }
    }
}