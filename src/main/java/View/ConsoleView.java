package View;

import Algorithms.CostCalculator;
import Model.Employee;
import Model.Task;
import Utilities.Observer;
import Utilities.ObserverException;

import java.util.*;

public class ConsoleView implements Observer
{

    /**
     * Class for displaying formatted results to console
     */
    List<Employee> employeeList;
    List<Task> taskList;
    private Scanner sc;
    private Map<String, String> loadedData;
    private final int DIV_LENGTH = 60;

    /**
     * Constructor for the ConsoleView.
     */
    public ConsoleView() {
        this.sc = new Scanner(System.in);
        this.loadedData = new HashMap<>();
        this.loadedData.put("Employees", "none");
        this.loadedData.put("Tasks", "none");
    }

    /**
     * Constructor with initial data
     * @param employeeList list of employees
     * @param taskList list of tasks
     */
    public ConsoleView(List<Employee> employeeList, List<Task> taskList, Scanner sc)
    {
        this();
        this.employeeList = employeeList;
        this.taskList = taskList;
        if (employeeList != null && !employeeList.isEmpty()) {
            this.loadedData.put("Employees", "loaded");
        }
        if (taskList != null && !taskList.isEmpty()) {
            this.loadedData.put("Tasks", "loaded");
        }
    }

    @Override
    public void update(String messageType, String title, String content)
    {
        if (messageType.equals("MENU"))
        {
            displayMenu(title, content);
        }
        else if (messageType.equals("INFO"))
        {
            displayInfo(title, content);
        }
        else if (messageType.equals("ERROR"))
        {
            displayError(title, content);
        }
        else if (messageType.equals("SUCCESS"))
        {
            displaySuccess(title, content);
        }
    }

    /**
     * @param prompt  The prompt to display to the user
     * @param options The options the user can choose from
     * @return
     */

//    @Override
//    public int requestInput(String prompt, String[] options) throws InputException
//    {
//
//        System.out.println(prompt);
//        for (int i = 1; i < options.length; i++)
//        {
//            System.out.println(i + ". " + options[i]);
//        }
//        if (options[0] != null)
//        {
//            System.out.println("0. " + options[0]);
//        }
//
//        int selection = -1;
//        while (selection < 0 || selection >= options.length)
//        {
//            System.out.print("Enter selection (1-" + options.length + "): \n");
//            try
//            {
//
//                selection = sc.nextInt();
//                if (selection < 0 || selection >= options.length)
//                {
//                    System.out.println("Invalid selection. Please try again.");
//                }
//            }
//            catch (NumberFormatException e)
//            {
//                throw new InputException("Invalid selection. Please try again.");
//            }
//            catch (NoSuchElementException e)
//            {
//                throw new InputException(e.getMessage());
//            }
//        }
//        return selection;
//    }

//    @Override
//    public int requestInput(String prompt, String[] options) {
//        // Build the menu string
//        StringBuilder menuString = new StringBuilder();
//        menuString.append(prompt).append("\n");
//
//        // Add numbered options (1 to n-1)
//        for (int i = 1; i < options.length; i++) {
//            menuString.append(i).append(". ").append(options[i]).append("\n");
//        }
//
//        // Add exit option if available
//        if (options[0] != null) {
//            menuString.append("0. ").append(options[0]).append("\n");
//        }
//
//        // Create a new BufferedReader for each input request
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//
//        // Loop until valid input is received
//        while (true) {
//            try {
//                // Display the menu
//                System.out.print(menuString);
//                System.out.print("Select a number between 0-" + (options.length - 1) + ": \n");
//
//                // Read input
//                String inputLine = reader.readLine();
//
//                // Check for null (end of stream)
//                if (inputLine == null) {
//                    System.out.println("End of input stream reached. Using default option 0.");
//                    return 0; // Return the exit option as default
//                }
//
//                // Parse input
//                try {
//                    int userInput = Integer.parseInt(inputLine.trim());
//
//                    // Check if input is within valid range
//                    if (userInput < 0 || userInput >= options.length) {
//                        System.out.println("Invalid selection: Number must be between 0 and " + (options.length - 1) + "\n");
//                    } else {
//                        return userInput; // Valid input
//                    }
//                } catch (NumberFormatException e) {
//                    System.out.println("Invalid selection: Please enter a number\n");
//                }
//
//            } catch (IOException e) {
//                System.out.println("Error reading input. Please try again.\n");
//
//                // In case of severe IO error, return the exit option
//                if (e.getMessage() != null && e.getMessage().contains("Stream closed")) {
//                    System.out.println("Input stream closed. Using default option 0.");
//                    return 0;
//                }
//            }
//        }
//    }

