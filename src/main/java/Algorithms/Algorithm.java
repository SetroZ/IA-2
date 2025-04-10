package Algorithms;

import java.util.List;

public interface Algorithm
{
    /**
     * Runs the genetic algorithm to find an optimal assignment.
     *
     * @return The best Assignment found
     */
    int[] run();

    /**
     * Gets the best cost history throughout the optimization process.
     * This is used for performance evaluation and visualization.
     *
     * @return List of best cost values per iteration/generation
     */
    List<Double> getBestCostHistory();

    /**
     * Gets the average cost history throughout the optimization process.
     * This is used for performance evaluation and visualization.
     *
     * @return List of average cost values per iteration/generation
     */
    List<Double> getAvgCostHistory();

    /**
     * Gets the feasible solutions history throughout the optimization process.
     * This tracks how many solutions satisfy all constraints at each iteration.
     *
     * @return List of feasible solution counts per iteration/generation
     */
    List<Integer> getFeasibleSolutionsHistory();
}
