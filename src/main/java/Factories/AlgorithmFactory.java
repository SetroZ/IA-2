package Factories;

import java.util.List;

import Algorithms.GeneticAlgorithm;
import Algorithms.ParticleSwarm;
import Model.Employee;
import Model.Task;
import Utilities.Observer;

/**
 * Creates and returns a registered an algorithm
 */
public class AlgorithmFactory {

    private final List<Task> tasks;
    private final List<Employee> employees;
    private final List<Observer> observers;

    public AlgorithmFactory(List<Task> tasks, List<Employee> employees, List<Observer> observers) {
        this.tasks = tasks;
        this.employees = employees;
        this.observers = observers;
    }

    public GeneticAlgorithm createGeneticAlgorithm(Integer populationSize, Double crossoverRate, Double mutationRate,
            Integer maxGenerations, Integer reportingFrequency, Boolean fileOutput) {
        GeneticAlgorithm ga = new GeneticAlgorithm(tasks, employees, populationSize, crossoverRate, mutationRate,
                maxGenerations, reportingFrequency, fileOutput);
        for (Observer observer : observers) {
            ga.registerObserver(observer);
        }
        return ga;
    }

    public ParticleSwarm createParticleSwarm(Integer populationSize, Integer maxIterations,
            Boolean fileOutput) {
        ParticleSwarm ps = new ParticleSwarm(tasks, employees, populationSize, maxIterations);
        for (Observer observer : observers) {
            ps.registerObserver(observer);
        }
        return ps;

    }
}
