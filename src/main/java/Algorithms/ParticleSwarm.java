package Algorithms;

import java.util.List;
import java.util.Random;

import Model.Employee;
import Model.Task;
import Utilities.Initialise;

public class ParticleSwarm implements Algorithm {

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

        Fitness[] gBest = new Fitness[tasks.size()];
        int[][] swarms = Initialise.getInitialPopulation(employees, tasks, populationSize);
        ParticleData[][] partiData = new ParticleData[populationSize][tasks.size()];

        // Intialize Velocities, positions, pBest and gBest
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < populationSize; j++) {
                partiData[i][j].velocity = Math.random();
                partiData[i][j].pBest.position = swarms[i][j];
                partiData[i][j].pBest.fitness = swarms[i][j];
            }
        }
        gBest = findGbest(partiData);

        for (int n = 0; n < maxIterations; n++) {
            for (int i = 0; i < partiData.length; i++) {
                for (int j = 0; j < partiData[0].length; j++) {
                    partiData[i][j].velocity = calculateVelocity(gBest[j].position, partiData[i][j], swarms[i][j]);
                    swarms[i][j] = calculatePosition(partiData[i][j].velocity, swarms[i][j]);
                }
            }
        }

    }

    private double calculateVelocity(double gBest, ParticleData currPar, int currP) {
        double c1 = 1.5;
        double c2 = 1.5;
        double r1 = Math.random();
        double r2 = Math.random();
        double w = 0.5;
        double newV = w * (currPar.velocity) + c1 * r1 * (currPar.pBest.position - currP) + c2 * r2 * (gBest - currP);
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

    private Fitness[] findGbest(ParticleData[][] particleData) {
        double gBest = Integer.MAX_VALUE;
        Fitness[] gBestArr = new Fitness[tasks.size()];
        for (int i = 0; i < particleData.length; i++) {

            for (int j = 0; j < particleData[0].length; j++) {
                gBest = Math.max(gBest, particleData[i][j].pBest.fitness);
            }

        }
        return gBestArr;
    }
}

class ParticleData {
    double velocity;
    Fitness pBest;
}

class Fitness {
    int position;
    double fitness;
}
