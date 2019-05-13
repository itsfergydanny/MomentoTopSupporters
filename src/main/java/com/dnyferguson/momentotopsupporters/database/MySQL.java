package com.dnyferguson.momentotopsupporters.database;

import com.dnyferguson.momentotopsupporters.MomentoTopSupporters;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQL {

    private MomentoTopSupporters plugin;
    private HikariDataSource datasource;

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

        this.datasource = new HikariDataSource(config);

        createTables(cfg.getString("mysql.database"));
    }

    private void createTables(String database) {
        try (Connection con = datasource.getConnection()) {
            // Create topvoter old table
            PreparedStatement pst = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + database + "`.`TopVoterRecent`  (UUID VARCHAR(37) PRIMARY KEY,PLAYERNAME TEXT NOT NULL,Votes INT UNSIGNED NOT NULL,Creation BIGINT UNSIGNED NOT NULL);");
            pst.execute();

            // Create topvoter new table
            pst = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + database + "`.`TopVoterOld`  (UUID VARCHAR(37) PRIMARY KEY,PLAYERNAME TEXT NOT NULL,Votes INT UNSIGNED NOT NULL,Creation BIGINT UNSIGNED NOT NULL);");
            pst.execute();

            // Create streaks table
            pst = con.prepareStatement("CREATE TABLE IF NOT EXISTS `" + database + "`.`TopVoterStreak` ( `uuid` VARCHAR(36) NOT NULL , `ign` VARCHAR(16) NOT NULL , `streak` INT NOT NULL , `time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , PRIMARY KEY (`uuid`)) ENGINE = InnoDB;");
            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnections() {
        this.datasource.close();
    }

    public HikariDataSource getDatasource() {
        return datasource;
    }
}
