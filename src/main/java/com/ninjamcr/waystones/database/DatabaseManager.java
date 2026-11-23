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
    private String insertWaystone;
    private String deleteWaystone;
    private String selectWaystones;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.isDirectory() && !dataFolder.mkdirs() && !dataFolder.isDirectory()) {
            throw new IllegalStateException("Could not create plugin data directory: " + dataFolder);
        }

        File dbFile = new File(dataFolder, "waystones.db");
        try {
            insertWaystone = DatabaseMigrations.readResource("queries/insert-waystone.sql");
            deleteWaystone = DatabaseMigrations.readResource("queries/delete-waystone.sql");
            selectWaystones = DatabaseMigrations.readResource("queries/select-waystones.sql");

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            DatabaseMigrations.apply(connection);
        } catch (ClassNotFoundException | IOException | SQLException e) {
            close();
            throw new IllegalStateException("Failed to initialize SQLite database: " + dbFile, e);
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

        try (PreparedStatement pstmt = connection.prepareStatement(insertWaystone)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(deleteWaystone)) {
            pstmt.setString(1, ws.getId().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch all Waystones on server startup
    public List<Waystone> loadWaystones() {
        List<Waystone> waystones = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectWaystones)) {

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
