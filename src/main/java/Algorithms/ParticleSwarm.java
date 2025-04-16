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

        int[][] swarms = Initialise.getInitialPopulation(employees, tasks, populationSize);
        SwarmData[] swarmData = new SwarmData[populationSize];
        for (int i = 0; i < tasks.size(); i++) {
            swarmData[i].pData = new positionData[tasks.size()];
        }
        int gBest;

    }
}

class SwarmData {
    positionData[] pData;
    int gBest;

    public int getandUpdategBest() {
        for (int i = 0; i < pData.length; i++) {
            gBest = Math.max(gBest, pData[i].pBest);
        }
        return gBest;
    }

    public void updateSwarm(int w, int[] positions, int employeeSize) {
        for (int i = 0; i < pData.length; i++) {
            positionData currData = pData[i];
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

    private double getNewVelocity(positionData currData, int currP) {
        double c1 = 1.5;
        double c2 = 1.5;
        double r1 = Math.random();
        double r2 = Math.random();
        double w = 0.5;
        double newV = w * (currData.velocity) + c1 * r1 * (currData.pBest - currP) + c2 * r2 * (gBest - currP);
        return sigmoid(newV);

    }

    private double sigmoid(double v) {
        return 1 / (1 + Math.pow(Math.E, -v));
    }

}

class positionData {
    double velocity;
    int pBest;
}
