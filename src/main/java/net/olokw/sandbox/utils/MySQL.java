package net.olokw.sandbox.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.configs.PlayerData;
import net.olokw.sandbox.managers.PlayerDataManager;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static net.olokw.sandbox.utils.TrimUtils.MATERIALS;
import static net.olokw.sandbox.utils.TrimUtils.PATTERNS;

public class MySQL {
    private final HikariDataSource ds;

    public MySQL(String jdbcUrl, String username, String password) {
        if (jdbcUrl == null || jdbcUrl.isEmpty() ||
                username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Algo está errado na configuração da Database.");
        }


        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);


        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(5000);

        ds = new HikariDataSource(config);

        createTableIfNotExists();
    }

    public Connection getConnection() throws SQLException {
        if (ds == null || ds.isClosed()) {
            throw new IllegalStateException("DataSource is not available or has been closed");
        }
        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    public void close() {
        if (!ds.isClosed()) {
            ds.close();
        }
    }




    private void createTableIfNotExists() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection()) {
                    String sql = "CREATE TABLE IF NOT EXISTS player_info (" +
                            "uuid VARCHAR(36) PRIMARY KEY," +
                            "last_kit TEXT NOT NULL," +
                            "ignored TEXT NOT NULL," +
                            "trim_map TEXT" +
                            ")";
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate(sql);
                    }
                    conn.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Sandbox.instance);
    }

    public void loadData(UUID uuid) {
        PlayerDataManager playerDataManager = Sandbox.instance.getPlayerDataManager();

        // Valor padrão
        Map<PlayerData.ArmorType, ArmorTrim> trimMap = new HashMap<>();
        for (PlayerData.ArmorType type : PlayerData.ArmorType.values()) {
            trimMap.put(type, null);
        }

        String[] initialKit = new String[2];
        initialKit[0] = "";
        initialKit[1] = "";

        Sandbox.instance.getPlayerDataManager().set(uuid, new PlayerData(new HashSet<>(), initialKit, trimMap));

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection()) {
                    String sql = "SELECT ignored, last_kit, trim_map FROM player_info WHERE uuid = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, uuid.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                String ignoredRaw = rs.getString("ignored");
                                String lastKit = rs.getString("last_kit");
                                String trimMapRaw = rs.getString("trim_map");

                                String[] lastKitSplit = lastKit.split("#");

                                Set<UUID> ignoredSet = Arrays.stream(ignoredRaw.split(","))
                                        .filter(s -> !s.isEmpty())
                                        .map(UUID::fromString)
                                        .collect(Collectors.toSet());

                                Map<PlayerData.ArmorType, ArmorTrim> loadedTrimMap = new HashMap<>();

                                if (trimMapRaw != null && !trimMapRaw.isEmpty()) {
                                    String[] parts = trimMapRaw.split(",");

                                    for (String entry : parts) {
                                        try {
                                            String[] slotAndTrim = entry.split(":");
                                            String[] patternAndMaterial = slotAndTrim[1].split("@");

                                            PlayerData.ArmorType slot = PlayerData.ArmorType.valueOf(slotAndTrim[0]);
                                            TrimPattern pattern = getTrimPatternByName(patternAndMaterial[0]);
                                            TrimMaterial material = getTrimMaterialByName(patternAndMaterial[1]);

                                            if (pattern != null && material != null) {
                                                loadedTrimMap.put(slot, new ArmorTrim(material, pattern));
                                            }
                                        } catch (Exception ex) {
                                            System.err.println("Erro ao carregar trim: " + entry);
                                            ex.printStackTrace(); // ajuda a debugar strings malformadas
                                        }
                                    }
                                }

                                playerDataManager.set(uuid, new PlayerData(ignoredSet, lastKitSplit, loadedTrimMap));
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Sandbox.instance);
    }

    public void loadDataNotAsync(UUID uuid) {
        PlayerDataManager playerDataManager = Sandbox.instance.getPlayerDataManager();

        // Valor padrão
        Map<PlayerData.ArmorType, ArmorTrim> trimMap = new HashMap<>();
        for (PlayerData.ArmorType type : PlayerData.ArmorType.values()) {
            trimMap.put(type, null);
        }

        String[] initialKit = new String[2];
        initialKit[0] = "";
        initialKit[1] = "";

        Sandbox.instance.getPlayerDataManager().set(uuid, new PlayerData(new HashSet<>(), initialKit, trimMap));

        try (Connection conn = getConnection()) {
            String sql = "SELECT ignored, last_kit, trim_map FROM player_info WHERE uuid = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String ignoredRaw = rs.getString("ignored");
                        String lastKit = rs.getString("last_kit");
                        String trimMapRaw = rs.getString("trim_map");

                        String[] lastKitSplit = lastKit.split("#");

                        Set<UUID> ignoredSet = Arrays.stream(ignoredRaw.split(","))
                                .filter(s -> !s.isEmpty())
                                .map(UUID::fromString)
                                .collect(Collectors.toSet());

                        Map<PlayerData.ArmorType, ArmorTrim> loadedTrimMap = new HashMap<>();

                        if (trimMapRaw != null && !trimMapRaw.isEmpty()) {
                            String[] parts = trimMapRaw.split(",");

                            for (String entry : parts) {
                                try {
                                    String[] slotAndTrim = entry.split(":");
                                    String[] patternAndMaterial = slotAndTrim[1].split("@");

                                    PlayerData.ArmorType slot = PlayerData.ArmorType.valueOf(slotAndTrim[0]);
                                    TrimPattern pattern = getTrimPatternByName(patternAndMaterial[0]);
                                    TrimMaterial material = getTrimMaterialByName(patternAndMaterial[1]);

                                    if (pattern != null && material != null) {
                                        loadedTrimMap.put(slot, new ArmorTrim(material, pattern));
                                    }
                                } catch (Exception ex) {
                                    System.err.println("Erro ao carregar trim: " + entry);
                                    ex.printStackTrace(); // para debugar em caso de erro.
                                }
                            }
                        }

                        playerDataManager.set(uuid, new PlayerData(ignoredSet, lastKitSplit, loadedTrimMap));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private TrimPattern getTrimPatternByName(String name) {
        return PATTERNS.get(name.toLowerCase());
    }

    private TrimMaterial getTrimMaterialByName(String name) {
        return MATERIALS.get(name.toLowerCase());
    }



    public void saveData(UUID uuid, PlayerData data) {
        TrimUtils trimUtils = Sandbox.instance.getTrimUtils();
        new BukkitRunnable() {
            @Override
            public void run() {
                String ignoredString = data.getIgnoring().stream()
                        .map(UUID::toString)
                        .collect(Collectors.joining(","));

                String trimMapStr = data.getTrimMap().entrySet().stream()
                        .filter(e -> e.getValue() != null)
                        .map(entry -> {
                            ArmorTrim trim = entry.getValue();
                            String slot = entry.getKey().name();

                            String pattern = trimUtils.getPatternString(trim.getPattern());
                            String material = trimUtils.getMaterialString(trim.getMaterial());

                            return slot + ":" + pattern + "@" + material;
                        })
                        .collect(Collectors.joining(","));

                try (Connection conn = getConnection()) {
                    String sql = "INSERT INTO player_info (uuid, last_kit, ignored, trim_map) VALUES (?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE last_kit = VALUES(last_kit), ignored = VALUES(ignored), trim_map = VALUES(trim_map)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, uuid.toString());
                        ps.setString(2, String.join("#", data.getLastKit()));
                        ps.setString(3, ignoredString);
                        ps.setString(4, trimMapStr);
                        ps.executeUpdate();
                    }
                    conn.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Sandbox.instance);
    }

    public void saveDataNotAsync(UUID uuid) {
        TrimUtils trimUtils = Sandbox.instance.getTrimUtils();
        PlayerData data = Sandbox.instance.getPlayerDataManager().get(uuid);
        String ignoredString = data.getIgnoring().stream()
                .map(UUID::toString)
                .collect(Collectors.joining(","));

        String trimMapStr = data.getTrimMap().entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(entry -> {
                    ArmorTrim trim = entry.getValue();
                    String slot = entry.getKey().name();

                    String pattern = trimUtils.getPatternString(trim.getPattern());
                    String material = trimUtils.getMaterialString(trim.getMaterial());

                    return slot + ":" + pattern + "@" + material;
                })
                .collect(Collectors.joining(","));

        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO player_info (uuid, last_kit, ignored, trim_map) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE last_kit = VALUES(last_kit), ignored = VALUES(ignored), trim_map = VALUES(trim_map)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, String.join("#", data.getLastKit()));
                ps.setString(3, ignoredString);
                ps.setString(4, trimMapStr);
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





}
