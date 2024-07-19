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

public class ProtocolLibUpdater {

    private static final String JOB_NAME = "ProtocolLib";
    private static final String BASE_URL = "https://ci.dmulloy2.net/job/" + JOB_NAME;
    private static final String ARTIFACT_URL = BASE_URL + "/lastSuccessfulBuild/artifact";
    private static final String PLUGIN_DIRECTORY = "/home/container/plugins/";

    public static void updateProtocolLib(Logger logger) {
        try {
            int latestBuild = getLatestBuild();
            String latestDownloadPath = getLatestDownload();
            String latestDownloadUrl = ARTIFACT_URL + "/" + latestDownloadPath;
            String latestFileName = "ProtocolLib-" + latestBuild + ".jar";

            logger.info("Iniciando a busca por atualizações do ProtocolLib...");

            // Verificar se a versão instalada é a mais recente
            if (isUpToDate(latestFileName, logger)) {
                logger.info("A versão do " + JOB_NAME + " já está atualizada.");
                return;
            }

            // Remove qualquer versão antiga do ProtocolLib
            removeOldVersions(logger);

            // Baixa a nova versão do ProtocolLib
            downloadUpdate(latestDownloadUrl, logger);

            // Renomeia o arquivo baixado para incluir o número da build
            renameDownloadedFile(latestFileName, logger);

            logger.info(JOB_NAME + " foi atualizado para a versão mais recente. Por favor, reinicie o servidor para aplicar as mudanças.");
        } catch (IOException e) {
            logger.severe("Erro ao tentar atualizar " + JOB_NAME + ": " + e.getMessage());
        }
    }

    private static boolean isUpToDate(String latestFileName, Logger logger) {
        File pluginFolder = new File(PLUGIN_DIRECTORY);
        File[] files = pluginFolder.listFiles((dir, name) -> name.toLowerCase().startsWith("protocollib-") && name.toLowerCase().endsWith(".jar"));
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
        File[] files = pluginFolder.listFiles((dir, name) -> name.toLowerCase().startsWith("protocollib-") && name.toLowerCase().endsWith(".jar"));
        if (files != null) {
            for (File file : files) {
                if (file.delete()) {
                    logger.info("Removida versão antiga do ProtocolLib: " + file.getName());
                } else {
                    logger.warning("Não foi possível remover o arquivo: " + file.getName());
                }
            }
        }
    }

    private static int getLatestBuild() throws IOException {
        String apiUrl = BASE_URL + "/lastSuccessfulBuild/api/json";
        URLConnection request = new URL(apiUrl).openConnection();
        request.connect();

        JsonObject root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();
        return root.get("number").getAsInt();
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
            if (fileName.equals("ProtocolLib.jar")) {
                return artifact.get("relativePath").getAsString();
            }
        }

        return artifacts.get(0).getAsJsonObject().get("relativePath").getAsString(); // fallback
    }

    private static void downloadUpdate(String url, Logger logger) {
        logger.info("Baixando atualização e aplicando para " + "/home/container/plugins/ProtocolLib.jar" + "...");
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, Paths.get("/home/container/plugins/ProtocolLib.jar"), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Nova versão do " + JOB_NAME + " foi baixada e aplicada com sucesso.");
        } catch (IOException e) {
            logger.severe("Erro ao baixar e substituir o arquivo do " + JOB_NAME + ": " + e.getMessage());
            e.printStackTrace();  // Adiciona mais detalhes ao log para depuração
        }
    }

    private static void renameDownloadedFile(String latestFileName, Logger logger) {
        File oldFile = new File(PLUGIN_DIRECTORY + "ProtocolLib.jar");
        File newFile = new File(PLUGIN_DIRECTORY + latestFileName);
        if (oldFile.renameTo(newFile)) {
            logger.info("Arquivo renomeado para " + latestFileName);
        } else {
            logger.warning("Não foi possível renomear o arquivo para " + latestFileName);
        }
    }
}
