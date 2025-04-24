package Controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import Algorithms.Algorithm;
import Algorithms.AntColAlgorithm;
import Algorithms.GeneticAlgorithm;
import Algorithms.ParticleSwarm;
import Factories.AlgorithmFactory;
import Model.Employee;
import Model.Task;
import Utilities.DataGenerator;
import Utilities.FileOutput;
import Utilities.InputException;
import Utilities.LoadDataException;
import Utilities.Observer;
import Utilities.ObserverException;
import Utilities.RandomDataGen;
import Utilities.RandomDataGen.DataSet;
import View.ConsoleObserver;

public class MenuController{
    private final List<Observer> observers;
    private List<Employee> employees;
    private List<Task> tasks;
    private final ConsoleObserver consoleObserver;
    private String employeesFileName;
    private String tasksFileName;

    /**
     * Constructor for menu controller
     * 
     * @param consoleObserver the ConsoleObserver instance to use for user interaction
     */

    public MenuController(ConsoleObserver consoleObserver, FileOutput fileOutput) {
        observers = new ArrayList<>();
        this.consoleObserver = consoleObserver;
        registerObserver(consoleObserver);
        registerObserver(fileOutput);
    }

    /**
     *
     * @param observer The observer to register
     */

    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    /**
     * Notify all observers of change
     *
     * @param messageType The type of message being sent
     * @param title       The title of the message
     * @param content     The content or body of the message
     */


    public void notifyObservers(String messageType, String title, String content) {
        for (Observer observer : observers) {
            observer.update(messageType, title, content);
        }
    }

    /**
     * Run the menu application loop
     */

    public void start() throws ObserverException {
        boolean exit = false;

        StringBuilder sb = new StringBuilder();
        if (employees != null && tasks != null) {
            sb.append("Loaded data: Employees: ")
                    .append(employeesFileName != null ? employeesFileName : "none")
                    .append(", Tasks: ")
                    .append(tasksFileName != null ? tasksFileName : "none")
                    .append("\n");
        }

        while (!exit) {
            try {
                int choice = consoleObserver.requestInput("WELCOME", sb.toString(),
                        new String[] { "Exit", "Load stored data from csv", "Run an algorithm", "Load Random Data" });
                switch (choice) {
                    case 1:
                        loadDataMenu();
                        break;
                    case 2:
                        runAlgorithmMenu();
                        break;
                    case 3:
                        RandomDataMenu();
                        break;
                    case 0:
                        exit = true;
                        notifyObservers("INFO", "Goodbye", "Exiting application...");
                        break;
                }
            } catch (InputException e) {
                notifyObservers("ERROR", "Error occurred while getting input", e.getMessage());
                throw new InputException(e.getMessage());
            }
        }
    }

    /**
     * Show the loading meny
     */

    private void loadDataMenu() {
        // Get a list of all files in /resources

        List<String> employeeFiles = getResourceFiles();

        if (employeeFiles.isEmpty()) {
            notifyObservers("ERROR", "No Data Files", "No CSV files found in resources folder.");
            return;
        }

        // Add exit option
        employeeFiles.addFirst("Exit");

        int choice = consoleObserver.requestInput("LOAD EMPLOYEE DATA",
                "Select the CSV file to use from the resources folder\n" + consoleObserver.getLoadedDataStatus(),
                employeeFiles.toArray(new String[0]));

        if (choice == 0) {
            return;
        }

        try {
            // Load selected employee file
            String employeesFilePath = employeeFiles.get(choice);

            String[] fileName = employeesFilePath.split("/");
            employeesFileName = fileName[fileName.length - 1];

            employees = DataGenerator.loadEmployees("/" + employeesFileName);
            consoleObserver.updateLoadedData("Employees", employeesFileName);

            // Display loaded employees
            consoleObserver.displayData("EMPLOYEES", employees);

            List<String> taskFiles = getResourceFiles();

            if (taskFiles.isEmpty()) {
                notifyObservers("ERROR", "No Data Files", "No CSV files found in resources folder.");
                return;
            }

            // Add exit option
            taskFiles.addFirst("Exit");

            choice = consoleObserver.requestInput("LOAD TASK DATA",
                    "Select the CSV file to use from the resources folder\n" +
                            consoleObserver.getLoadedDataStatus(),
                    taskFiles.toArray(new String[0]));

            if (choice == 0) {
                return; // User selected exit
            }

            // Load selected task file
            String tasksFilePath = taskFiles.get(choice);
            String[] fname = tasksFilePath.split("/");

            tasksFileName = fname[fname.length - 1];
            tasks = DataGenerator.loadTasks("/" + tasksFileName);
            consoleObserver.updateLoadedData("Tasks", tasksFileName);

            // Display loaded tasks
            consoleObserver.displayData("TASKS", tasks);

            notifyObservers("SUCCESS", "Data Loaded",
                    "Successfully loaded employees from " + employeesFileName +
                            " and tasks from " + tasksFileName);

        } catch (LoadDataException e) {
            notifyObservers("ERROR", "Data Loading Error",
                    e.getMessage());
        }
    }

