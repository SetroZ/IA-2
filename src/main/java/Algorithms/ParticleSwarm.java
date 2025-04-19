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
    int STAG_LIMIT = 8;
    int lastgBestUpdate = 0;

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
        gBestData.gBest = Double.MAX_VALUE;
        gBestData.gBestArr = new int[tasks.size()];
        double[] fitnessPBest = new double[populationSize]; // contains the fitness value for each pBest.

        Random rd = new Random();
        // Intialize Velocities, positions, pBest and gBest
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < tasks.size(); j++) {
                v[i][j] = rd.nextDouble(0.5, 2) * (rd.nextBoolean() ? 1 : -1);
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
                    swarms[i][j] = calculatePosition(v[i][j], swarms[i][j], j);

                }
                // Find pBest
                double newCost = CostCalculator.calculateTotalCost(swarms[i], tasks, employees);
                if (newCost <= fitnessPBest[i]) {

                    fitnessPBest[i] = newCost;
                    pBest[i] = swarms[i];
                }
            }

            gBestData = findGbest(gBestData, fitnessPBest, pBest);
            printProgress(gBestData.gBest, n);
        }
        printFinalResult(gBestData.gBestArr, n);
        System.out.println("Gen:" + n + "  Gbest:" + gBestData.gBest);
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
        double c1 = 1.1;
        double c2 = 1.6;
        Random rd = new Random();
        double r1 = rd.nextDouble(0.1, 1);
        double r2 = rd.nextDouble(0.1, 1);
        double w = 0.5;
        double stag = (STAG_LIMIT < lastgBestUpdate) ? Math.copySign(rd.nextDouble(0.5, 1), v) : 0;
        int maxV = employees.size() + 10;
        double newV = w * v + c1 * r1 * (pBest - currP) + c2 * r2 * (gBest - currP) + stag;
        newV = Math.max(-maxV, Math.min(maxV, newV));
        System.out.println(newV);
        return newV;
    }

    private int calculatePosition(double velocity, int currentPos, int taskId) {
        int size = employees.size();
        Task task = tasks.get(taskId);

        List<Integer> compatibleEmployees = new ArrayList<>();
        int i = 0;
        for (Employee employee : employees) {
            if (currentPos == i) {
                i++;
                continue;
            }
            if (employee.hasSkill(task.getRequiredSkill()) &&
                    employee.getSkillLevel() >= task.getDifficulty()) {
                compatibleEmployees.add(i);
            }
            i++;
        }

        int move = (int) Math.round(velocity); // Step direction

        if (compatibleEmployees.size() == 0) {
            return currentPos;
        }
        int newPos = findClosest(compatibleEmployees, Math.floorMod(move + currentPos, size));

        return newPos;
    }

    private int findClosest(List<Integer> arr, int target) {
        int res = arr.get(0);
        int lo = 0, hi = arr.size() - 1;

        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;

            if (Math.abs(arr.get(mid) - target) < Math.abs(res - target)) {
                res = arr.get(mid);

            } else if (Math.abs(arr.get(mid) - target) == Math.abs(res - target)) {
                res = Math.max(res, arr.get(mid));
            }

            if (arr.get(mid) == target) {
                return arr.get(mid);
            } else if (arr.get(mid) < target) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }

        return res;
    }

    private void preventStagnation() {

    }

    private void printProgress(double gbest, int generation) {
        StringBuilder sb = new StringBuilder();

        sb.append("Generation ").append(generation)
                .append(": Best Cost = ").append(String.format("%.2f", gbest))
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
        boolean isFeasilble = CostCalculator.isFeasible(bestSolution, tasks, employees);

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

    private GBestData findGbest(GBestData currGBest, double[] fitnesspBest, int[][] pBest) {
        lastgBestUpdate += 1;
        GBestData newGBest = currGBest;

        for (int i = 0; i < populationSize; i++) {
            if (newGBest.gBest > fitnesspBest[i]) {
                newGBest.gBest = fitnesspBest[i];
                newGBest.gBestArr = pBest[i];
                lastgBestUpdate = 0;
            }
        }

        return newGBest;
    }
}
