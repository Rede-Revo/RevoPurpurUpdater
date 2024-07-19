package org.thiagogebrim.revoPurpurUpdater;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public class LuckPermsUpdater {

    private static final String JOB_NAME = "LuckPerms";
    private static final String BASE_URL = "https://ci.lucko.me/job/" + JOB_NAME;
    private static final String ARTIFACT_URL = BASE_URL + "/lastSuccessfulBuild/artifact";
    private static final String PLUGIN_DIRECTORY = "/home/container/plugins/";

    public static void updateLuckPerms(Logger logger) {
        try {
            String latestDownloadPath = getLatestDownload();
            String latestDownloadUrl = ARTIFACT_URL + "/" + latestDownloadPath;
            String latestFileName = new File(latestDownloadPath).getName();

            logger.info("Iniciando a busca por atualizações do LuckPerms...");

            // Verificar se a versão instalada é a mais recente
            if (isUpToDate(latestFileName)) {
                logger.info("A versão do " + JOB_NAME + " já está atualizada.");
                return;
            }

            // Remove qualquer versão antiga do LuckPerms
            removeOldVersions(logger);

            // Baixa a nova versão do LuckPerms
            downloadUpdate(latestDownloadUrl, PLUGIN_DIRECTORY + latestFileName, logger);
            logger.info(JOB_NAME + " foi atualizado para a versão mais recente. Por favor, reinicie o servidor para aplicar as mudanças.");
        } catch (IOException e) {
            logger.severe("Erro ao tentar atualizar " + JOB_NAME + ": " + e.getMessage());
        }
    }

    private static boolean isUpToDate(String latestFileName) {
        File pluginFolder = new File(PLUGIN_DIRECTORY);
        File[] files = pluginFolder.listFiles((dir, name) -> name.toLowerCase().contains("luckperms-bukkit") && name.toLowerCase().endsWith(".jar"));
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(latestFileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void removeOldVersions(Logger logger) {
        File pluginFolder = new File(PLUGIN_DIRECTORY);
        File[] files = pluginFolder.listFiles((dir, name) -> name.toLowerCase().contains("luckperms-bukkit") && name.toLowerCase().endsWith(".jar"));
        if (files != null) {
            for (File file : files) {
                if (file.delete()) {
                    logger.info("Removida versão antiga do LuckPerms: " + file.getName());
                } else {
                    logger.warning("Não foi possível remover o arquivo: " + file.getName());
                }
            }
        }
    }

    private static String getLatestDownload() throws IOException {
        String apiUrl = BASE_URL + "/lastSuccessfulBuild/api/json";
        URLConnection request = new URL(apiUrl).openConnection();
        request.connect();

        JsonObject root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();
        JsonArray artifacts = root.getAsJsonArray("artifacts");

        for (int i = 0; i < artifacts.size(); i++) {
            JsonObject artifact = artifacts.get(i).getAsJsonObject();
            String fileName = artifact.get("fileName").getAsString();
            if (!fileName.contains("sources")) {
                return artifact.get("relativePath").getAsString();
            }
        }

        return artifacts.get(0).getAsJsonObject().get("relativePath").getAsString(); // fallback
    }

    private static void downloadUpdate(String url, String outputFilePath, Logger logger) {
        logger.info("Baixando atualização e aplicando para " + outputFilePath + "...");
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, Paths.get(outputFilePath), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Nova versão do " + JOB_NAME + " foi baixada e aplicada com sucesso.");
        } catch (IOException e) {
            logger.severe("Erro ao baixar e substituir o arquivo do " + JOB_NAME + ": " + e.getMessage());
            e.printStackTrace();  // Adiciona mais detalhes ao log para depuração
        }
    }
}
