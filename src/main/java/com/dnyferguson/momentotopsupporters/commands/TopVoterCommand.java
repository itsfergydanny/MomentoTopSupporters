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

public class TopVoterCommand implements CommandExecutor {

    private MomentoTopSupporters plugin;

    public TopVoterCommand(MomentoTopSupporters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (args.length < 1) {
            StringBuilder message = new StringBuilder();
            message.append("&bTopvoter Commands: \n&b/topvoter info &7- &eSee prize information.\n&b/topvoter top &7- &eShow the current top 10.\n&b/topvoter last &7- &eShow last months top 10.\n" +
                    "&b/topvoter count &7- &eSee your current vote count.\n&b/topvoter streak &7- &eSee your current streak and rewards.\n&b/topvoter topstreak &7- &eSee top 10 streaks all-time.\n");
            if (sender.hasPermission("topsupporters.reset")) {
                message.append("&b/topvoter reset &7- &eReset the top 10 voters and send out the prizes.\n");
            }
            if (sender.hasPermission("topsupporters.addvote")) {
                message.append("&b/addvote (name/uuid) &7- &eAdd a vote to a player\'s topvoter count.");
            }

            sender.sendMessage(Chat.format(message.toString()));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "top":
                showTopTen(sender);
                break;
            case "count":
                if (player != null) {
                    getCount(player);
                } else {
                    sender.sendMessage(Chat.format("&cOnly players can do this."));
                }
                break;
            case "reset":
                if (sender.hasPermission("topsupporters.reset")) {
                    reset(sender);
                }
                break;
            case "last":
                showLastMonthsTopTen(sender);
                break;
            case "streak":
                if (player != null) {
                    getStreak(player);
                } else {
                    sender.sendMessage(Chat.format("&cOnly players can do this."));
                }
                break;
            case "info":
                showInfo(sender);
                break;
            case "topstreak":
                showTopTenStreak(sender);
                break;
        }
        return true;
    }

    private void reset(CommandSender sender) {
        plugin.getSql().executeStatementAsync("TRUNCATE TopVoterOld");
        plugin.getSql().getResultAsync("SELECT * FROM `TopVoterRecent` ORDER BY `Votes` DESC, `Creation` ASC LIMIT 10", new FindResultCallback() {
            @Override
            public void onQueryDone(ResultSet result) throws SQLException {
                int position = 0;
                while (result.next()) {
                    position++;
                    String username = result.getString("PLAYERNAME");
                    String uuid = result.getString("UUID");
                    int votes = result.getInt("Votes");
                    long creation  = result.getLong("Creation");
                    plugin.getSql().executeStatementAsync("INSERT INTO `TopVoterOld` (`UUID`, `PLAYERNAME`, `Votes`, `Creation`) VALUES ('" + uuid + "', '" + username + "', '" + votes + "', '" + creation + "')");
//                    if (position == 1) {
//                        addBalance(sender, uuid, 50.0);
//                    } else {
//                        addBalance(sender, uuid, 25.0);
//                    }
                }

                plugin.getSql().executeStatementAsync("TRUNCATE TopVoterRecent");
                sender.sendMessage(Chat.format("&aSuccessfully reset top 10 voters."));
            }
        });
    }

//    private void addBalance(CommandSender sender, String target, double amount) {
//        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
//            @Override
//            public void run() {
//                Bukkit.dispatchCommand(sender, "ad addbalance " + target + " " + amount);
//            }
//        });
//    }

    private void showInfo(CommandSender sender) {
        String message = ("&bTop voters: \n&eThe top 3 voters every month earn themselves a special prize. The prizes are displayed on the right of &7/topvoter top &eand are handed out" +
                " in the form of our virtual currency that you can use to get store giftcards (momento money) at the start of every month.");
        sender.sendMessage(Chat.format(message));
    }

    private void showTopTenStreak(CommandSender sender) {
        StringBuilder message = new StringBuilder();
        message.append("&bTop 10 streaks: \n");

        plugin.getSql().getResultAsync("SELECT * FROM `TopVoterStreak` ORDER BY `streak` DESC, `time` ASC LIMIT 10", new FindResultCallback() {
            @Override
            public void onQueryDone(ResultSet result) throws SQLException {
                int position = 1;
                int count = 0;
                while (result.next()) {
                    count++;
                    message.append("&b#");
                    message.append(position);
                    message.append(" &7- &e");
                    message.append(result.getString("ign"));
                    message.append(" &7with a streak of &e");
                    message.append(result.getInt("streak"));
                    message.append("\n");
                    position++;
                }

                if (count < 1) {
                    message.append("&C(no streaks yet)");
                }

                sender.sendMessage(Chat.format(message.toString()));
            }
        });
    }

