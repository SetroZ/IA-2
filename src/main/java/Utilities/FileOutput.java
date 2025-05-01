package Utilities;

import Exceptions.ObserverException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOutput implements Observer {

    public boolean isRunAll = false;

    @Override
    public void update(String messageType, String title, String content) throws ObserverException {
        if (messageType.equalsIgnoreCase("FILE")) {
            String uniqueFName = getUniqueFile(title);
            String[] filepath = uniqueFName.split("/");
            String fileName = filepath[filepath.length - 1];
            File f = new File(uniqueFName);

            try (FileWriter fw = new FileWriter(f)) {
                fw.write(content);
                System.out.println("\n[SUCCESS] " + "Writing to File:");
                System.out.println(fileName);
            } catch (IOException e) {
                throw new ObserverException("Error writing to file: " + e.getMessage());
            }
        }
    }

    public String getUniqueFile(String title) {

        String directory = "results";
        String extension = ".txt";
        int runNumber = 1;
        if (isRunAll) {
            File runDir;
            do {
                runDir = new File(directory, "run(" + runNumber + ")");
                runNumber++;
            } while (runDir.exists() && Files.exists(Paths.get(runDir + "/" + title + extension)));

            directory = runDir.getPath();
        }

        // Ensure the directory exists
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        int counter = 0;
        Path path;
        String fileName;

        do {
            fileName = directory + "/" + title + (counter == 0 ? "" : "(" + counter + ")") + extension;
            path = Paths.get(fileName);
            counter++;
        } while (Files.exists(path));

        return fileName;
    }

    @Override
    public String getFinalSolution(int[] solution, double cost, int generation, boolean feasible)
            throws ObserverException {
        return "";
    }
}
