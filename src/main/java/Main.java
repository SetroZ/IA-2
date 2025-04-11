import Algorithms.GeneticAlgorithm;
import Controller.MenuController;
import Model.Employee;
import Model.Task;
import Utilities.DataGenerator;
import Utilities.FileOutput;
import Utilities.Initialise;
import View.ConsoleView;

import java.util.List;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            ConsoleView consoleView = new ConsoleView();
            FileOutput fileOutput = new FileOutput();

            MenuController menuController = new MenuController(consoleView, fileOutput);
            menuController.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }
}