    /**
     * Algorithm run menu
     */
    private void RandomDataMenu() {
        boolean exit = false;

        try {
            while (!exit) {

                int taskCount = consoleObserver.requestInput("SET RANDOM PARAMETERS:",
                        "Enter Task count:",
                        1, 10000);
                int employeeCount = consoleObserver.requestInput("SET RANDOM PARAMETERS:",
                        "Enter Employee count:",
                        1, 10000);
                DataSet ds = RandomDataGen.generateDataSet(taskCount, employeeCount);
                tasks = ds.tasks;
                employees = ds.employees;
                consoleObserver.displayData("EMPLOYEES", employees);
                consoleObserver.displayData("TASKS", tasks);
                exit = true;
            }
        } catch (InputException e) {
            notifyObservers("ERROR", "Error occurred while getting input", e.getMessage());
        }
    }

    private void runAlgorithmMenu() {
        boolean exit = false;

        try {
            while (!exit) {

                int choice = consoleObserver.requestInput("CHOOSE ALGORITHM",
                        "Select an Algorithm to run",
                        new String[] { "Exit", "Genetic Algorithm", "Swarm Optimisation", "Ant Colony" });

                switch (choice) {
                    case 0:
                        exit = true;
                        break;
                    case 1:
                        runGeneticAlgMenu();
                        break;
                    case 2:
                        runParticleSwarmMenu();
                        break;
                    case 3:
                        break;
                }
            }
        } catch (InputException e) {
            notifyObservers("ERROR", "Error occurred while getting input", e.getMessage());
        }
    }

    private void runParticleSwarmMenu() {

        int PS_POPULATION_SIZE_DEFAULT = 100;
        int PS_MAX_GEN_DEFAULT = 200;
        int GA_REPORTING_FREQUENCY_DEFAULT = 5;
        boolean GA_FILE_OUTPUT_DEFAULT = true;

        ParticleSwarm ps = new AlgorithmFactory(tasks, employees, observers).createParticleSwarm(
                PS_POPULATION_SIZE_DEFAULT,
                PS_MAX_GEN_DEFAULT,
                GA_REPORTING_FREQUENCY_DEFAULT,
                GA_FILE_OUTPUT_DEFAULT);
        runMenu(ps, "ParticleSwarm");

    }

