package com.ninjamcr.waystones.database;

import com.ninjamcr.waystones.models.Waystone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        // Ensure the plugin's data folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File dbFile = new File(plugin.getDataFolder(), "waystones.db");
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create waystones.db file!");
                e.printStackTrace();
            }
        }

        try {
            // Establish Connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            // Create the table structure if it doesn't exist
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS waystones (" +
                        "id STRING PRIMARY KEY," +
                        "world TEXT NOT NULL," +
                        "x INTEGER NOT NULL," +
                        "y INTEGER NOT NULL," +
                        "z INTEGER NOT NULL," +
                        "name TEXT NOT NULL" +
                        ");");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize SQLite Database!");
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Save a new Waystone into the database
    public void saveWaystone(Waystone waystone) {
        String id = waystone.getId().toString();
        Location loc = waystone.getLocation();
        String name = waystone.getName();

        String sql = "INSERT INTO waystones (id, world, x, y, z, name) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, loc.getWorld().getName());
            pstmt.setInt(3, loc.getBlockX());
            pstmt.setInt(4, loc.getBlockY());
            pstmt.setInt(5, loc.getBlockZ());
            pstmt.setString(6, name);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete a Waystone when broken
    public void deleteWaystone(Waystone ws) {
        String sql = "DELETE FROM waystones WHERE id = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ws.getId().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch all Waystones on server startup
    public List<Waystone> loadWaystones() {
        List<Waystone> waystones = new ArrayList<>();
        String sql = "SELECT * FROM waystones;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                String worldName = rs.getString("world");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                String name = rs.getString("name");

                if (Bukkit.getWorld(worldName) != null) {
                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                    Waystone waystone = new Waystone(UUID.fromString(id), name, location);
                    waystones.add(waystone);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return waystones;
    }
}
