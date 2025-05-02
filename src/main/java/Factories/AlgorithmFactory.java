package Factories;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Algorithms.*;
import Model.Employee;
import Model.Task;
import Utilities.AlgParameters;
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

    public GeneticAlg createGeneticAlgorithm(Integer populationSize, Double crossoverRate, Double mutationRate,
            Integer elitismCount, Integer maxGenerations, Integer reportingFrequency, Boolean fileOutput, int runId) {
        GeneticAlg ga = new GeneticAlg(tasks, employees, populationSize, crossoverRate, mutationRate,
                elitismCount, maxGenerations, reportingFrequency, fileOutput, runId);
        for (Observer observer : observers) {
            ga.registerObserver(observer);
        }
        return ga;
    }

    public ParticleSwarmAlg createParticleSwarm(Integer populationSize, Integer maxIterations, double c1, double c2,
            double w,
            Integer reportingFrequency, Boolean fileOutput, int runId) {
        ParticleSwarmAlg ps = new ParticleSwarmAlg(tasks, employees, populationSize, maxIterations, c1, c2, w,
                reportingFrequency, fileOutput, runId);
        for (Observer observer : observers) {
            ps.registerObserver(observer);
        }
        return ps;

    }

    public AntColAlg createAntColonyOptimisation(Integer numAnts, Double pherDecayRate, Double initPheromone,
            Integer maxIterations, Integer reportingFrequency, Boolean fileOutput, int runId) {
        AntColAlg aco = new AntColAlg(tasks, employees, numAnts, pherDecayRate, initPheromone, maxIterations,
                reportingFrequency, fileOutput, runId);
        for (Observer observer : observers) {
            aco.registerObserver(observer);
        }
        return aco;
    }

    public Map<String, AbstractOptimisationAlgorithm> createStandardisedAlgorithms(Integer populationSize,
            Integer maxIterations,
            Integer reportingFrequency, Boolean fileOutput,
            Double pherDecayRate, Double initPheromone,
            Double crossoverRate, Double mutationRate,
            Integer elitismCount, Double c1, Double c2, Double w, int runID) {
        Map<String, AbstractOptimisationAlgorithm> algos = new HashMap<>();
        GeneticAlg ga = createGeneticAlgorithm(populationSize, crossoverRate, mutationRate, elitismCount,
                maxIterations, reportingFrequency, fileOutput, runID);
        ParticleSwarmAlg ps =
                createParticleSwarm(populationSize, maxIterations, c1, c2, w, reportingFrequency, fileOutput, runID);
        AntColAlg ac = createAntColonyOptimisation(populationSize, pherDecayRate, initPheromone,
                maxIterations, reportingFrequency, fileOutput, runID);

        for (Observer observer : observers) {
            ga.registerObserver(observer);
            ac.registerObserver(observer);
            ps.registerObserver(observer);
        }

        algos.put("GeneticAlg", ga);
        algos.put("ParticleSwarmAlg", ps);
        algos.put("AntColonyAlg", ac);

        return algos;
    }

    public Map<String, AbstractOptimisationAlgorithm> createStandardisedAlgorithms(AlgParameters p, int runID)
    {
        Map<String, AbstractOptimisationAlgorithm> algos = new HashMap<>();

        GeneticAlg ga = createGeneticAlgorithm(p.getPopulationSize(),
                p.getCrossoverRate(), p.getMutationRate(), p.getElitismCount(),
                p.getMaxIterations(), p.getReportingFrequency(), p.isFileOutput(), runID);
       ParticleSwarmAlg ps = createParticleSwarm(p.getPopulationSize(), p.getMaxIterations(), p.getC1(),
                p.getC2(), p.getW(), p.getReportingFrequency(), p.isFileOutput(), runID);
        AntColAlg ac = createAntColonyOptimisation(p.getPopulationSize(),p.getPherDecayRate(),
                p.getInitPheromone(), p.getMaxIterations(), p.getReportingFrequency(), p.isFileOutput(), runID);

        for (Observer observer : observers) {
            ga.registerObserver(observer);
            ac.registerObserver(observer);
            ps.registerObserver(observer);
        }

        p.setType("GeneticAlg");
        ga.setLoggerParameters(p);

        p.setType("AntColonyAlg");
        ac.setLoggerParameters(p);

        p.setType("ParticleSwarmAlg");
        ps.setLoggerParameters(p);



        algos.put("GeneticAlg", ga);
        algos.put("ParticleSwarmAlg", ps);
        algos.put("AntColonyAlg", ac);

        return algos;
    }
}