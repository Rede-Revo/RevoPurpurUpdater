package org.thiagogebrim.revoPurpurUpdater;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PurpurUpdater {

    private static final String PURPUR_API_URL = "https://api.purpurmc.org/v2/purpur/";
    private static final String MC_VERSION = "1.21";
    private static final String SERVER_JAR_NAME = "server.jar";

    public static void updatePurpur(Logger logger) {
        logger.info("Iniciando a busca por atualizações do Purpur...");

        String bukkitVersion = Bukkit.getVersion();
        String mcAndVersion = bukkitVersion.substring(0, bukkitVersion.lastIndexOf("-"));
        String jarVersion = mcAndVersion.substring(mcAndVersion.indexOf("-") + 1);
        int currentVersion = Integer.parseInt(jarVersion);

        if (mcAndVersion.startsWith(MC_VERSION)) {
            try {
                URLConnection connection = new URL(PURPUR_API_URL + MC_VERSION).openConnection();
                try (Scanner scanner = new Scanner(connection.getInputStream())) {
                    String response = scanner.useDelimiter("\\A").next();
                    JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
                    int latestVersion = jsonObject.getAsJsonObject("builds").get("latest").getAsInt();

                    logger.info("Versão atual do Purpur: " + currentVersion + " (MC: " + MC_VERSION + ")");
                    if (currentVersion < latestVersion) {
                        logger.warning("A versão NÃO está atualizada! A nova versão do Purpur é " + latestVersion);
                        downloadAndReplacePurpur(logger, latestVersion);
                    } else {
                        logger.info("O Purpur está atualizado!");
                    }
                }
            } catch (IOException e) {
                logger.severe("Erro ao buscar atualizações do Purpur: " + e.getMessage());
            }
        } else {
            logger.warning("ERRO: A versão do servidor é mais antiga/mais recente, para evitar atualizações acidentais, o plugin não atualizará o Purpur.");
            logger.warning("Versão do servidor: " + mcAndVersion);
        }
    }

    private static void downloadAndReplacePurpur(Logger logger, int latestVersion) {
        logger.info("Baixando atualização e aplicando para " + SERVER_JAR_NAME + "...");
        try (InputStream in = new URL(PURPUR_API_URL + MC_VERSION + "/latest/download").openStream()) {
            Files.copy(in, Paths.get(SERVER_JAR_NAME), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Atualização do Purpur completa!");
            TimeUnit.SECONDS.sleep(3);
        } catch (IOException | InterruptedException e) {
            logger.severe("Erro ao baixar e substituir o arquivo do Purpur: " + e.getMessage());
        }
    }
}
