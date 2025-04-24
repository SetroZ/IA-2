import Controller.MenuController;
import Utilities.FileOutput;
import View.ConsoleObserver;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            ConsoleObserver consoleObserver = new ConsoleObserver();
            FileOutput fileOutput = new FileOutput();

            MenuController menuController = new MenuController(consoleObserver, fileOutput);
            menuController.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }
}