    private void showTopTen(CommandSender sender) {
        StringBuilder message = new StringBuilder();
        message.append("&bThis month\'s top 10 voters: \n");

        plugin.getSql().getResultAsync("SELECT * FROM `TopVoterRecent` ORDER BY `Votes` DESC, `Creation` ASC LIMIT 10", new FindResultCallback() {
            @Override
            public void onQueryDone(ResultSet result) throws SQLException {
                int position = 1;
                int count = 0;
                while (result.next()) {
                    count++;
                    message.append("&b#");
                    message.append(position);
                    message.append(" &7- &e");
                    message.append(result.getString("PLAYERNAME"));
                    message.append(" &7with &e");
                    message.append(result.getInt("Votes"));
                    message.append(" &7votes.");
                    if (position == 1) {
                        message.append(" &a($20 Store Coupon)\n");
                    }
                    if (position == 2) {
                        message.append(" &a($10 Store Coupon)\n");
                    }
                    if (position == 3) {
                        message.append(" &a($5 Store Coupon)\n");
                    }
                    message.append("\n");
                    position++;
                }

                if (count < 1) {
                    message.append("&C(no votes yet)");
                }

                sender.sendMessage(Chat.format(message.toString()));
            }
        });
    }

    private void showLastMonthsTopTen(CommandSender sender) {
        StringBuilder message = new StringBuilder();
        message.append("&bLast month\'s top 10 voters: \n");

        plugin.getSql().getResultAsync("SELECT * FROM `TopVoterOld` ORDER BY `Votes` DESC, `Creation` ASC LIMIT 10", new FindResultCallback() {
            @Override
            public void onQueryDone(ResultSet result) throws SQLException {
                int position = 1;
                int count = 0;
                while (result.next()) {
                    count++;
                    message.append("&b#");
                    message.append(position);
                    message.append(" &7- &e");
                    message.append(result.getString("PLAYERNAME"));
                    message.append(" &7with &e");
                    message.append(result.getInt("Votes"));
                    message.append(" &7votes.");
                    if (position == 1) {
                        message.append(" &a($20 Store Coupon)\n");
                    }
                    if (position == 2) {
                        message.append(" &a($10 Store Coupon)\n");
                    }
                    if (position == 3) {
                        message.append(" &a($5 Store Coupon)\n");
                    }
                    message.append("\n");
                    position++;
                }

                if (count < 1) {
                    message.append("&C(no top voters last month)");
                }

                sender.sendMessage(Chat.format(message.toString()));
            }
        });
    }

    private void getCount(Player player) {
        plugin.getSql().getResultAsync("SELECT * FROM `TopVoterRecent` WHERE `UUID` = '" + player.getUniqueId().toString() + "'", new FindResultCallback() {
            @Override
            public void onQueryDone(ResultSet result) throws SQLException {
                if (result.next()) {
                    player.sendMessage(Chat.format("&aYou currently have &e" + result.getInt("Votes") + " &avotes."));
                } else {
                    player.sendMessage(Chat.format("&cYou have not voted yet this month."));
                }
            }
        });
    }

    private void getStreak(Player player) {
        plugin.getSql().getResultAsync("SELECT * FROM `TopVoterStreak` WHERE `uuid` = '" + player.getUniqueId().toString() + "'", new FindResultCallback() {
            @Override
            public void onQueryDone(ResultSet result) throws SQLException {
                if (result.next()) {
                    int streak = result.getInt("streak");
                    double multiplier = 1.0;

                    if (streak >= 50) {
                        multiplier = 1.1;
                    }

                    if (streak >= 100) {
                        multiplier = 1.2;
                    }

                    if (streak >= 150) {
                        multiplier = 1.3;
                    }

                    if (streak >= 200) {
                        multiplier = 1.4;
                    }

                    if (streak >= 250) {
                        multiplier = 1.5;
                    }

                    if (streak >= 300) {
                        multiplier = 1.6;
                    }

                    if (streak >= 350) {
                        multiplier = 1.7;
                    }

                    if (streak >= 400) {
                        multiplier = 1.8;
                    }

                    if (streak >= 450) {
                        multiplier = 1.9;
                    }

                    if (streak >= 500) {
                        multiplier = 2;
                    }

                    player.sendMessage(Chat.format("&aYou currently have a streak of &e" + streak + " &avotes."));
                } else {
                    player.sendMessage(Chat.format("&cYou currently have no streak (no vote in the last 48h)"));
                }
            }
        });
    }
}
