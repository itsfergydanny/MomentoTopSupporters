package com.dnyferguson.momentotopsupporters;

import com.dnyferguson.momentotopsupporters.commands.AddVoteCommand;
import com.dnyferguson.momentotopsupporters.commands.TopVoterCommand;
import com.dnyferguson.momentotopsupporters.database.MySQL;
import org.bukkit.plugin.java.JavaPlugin;

public final class MomentoTopSupporters extends JavaPlugin {

    private MySQL sql;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        sql = new MySQL(this);

        getCommand("addvote").setExecutor(new AddVoteCommand(this));
        getCommand("topvoter").setExecutor(new TopVoterCommand(this));
    }

    @Override
    public void onDisable() {
        sql.closeConnections();
    }

    public MySQL getSql() {
        return sql;
    }
}
