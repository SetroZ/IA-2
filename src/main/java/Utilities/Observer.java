package Utilities;

public interface Observer {
    /**
     * Update method called when a subject changes its state.
     *
     * @param messageType The type of message being sent (e.g., "INFO", "ERROR", "MENU")
     * @param title       The title of the message
     * @param content     The content or body of the message
     */
    void update(String messageType, String title, String content) throws ObserverException;

    /**
     * Display data in a formatted way.
     *
     * @param dataType The type of data being displayed (e.g., "TASKS", "EMPLOYEES", "SOLUTION")
     * @param data     The data to display
     */
    void displayData(String dataType, Object data) throws ObserverException;

    /**
     * Show progress of an algorithm.
     *
     * @param algorithmName The name of the algorithm
     * @param iteration     The current iteration/generation
     * @param cost          The current best cost
     * @param info          Additional information to display
     */
    void showProgress(String algorithmName, int iteration, double cost, String info) throws ObserverException;

    String getFinalSolution(int[] solution, double cost, int generation, boolean feasible) throws ObserverException;
}