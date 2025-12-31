package pl.milkcounter.io;

import java.io.*;
import java.nio.file.Paths;

public class StorageConfig {
    //Mac: /Users/Login/.milkcounter_cfg/
    //Windows: C:\Users\Login\.milkcounter_cfg\
    private static final File CONFIG_DIR = new File(System.getProperty("user.home"), ".milkcounter_cfg");
    private static final File PATH_FILE = new File(CONFIG_DIR, "data_path.txt");

    public String getActiveDataFolderPath() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }

        if (PATH_FILE.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(PATH_FILE))) {
                String savedPath = br.readLine();
                if (savedPath != null && !savedPath.isEmpty()) {
                    File folder = new File(savedPath);
                    if (folder.exists() && folder.isDirectory()) {
                        return savedPath;
                    }
                }
            } catch (IOException e) {
                System.err.println("Błąd odczytu konfiguracji: " + e.getMessage());
            }
        }
        //jeśli nie ma ścieżki zapisu
        String defaultPath = createDefaultPath();
        saveDataFolderPath(defaultPath); //zapisujemy ścieżkę
        return defaultPath;
    }

    public void saveDataFolderPath(String newPath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PATH_FILE))) {
            bw.write(newPath);
            System.out.println("Zapisano nową lokalizację danych: " + newPath);
        } catch (IOException e) {
            System.err.println("Nie udało się zapisać konfiguracji: " + e.getMessage());
        }
    }

    private String createDefaultPath() {
        String userHome = System.getProperty("user.home");
        File defaultDataFolder = Paths.get(userHome, ".milkcounter_data").toFile();

        if (!defaultDataFolder.exists()) {
            defaultDataFolder.mkdirs();
        }

        return defaultDataFolder.getAbsolutePath();
    }
}
