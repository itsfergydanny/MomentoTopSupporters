package com.dnyferguson.momentotopsupporters.database;

import com.dnyferguson.momentotopsupporters.MomentoTopSupporters;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQL {

    private MomentoTopSupporters plugin;
    private HikariDataSource ds;

    public MySQL(MomentoTopSupporters plugin) {
        this.plugin = plugin;
        FileConfiguration cfg = plugin.getConfig();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + cfg.getString("mysql.ip") + ":" + cfg.getString("mysql.port") + "/" + cfg.getString("mysql.database"));
        config.setUsername(cfg.getString("mysql.username"));
        config.setPassword(cfg.getString("mysql.password"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.ds = new HikariDataSource(config);

        createTables(cfg.getString("mysql.database"));
    }

    private void createTables(String database) {
        executeStatementAsync("CREATE TABLE IF NOT EXISTS `" + database + "`.`TopVoterRecent`  (UUID VARCHAR(37) PRIMARY KEY,PLAYERNAME TEXT NOT NULL,Votes INT UNSIGNED NOT NULL,Creation BIGINT UNSIGNED NOT NULL);");
        executeStatementAsync("CREATE TABLE IF NOT EXISTS `" + database + "`.`TopVoterOld`  (UUID VARCHAR(37) PRIMARY KEY,PLAYERNAME TEXT NOT NULL,Votes INT UNSIGNED NOT NULL,Creation BIGINT UNSIGNED NOT NULL);");
        executeStatementAsync("CREATE TABLE IF NOT EXISTS `" + database + "`.`TopVoterStreak` ( `uuid` VARCHAR(36) NOT NULL , `ign` VARCHAR(16) NOT NULL , `streak` INT NOT NULL , `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , PRIMARY KEY (`uuid`)) ENGINE = InnoDB;");
        executeStatementAsync("CREATE TABLE IF NOT EXISTS `" + database + "`.`users` ( `id` INT NOT NULL AUTO_INCREMENT , `uuid` VARCHAR(36) NOT NULL , `ign` VARCHAR(16) NOT NULL , `updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
    }

    public void getResultAsync(String stmt, FindResultCallback callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try (Connection con = ds.getConnection()) {
                    PreparedStatement pst = con.prepareStatement(stmt);
                    ResultSet rs = pst.executeQuery();
                    callback.onQueryDone(rs);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getResultSync(String stmt, FindResultCallback callback) {
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                try (Connection con = ds.getConnection()) {
                    PreparedStatement pst = con.prepareStatement(stmt);
                    ResultSet rs = pst.executeQuery();
                    callback.onQueryDone(rs);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void executeStatementSync(String stmt) {
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                try (Connection con = ds.getConnection()) {
                    PreparedStatement pst = con.prepareStatement(stmt);
                    pst.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void executeStatementAsync(String stmt) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try (Connection con = ds.getConnection()) {
                    PreparedStatement pst = con.prepareStatement(stmt);
                    pst.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public HikariDataSource getDs() {
        return ds;
    }

    public void close() {
        ds.close();
    }
}