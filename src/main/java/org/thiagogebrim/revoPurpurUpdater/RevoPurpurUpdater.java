package org.thiagogebrim.revoPurpurUpdater;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class RevoPurpurUpdater extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("RevoPurpurUpdater está em modo espera, pronto para atualizar o Purpur no desligamento.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Iniciando a busca por atualizações...");

        // Plugin Version
        String pluginVersion = "1.21";

        // Changes the Bukkit Version to a string and then gets the jar version and the MC version.
        String BukkitVersion = Bukkit.getVersion();
        String MCandVersion = BukkitVersion.substring(0, BukkitVersion.lastIndexOf("-"));
        String jarVersion = MCandVersion.substring(MCandVersion.indexOf("-") + 1);
        String MCVersion = MCandVersion.substring(0, MCandVersion.lastIndexOf("-"));
        int version = Integer.parseInt(jarVersion);

        String serverJarName = "server.jar";

        if (MCVersion.equals("1.21")) {
            URLConnection connection = null;
            try {
                connection = new URL("https://api.purpurmc.org/v2/purpur/1.21").openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                String response = scanner.useDelimiter("\\A").next();
                JsonObject jobj = new Gson().fromJson(response, JsonObject.class);
                String allbuilds = jobj.get("builds").toString();
                JsonObject jsonbuilds = new Gson().fromJson(allbuilds, JsonObject.class);
                String complatest = jsonbuilds.get("latest").toString();
                String simpleLatest = complatest.substring(1, 5);
                int latest = Integer.parseInt(simpleLatest);
                String[] pathNames;
                File ServerJar = new File("../");
                pathNames = ServerJar.list();
                getLogger().warning("-------------------------------");
                getLogger().info("RevoPurpurUpdater");
                getLogger().info("Atual versão do Purpur: " + jarVersion + " (MC: " + MCVersion + ")");

                // ===========================
                // PURPUR VERSION OF UPDATER
                // ===========================
                // If the version is not up-to-date it will grab the latest version and download and replace the file and name it based on the config.
                if (version != latest) {
                    getLogger().warning("A versão NÃO está atualizada! A nova versão do PURPUR é " + latest);
                    getLogger().info("Baixando atualização e aplicando para " + serverJarName + "...");
                    InputStream in = new URL("https://api.purpurmc.org/v2/purpur/1.21/latest/download").openStream();
                    Files.copy(in, Paths.get(serverJarName), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().info("Atualização completa!");
                    getLogger().warning("-------------------------------");
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getLogger().info("Servidor atualizado e o RevoPurpurUpdater foi desativado!");
                } else {
                    getLogger().info("O servidor está atualizado!");
                    getLogger().info("Desativando o plugin...");
                    getLogger().warning("-------------------------------");
                    getLogger().info("Desativado RevoPurpurUpdater com sucesso!");
                    getServer().getPluginManager().disablePlugin(this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            getLogger().warning("-------------------------------");
            getLogger().warning("RevoPurpurUpdater");
            getLogger().warning("ERRO: A versão do servidor é mais antiga/mais recente, para evitar atualizações acidentais, o plugin será desativado.");
            getLogger().warning("Versão do servidor: " + MCVersion);
            getLogger().warning("Versão do Plugin: " + pluginVersion);
            getLogger().warning("Desativando o plugin...");
            getLogger().warning("-------------------------------");
            // Time out so user sees the error message.
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getLogger().info("Desativado RevoPurpurUpdater com sucesso!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
