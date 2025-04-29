package Factories;

import java.util.List;

import Algorithms.AntColAlg;
import Algorithms.GeneticAlg;
import Algorithms.ParticleSwarmAlg;
import Model.Employee;
import Model.Task;
import Utilities.Observer;

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

    public GeneticAlg createGeneticAlgorithm(Integer populationSize, Double crossoverRate, Double mutationRate,
                                             Integer maxGenerations, Integer reportingFrequency, Boolean fileOutput)
    {
        GeneticAlg ga = new GeneticAlg(tasks, employees, populationSize, crossoverRate, mutationRate,
                maxGenerations, reportingFrequency, fileOutput);
        for (Observer observer : observers)
        {
            ga.registerObserver(observer);
        }
        return ga;
    }

    public ParticleSwarmAlg createParticleSwarm(Integer populationSize, Integer maxIterations,
                                                Integer reportingFrequency, Boolean fileOutput)
    {
        ParticleSwarmAlg ps = new ParticleSwarmAlg(tasks, employees, populationSize, maxIterations, reportingFrequency, fileOutput);
        for (Observer observer : observers)
        {
            ps.registerObserver(observer);
        }
        return ps;

    }

    public AntColAlg createAntColonyOptimisation(Integer numAnts, Double pherDecayRate, Double initPheromone, Integer maxIterations, Integer reportingFrequency, Boolean fileOutput)
    {
        AntColAlg aco = new AntColAlg(numAnts, pherDecayRate, initPheromone, maxIterations, reportingFrequency, fileOutput, this.tasks, this.employees);
        for (Observer observer : observers)
        {
            aco.registerObserver(observer);
        }
        return aco;
    }
}
