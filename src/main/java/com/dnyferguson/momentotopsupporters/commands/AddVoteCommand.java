package com.dnyferguson.momentotopsupporters.commands;

import com.dnyferguson.momentotopsupporters.MomentoTopSupporters;
import com.dnyferguson.momentotopsupporters.database.FindResultCallback;
import com.dnyferguson.momentotopsupporters.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AddVoteCommand implements CommandExecutor {

    private MomentoTopSupporters plugin;

    public AddVoteCommand(MomentoTopSupporters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("topsupporters.addvote")) {
            sender.sendMessage(Chat.format("&cYou don\'t have permission to do this."));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(Chat.format("&cInvalid syntax. Use /addvote (name/uuid)"));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(Chat.format("&cPlayer not found."));
            return true;
        }

        addVote(sender, player);
        return true;
    }

    private void addVote(CommandSender sender, Player player) {
        String uuid = player.getUniqueId().toString();
        String name = player.getName();

        // Handle adding vote
        plugin.getSql().getResultAsync("SELECT * FROM `TopVoterRecent` WHERE `UUID` = '" + uuid + "'", new FindResultCallback() {
            @Override
            public void onQueryDone(ResultSet result) throws SQLException {
                if (result.next()) {
                    int currentVotes = result.getInt("Votes");
                    sender.sendMessage(Chat.format("&aYou have successfully added a vote to " + name + "\'s topvoter count. They now have " + (currentVotes + 1) + " votes."));
                    plugin.getSql().executeStatementAsync("UPDATE `TopVoterRecent` SET `PLAYERNAME` = '" + name + "',Votes = '" + (currentVotes + 1) + "',Creation = '" + System.currentTimeMillis() + "' WHERE UUID = '" + uuid + "';");
                } else {
                    plugin.getSql().executeStatementAsync("INSERT INTO `TopVoterRecent` (`UUID`,`PLAYERNAME`, `Votes`,`Creation`) VALUES ('" + uuid + "', '" + name + "', '1', '" + System.currentTimeMillis() + "');");
                }
            }
        });

        // Handle adding streak
        plugin.getSql().getResultAsync("SELECT * FROM `TopVoterStreak` WHERE `uuid` = '" + uuid + "'", new FindResultCallback() {
            @Override
            public void onQueryDone(ResultSet result) throws SQLException {
                if (result.next()) {
                    Timestamp time = result.getTimestamp("time");
                    int streak = result.getInt("streak");
                    if (time.getTime() > com.dnyferguson.momentotopsupporters.utils.Time.getBackwards("2d").getTime()) {
                        plugin.getSql().executeStatementAsync("UPDATE `TopVoterStreak` SET `ign`='" + name + "',`streak`='" + (streak + 1) + "',`time`=CURRENT_TIMESTAMP WHERE `uuid` = '" + uuid + "'");
                    } else {
                        plugin.getSql().executeStatementAsync("UPDATE `TopVoterStreak` SET `ign`='" + name + "',`streak`='1',`time`=CURRENT_TIMESTAMP WHERE `uuid` = '" + uuid + "'");
                    }
                } else {
                    plugin.getSql().executeStatementAsync("INSERT INTO `TopVoterStreak` (`uuid`, `ign`, `streak`, `time`) VALUES ('" + uuid + "', '" + name + "', '1', CURRENT_TIMESTAMP)");
                }
            }
        });

        // Handle removing reminder
        if (plugin.getReminderTimers().containsKey(player.getUniqueId())) {
            plugin.getReminderTimers().get(player.getUniqueId()).cancel();
            plugin.getReminderTimers().remove(player.getUniqueId());
        }
    }
}
