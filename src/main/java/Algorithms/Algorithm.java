package Algorithms;

import Utilities.Observer;
/*
 * Core algorithm interface focusing solely on algorithm execution
 */
public interface Algorithm {
    /**
     * Execute the optimization algorithm
     */
    void run();

    /**
     * Register observers to receive algorithm updates
     */
    void registerObserver(Observer observer);

    /**
     * Remove an observer
     */
    void removeObserver(Observer observer);
}
