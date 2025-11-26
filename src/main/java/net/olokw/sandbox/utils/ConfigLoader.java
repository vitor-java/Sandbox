package net.olokw.sandbox.utils;

import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.configs.PluginConfig;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ConfigLoader {
    public void load(){
        File file = new File(Sandbox.instance.getDataFolder(), "config.yml");

        if (!Sandbox.instance.getDataFolder().exists()){
            Sandbox.instance.getDataFolder().mkdir();
        }

        if (!file.exists()){
            try {
                Files.copy(Objects.requireNonNull(Sandbox.instance.getResource("config.yml")), file.getAbsoluteFile().toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (config.getBoolean("mysql.enabled")) {
            String username = config.getString("mysql.username");
            String password = config.getString("mysql.password");
            String hostname = config.getString("mysql.hostname");
            String port = config.getString("mysql.port");
            String database = config.getString("mysql.database");

            if (username == null || password == null || hostname == null || port == null || database == null) {
                throw new IllegalArgumentException("One or more MySQL configuration values are missing.");
            }

            String jdbcString = "jdbc:mysql://" + hostname + ":" + port + "/" + database;

            Sandbox.instance.setMySQL(new MySQL(jdbcString, username, password));
        }

        Set<String> restartTimes = new HashSet<>(config.getStringList("restart-daily"));

        Sandbox.instance.setPluginConfig(new PluginConfig(restartTimes));


    }
}
