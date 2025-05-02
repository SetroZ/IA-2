package Algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Model.Employee;
import Model.Task;
import Utilities.Initialise;
import Utilities.Observer;
import Utilities.PerformanceLogger;

public class ParticleSwarmAlg extends AbstractOptimisationAlgorithm {

    static class GBestData {
        double gBest;
        int[] gBestArr;
    }

    int populationSize;
    int maxIterations;
    int STAG_LIMIT = 20;
    int lastgBestUpdate = 0;
    double c1;
    double c2;
    double w;

    public ParticleSwarmAlg(List<Task> tasks, List<Employee> employees,
            int populationSize, int maxIterations, double c1,
            double c2,
            double w, int reportingFrequency, boolean fileOutput, int runId) {
        super(tasks, employees, reportingFrequency, fileOutput, maxIterations, populationSize, runId);
        this.populationSize = populationSize;
        this.employees = employees;
        this.tasks = tasks;
        this.maxIterations = maxIterations;

    }

    public void notifyObservers(String messageType, String title, String content) {
        for (Observer observer : observers) {
            observer.update(messageType, title, content);
        }
    }

    @Override

    public String getAlgorithmName() {
        return "ParticleSwarmAlg";
    }

    @Override

    public int getMaxIterations() {
        return maxIterations;
    }

    @Override

    public void run() {

        // Start timing performance
        performanceLogger.startTimer();

        int[][] swarm = Initialise.getInitialPopulation(employees, tasks, populationSize);
        double[][] v = new double[populationSize][tasks.size()]; // contains velocities for each position.
        int[][] pBest = new int[populationSize][tasks.size()]; // contains pbest for each particle. which is an array of
        // the best positions for each position.
        GBestData gBestData = new GBestData();// contains the best pBest found i.e. gBest. which is an array of
        // the best positions found.
        gBestData.gBest = Double.MAX_VALUE;
        gBestData.gBestArr = new int[tasks.size()];
        double[] fitnessPBest = new double[populationSize]; // contains the fitness value for each pBest.

        Random rd = new Random();
        // Intialize Velocities, positions, pBest and gBest
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < tasks.size(); j++) {
                v[i][j] = rd.nextDouble(0.5, 2) * (rd.nextBoolean() ? 1 : -1);
                pBest[i][j] = swarm[i][j];
            }
            fitnessPBest[i] = CostCalculator.calculateTotalCost(pBest[i], tasks, employees);
        }
        gBestData = findGbest(gBestData, fitnessPBest, pBest);
        int n = 0;

        // Log initial state
        performanceLogger.logIteration(
                n,
                gBestData.gBestArr,
                gBestData.gBest,
                PerformanceLogger.getCurrentMemoryUsageMB());

        // Main loop.
        for (; n < maxIterations; n++) {
            if (gBestData.gBest == 0) {
                break;
            }
            for (int i = 0; i < populationSize; i++) {
                for (int j = 0; j < tasks.size(); j++) {
                    v[i][j] = calculateVelocity(gBestData.gBestArr[j], pBest[i][j], v[i][j], swarm[i][j]);
                    swarm[i][j] = calculatePosition(v[i][j], swarm[i][j], j);
                }
                // Find pBest
                double newCost = CostCalculator.calculateTotalCost(swarm[i], tasks, employees);
                if (newCost <= fitnessPBest[i]) {
                    fitnessPBest[i] = newCost;
                    pBest[i] = swarm[i].clone();
                }
            }

            gBestData = findGbest(gBestData, fitnessPBest, pBest);
            reportProgress(gBestData.gBestArr, n);

            // Log metrics for this generation
            performanceLogger.logIteration(
                    n,
                    gBestData.gBestArr,
                    gBestData.gBest,
                    PerformanceLogger.getCurrentMemoryUsageMB());
        }

        // Stop timer and save all metrics to CSV files
        performanceLogger.stopTimer();
        performanceLogger.saveMetricsToCSV();

        reportFinalResult(gBestData.gBestArr, n);
        // System.out.println("Gen:" + n + " Gbest:" + gBestData.gBest);
    }

    private double calculateVelocity(double gBest, int pBest, double v, int currP) {
        final int maxV = employees.size();

        Random rd = new Random();
        double r1 = rd.nextDouble(0.1, 1.0);
        double r2 = rd.nextDouble(0.1, 1.0);

        double cognitive = c1 * r1 * (pBest - currP);
        double social = c2 * r2 * (gBest - currP);

        double stag = (STAG_LIMIT < lastgBestUpdate)
                ? rd.nextDouble(1.0, 1.5)
                : 1;

        double newV = w * v + cognitive + social * stag;

        return Math.max(-maxV, Math.min(maxV, newV));
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

        if (compatibleEmployees.isEmpty()) {
            return currentPos;
        }

        return findClosest(compatibleEmployees, Math.floorMod(move + currentPos, size));
    }

    private int findClosest(List<Integer> arr, int target) {
        int res = arr.getFirst();
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

    private GBestData findGbest(GBestData currGBest, double[] fitnesspBest, int[][] pBest) {
        lastgBestUpdate += 1;

        for (int i = 0; i < populationSize; i++) {
            if (currGBest.gBest > fitnesspBest[i]) {
                currGBest.gBest = fitnesspBest[i];
                currGBest.gBestArr = pBest[i];
                lastgBestUpdate = 0;
            }
        }

        return currGBest;
    }
}
