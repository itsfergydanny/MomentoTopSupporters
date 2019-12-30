package com.dnyferguson.momentotopsupporters.listeners;

import com.dnyferguson.momentotopsupporters.MomentoTopSupporters;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class LoginListener implements Listener {

    private MomentoTopSupporters plugin;

    public LoginListener(MomentoTopSupporters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        plugin.updateUsername(player);
        plugin.checkForReminder(player);
    }
}