    private void runAntColAlgMenu() {
        // Initialise with parameters
        double ACO_DECAY_RATE_DEFAULT = 0.1;
        double ACO_INITIAL_PHEROMONE_DEFAULT = 0.1;
        int ACO_NUM_ANTS_DEFAULT = 5;
        int ACO_MAX_ITERATIONS_DEFAULT = 500;
        int ACO_REPORTING_FREQUENCY_DEFAULT = 5;
        boolean ACO_FILE_OUTPUT_DEFAULT = true;

        boolean exit = false;

        while (!exit) {
            StringBuilder sb = new StringBuilder();

            int choice = consoleObserver.requestInput("DEFINE ANT COLONY OPTIMISATION ",
                    "Specify the parameters to use for this algorithm or proceed",
                    new String[] { "Exit", "Number of Ants: " + ACO_NUM_ANTS_DEFAULT,
                            "Pheromone Decay Rate: " + ACO_DECAY_RATE_DEFAULT,
                            "Initial Pheromone Value: " + ACO_INITIAL_PHEROMONE_DEFAULT,
                            "Maximum Iterations: " + ACO_MAX_ITERATIONS_DEFAULT,
                            "Reporting Frequency: " + ACO_REPORTING_FREQUENCY_DEFAULT,
                            "Output to File: " + ACO_FILE_OUTPUT_DEFAULT,
                            "Proceed" });

            switch (choice) {
                case 0:
                    exit = true;
                    break;
                case 1:
                    ACO_NUM_ANTS_DEFAULT = getParameter("Number of Ants", ACO_NUM_ANTS_DEFAULT, 1,
                            Integer.MAX_VALUE);
                    break;
                case 2:
                    ACO_DECAY_RATE_DEFAULT = getParameter("Pheromone Decay Rate", ACO_DECAY_RATE_DEFAULT, 0.0, 1.0);
                    break;
                case 3:
                    ACO_INITIAL_PHEROMONE_DEFAULT = getParameter("Initial Pheromone Value", ACO_INITIAL_PHEROMONE_DEFAULT, 0.0, Double.MAX_VALUE);
                    break;
                case 4:
                    ACO_MAX_ITERATIONS_DEFAULT = getParameter("Maximum Iterations", ACO_MAX_ITERATIONS_DEFAULT, 1, Integer.MAX_VALUE);
                    break;
                case 5:
                    ACO_REPORTING_FREQUENCY_DEFAULT = getParameter("Reporting Frequency", ACO_REPORTING_FREQUENCY_DEFAULT,
                            1, Integer.MAX_VALUE);
                    break;
                case 6:
                    ACO_FILE_OUTPUT_DEFAULT = getParameter("Output to File", ACO_FILE_OUTPUT_DEFAULT);
                    break;
                case 7:
                    AntColAlgorithm aco = new AlgorithmFactory(tasks, employees, observers).createAntColonyOptimisation(
                            ACO_NUM_ANTS_DEFAULT, ACO_DECAY_RATE_DEFAULT, ACO_INITIAL_PHEROMONE_DEFAULT, ACO_MAX_ITERATIONS_DEFAULT,
                            ACO_REPORTING_FREQUENCY_DEFAULT, ACO_FILE_OUTPUT_DEFAULT);
                    runMenu(aco, "Ant Colony");
                default:
                    break;
            }

        }
    }

    private void runGeneticAlgMenu() {
        // Initialise with parameters
        double GA_CROSSOVER_DEFAULT = 0.2;
        double GA_MUTATION_DEFAULT = 0.1;
        int GA_POPULATION_SIZE_DEFAULT = 100;
        int GA_MAX_GEN_DEFAULT = 200;
        int GA_REPORTING_FREQUENCY_DEFAULT = 5;
        boolean GA_FILE_OUTPUT_DEFAULT = true;

        boolean exit = false;

        while (!exit) {

            int choice = consoleObserver.requestInput("DEFINE GENETIC ALGORITHM",
                    "Specify the parameters to use for this algorithm or proceed",
                    new String[] { "Exit", "Population size: " + GA_POPULATION_SIZE_DEFAULT,
                            "Crossover rate: " + GA_CROSSOVER_DEFAULT,
                            "Mutation rate: " + GA_MUTATION_DEFAULT,
                            "Maximum Generations: " + GA_MAX_GEN_DEFAULT,
                            "Reporting frequency: " + GA_REPORTING_FREQUENCY_DEFAULT,
                            "Output to file: " + GA_FILE_OUTPUT_DEFAULT,
                            "Proceed" });

            switch (choice) {
                case 0:
                    exit = true;
                    break;
                case 1:
                    GA_POPULATION_SIZE_DEFAULT = getParameter("Population size", GA_POPULATION_SIZE_DEFAULT, 1,
                            Integer.MAX_VALUE);
                    break;
                case 2:
                    GA_CROSSOVER_DEFAULT = getParameter("Crossover rate", GA_CROSSOVER_DEFAULT, 0.0, 1.0);
                    break;
                case 3:
                    GA_MUTATION_DEFAULT = getParameter("Mutation rate", GA_MUTATION_DEFAULT, 0.0, 1.0);
                    break;
                case 4:
                    GA_MAX_GEN_DEFAULT = getParameter("Maximum Generations", GA_MAX_GEN_DEFAULT, 1, Integer.MAX_VALUE);
                    break;
                case 5:
                    GA_REPORTING_FREQUENCY_DEFAULT = getParameter("Reporting frequency", GA_REPORTING_FREQUENCY_DEFAULT,
                            1, Integer.MAX_VALUE);
                    break;
                case 6:
                    GA_FILE_OUTPUT_DEFAULT = getParameter("Output to file", GA_FILE_OUTPUT_DEFAULT);
                    break;
                case 7:
                    GeneticAlgorithm ga = new AlgorithmFactory(tasks, employees, observers).createGeneticAlgorithm(
                            GA_POPULATION_SIZE_DEFAULT, GA_CROSSOVER_DEFAULT, GA_MUTATION_DEFAULT, GA_MAX_GEN_DEFAULT,
                            GA_REPORTING_FREQUENCY_DEFAULT, GA_FILE_OUTPUT_DEFAULT);
                    runMenu(ga, "Genetic");
                    break;
                default:
                    break;
            }

        }
    }

