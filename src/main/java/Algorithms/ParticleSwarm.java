package Algorithms;

import java.util.List;
import java.util.Random;

import Model.Employee;
import Model.Task;
import Utilities.Initialise;
import Utilities.Subject;

public class ParticleSwarm implements Algorithm, Subject {

    class GBestData {
        double gBest;
        int[] gBestArr;
    }

    private List<Employee> employees;
    private List<Task> tasks;
    int populationSize;

    int maxIterations;

    public ParticleSwarm(List<Task> tasks, List<Employee> employees,
            int populationSize, int maxIterations) {
        this.populationSize = populationSize;
        this.employees = employees;
        this.tasks = tasks;
        this.maxIterations = maxIterations;
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
            for (int j = 0; j < populationSize; j++) {
                v[i][j] = Math.random();
                pBest[i][j] = swarms[i][j];
                fitnessPBest[i] = CostCalculator.calculateTotalCost(pBest[i], tasks, employees);
            }
        }
        gBestData = findGbest(gBestData, fitnessPBest, pBest);

        // Main loop.
        for (int n = 0; n < maxIterations; n++) {
            if (gBestData.gBest == 0) {
                return gBestData.gBestArr;
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
