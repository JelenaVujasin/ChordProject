package app.file;

import app.AppConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileHelp {

    public static boolean isPathFile(String rootDirectory, String path) {
        System.out.println(rootDirectory);
        File f = new File(rootDirectory + "\\" + path);
        System.out.println("- file exists");
        System.out.println(f);
        return f.isFile();
    }


    public static FileInfo getFileInfoFromPath(String rootDirectory, String path) {
        path = rootDirectory + "\\" + path;
        File f = new File(path);

        if (!f.exists()) {
            AppConfig.timestampedErrorPrint("File " + path + " doesn't exist.");
            return null;
        }
        if (f.isDirectory()) {
            AppConfig.timestampedErrorPrint(path + " is a directory and not a file.");
            return null;
        }

        try {
            String filePath = path.replace(rootDirectory + "\\", "");

            BufferedReader reader = new BufferedReader(new FileReader(f));
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line + "\n");
            }
            reader.close();

            if (!fileContent.isEmpty())
                fileContent.deleteCharAt(fileContent.length() - 1);

            return new FileInfo(filePath, fileContent.toString(), AppConfig.myServentInfo.getListenerPort());
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Couldn't read " + path + ".");
        }

        return null;

    }



}
