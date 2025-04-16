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
        int gBest;
        int[][] swarms = Initialise.getInitialPopulation(employees, tasks, populationSize);
        ParticleData[][] posData = new ParticleData[populationSize][tasks.size()];

    }

    private double getNewVelocity(ParticleData currPar, int currP) {
        double c1 = 1.5;
        double c2 = 1.5;
        double r1 = Math.random();
        double r2 = Math.random();
        double w = 0.5;
        double newV = w * (currPar.velocity) + c1 * r1 * (currPar.pBest - currP) + c2 * r2 * (gBest - currP);
        return sigmoid(newV);

    }

    private double sigmoid(double v) {
        return 1 / (1 + Math.pow(Math.E, -v));
    }

    private void updateSwarm() {

    }

    private int findGbest(ParticleData[][] particleData) {
        int gBest = Integer.MAX_VALUE;
        for (int i = 0; i < particleData.length; i++) {

            for (int j = 0; j < particleData[0].length; j++) {
                gBest = Math.max(gBest, particleData[i][j].pBest);
            }

        }
        return gBest;
    }
}

class SwarmData {
    ParticleData[] pData;
    int gBest;7

    public void updateSwarm(int w, int[] positions, int employeeSize) {
        for (int i = 0; i < pData.length; i++) {
            ParticleData currData = pData[i];
            pData[i].velocity = getNewVelocity(currData, positions[i]);
            int newPosition = getNewPosition(currData.velocity, employeeSize)
            if 
        }
    }

    private int getNewPosition(double velocity, int employeeSize) {
        boolean update = Math.random() < velocity;
        if (update) {
            Random rand = new Random();
            return rand.nextInt(0, employeeSize);
        } else {
            return -1;
        }

    }

}

class ParticleData {
    double velocity;
    int pBest;
}