    private void runMenu(Algorithm algorithm, String name) {
        notifyObservers("MENU", "ALGORITHM RUN", name + " algorithm running......");
        algorithm.run();
    }

    private boolean getParameter(String parameter, boolean defaultVal) {
        int choice = (consoleObserver.requestInput("ENTER PARAMETER", "Enter a value for " + parameter.toLowerCase(),
                new String[] { "Exit", "True", "False" }));
        return (choice == 0) ? defaultVal : choice != 2;
    }

    private double getParameter(String parameter, double defaultVal, double min, double max) {
        double choice = consoleObserver.requestInput("ENTER PARAMETER", "Enter a value for " + parameter.toLowerCase(), min,
                max);
        return ((choice == min - 1.0) ? defaultVal : choice);
    }

    private int getParameter(String parameter, int defaultVal, int min, int max) {
        int choice = consoleObserver.requestInput("ENTER PARAMETER", "Enter a value for " + parameter.toLowerCase(), min,
                max);
        return ((choice == min - 1.0) ? defaultVal : choice);
    }

    /**
     * Scan and return a list of all csv files in resource folder
     * 
     * @return String List of all file paths found
     */

    /*
     * private List<String> getResourceFiles() {
     * List<String> fileNames = new ArrayList<>();
     * try {
     * URL resourceURL = getClass().getResource(".");
     * 
     * System.err.println(resourceURL);
     * if (resourceURL == null) {
     * System.out.println("Resource path not found.");
     * return fileNames;
     * }
     * 
     * File directory = new File(resourceURL.toURI());
     * File[] files = directory.listFiles();
     * 
     * // Print name of the all files present in that path
     * if (files != null) {
     * for (File file : files) {
     * if (file.getName().endsWith(".csv")) {
     * System.err.println("wrtiting" + file.getAbsolutePath());
     * fileNames.add(file.getPath());
     * }
     * }
     * }
     * } catch (NullPointerException e) {
     * System.out.println("JEL:P");
     * notifyObservers("ERROR", "File Scanning Error",
     * "Error scanning resource files: " + e.getMessage());
     * } catch (URISyntaxException e) {
     * System.out.println("JE:PP");
     * notifyObservers("ERROR", "URI Syntax Error",
     * "Error scanning resource files: " + e.getMessage());
     * }
     * return fileNames;
     * }
     * }
     */
    private List<String> getResourceFiles() {
        List<String> fileNames = new ArrayList<>();
        try {
            // Root of classpath
            URL resourceURL = getClass().getClassLoader().getResource("");

            System.err.println("Resource root: " + resourceURL);

            if (resourceURL == null) {
                System.out.println("Resource path not found.");
                return fileNames;
            }

            File directory = new File(resourceURL.toURI());
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".csv")) {
                        System.err.println("Adding: " + file.getAbsolutePath());
                        fileNames.add(file.getPath());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileNames;
    }

}