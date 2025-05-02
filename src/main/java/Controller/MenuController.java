package Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Algorithms.*;
import Factories.AlgorithmFactory;
import Model.Employee;
import Model.Task;
import Utilities.*;
import Exceptions.InputException;
import Exceptions.LoadDataException;
import Exceptions.ObserverException;
import Utilities.RandomDataGen.DataSet;
import View.ConsoleObserver;

public class MenuController {

    private final List<Observer> observers;
    private List<Employee> employees;
    private List<Task> tasks;
    private final ConsoleObserver consoleObserver;
    FileOutput fileOutput;

    // Default params for algorithms
    // GA
    private double GA_CROSSOVER_DEFAULT = 0.2;
    private double GA_MUTATION_DEFAULT = 0.1;
    private int GA_ELITISM_DEFAULT = 2;
    // private int GA_RUN_ID = 0;

    // PS (None)
    private double PSO_INERTIA_WEIGHT = 0.5;
    private double PSO_PBEST_W = 1.5;
    private double PSO_GBEST_W = 1.5;

    // private int PS_RUN_ID = 0;

    // AC
    private double ACO_DECAY_RATE_DEFAULT = 0.1;
    private double ACO_INITIAL_PHEROMONE_DEFAULT = 0.1;
    // private int AC_RUN_ID = 0;

    // ALL
    private int POPULATION_SIZE_DEFAULT = 100;
    private int MAX_GEN_DEFAULT = 200;
    private int REPORTING_FREQUENCY_DEFAULT = 5;
    private boolean FILE_OUTPUT_DEFAULT = true;
    private int TRIAL_NUMBER_DEFAULT = 1;
    // private int ALL_RUN_ID = 1;

    /**
     * Constructor for menu controller
     * 
     * @param consoleObserver the ConsoleObserver instance to use for user
     *                        interaction
     */

