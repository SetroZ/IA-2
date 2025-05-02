package Algorithms;

import Model.Employee;
import Model.Task;
import Utilities.AlgParameters;
import Utilities.Observer;
import Utilities.PerformanceLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for optimization algorithms that handles observer
 * management
 */
public abstract class AbstractOptimisationAlgorithm implements Algorithm {
    protected List<Observer> observers = new ArrayList<>();
    protected List<Task> tasks;
    protected List<Employee> employees;

    protected int[] bestSolution;
    protected double bestCost = Double.MAX_VALUE;
    protected final int maxIterations;
    protected final int reportingFrequency;
    protected final boolean fileOutput;
    protected final int populationSize;
    protected String output = "";
    protected final int runID;

    protected final PerformanceLogger performanceLogger;

    public AbstractOptimisationAlgorithm(List<Task> tasks, List<Employee> employees,
            int reportingFrequency, boolean fileOutput, int maxIterations, int populationSize, int runId) {
        this.tasks = tasks;
        this.employees = employees;
        this.reportingFrequency = reportingFrequency;
        this.fileOutput = fileOutput;
        this.maxIterations = maxIterations;
        this.populationSize = populationSize;
        this.runID = runId;
        this.performanceLogger = new PerformanceLogger(getAlgorithmName(), tasks, employees, runId);
    }

    public void setLoggerParameters(AlgParameters parameters)
    {
        performanceLogger.setParameters(parameters);
    }

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(String messageType, String title, String content) {
        for (Observer observer : observers) {
            observer.update(messageType, title, content);
        }
    }

    /**
     * Reports progress at the current iteration
     */
    protected void reportProgress(int[] currentBest, int iteration) {
        double cost = CostCalculator.calculateTotalCost(currentBest, tasks, employees);
        boolean isFeasible = CostCalculator.isFeasible(currentBest, tasks, employees);

        StringBuilder sb = new StringBuilder();
        sb.append("Iteration ").append(iteration)
                .append(": Best Cost = ").append(String.format("%.2f", cost))
                .append(", Feasible: ").append(isFeasible)
                .append("\n");

        output += sb.toString();

        if (iteration % reportingFrequency == 0 || iteration == getMaxIterations() - 1) {
            notifyObservers("INFO", getAlgorithmName() + " PROGRESS", sb.toString());
        }
    }

    /**
     * Reports the final result
     */
    protected void reportFinalResult(int[] bestSolution, int iteration) {
        double cost = CostCalculator.calculateTotalCost(bestSolution, tasks, employees);
        boolean isFeasible = CostCalculator.isFeasible(bestSolution, tasks, employees);

        String finalResult = !observers.isEmpty()
                ? observers.getFirst().getFinalSolution(bestSolution, cost, iteration, isFeasible)
                : "No observer to format final solution";

        output += finalResult;

        if (fileOutput) {
            notifyObservers("FILE", getAlgorithmName(), output);
        } else {
            notifyObservers("INFO", getAlgorithmName() + " RESULT", output);
        }
    }

    public int[] getBestSolution()
    {
        return bestSolution;
    }

    public void setBestSolution(int[] bestSolution)
    {
        this.bestSolution = bestSolution;
    }

    public double getBestCost()
    {
        return bestCost;
    }

    public void setBestCost(double bestCost)
    {
        this.bestCost = bestCost;
    }

    public int getReportingFrequency()
    {
        return reportingFrequency;
    }

    public boolean isFileOutput()
    {
        return fileOutput;
    }

    public int getPopulationSize()
    {
        return populationSize;
    }

    public int getRunID()
    {
        return runID;
    }
    /**
     * Get the name of this algorithm
     */
    public abstract String getAlgorithmName();

    /**
     * Get the maximum number of iterations
     */
    public abstract int getMaxIterations();

}