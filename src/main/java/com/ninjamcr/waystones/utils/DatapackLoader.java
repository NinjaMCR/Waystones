package com.ninjamcr.waystones.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class DatapackLoader {
    private static final String SOURCE_PREFIX = "waystones_datapack/";
    private static final String DESTINATION_NAME = "waystones.zip";

    public static void installDatapack(JavaPlugin plugin) {
        try {
            Properties properties = new Properties();
            Path propertiesFile = Path.of("server.properties");
            if (Files.isRegularFile(propertiesFile)) {
                try (InputStream in = Files.newInputStream(propertiesFile)) {
                    properties.load(in);
                }
            }

            String worldName = properties.getProperty("level-name", "world").trim();
            if (worldName.isEmpty()) {
                worldName = "world";
            }

            Path dataPacks = plugin.getServer().getWorldContainer().toPath().resolve(worldName).resolve("datapacks");
            Files.createDirectories(dataPacks);
            Path target = dataPacks.resolve(DESTINATION_NAME);
            Path temp = Files.createTempFile(dataPacks, "waystones-", ".zip.tmp");

            try {
                buildDatapackZip(plugin, temp);
                if (Files.exists(target) && Files.mismatch(temp, target) == -1L) {
                    plugin.getLogger().info("Waystones datapack already installed.");
                    return;
                }

                try {
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException ex) {
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
                }
                plugin.getLogger().info("Installed Waystones datapack to " + target + ". Restart the server if the advancement is not available yet.");
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException | URISyntaxException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not install the bundled Waystones advancement datapack", ex);
        }
    }

    private static void buildDatapackZip(JavaPlugin plugin, Path output) throws IOException, URISyntaxException {
        File pluginJar = Path.of(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();

        boolean foundPackMeta = false;
        try (JarFile jar = new JarFile(pluginJar); ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(output))) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String path = entry.getName();
                if (entry.isDirectory() || !path.startsWith(SOURCE_PREFIX)) {
                    continue;
                }
                String relative = path.substring(SOURCE_PREFIX.length());
                if (relative.isEmpty()) {
                    continue;
                }
                if (relative.equals("pack.mcmeta")) {
                    foundPackMeta = true;
                }

                ZipEntry outEntry = new ZipEntry(relative);
                if (entry.getTime() >= 0) {
                    outEntry.setTime(entry.getTime());
                }
                zip.putNextEntry(outEntry);
                try (InputStream source = jar.getInputStream(entry)) {
                    source.transferTo(zip);
                }
                zip.closeEntry();
            }
        }
        if (!foundPackMeta) {
            throw new IOException("Missing " + SOURCE_PREFIX + "pack.mcmeta in plugin JAR. ");
        }
    }
}
