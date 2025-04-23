package Factories;

import Algorithms.GeneticAlgorithm;
import Algorithms.AntColAlgorithm;
import Model.Employee;
import Model.Task;
import Utilities.Observer;

import java.util.List;

/**
 * Creates and returns a registered an algorithm
 */
public class AlgorithmFactory
{

    private final List<Task> tasks;
    private final List<Employee> employees;
    private final List<Observer> observers;

    public AlgorithmFactory(List<Task> tasks, List<Employee> employees, List<Observer> observers)
    {
        this.tasks = tasks;
        this.employees = employees;
        this.observers = observers;
    }

    public GeneticAlgorithm createGeneticAlgorithm(Integer populationSize, Double crossoverRate, Double mutationRate, Integer maxGenerations, Integer reportingFrequency, Boolean fileOutput)
    {
        GeneticAlgorithm ga = new GeneticAlgorithm(tasks, employees, populationSize, crossoverRate, mutationRate, maxGenerations, reportingFrequency, fileOutput);
        for(Observer observer : observers)
        {
            ga.registerObserver(observer);
        }
        return ga;
    }

    public AntColAlgorithm createAntColonyOptimisation(Integer numAnts, Double pherDecayRate, Double initPheromone, Integer maxIterations, Integer reportingFrequency, Boolean fileOutput)
    {
        AntColAlgorithm aco = new AntColAlgorithm(numAnts, pherDecayRate, initPheromone, maxIterations, reportingFrequency, fileOutput, this.tasks, this.employees);
        for(Observer observer : observers)
        {
            aco.registerObserver(observer);
        }
        return aco;
    }
}