    public MenuController(ConsoleObserver consoleObserver, FileOutput fileOutput) {
        observers = new ArrayList<>();
        this.consoleObserver = consoleObserver;
        this.fileOutput = fileOutput;
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
        while (!exit) {
            try {
                int choice = consoleObserver.requestInput("WELCOME", consoleObserver.getLoadedDataStatus(),
                        new String[] { "Exit", "Load stored data from csv", "Run an algorithm",
                                "Load Random Data", "Generate Visualisations" });
                switch (choice) {
                    case 1:
                        loadDataMenu();
                        break;
                    case 2:
                        runAlgorithmMenu();
                        break;
                    case 3:
                        randomDataMenu();
                        break;
                    case 4:
                        generateVisualisationsMenu();
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
     * Menu for generating performance visualizations
     */
    private void generateVisualisationsMenu() {
        boolean exit = false;

        try {
            determineNextRunId();
            int currentRunId = PathUtility.getRunId() - 1;
            VisualisationController visualController = new VisualisationController(currentRunId);

            boolean perIteration;
            System.out.println("Current Run ID");
            if (currentRunId <= 0) {
                notifyObservers("ERROR", "No existing runs found in results",
                        "No previous run data exists in results/performance");
                return;
            }

            while (!exit) {
                int choice = consoleObserver.requestInput("GENERATE VISUALIZATIONS",
                        "Select which charts to generate from run 1 to " + (currentRunId),
                        new String[] { "Exit",
                                "Run ID: " + (currentRunId),
                                "Solution Quality Comparison",
                                "Computational Efficiency Comparison",
                                "Constraint Satisfaction Comparison",
                                "Generate All Charts" });

                switch (choice) {
                    case 0:
                        exit = true;
                        break;
                    case 1:
                        int selectedRunId = getParameter("run id", currentRunId, 1, currentRunId);
                        visualController.setRUN_ID(selectedRunId);
                        break;
                    case 2:
                        try {
                            String result = visualController.generateSolutionQualityChart();
                            notifyObservers("SUCCESS", "Solution Quality Chart", result);
                        } catch (LoadDataException e) {
                            notifyObservers("ERROR", "Chart Generation Failed",
                                    "Failed to generate solution quality chart: " + e.getMessage());
                        }
                        break;
                    case 3:
                        try {
                            perIteration = selectGraphType();
                            String result = visualController.generateComputationalEfficiencyChart(perIteration);
                            notifyObservers("SUCCESS", "Computational Efficiency Chart", result);
                        } catch (LoadDataException e) {
                            notifyObservers("ERROR", "Chart Generation Failed",
                                    "Failed to generate computational efficiency chart: " + e.getMessage());
                        }
                        break;
                    case 4:
                        try {
                            String result = visualController.generateConstraintSatisfactionChart();
                            notifyObservers("SUCCESS", "Constraint Satisfaction Chart", result);
                        } catch (LoadDataException e) {
                            notifyObservers("ERROR", "Chart Generation Failed",
                                    "Failed to generate constraint satisfaction chart: " + e.getMessage());
                        }
                        break;
                    case 5:
                        perIteration = selectGraphType();
                        try {
                            String result = visualController.generateAllCharts(perIteration);
                            notifyObservers("SUCCESS", "All Charts Generated", result);
                        } catch (ObserverException e) {
                            notifyObservers("ERROR", "Chart Generation Failed",
                                    "Failed to generate charts: " + e.getMessage());
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            notifyObservers("ERROR", "Visualisation Error",
                    "An error occurred while trying to generate visualisations: " + e.getMessage());
        }
    }

    /**
     * Gets the users graph choice
     */

    private boolean selectGraphType() {
        int anotherChoice = consoleObserver.requestInput("DEFINE PARAMETER", "What efficiency graph to use",
                new String[] { "Average Runtime/Iteration", "Total Average Runtime" });
        return switch (anotherChoice) {
            case 0 -> false;
            case 1 -> true;
            default -> false;
        };
    }

    /**
     * Show the loading meny
     */

    private void loadDataMenu() {
        // Get a list of all files in /resources

        while (true) {
            try {
                List<String> files = DataGenerator.getResourceFiles();
                if (files.isEmpty()) {
                    notifyObservers("ERROR", "No Data Files", "No CSV files found in resources folder.");
                    return;
                }

                // Add exit option
                files.addFirst("Exit");
                int choice = consoleObserver.requestInput("LOAD DATA",
                        "Select the CSV file to use from the resources folder\n"
                                + consoleObserver.getLoadedDataStatus(),
                        files.toArray(new String[0]));

                if (choice == 0) {
                    return;
                }

                try {

                    loadSelectedData("employees", files.get(choice));
                } catch (LoadDataException e) {
                    try {
                        loadSelectedData("tasks", files.get(choice));
                    } catch (LoadDataException e1) {
                        notifyObservers("ERROR", "Data Loading Error",
                                e.getMessage());
                    }
                }
            } catch (LoadDataException e) {
                notifyObservers("ERROR", "Critical Error Loading Data", e.getMessage());
                return;
            }
        }
    }

    private void loadSelectedData(String fileType, String filePath) throws LoadDataException {

        String[] fileName = filePath.split("/");
        if (fileType.equalsIgnoreCase("employees")) {
            String employeesFileName = fileName[fileName.length - 1];
            employees = DataGenerator.loadEmployees(filePath);
            consoleObserver.updateLoadedData("Employees", employeesFileName);
            // Display loaded employees
            consoleObserver.displayData("EMPLOYEES", employees);
            notifyObservers("SUCCESS", "Data Loaded",
                    "Successfully loaded employees from " + employeesFileName);
        } else if (fileType.equalsIgnoreCase("tasks")) {
            String tasksFileName = fileName[fileName.length - 1];
            tasks = DataGenerator.loadTasks(filePath);
            consoleObserver.updateLoadedData("Tasks", tasksFileName);
            // Display Loaded Tasks
            consoleObserver.displayData("TASKS", tasks);

            notifyObservers("SUCCESS", "Data Loaded",
                    "Successfully loaded tasks from " + tasksFileName);
        } else {
            throw new LoadDataException("Unrecognised file type: " + fileType);
        }

    }

    /**
     * Algorithm run menu
     */
    private void randomDataMenu() {
        boolean exit = false;
        int employeeCount = 10;
        int taskCount = 10;

        try {
            while (!exit) {
                int choice = consoleObserver.requestInput("CHOOSE PARAMETERS TO GENERATE",
                        "Select a choice: ",
                        new String[] { "Exit", "Employee Size: " + employeeCount, "Task Size: " + taskCount,
                                "Generate" });
                switch (choice) {
                    case 1:
                        employeeCount = consoleObserver.requestInput("SET EMPLOYEE SIZE",
                                "Enter Employee count:",
                                1, 10000);
                        break;
                    case 2:
                        taskCount = consoleObserver.requestInput("SET TASK SIZe",
                                "Enter Task count:",
                                1, 10000);
                        break;
                    case 3:
                        DataSet ds = RandomDataGen.generateDataSet(taskCount, employeeCount);
                        tasks = ds.tasks;
                        employees = ds.employees;
                        consoleObserver.updateLoadedData("Employees", "Random of size " + taskCount);
                        consoleObserver.updateLoadedData("Tasks", "Random of size " + employeeCount);
                        consoleObserver.displayData("EMPLOYEES", employees);
                        consoleObserver.displayData("TASKS", tasks);
                        exit = true;
                        break;
                    case 0:
                        exit = true;
                        break;
                    default:
                        break;
                }
            }
        } catch (InputException e) {
            notifyObservers("ERROR", "Error occurred while getting input", e.getMessage());
        }
    }

    private void runMenuMultiple(String algorithmType) {
        determineNextRunId();
        int currentRunId = PathUtility.getRunId();
        System.out.println("RUN ID = " + currentRunId);
        notifyObservers("INFO", "RUNNING TRIALS",
                "Running " + TRIAL_NUMBER_DEFAULT + " trials of " + algorithmType + "...");

        for (int i = 0; i < TRIAL_NUMBER_DEFAULT; i++) {
            // Create and run algorithm with this run ID
            switch (algorithmType) {
                case "GeneticAlg" -> {
                    notifyObservers("ISRUNALL", "false", String.valueOf(currentRunId));
                    GeneticAlg ga = new AlgorithmFactory(tasks, employees, observers)
                            .createGeneticAlgorithm(POPULATION_SIZE_DEFAULT, GA_CROSSOVER_DEFAULT,
                                    GA_MUTATION_DEFAULT, GA_ELITISM_DEFAULT,
                                    MAX_GEN_DEFAULT, REPORTING_FREQUENCY_DEFAULT,
                                    FILE_OUTPUT_DEFAULT, currentRunId);
                    // notifyObservers("ISRUNALL", "false", String.valueOf(currentRunId));
                    runMenu(ga, "Genetic Algorithm (Trial " + currentRunId + ")");
                }
                case "AntColonyAlg" -> {
                    notifyObservers("ISRUNALL", "false", String.valueOf(currentRunId));
                    AntColAlg ac = new AlgorithmFactory(tasks, employees, observers)
                            .createAntColonyOptimisation(POPULATION_SIZE_DEFAULT, ACO_DECAY_RATE_DEFAULT,
                                    ACO_INITIAL_PHEROMONE_DEFAULT,
                                    MAX_GEN_DEFAULT, REPORTING_FREQUENCY_DEFAULT,
                                    FILE_OUTPUT_DEFAULT, currentRunId);
                    // notifyObservers("ISRUNALL", "false", String.valueOf(currentRunId));
                    runMenu(ac, "Ant Colony Algorithm (Trial " + currentRunId + ")");
                }
                case "ParticleSwarmAlg" -> {
                    notifyObservers("ISRUNALL", "false", String.valueOf(currentRunId));
                    ParticleSwarmAlg ps = new AlgorithmFactory(tasks, employees, observers)
                            .createParticleSwarm(POPULATION_SIZE_DEFAULT, MAX_GEN_DEFAULT, PSO_PBEST_W, PSO_GBEST_W,
                                    PSO_INERTIA_WEIGHT,
                                    REPORTING_FREQUENCY_DEFAULT, FILE_OUTPUT_DEFAULT, currentRunId);
                    // notifyObservers("ISRUNALL", "false", String.valueOf(currentRunId));
                    runMenu(ps, "Particle Swarm Algorithm (Trial " + currentRunId + ")");
                }
                case "All" -> {
                    notifyObservers("ISRUNALL", "true", String.valueOf(currentRunId));
                    Map<String, AbstractOptimisationAlgorithm> algs = new AlgorithmFactory(tasks, employees, observers)
                            .createStandardisedAlgorithms(
                                    POPULATION_SIZE_DEFAULT, MAX_GEN_DEFAULT,
                                    REPORTING_FREQUENCY_DEFAULT, FILE_OUTPUT_DEFAULT,
                                    ACO_DECAY_RATE_DEFAULT, ACO_INITIAL_PHEROMONE_DEFAULT,
                                    GA_CROSSOVER_DEFAULT, GA_MUTATION_DEFAULT, GA_ELITISM_DEFAULT, PSO_PBEST_W,
                                    PSO_GBEST_W,
                                    PSO_INERTIA_WEIGHT,
                                    currentRunId);
                    runMenu(algs.get("AntColonyAlg"), "Ant Colony Algorithm (Trial " + currentRunId + ")");
                    runMenu(algs.get("GeneticAlg"), "Genetic Algorithm (Trial " + currentRunId + ")");
                    runMenu(algs.get("ParticleSwarmAlg"), "Particle Swarm Algorithm (Trial " + currentRunId + ")");
                    notifyObservers("ISRUNALL", "false", String.valueOf(currentRunId));
                }
                default -> {
                    notifyObservers("ERROR", "Invalid Algorithm Type",
                            "The algorithm type of: " + algorithmType + " cannot be determined");
                    return;
                }
            }

        }

        notifyObservers("SUCCESS", "RUN COMPLETE",
                "Completed " + TRIAL_NUMBER_DEFAULT + " trials of " + algorithmType);
    }

    private void runAlgorithmMenu() {
        boolean exit = false;
        if (employees == null || tasks == null) {
            notifyObservers("ERROR", "Invalid Data",
                    "Employee or Task data has not been loaded \n" + consoleObserver.getLoadedDataStatus());
            return;
        }
        try {
            while (!exit) {

                int choice = consoleObserver.requestInput("CHOOSE ALGORITHM",
                        "Select an Algorithm to run",
                        new String[] { "Exit", "Genetic Algorithm", "Swarm Optimisation", "Ant Colony", "All" });

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
                        runAntColAlgMenu();
                        break;
                    case 4:
                        runStandardisedMenu();
                        break;
                    default:
                        break;
                }
            }
        } catch (InputException e) {
            notifyObservers("ERROR", "Error occurred while getting input", e.getMessage());
        }
    }

    private void runParticleSwarmMenu() {

        boolean exit = false;

        while (!exit) {

            int choice = consoleObserver.requestInput("DEFINE Particle Swarm Algorithm",
                    "Specify the parameters to use for this algorithm or proceed",
                    new String[] { "Exit", "Population size: " + POPULATION_SIZE_DEFAULT,
                            "Personal Best Weight " + PSO_PBEST_W,
                            "Global Best Weight: " + PSO_GBEST_W,
                            "Intertia Weight" + PSO_INERTIA_WEIGHT,
                            "Maximum Generations: " + MAX_GEN_DEFAULT,
                            "Reporting frequency: " + REPORTING_FREQUENCY_DEFAULT,
                            "Output to file: " + FILE_OUTPUT_DEFAULT,
                            "Number of Trials: " + TRIAL_NUMBER_DEFAULT,
                            "Proceed" });

            switch (choice) {
                case 0:
                    exit = true;
                    break;
                case 1:
                    POPULATION_SIZE_DEFAULT = getParameter("Population size", POPULATION_SIZE_DEFAULT, 1,
                            Integer.MAX_VALUE);
                    break;
                case 2:
                    PSO_PBEST_W = getParameter("Personal Best Weight", PSO_PBEST_W, 0.0, 10.0);
                    break;
                case 3:
                    PSO_GBEST_W = getParameter("Global Best Weight", PSO_GBEST_W, 0.0, 10.0);
                    break;
                case 4:
                    PSO_INERTIA_WEIGHT = getParameter("Intertia Weight", PSO_INERTIA_WEIGHT, 0, 1.0);
                    break;
                case 5:
                    MAX_GEN_DEFAULT = getParameter("Maximum Generations", MAX_GEN_DEFAULT, 1, Integer.MAX_VALUE);
                    break;
                case 6:
                    REPORTING_FREQUENCY_DEFAULT = getParameter("Reporting frequency", REPORTING_FREQUENCY_DEFAULT,
                            1, Integer.MAX_VALUE);
                    break;
                case 7:
                    FILE_OUTPUT_DEFAULT = getParameter("Output to file", FILE_OUTPUT_DEFAULT);
                    break;
                case 8:
                    TRIAL_NUMBER_DEFAULT = getParameter("Number of Trials", TRIAL_NUMBER_DEFAULT, 1, Integer.MAX_VALUE);
                    break;
                case 9:
                    runMenuMultiple("ParticleSwarmAlg");
                    break;
                default:
                    break;
            }

        }

    }

    private void runStandardisedMenu() {
        boolean exit = false;

        while (!exit) {

            int choice = consoleObserver.requestInput("DEFINE ALL PARAMETERS",
                    "Specify the parameters to use for all algorithms or proceed",
                    new String[] { "Exit", "Population Size: " + POPULATION_SIZE_DEFAULT,
                            "(Genetic) Crossover Rate: " + GA_CROSSOVER_DEFAULT,
                            "(Genetic) Mutation Rate: " + GA_MUTATION_DEFAULT,
                            "(Genetic) Elitism Count: " + GA_ELITISM_DEFAULT,
                            "(Particle Swarm) Personal Best Weight: " + PSO_PBEST_W,
                            "(Particle Swarm) Global Best Weight: " + PSO_GBEST_W,
                            "(Paritcle Swarm) Inertia Weight: " + PSO_INERTIA_WEIGHT,
                            "(Ant Colony) Pheromone Decay Rate: " + ACO_DECAY_RATE_DEFAULT,
                            "(Ant Colony) Initial Pheromone Value: " + ACO_INITIAL_PHEROMONE_DEFAULT,
                            "Maximum Iterations: " + MAX_GEN_DEFAULT,
                            "Reporting Frequency: " + REPORTING_FREQUENCY_DEFAULT,
                            "Output to File: " + FILE_OUTPUT_DEFAULT,
                            "Number of Trials: " + TRIAL_NUMBER_DEFAULT,
                            "Proceed" });

            switch (choice) {
                case 0:
                    exit = true;
                    break;
                case 1:
                    POPULATION_SIZE_DEFAULT = getParameter("Population Size", POPULATION_SIZE_DEFAULT, 1,
                            Integer.MAX_VALUE);
                    break;
                case 2:
                    GA_MUTATION_DEFAULT = getParameter("Mutation Rate", GA_MUTATION_DEFAULT, 0.0, 1.0);
                    break;
                case 3:
                    GA_CROSSOVER_DEFAULT = getParameter("Crossover Rate", GA_CROSSOVER_DEFAULT, 0.0, 1.0);
                    break;
                case 4:
                    GA_ELITISM_DEFAULT = getParameter("Elitsim Count", GA_ELITISM_DEFAULT, 0, employees.size());
                    break;
                case 5:
                    PSO_PBEST_W = getParameter("Personal Best Weight", PSO_PBEST_W, 0.0, 10.0);
                    break;
                case 6:
                    PSO_GBEST_W = getParameter("Global Best Weight", PSO_GBEST_W, 0.0, 10.0);
                    break;
                case 7:
                    PSO_INERTIA_WEIGHT = getParameter("Intertia Weight", PSO_INERTIA_WEIGHT, 0, 1.0);
                    break;
                case 8:
                    ACO_DECAY_RATE_DEFAULT = getParameter("Pheromone Decay Rate", ACO_DECAY_RATE_DEFAULT, 0.0, 1.0);
                    break;
                case 9:
                    ACO_INITIAL_PHEROMONE_DEFAULT = getParameter("Initial Pheromone Value",
                            ACO_INITIAL_PHEROMONE_DEFAULT, 0.0, Double.MAX_VALUE);
                    break;
                case 10:
                    MAX_GEN_DEFAULT = getParameter("Maximum Iterations", MAX_GEN_DEFAULT, 1, Integer.MAX_VALUE);
                    break;
                case 11:
                    REPORTING_FREQUENCY_DEFAULT = getParameter("Reporting Frequency", REPORTING_FREQUENCY_DEFAULT,
                            1, Integer.MAX_VALUE);
                    break;
                case 12:
                    FILE_OUTPUT_DEFAULT = getParameter("Output to File", FILE_OUTPUT_DEFAULT);
                    break;
                case 13:
                    TRIAL_NUMBER_DEFAULT = getParameter("Number of Trials", TRIAL_NUMBER_DEFAULT, 1, Integer.MAX_VALUE);
                    break;
                case 14:
                    fileOutput.isRunAll = true;
                    runMenuMultiple("All");
                    fileOutput.isRunAll = false;
                    break;

                default:
                    break;
            }

        }

    }

    private void runAntColAlgMenu() {
        // Initialise with parameters

        boolean exit = false;

        while (!exit) {

            int choice = consoleObserver.requestInput("DEFINE ANT COLONY ALGORITHM ",
                    "Specify the parameters to use for this algorithm or proceed",
                    new String[] { "Exit", "Number of Ants (Population Size): " + POPULATION_SIZE_DEFAULT,
                            "Pheromone Decay Rate: " + ACO_DECAY_RATE_DEFAULT,
                            "Initial Pheromone Value: " + ACO_INITIAL_PHEROMONE_DEFAULT,
                            "Maximum Iterations: " + MAX_GEN_DEFAULT,
                            "Reporting Frequency: " + REPORTING_FREQUENCY_DEFAULT,
                            "Output to File: " + FILE_OUTPUT_DEFAULT,
                            "Number of Trials: " + TRIAL_NUMBER_DEFAULT,
                            "Proceed" });

            switch (choice) {
                case 0:
                    exit = true;
                    break;
                case 1:
                    POPULATION_SIZE_DEFAULT = getParameter("Number of Ants", POPULATION_SIZE_DEFAULT, 1,
                            Integer.MAX_VALUE);
                    break;
                case 2:
                    ACO_DECAY_RATE_DEFAULT = getParameter("Pheromone Decay Rate", ACO_DECAY_RATE_DEFAULT, 0.0, 1.0);
                    break;
                case 3:
                    ACO_INITIAL_PHEROMONE_DEFAULT = getParameter("Initial Pheromone Value",
                            ACO_INITIAL_PHEROMONE_DEFAULT, 0.0, Double.MAX_VALUE);
                    break;
                case 4:
                    MAX_GEN_DEFAULT = getParameter("Maximum Iterations", MAX_GEN_DEFAULT, 1, Integer.MAX_VALUE);
                    break;
                case 5:
                    REPORTING_FREQUENCY_DEFAULT = getParameter("Reporting Frequency", REPORTING_FREQUENCY_DEFAULT,
                            1, Integer.MAX_VALUE);
                    break;
                case 6:
                    FILE_OUTPUT_DEFAULT = getParameter("Output to File", FILE_OUTPUT_DEFAULT);
                    break;
                case 7:
                    TRIAL_NUMBER_DEFAULT = getParameter("Number of Trials", TRIAL_NUMBER_DEFAULT, 1, Integer.MAX_VALUE);
                    break;
                case 8:
                    runMenuMultiple("AntColonyAlg");
                default:
                    break;
            }

        }
    }

    private void runGeneticAlgMenu() {

        boolean exit = false;

        while (!exit) {

            int choice = consoleObserver.requestInput("DEFINE GENETIC ALGORITHM",
                    "Specify the parameters to use for this algorithm or proceed",
                    new String[] { "Exit", "Population size: " + POPULATION_SIZE_DEFAULT,
                            "Crossover rate: " + GA_CROSSOVER_DEFAULT,
                            "Mutation rate: " + GA_MUTATION_DEFAULT,
                            "Elitism count: " + GA_ELITISM_DEFAULT,
                            "Maximum Generations: " + MAX_GEN_DEFAULT,
                            "Reporting frequency: " + REPORTING_FREQUENCY_DEFAULT,
                            "Output to file: " + FILE_OUTPUT_DEFAULT,
                            "Number of Trials: " + TRIAL_NUMBER_DEFAULT,
                            "Proceed" });

            switch (choice) {
                case 0:
                    exit = true;
                    break;
                case 1:
                    POPULATION_SIZE_DEFAULT = getParameter("Population size", POPULATION_SIZE_DEFAULT, 1,
                            Integer.MAX_VALUE);
                    break;
                case 2:
                    GA_CROSSOVER_DEFAULT = getParameter("Crossover rate", GA_CROSSOVER_DEFAULT, 0.0, 1.0);
                    break;
                case 3:
                    GA_MUTATION_DEFAULT = getParameter("Mutation rate", GA_MUTATION_DEFAULT, 0.0, 1.0);
                    break;
                case 4:
                    GA_ELITISM_DEFAULT = getParameter("Elitsim Count", GA_ELITISM_DEFAULT, 0, employees.size());
                    break;
                case 5:
                    MAX_GEN_DEFAULT = getParameter("Maximum Generations", MAX_GEN_DEFAULT, 1, Integer.MAX_VALUE);
                    break;
                case 6:
                    REPORTING_FREQUENCY_DEFAULT = getParameter("Reporting frequency", REPORTING_FREQUENCY_DEFAULT,
                            1, Integer.MAX_VALUE);
                    break;
                case 7:
                    FILE_OUTPUT_DEFAULT = getParameter("Output to file", FILE_OUTPUT_DEFAULT);
                    break;
                case 8:
                    TRIAL_NUMBER_DEFAULT = getParameter("Number of Trials", TRIAL_NUMBER_DEFAULT, 1, Integer.MAX_VALUE);
                case 9:
                    runMenuMultiple("GeneticAlg");
                    break;
                default:
                    break;
            }

        }
    }

    private void runMenu(Algorithm algorithm, String name) {
        notifyObservers("MENU", "RUN " + name.toUpperCase(), name + " algorithm running......");
        algorithm.run();
    }

    private boolean getParameter(String parameter, boolean defaultVal) {
        int choice = (consoleObserver.requestInput("ENTER " + parameter.toUpperCase(),
                "Enter a value for " + parameter.toLowerCase(),
                new String[] { "Exit", "True", "False" }));
        return (choice == 0) ? defaultVal : choice != 2;
    }

    private double getParameter(String parameter, double defaultVal, double min, double max) {
        double choice = consoleObserver.requestInput("ENTER " + parameter.toUpperCase(),
                "Enter a value for " + parameter.toLowerCase(), min,
                max);
        return ((choice == min - 1.0) ? defaultVal : choice);
    }

    private int getParameter(String parameter, int defaultVal, int min, int max) {
        int choice = consoleObserver.requestInput("ENTER " + parameter.toUpperCase(),
                "Enter a value for " + parameter.toLowerCase(), min,
                max);
        return ((choice == min - 1.0) ? defaultVal : choice);
    }

    public void determineNextRunId() {
        int nextRunId = PathUtility.determineNextRunId();
        PathUtility.setRunId(nextRunId);
        System.out.println("Next RUN ID determined to be: " + nextRunId);
    }
}
