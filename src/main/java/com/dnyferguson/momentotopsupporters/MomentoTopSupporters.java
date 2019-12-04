package com.dnyferguson.momentotopsupporters;

import com.dnyferguson.momentotopsupporters.commands.AddVoteCommand;
import com.dnyferguson.momentotopsupporters.commands.TopVoterCommand;
import com.dnyferguson.momentotopsupporters.database.FindResultCallback;
import com.dnyferguson.momentotopsupporters.database.MySQL;
import com.dnyferguson.momentotopsupporters.listeners.LoginListener;
import com.dnyferguson.momentotopsupporters.listeners.LogoutListener;
import com.dnyferguson.momentotopsupporters.utils.Chat;
import com.dnyferguson.momentotopsupporters.utils.Time;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MomentoTopSupporters extends JavaPlugin {

    private MySQL sql;
    private Map<UUID, BukkitTask> reminderTimers = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        sql = new MySQL(this);

        getCommand("addvote").setExecutor(new AddVoteCommand(this));
        getCommand("topvoter").setExecutor(new TopVoterCommand(this));

        getServer().getPluginManager().registerEvents(new LoginListener(this), this);
        getServer().getPluginManager().registerEvents(new LogoutListener(this), this);

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkForReminder(player);
                }
            }
        }, 100, 72000);
    }

    @Override
    public void onDisable() {
        sql.close();
        for (BukkitTask task : reminderTimers.values()) {
            task.cancel();
        }
    }

    public MySQL getSql() {
        return sql;
    }

    public Map<UUID, BukkitTask> getReminderTimers() {
        return reminderTimers;
    }

    public void checkForReminder(Player player) {
        if (getReminderTimers().containsKey(player.getUniqueId())) {
            return;
        }

        getSql().getResultAsync("SELECT * FROM `TopVoterRecent` WHERE `UUID` = '" + player.getUniqueId() + "'", new FindResultCallback() {
            @Override
            public void onQueryDone(ResultSet result) throws SQLException {
                if (result.next()) {
                    long lastVoted = result.getLong("Creation");
                    long dayAgo = Time.getBackwards("24h").getTime();

                    if (lastVoted < dayAgo) {
                        addTimer(player);
                    }
                } else {
                    addTimer(player);
                }
            }
        });
    }

    private void addTimer(Player player) {
        BukkitTask reminder = getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                player.sendMessage(Chat.format("\n \n&eYou have not voted &7(/vote)&e in the past 24 hours! Please do so to support the server and remove this message!\n \n"));
            }
        }, 100, 72000);

        getReminderTimers().put(player.getUniqueId(), reminder);
    }
}
