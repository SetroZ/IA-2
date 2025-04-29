package Utilities;

import Exceptions.ObserverException;

public interface Observer {
    /**
     * Update method called when a subject changes its state.
     *
     * @param messageType The type of message being sent (e.g., "INFO", "ERROR", "MENU")
     * @param title       The title of the message
     * @param content     The content or body of the message
     */
    void update(String messageType, String title, String content) throws ObserverException;

    /**Format the final solution for reporting
     * @param solution The best solution found
     * @param cost The cost of the best solution
     * @param generation The final iteration/generation count
     * @param isFeasible Whether the solution is feasible
     * @return A formatted string representing the final solution
     */
    String getFinalSolution(int[] solution, double cost, int generation, boolean isFeasible) throws ObserverException;

}