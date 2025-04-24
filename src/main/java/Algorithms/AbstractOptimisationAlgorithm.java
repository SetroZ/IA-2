package Algorithms;

import Model.Employee;
import Model.Task;
import Utilities.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for optimization algorithms that handles observer management
 */
public abstract class AbstractOptimisationAlgorithm implements Algorithm {
    protected List<Observer> observers = new ArrayList<>();
    protected List<Task> tasks;
    protected List<Employee> employees;
    protected int[] bestSolution;
    protected double bestCost = Double.MAX_VALUE;
    protected final int REPORTING_FREQUENCY;
    protected final boolean fileOutput;
    protected String output = "";

    public AbstractOptimisationAlgorithm(List<Task> tasks, List<Employee> employees,
                                         int reportingFrequency, boolean fileOutput) {
        this.tasks = tasks;
        this.employees = employees;
        this.REPORTING_FREQUENCY = reportingFrequency;
        this.fileOutput = fileOutput;
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

        if (iteration % REPORTING_FREQUENCY == 0 || iteration == getMaxGenerations() - 1) {
            notifyObservers("INFO", getAlgorithmName() + " PROGRESS", sb.toString());
        }
    }

    /**
     * Reports the final result
     */
    protected void reportFinalResult(int[] bestSolution, int iteration) {
        double cost = CostCalculator.calculateTotalCost(bestSolution, tasks, employees);
        boolean isFeasible = CostCalculator.isFeasible(bestSolution, tasks, employees);

        String finalResult = !observers.isEmpty() ?
                observers.get(0).getFinalSolution(bestSolution, cost, iteration, isFeasible) :
                "No observer to format final solution";

        output += finalResult;

        if (fileOutput) {
            notifyObservers("FILE", getAlgorithmName().toLowerCase(), output);
        } else {
            notifyObservers("INFO", getAlgorithmName() + " RESULT", output);
        }
    }

    /**
     * Get the name of this algorithm
     */
    protected abstract String getAlgorithmName();

    /**
     * Get the maximum number of iterations
     */
    protected abstract int getMaxGenerations();
}