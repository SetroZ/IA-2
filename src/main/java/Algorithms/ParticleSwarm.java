package Algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Model.Employee;
import Model.Task;
import Utilities.Initialise;
import Utilities.Observer;
import Utilities.ObserverException;
import Utilities.Subject;

public class ParticleSwarm implements Algorithm, Subject {

    class GBestData {
        double gBest;
        int[] gBestArr;
    }

    private final boolean fileOutput;
    private String output = "";

    private List<Employee> employees;

    private List<Task> tasks;

    private List<Observer> observers = new ArrayList<>();

    int populationSize;
    int maxIterations;

    public ParticleSwarm(List<Task> tasks, List<Employee> employees,
            int populationSize, int maxIterations) {
        this.populationSize = populationSize;
        this.employees = employees;
        this.tasks = tasks;
        this.maxIterations = maxIterations;
        this.fileOutput = true;
    }

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String messageType, String title, String content) {
        for (Observer observer : observers) {
            observer.update(messageType, title, content);
        }
    }

    @Override

    public void run() {

        int[][] swarms = Initialise.getInitialPopulation(employees, tasks, populationSize);
        double[][] v = new double[populationSize][tasks.size()]; // contains velocities for each position.
        int[][] pBest = new int[populationSize][tasks.size()]; // contains pbest for each particle. which is an array of
                                                               // the best positions for each position.
        GBestData gBestData = new GBestData();// contains the best pBest found i.e gBest. which is an array of
        // the best positions found.
        gBestData.gBestArr = new int[populationSize];
        double[] fitnessPBest = new double[populationSize]; // contains the fitness value for each pBest.

        // Intialize Velocities, positions, pBest and gBest
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < tasks.size(); j++) {
                v[i][j] = Math.random();
                pBest[i][j] = swarms[i][j];
                fitnessPBest[i] = CostCalculator.calculateTotalCost(pBest[i], tasks, employees);
            }
        }
        gBestData = findGbest(gBestData, fitnessPBest, pBest);

        int n = 0;
        // Main loop.
        for (; n < maxIterations; n++) {
            if (gBestData.gBest == 0) {
                break;
            }
            for (int i = 0; i < populationSize; i++) {
                for (int j = 0; j < tasks.size(); j++) {
                    v[i][j] = calculateVelocity(gBestData.gBestArr[j], pBest[i][j], v[i][j], swarms[i][j]);
                    swarms[i][j] = calculatePosition(v[i][j], swarms[i][j]);
                    // Find pBest
                    double newCost = CostCalculator.calculateTotalCost(swarms[i], tasks, employees);
                    if (newCost <= fitnessPBest[i]) {
                        fitnessPBest[i] = newCost;
                        pBest[i] = swarms[i];
                    }
                }
            }
            gBestData = findGbest(gBestData, fitnessPBest, pBest);
        }
        printFinalResult(gBestData.gBestArr, n);

    }

    @Override
    public List<Double> getBestCostHistory() {
        return List.of();
    }

    @Override
    public List<Double> getAvgCostHistory() {
        return List.of();
    }

    @Override
    public List<Integer> getFeasibleSolutionsHistory() {
        return List.of();
    }

    private double calculateVelocity(double gBest, int pBest, double v, int currP) {
        double c1 = 1.5;
        double c2 = 1.5;
        double r1 = Math.random();
        double r2 = Math.random();
        double w = 0.5;
        double newV = w * (v) + c1 * r1 * (pBest - currP) + c2 * r2 * (gBest - currP);
        return sigmoid(newV);

    }

    private int calculatePosition(double velocity, int defaultPos) {
        boolean update = Math.random() < velocity;
        if (update) {
            Random rand = new Random();
            return rand.nextInt(0, this.employees.size());
        } else {
            return defaultPos;
        }

    }

    private void printProgress(int[] bestSolution, int generation) {
        StringBuilder sb = new StringBuilder();
        double cost = CostCalculator.calculateTotalCost(bestSolution, tasks, employees);

        sb.append("Generation ").append(generation)
                .append(": Best Cost = ").append(String.format("%.2f", cost))
                .append(", Feasible: ").append(ConstraintValidator.isSolutionFeasible(bestSolution, tasks, employees))
                .append("\n");

        output += sb.toString();

        if (!fileOutput) {
            System.out.print(sb.toString());
        }
    }

    /**
     * Prints the final result of the algorithm.
     *
     * @param bestSolution The best Solution found
     * @param generation   The final generation number
     */
    private void printFinalResult(int[] bestSolution, int generation) {

        double cost = CostCalculator.calculateTotalCost(bestSolution, tasks, employees);
        boolean isFeasilble = ConstraintValidator.isSolutionFeasible(bestSolution, tasks, employees);

        String finalResult = observers.getFirst().getFinalSolution(bestSolution, cost, generation, isFeasilble);

        if (fileOutput) {
            output += finalResult;
            try {
                notifyObservers("FILE", "ParticleAlg", output);
            } catch (ObserverException e) {
                notifyObservers("ERROR", "Writing To File", e.getMessage());
            }
        } else {
            notifyObservers("INFO", "GENETIC ALGORITHM RESULT", finalResult);
        }
    }

    private double sigmoid(double v) {
        return 1 / (1 + Math.pow(Math.E, -v));
    }

    private GBestData findGbest(GBestData currGBest, double[] fitnesspBest, int[][] pBest) {
        GBestData newGBest = currGBest;
        double gBest = currGBest.gBest;
        for (int i = 0; i < populationSize; i++) {
            if (gBest > fitnesspBest[i]) {
                newGBest.gBest = fitnesspBest[i];
                newGBest.gBestArr = pBest[i];
            }
        }

        return newGBest;
    }
}
