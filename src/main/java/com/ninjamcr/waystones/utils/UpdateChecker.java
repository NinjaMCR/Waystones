package com.ninjamcr.waystones.utils;

import com.ninjamcr.waystones.Waystones;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class UpdateChecker {
    private final Waystones plugin;

    public UpdateChecker(Waystones plugin) {
        this.plugin = plugin;
    }

    public void CheckForUpdates(){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                BufferedReader reader = getBufferedReader();
                String line = "";
                String currentVersion = plugin.getPluginMeta().getVersion();
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("version:")) {
                        String latestVersion = line.replace("version: ", "").replace("'", "");
                        if (!latestVersion.equals(currentVersion)){
                            plugin.getLogger().warning("There is a newer version of Waystones available!");
                            plugin.getLogger().warning("Current version: " + plugin.getPluginMeta().getVersion() + " | Latest version: " + latestVersion);
                            plugin.getLogger().warning("Update your plugin version to the latest at https://modrinth.com/plugin/waystones-ninjamcr");
                        }
                    }
                }
            } catch (URISyntaxException | MalformedURLException e) {
                throw new RuntimeException("URL was incorrect", e);
            } catch (IOException e) {
                throw new RuntimeException("Connection failed to open", e);
            }
        });
    }

    private static @NonNull BufferedReader getBufferedReader() throws URISyntaxException, IOException {
        URI uri = new URI("https://raw.githubusercontent.com/NinjaMCR/Waystones/refs/heads/master/src/main/resources/paper-plugin.yml");
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("UpdateChecker-Requested-With", "Curl");
        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(5000);
        return new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
    }
}