    public int requestInput(String title,  String prompt, String[] options) {
        int userInput;
        int max = options.length;
        StringBuilder menuString = new StringBuilder();;
        for (int i = 1; i < max; i++)
        {
            menuString.append(i).append(". ").append(options[i]).append("\n");
        }
        if (options[0] != null)
        {
            menuString.append("0. ").append(options[0]).append("\n");
        }
        menuString.append("Select and integer between (0-").append(max-1).append("): ");
        String menuStr = menuString.toString();
        while (true) {
            try {
                displayMenu(title, prompt);
                System.out.println(menuStr);

                while (!sc.hasNextInt()) {
                    displayError("Invalid selection", "Please enter an integer.");
                    displayMenu(title, prompt);
                    System.out.print(menuStr);
                    sc.nextLine();
                }
                userInput = sc.nextInt();
                if ((userInput < 0 || userInput >= max)) {
                    throw new IllegalArgumentException("Integer must be within 0 -> " + (max-1));
                }
                return userInput;
            } catch (InputMismatchException e) {
                displayError("Invalid selection", "Please enter an integer.");
            } catch (IllegalArgumentException e) {
                displayError("Invalid selection", e.getMessage());
            }
        }
    }

    public double requestInput(String title, String prompt, double min, double max) {
        double userInput;

        String menuStr =  "Select a decimal between " + min + " and " + max + ":\n";
        while (true) {
            try {
                displayMenu(title, prompt);
                System.out.println(menuStr);

                while (!sc.hasNextDouble()) {
                    displayError("Invalid selection", "Please enter a decimal.");
                    displayMenu(title, prompt);
                    System.out.print(menuStr);
                    sc.nextLine();
                }
                userInput = sc.nextDouble();
                if ((userInput < min || userInput > max)) {
                    throw new IllegalArgumentException("Decimal must be within " + min + " -> " + max);
                }
                return userInput;
            } catch (InputMismatchException e) {
                displayError("Invalid selection", "Please enter a decimal.");
            } catch (IllegalArgumentException e) {
                displayError("Invalid selection", e.getMessage());
            }
        }
    }

    public int requestInput(String title, String prompt, int min, int max) {
        int userInput;
        String menuStr =  "Select an integer between " + min + " and " + max + ":\n";
        while (true)
        {
            try {
                displayMenu(title, prompt);
                System.out.println(menuStr);

                while (!sc.hasNextInt()) {
                    displayError("Invalid selection", "Please enter an integer.");
                    displayMenu(title, prompt);
                    System.out.print(menuStr);
                    sc.nextLine();
                }
                userInput = sc.nextInt();
                if ((userInput < min || userInput > max)) {
                    throw new IllegalArgumentException("Integer must be within " + min + " -> " + max);
                }
                return userInput;
            } catch (InputMismatchException e) {
                displayError("Invalid selection", "Please enter an integer.");
            } catch (IllegalArgumentException e) {
                displayError("Invalid selection", e.getMessage());
            }
        }
    }

    @Override
    public void displayData(String dataType, Object data)
    {
        if (dataType.equals("TASKS") && data instanceof List<?>) {
            this.taskList = (List<Task>) data;
            System.out.println(getTaskTable());
        } else if (dataType.equals("EMPLOYEES") && data instanceof List<?>) {
            this.employeeList = (List<Employee>) data;
            System.out.println(getEmployeeTable());
        } else if (dataType.equals("SOLUTION") && data instanceof int[]) {
            System.out.println(getSolutionTable((int[]) data));
        } else if (dataType.equals("POPULATION") && data instanceof int[][]) {
            System.out.println(getPopulationTable((int[][]) data));
        } else if (dataType.equals("ALL_DATA")) {
            System.out.println(getTaskTable() + "\n" + getEmployeeTable());
        }
    }

    @Override
    public void showProgress(String algorithmName, int iteration, double cost, String info) {
        StringBuilder sb = new StringBuilder();
        sb.append("Algorithm: ").append(algorithmName)
                .append(" | Iteration: ").append(iteration)
                .append(" | Best Cost: ").append(String.format("%.2f", cost));

        if (info != null && !info.isEmpty()) {
            sb.append(" | ").append(info);
        }

        System.out.println(sb.toString());
    }

