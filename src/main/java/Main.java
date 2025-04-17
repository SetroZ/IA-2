import Controller.MenuController;
import Utilities.FileOutput;
import View.ConsoleView;

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