package Utilities;

import Algorithms.AbstractOptimisationAlgorithm;
import Algorithms.AntColAlg;
import Algorithms.GeneticAlg;
import Algorithms.ParticleSwarmAlg;

public class AlgParameters
{

    // All algorithm parameters
    private String type;
    private final int maxIterations;
    private final int reportingFrequency;
    private boolean fileOutput;
    private int populationSize;

    // Genetic
    private double mutationRate;
    private double crossoverRate;
    private int elitismCount;

    // Particle Swarm
    private  double c1;
    private  double c2;
    private  double w;

    // Ant Colony
    private double initPheromone;
    private double pherDecayRate;




    /**
     * Constructor for All

     */


    public AlgParameters(int maxIterations,
                         int reportingFrequency, boolean fileOutput, int populationSize,
                         double mutationRate, double crossoverRate, int elitismCount,
                         double c1, double c2, double w, double initPheromone,
                         double pherDecayRate)
    {
        this.maxIterations = maxIterations;
        this.reportingFrequency = reportingFrequency;
        this.fileOutput = fileOutput;
        this.populationSize = populationSize;
        this.type = "all";

        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.elitismCount = elitismCount;



        this.c1 = c1;
        this.c2 = c2;
        this.w = w;


        this.initPheromone = initPheromone;
        this.pherDecayRate = pherDecayRate;

    }



    public AlgParameters(AbstractOptimisationAlgorithm a)
    {
        this.maxIterations = a.getMaxIterations();
        this.reportingFrequency = a.getReportingFrequency();
        this.fileOutput = a.isFileOutput();
        this.populationSize = a.getPopulationSize();
        this.type = a.getAlgorithmName();
        switch (type)
        {
            case "GeneticAlg" ->
            {
                GeneticAlg ga = (GeneticAlg) a;
                this.mutationRate = ga.getMutationRate();
                this.crossoverRate = ga.getCrossoverRate();
                this.elitismCount = ga.getElitismCount();
            }
            case "ParticleSwarmAlg" ->
            {
                ParticleSwarmAlg ps = (ParticleSwarmAlg) a;
                this.c1 = ps.getC1();
                this.c2 = ps.getC2();
                this.w = ps.getW();
            }
            case "AntColonyAlg" ->
            {
                AntColAlg ac = (AntColAlg) a;
                this.initPheromone = ac.getInitPheromone();
                this.pherDecayRate = ac.getPherDecayRate();
            }
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        }
    }


    // Reporting method
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        //Write header

        switch (type)
        {
            case "AntColonyAlg" ->
            {
                //Write header
                sb.append("type,maxIterations,reportingFrequency," +
                        "fileOutput,populationSize,initPheromone,pherDecayRate\n");

                sb.append(type).append(",")
                        .append(maxIterations).append(",").append(reportingFrequency)
                        .append(",").append(initPheromone).append(",").append(pherDecayRate).append("\n");
                return sb.toString();
            }
            case "ParticleSwarmAlg" ->
            {
                //Write header
                sb.append("type,maxIterations,reportingFrequency," +
                        "fileOutput,populationSize,c1,c2,w\n");

                sb.append(type).append(",")
                        .append(maxIterations).append(",").append(reportingFrequency)
                        .append(",").append(fileOutput).append(",").append(populationSize)
                        .append(",").append(c1).append(",").append(c2).append(",").append(w).append("\n");
                return sb.toString();
            }
            case "GeneticAlg" ->
            {
                //Write header
                sb.append("type,maxIterations,reportingFrequency," +
                        "fileOutput,populationSize,mutationRate,crossoverRate," +
                        "elitismCount\n");

                sb.append(type).append(",").append(maxIterations).append(",").append(reportingFrequency)
                        .append(",").append(fileOutput).append(",").append(populationSize)
                        .append(",").append(mutationRate).append(",").append(crossoverRate)
                        .append(",").append(elitismCount).append("\n");
                return sb.toString();
            }
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        }
    }



    // Getters and Setters




    public void setType(String type)
    {
        this.type = type;
    }


    public int getMaxIterations()
    {
        return maxIterations;
    }


    public int getReportingFrequency()
    {
        return reportingFrequency;
    }



    public boolean isFileOutput()
    {
        return fileOutput;
    }

    public void setFileOutput(boolean fileOutput)
    {
        this.fileOutput = fileOutput;
    }

    public int getPopulationSize()
    {
        return populationSize;
    }

    public void setPopulationSize(int populationSize)
    {
        this.populationSize = populationSize;
    }

    public double getMutationRate()
    {
        return mutationRate;
    }


    public double getCrossoverRate()
    {
        return crossoverRate;
    }


    public int getElitismCount()
    {
        return elitismCount;
    }

    public double getC1()
    {
        return c1;
    }



    public double getC2()
    {
        return c2;
    }


    public double getW()
    {
        return w;
    }


    public double getInitPheromone()
    {
        return initPheromone;
    }



    public double getPherDecayRate()
    {
        return pherDecayRate;
    }

}
