package View;

import Model.Employee;
import Model.Task;

import java.util.List;

public class ConsoleView
{

    /**
     * Class for displaying formatted results to console
     */
    List<Employee> employeeList;
    List<Task> taskList;

    private final int DIV_LENGTH = 60;

    /**
     * Constructor stores data used to populate entity data results
     * @param employeeList list of employees
     * @param taskList list of tasks
     */
    public ConsoleView(List<Employee> employeeList, List<Task> taskList)
    {
        this.employeeList = employeeList;
        this.taskList = taskList;
    }

    // PRINTERS

    /**
     * Prints a solution table to the console
     * @param solution The solution to print
     */

    public void printSolution(int[] solution)
    {
        System.out.println(getSolutionTable(solution));
    }

    /**
     * Prints a population table to the console
     * @param population The population to print
     */
    public void printPopulation(int[][] population)
    {
        System.out.println(getPopulationTable(population));
    }

    /**
     * Prints both employee and task tables to console
     */

    public void printData()
    {
        System.out.println(getTaskTable() + "\n" + getEmployeeTable());
    }

    /**
     * Prints employee table to console
     */
    public void printEmployees()
    {
        System.out.println(getEmployeeTable());
    }

    /**
     * Prints task table to console
     */
    public void printTasks()
    {
        System.out.println(getTaskTable());
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
