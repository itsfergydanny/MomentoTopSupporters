package com.dnyferguson.momentotopsupporters.commands;

import com.dnyferguson.momentotopsupporters.MomentoTopSupporters;
import com.dnyferguson.momentotopsupporters.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.*;

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
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try (Connection con = plugin.getSql().getDatasource().getConnection()) {
                    String uuid = player.getUniqueId().toString();
                    String name = player.getName();
                    int voteCount = 1;

                    // handle checking daily limit
                    String ip = player.getAddress().getAddress().getHostAddress();
                    PreparedStatement pst = con.prepareStatement("SELECT * FROM `TopVoterDailyLimit` WHERE `ip` = '" + ip + "'");
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        int count = rs.getInt("count");
                        if (count >= 24) {
                            player.sendMessage(Chat.format("&cYou have reached your daily limit of votes. You are only allowed to vote on 3 accounts per day!"));
                            return;
                        }
                        pst = con.prepareStatement("UPDATE `TopVoterDailyLimit` SET `count`='" + (count + 1) + "' WHERE `ip` = '" + ip + "'");
                        pst.execute();
                    } else {
                        pst = con.prepareStatement("INSERT INTO `TopVoterDailyLimit`(`ip`, `count`) VALUES ('" + ip + "','1')");
                        pst.execute();
                    }

                    // handle adding vote
                    pst = con.prepareStatement("SELECT * FROM `TopVoterRecent` WHERE `UUID` = '" + uuid + "'");
                    rs = pst.executeQuery();
                    if (rs.next()) {
                        int currentVotes = rs.getInt("Votes");
                        voteCount = currentVotes + 1;
                        pst = con.prepareStatement("UPDATE `TopVoterRecent` SET `PLAYERNAME` = '" + name + "',Votes = '" + (currentVotes + 1) + "',Creation = '" + System.currentTimeMillis() + "' WHERE UUID = '" + uuid + "';");
                        pst.execute();
                    } else {
                        pst = con.prepareStatement("INSERT INTO `TopVoterRecent` (`UUID`,`PLAYERNAME`, `Votes`,`Creation`) VALUES ('" + uuid + "', '" + name + "', '1', '" + System.currentTimeMillis() + "');");
                        pst.execute();
                    }

                    // handle adding streak
                    pst = con.prepareStatement("SELECT * FROM `TopVoterStreak` WHERE `uuid` = '" + uuid + "'");
                    rs = pst.executeQuery();
                    if (rs.next()) {
                        Timestamp time = rs.getTimestamp("time");
                        int streak = rs.getInt("streak");
                        if (time.getTime() > com.dnyferguson.momentotopsupporters.utils.Time.getBackwards("2d").getTime()) {
                            pst = con.prepareStatement("UPDATE `TopVoterStreak` SET `ign`='" + name + "',`streak`='" + (streak + 1) + "',`time`=CURRENT_TIMESTAMP WHERE `uuid` = '" + uuid + "'");
                            pst.execute();
                        } else {
                            pst = con.prepareStatement("UPDATE `TopVoterStreak` SET `ign`='" + name + "',`streak`='1',`time`=CURRENT_TIMESTAMP WHERE `uuid` = '" + uuid + "'");
                            pst.execute();
                        }
                    } else {
                        pst = con.prepareStatement("INSERT INTO `TopVoterStreak` (`uuid`, `ign`, `streak`, `time`) VALUES ('" + uuid + "', '" + name + "', '1', CURRENT_TIMESTAMP)");
                        pst.execute();
                    }



                    sender.sendMessage(Chat.format("&aYou have successfully added a vote to " + name + "\'s topvoter count. They now have " + voteCount + " votes."));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