    @Override
    public String getFinalSolution(int[] solution, double cost, int generation, boolean feasible) throws ObserverException
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "\nGeneration: ").append(generation).append(" \n");
        sb.append("Total cost: ").append(String.format("%.2f\n", cost));
        sb.append("Feasible Solution: ").append(feasible).append("\n\n");

        sb.append(getSolutionTable(solution)).append("\n");

        sb.append("\nWorkload Distribution:\n");

        for (Employee employee : employeeList)
        {
            String employeeId = employee.getId();
            int totalTime = CostCalculator.calculateEmployeeWorkload(solution, taskList, employeeList, employeeId);

            sb.append("  Employee ").append(employeeId)
                    .append(": ").append(totalTime)
                    .append(" / ").append(employee.getAvailableHours())
                    .append(" hours (")
                    .append(String.format("%.1f", (double) totalTime / employee.getAvailableHours() * 100))
                    .append("%)");

            if (totalTime > employee.getAvailableHours())
            {
                sb.append(" - OVERLOADED");
            }

            sb.append("\n");
        }

        // Print penalty breakdown
        sb.append("\nPenalty Breakdown:\n");
        double overloadPenalty = 0;
        for (Employee employee : employeeList)
        {
            overloadPenalty += CostCalculator.calculateOverloadPenalty(solution, taskList, employeeList);
        }

        double skillMismatchPenalty = CostCalculator.calculateSkillMismatchPenalty(solution, taskList, employeeList);
        double deadlineViolationPenalty = CostCalculator.calculateDeadlineViolationPenalty(solution, taskList, employeeList);

        sb.append("  Overload Penalty: ").append(String.format("%.2f", overloadPenalty)).append("\n");
        sb.append("  Skill Mismatch Penalty: ").append(String.format("%.2f", skillMismatchPenalty)).append("\n");
        sb.append("  Deadline Violation Penalty: ").append(String.format("%.2f", deadlineViolationPenalty)).append("\n");


        return sb.toString();
    }

    /**
     * Update the loaded data status.
     *
     * @param dataType The type of data ("Employees" or "Tasks")
     * @param filename The filename that was loaded
     */
    public void updateLoadedData(String dataType, String filename) {
        this.loadedData.put(dataType, filename);
    }

    /**
     * Get the current loaded data status.
     *
     * @return A string representation of the loaded data
     */
    public String getLoadedDataStatus() {
        return "Current Data loaded: Employees: " + loadedData.get("Employees") +
                ", Tasks: " + loadedData.get("Tasks");
    }

    /**
     * Display a menu to the user.
     *
     * @param title The menu title
     * @param content The menu content
     */
    private void displayMenu(String title, String content) {
        System.out.println("\n" + centerString(title, DIV_LENGTH, true));
        if (content != null && !content.isEmpty()) {
            System.out.println(content);
        }
    }

    /**
     * Display info message.
     *
     * @param title The info title
     * @param content The info content
     */
    private void displayInfo(String title, String content) {
        System.out.println("\n" + centerString(title, DIV_LENGTH, false));
        System.out.println(content);
    }

    /**
     * Display error message.
     *
     * @param title The error title
     * @param content The error content
     */
    private void displayError(String title, String content) {
        System.out.println("\n \n[ERROR] " + title);
        System.out.println(content);
    }

    /**
     * Display success message.
     *
     * @param title The success title
     * @param content The success content
     */
    private void displaySuccess(String title, String content) {
        System.out.println("\n[SUCCESS] " + title);
        System.out.println(content);
    }

    // CREATORS

    /**
     * Creates a table of all employees
     * @return String of employee table and stats
     */
    public String getEmployeeTable()
    {
        StringBuilder sb = new StringBuilder();

        int idWidth = 4;
        int idxWidth = 5;
        int hoursWidth = 7;
        int skillLvlWidth= 11;
        int skillsWidth = 12;

        sb.append(centerString("EMPLOYEES", DIV_LENGTH, true)+"\n");

        String horizontalLine = "_".repeat(45);

        sb.append(horizontalLine+"\n");
        sb.append("| ID | IDX | HOURS | SKILL LVL |   SKILLS   |\n");
        sb.append(horizontalLine+"\n");

        for(Employee employee : employeeList)
        {
            String id = centerString(employee.getId(), idWidth, false);
            String idx = centerString(Integer.toString(employee.getIdx()), idxWidth, false);
            String hours = centerString(Integer.toString(employee.getAvailableHours()), hoursWidth, false);
            String skillLvl = centerString(Integer.toString(employee.getSkillLevel()), skillLvlWidth, false);
            String skills = centerString(employee.getSkills().toString(), skillsWidth, false);

            sb.append("|"+id+"|"+idx+"|"+hours+"|"+skillLvl+"|"+skills+"|\n");
        }
        sb.append(horizontalLine+"\n");

        return sb.toString();
    }

    /**
     * Creates a table of all tasks
     * @return String of task table and stats
     */

    public String getTaskTable()
    {
        String horizontalLine = "_".repeat(59);

        int taskIDWidth = 4;
        int taskIdxWidth = 5;
        int estTimeWidth = 10;
        int difficultyWidth = 12;
        int deadlineWidth = 10;
        int taskSkillWidth = 11;


        StringBuilder sb = new StringBuilder();

        sb.append(centerString("TASKS", DIV_LENGTH, true)+"\n");

        sb.append(horizontalLine+"\n");
        sb.append("| ID | IDX | EST TIME | DIFFICULTY | DEADLINE | SKILL REQ |\n");
        sb.append(horizontalLine+"\n");
        for(Task task : taskList)
        {
            String taskId = centerString(task.getId(), taskIDWidth, false);
            String taskIdx = centerString(Integer.toString(task.getIdx()), taskIdxWidth, false);
            String estTime = centerString(Integer.toString(task.getEstimatedTime()), estTimeWidth, false);
            String difficulty = centerString(Integer.toString(task.getDifficulty()), difficultyWidth, false);
            String deadline = centerString(Integer.toString(task.getDeadline()), deadlineWidth, false);
            String taskSkill = centerString(task.getRequiredSkill(), taskSkillWidth, false);

            sb.append("|" + taskId + "|" + taskIdx +"|"+ estTime + "|" + difficulty + "|" + deadline + "|" + taskSkill + "|\n");

        }
        sb.append(horizontalLine+"\n");


        return sb.toString();
    }

    /**
     * Creates a solution table
     * @param solution The solution to print
     * @return a string of the solution table
     */
    public String getSolutionTable(int[] solution)
    {
        int lineLength = 55;

        int taskIDWidth = 9;
        int taskSkillWidth = 12;
        int employeeIdWidth = 13;
        int employeeSkillWidth = 16;
        StringBuilder sb = new StringBuilder();
        String horizontalLine = "_".repeat(lineLength);
        sb.append(horizontalLine+"\n");
        sb.append("| Task ID | Employee ID | Task Skill | Employee Skill |\n");
        sb.append(horizontalLine+"\n");
        for(int i = 0; i < solution.length; i++)
        {
            String taskId = centerString(taskList.get(i).getId(), taskIDWidth, false);
            String taskSkill = centerString(taskList.get(i).getRequiredSkill(), taskSkillWidth, false);
            String employeeId = centerString(employeeList.get(solution[i]).getId(), employeeIdWidth, false);
            String employeeSkills = centerString(employeeList.get(solution[i]).getSkills().toString(), employeeSkillWidth, false);
            sb.append("|" + taskId + "|" + employeeId + "|" + taskSkill + "|" + employeeSkills + "|\n");
        }
        sb.append(horizontalLine+"\n");
        return sb.toString();
    }

    /**
     * Creates tables of all solutions in a population
     * @param population The population to print
     * @return a string of the solution tables
     */

    public String getPopulationTable(int[][] population)
    {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < population.length; i++)
        {
            String title = "POPULATION "+ i;
            sb.append(centerString(title, DIV_LENGTH, true)+"\n");
            sb.append(getSolutionTable(population[i]));
        }
        return sb.toString();
    }

    /**
     * Function to center text within a given size
     *
     * @param text  The text to center
     * @param width The total width of the area the text must be centered in
     * @return A padded version of the original text centered within the given width.
     */

    public static String centerString(String text, int width, boolean heading) {

        String padding = " ";
        if(heading)
        {
            padding = "=";
        }

        if (text == null || width <= text.length()) {
            return text;
        }

        int paddingTotal = width - text.length();
        int paddingStart = paddingTotal / 2;
        int paddingEnd = paddingTotal - paddingStart;

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < paddingStart; i++) result.append(padding);
        result.append(text);
        for (int i = 0; i < paddingEnd; i++) result.append(padding);

        return result.toString();
    }


}
