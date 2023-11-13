package io.greitan.mineserv.methods;

import org.bukkit.Bukkit;

import io.greitan.mineserv.MineservRewards;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.VoteHandler;
import com.vexsoftware.votifier.net.VotifierSession;

public class Methods {
    private static MineservRewards plugin;

    public Methods(MineservRewards plugin) {
        Methods.plugin = plugin;
    }

    public static void runMethods(String project, String username, String timestamp, String signature) {
        // Проверка, включен ли NuVotifier в конфиге
        if (plugin.getConfig().getBoolean("config.methods.NuVotifier.enabled")) {
            votifier(project, username, timestamp, signature);
        }

        // Проверка, включен ли Commands в конфиге
        if (plugin.getConfig().getBoolean("config.methods.Commands.enabled")) {
            commands(project, username, timestamp, signature);
        }

        // Проверка, включен ли MySqlRequests в конфиге
        if (plugin.getConfig().getBoolean("config.methods.MySqlRequests.enabled")) {
            mysql(project, username, timestamp, signature);
        }
    }

    private static void votifier(String project, String username, String timestamp, String signature) {
        Vote vote = new Vote("mineserv.top", username, "mineserv.top", timestamp);
        VoteHandler voteHandler = plugin.getVoteHandler();
        try {
            voteHandler.onVoteReceived(vote, VotifierSession.ProtocolVersion.UNKNOWN, "mineserv.top");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void commands(String project, String username, String timestamp, String signature) {
        List<String> commands = plugin.getConfig().getStringList("config.methods.Commands.commands");
        executeCommands(commands, username);
    }

    private static void executeCommands(List<String> commands, String username) {
        for (String command : commands) {
            String processedCommand = command.replace("$player", username);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
    }

    private static void mysql(String project, String username, String timestamp, String signature) {
        String url = plugin.getConfig().getString("config.methods.MySqlRequests.url");
        String database = plugin.getConfig().getString("config.methods.MySqlRequests.database");
        String dbUsername = plugin.getConfig().getString("config.methods.MySqlRequests.username");
        String dbPassword = plugin.getConfig().getString("config.methods.MySqlRequests.password");

        List<String> sqlQueries = plugin.getConfig().getStringList("config.methods.MySqlRequests.sqlQuery");

        try (Connection connection = DriverManager.getConnection(url + database, dbUsername, dbPassword)) {
            for (String sqlQuery : sqlQueries) {
                String processedQuery = sqlQuery.replace("$player", username);
                try (PreparedStatement preparedStatement = connection.prepareStatement(processedQuery)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
