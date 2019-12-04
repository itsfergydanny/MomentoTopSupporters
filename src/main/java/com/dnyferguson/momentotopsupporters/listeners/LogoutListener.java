package com.dnyferguson.momentotopsupporters.listeners;

import com.dnyferguson.momentotopsupporters.MomentoTopSupporters;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LogoutListener implements Listener {

    private MomentoTopSupporters plugin;

    public LogoutListener(MomentoTopSupporters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (plugin.getReminderTimers().containsKey(player.getUniqueId())) {
            plugin.getReminderTimers().get(player.getUniqueId()).cancel();
            plugin.getReminderTimers().remove(player.getUniqueId());
        }
    }
}
