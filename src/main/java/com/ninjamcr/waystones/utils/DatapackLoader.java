package com.ninjamcr.waystones.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.packs.DataPack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

// Handles datapack discovery for non-paper server types eg spigot
public final class DatapackLoader {
    private static final String RESOURCE_PREFIX = "waystones_datapack/";
    private static final String PACK_DIRECTORY = "waystones";
    private static final String PACK_ID = "file/waystones";
    private static final NamespacedKey ADVANCEMENT = NamespacedKey.fromString("waystones:first_waystone");
    private static final int MAX_CHECKS = 10;

    private DatapackLoader() {}

    public static void installSpigotDatapack(JavaPlugin plugin) {
        try {
            Path container = plugin.getServer().getWorldContainer().toPath().toAbsolutePath().normalize();

            Path propertiesPath = Path.of("server.properties");
            Properties properties = new Properties();

            if (Files.isRegularFile(propertiesPath)) {
                try (InputStream input = Files.newInputStream(propertiesPath)) {
                    properties.load(input);
                }
            }

            String worldName = properties.getProperty("level-name", "world").trim();
            if (worldName.isEmpty()) worldName = "world";

            Path world = container.resolve(worldName).normalize();
            if (!world.startsWith(container)) {
                throw new IOException("Invalid level-name path: " + worldName);
            }
            Path destination = world.resolve("datapacks").resolve(PACK_DIRECTORY);

            Path pluginJar = Path.of(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());

            int updated = 0;
            try (JarFile jar = new JarFile(pluginJar.toFile())) {
                List<JarEntry> resources = new ArrayList<>();
                boolean foundPackMeta = false;
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.isDirectory() || !entry.getName().startsWith(RESOURCE_PREFIX)) {
                        continue;
                    }
                    String relative = entry.getName().substring(RESOURCE_PREFIX.length());
                    if (relative.equals("pack.mcmeta")) foundPackMeta = true;
                    if (!relative.isBlank()) resources.add(entry);
                }
                if (!foundPackMeta) {
                    throw new IOException("Missing " + RESOURCE_PREFIX + "pack.mcmeta in JAR");
                }

                for (JarEntry resource : resources) {
                    String relative = resource.getName().substring(RESOURCE_PREFIX.length());
                    Path output = destination.resolve(relative).normalize();
                    if (!output.startsWith(destination)) {
                        throw new IOException("Unsafe bundled datapack path: " + relative);
                    }

                    byte[] contents;
                    try (InputStream input = jar.getInputStream(resource)) {
                        contents = input.readAllBytes();
                    }

                    if (Files.isRegularFile(output)
                            && Arrays.equals(Files.readAllBytes(output), contents)) {
                        continue;
                    }

                    Files.createDirectories(output.getParent());
                    Path temp = Files.createTempFile(output.getParent(), ".waystones-", ".tmp");

                    try {
                        Files.write(temp, contents);
                        try {
                            Files.move(temp, output, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                        } catch (AtomicMoveNotSupportedException ex) {
                            Files.move(temp, output, StandardCopyOption.REPLACE_EXISTING);
                        }
                        updated++;
                    } finally {
                        Files.deleteIfExists(temp);
                    }
                }
            }
            plugin.getLogger().info(updated == 0
                    ? "Spigot Waystones datapack is already installed."
                    : "Installed/updated " + updated + " Waystones datapack file(s) in " + destination);
        } catch (IOException | URISyntaxException | SecurityException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not install Waystones datapack on Spigot (world untouched outside datapacks/waystones)", ex);
        }
    }

    // Activates the data pack on spigot if this is a first time install
    public static void activateSpigotDatapack(JavaPlugin plugin) {
        if (!plugin.getConfig().getBoolean("auto-enable-advancements-datapack", true)) {
            plugin.getLogger().info("Automatic Waystones datapack activation is disabled in config.yml.");
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> verifyPack(plugin, 0, false, false), 40L);
    }

    private static void verifyPack(JavaPlugin plugin, int checks, boolean rescanRequested, boolean enableRequested) {
        if (Bukkit.getAdvancement(ADVANCEMENT) != null) {
            if (checks == 0 || rescanRequested || enableRequested) {
                plugin.getLogger().info("Waystones advancement is loaded.");
            }
            return;
        }

        if (Bukkit.getWorlds().isEmpty()) {
            retryOrWarn(plugin, checks, rescanRequested, enableRequested, "No loaded world to check datapacks");
            return;
        }

        World world = Bukkit.getWorlds().getFirst();
        boolean enabled = Bukkit.getDataPackManager().getEnabledDataPacks(world).stream().anyMatch(DatapackLoader::isWaystonesPack);
        boolean disabled = Bukkit.getDataPackManager().getDisabledDataPacks(world).stream().anyMatch(DatapackLoader::isWaystonesPack);

        if (enabled) {
            retryOrWarn(plugin, checks, rescanRequested, enableRequested, "Waystones datapack is enabled, but its advancement is not registered");
            return;
        }

        if (disabled && !enableRequested) {
            plugin.getLogger().info("Waystones Spigot datapack is disabled. Enabling it...");
            boolean dispatched;
            try {
                dispatched = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:datapack enable \"" + PACK_ID + "\"");
            } catch (RuntimeException ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not run the datapack enable command", ex);
                return;
            }
            if (!dispatched) {
                plugin.getLogger().warning("The datapack enable command was not dispatched.");
                return;
            }

            scheduleCheck(plugin, checks + 1, rescanRequested, true);
            return;
        }

        if (!enabled && !disabled && !rescanRequested) {
            plugin.getLogger().info("New Waystones Spigot datapack not discovered yet, starting one Minecraft data rescan.");
            try {
                Bukkit.reloadData();
            } catch (RuntimeException ex) {
                plugin.getLogger().log(Level.WARNING, "Spigot could not rescan datapacks in this startup", ex);
                return;
            }
            scheduleCheck(plugin, checks + 1, true, enableRequested);
            return;
        }

        retryOrWarn(plugin, checks, rescanRequested, enableRequested,
                disabled ? "Waystones datapack still appears disabled after enable command" : "Waystones datapack not discovered by the running server");
    }

    private static void scheduleCheck(JavaPlugin plugin, int checks, boolean rescanRequested, boolean enableRequested) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> verifyPack(plugin, checks, rescanRequested, enableRequested), 20L);
    }

    private static void retryOrWarn(JavaPlugin plugin, int checks, boolean rescanRequested, boolean enableRequested, String cause) {
        if (checks < MAX_CHECKS) {
            scheduleCheck(plugin, checks + 1, rescanRequested, enableRequested);
        } else {
            plugin.getLogger().warning(cause + ". Check /datapack list and startup logs. A restart may be required if Spigot cannot discover a pack installed after its initial scan.");
        }
    }

    private static boolean isWaystonesPack(DataPack pack) {
        return PACK_ID.equals(pack.getKey().getKey());
    }
}
