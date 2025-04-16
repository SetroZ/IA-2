package Utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileOutput implements Observer
{

    @Override
    public void update(String messageType, String title, String content) throws ObserverException
    {
        if(messageType.equalsIgnoreCase("FILE"))
        {
            String fileName = "results/" + title ;

            String extension = ".txt";

            Path path = Paths.get(fileName+extension);
            int counter = 1;
            File f;

            // Create directories if they don't exist
            File dir = new File("results");
            if (!dir.exists())
            {
                dir.mkdirs();
            }

            while (Files.exists(path))
            {
                fileName = "results/"+title + "(" + counter + ")" + extension;
                path = Paths.get(fileName);
                counter++;
            }
           if(counter == 1)
           {
               fileName += extension;
           }
            f = new File(fileName);

            try (FileWriter fw = new FileWriter(f))
            {
                fw.write(content);
                System.out.println("\n[SUCCESS] " + "Writing to File:");
                System.out.println("out/"+fileName);
            }
            catch (IOException e)
            {
                throw new ObserverException("Error writing to file: " + e.getMessage());
            }
        }
    }


    @Override
    public void displayData(String dataType, Object data) throws ObserverException
    {

    }

    @Override
    public void showProgress(String algorithmName, int iteration, double cost, String info) throws ObserverException
    {

    }

    @Override
    public String getFinalSolution(int[] solution, double cost, int generation, boolean feasible) throws ObserverException
    {
        return "";
    }
}
