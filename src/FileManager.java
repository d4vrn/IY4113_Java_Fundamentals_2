import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public static final String PROFILES_DIR = "profiles/";
    public static final String EXPORTS_DIR = "exports/";
    public static final String IMPORTS_DIR = "imports/";
    public static final String CONFIG_FILE = "config.json";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void ensureDirectoryExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }


    public static List<String> listFiles(String directory) {
        List<String> fileNames = new ArrayList<>();
        File dir = new File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            return fileNames;
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    fileNames.add(file.getName().replace(".json", ""));
                }
            }
        }
        return fileNames;
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("FILE ERROR: File not found — " + path);
            return false;
        }
        boolean deleted = file.delete();
        if (!deleted) {
            System.out.println("FILE ERROR: Could not delete — " + path);
        }
        return deleted;
    }

    public static boolean writeJson(String path, Object object) {
        try (Writer writer = new FileWriter(path)) {
            gson.toJson(object, writer);
            return true;
        } catch (IOException e) {
            System.out.println("FILE ERROR: Could not write to " + path + " — " + e.getMessage());
            return false;
        }
    }

    public static boolean writeText(String path, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, false))) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            System.out.println("FILE ERROR: Could not write text to " + path + " — " + e.getMessage());
            return false;
        }
    }

    public static <T> T readJson(String path, Class<T> clazz) {
        if (!fileExists(path)) {
            return null;
        }
        try (Reader reader = new FileReader(path)) {
            return gson.fromJson(reader, clazz);
        } catch (IOException e) {
            System.out.println("FILE ERROR: Could not read " + path + " — " + e.getMessage());
            return null;
        }
    }

    public static <T> T readJson(String path, com.google.gson.reflect.TypeToken<T> token) {
        if (!fileExists(path)) {
            return null;
        }
        try (Reader reader = new FileReader(path)) {
            return gson.fromJson(reader, token.getType());
        } catch (IOException e) {
            System.out.println("FILE ERROR: Could not read " + path + " — " + e.getMessage());
            return null;
        }
    }

    public static boolean writeCsv(String path, List<String> rows) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, false))) {
            for (String row : rows) {
                writer.write(row);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("FILE ERROR: Could not write CSV to " + path + " — " + e.getMessage());
            return false;
        }
    }

    public static boolean appendCsv(String path, String row) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
            writer.write(row);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.out.println("FILE ERROR: Could not append to " + path + " — " + e.getMessage());
            return false;
        }
    }

    public static List<String> readCsv(String path) {
        List<String> rows = new ArrayList<>();
        if (!fileExists(path)) {
            return rows;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    rows.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("FILE ERROR: Could not read CSV from " + path + " — " + e.getMessage());
        }
        return rows;
    }

    public static void initialiseDirectories() {
        ensureDirectoryExists(PROFILES_DIR);
        ensureDirectoryExists(EXPORTS_DIR);
        ensureDirectoryExists(IMPORTS_DIR);
    }
}
