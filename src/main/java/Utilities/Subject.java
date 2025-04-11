package Utilities;

/**
 * Subject interface for the Observer pattern implementation.
 * This interface is implemented by classes that need to notify observers of changes.
 */
public interface Subject {
    /**
     * Register an observer to receive notifications.
     *
     * @param observer The observer to register
     */
    void registerObserver(Observer observer);

    /**
     * Remove an observer from the notification list.
     *
     * @param observer The observer to remove
     */
    void removeObserver(Observer observer);

    /**
     * Notify all registered observers of a change.
     *
     * @param messageType The type of message being sent
     * @param title The title of the message
     * @param content The content or body of the message
     */
    void notifyObservers(String messageType, String title, String content);

}