package com.ninjamcr.waystones.utils;

import com.ninjamcr.waystones.Waystones;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class UpdateChecker {
    private final Waystones plugin;

    public UpdateChecker(Waystones plugin) {
        this.plugin = plugin;
    }

    public String CheckForUpdates(){
        try {
            URI uri = new URI("https://raw.githubusercontent.com/NinjaMCR/Waystones/refs/heads/master/src/main/resources/paper-plugin.yml");
            URL url = uri.toURL();
            URLConnection urlConnection = url.openConnection();

            urlConnection.setRequestProperty("UpdateChecker-Requested-With", "Curl");
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line = "";
            String currentVersion = plugin.getPluginMeta().getVersion();
            while ((line = reader.readLine()) != null){
                if (line.startsWith("version:")){
                    String latestVersion = line.replace("version: ", "").replace("'", "");
                    if (!latestVersion.equals(currentVersion)) return latestVersion;
                }
            }
            return null;
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException("URL was incorrect", e);
        } catch (IOException e) {
            throw new RuntimeException("Connection failed to open", e);
        }
    }
}
