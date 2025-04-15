package Algorithms;

import Model.Employee;
import Model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for calculating costs and penalties in the task solution problem.
 */

public class CostCalculator {

    // Penalty weighting factors as defined in the problem specification
    private static final double OVERLOAD_WEIGHT = 0.33;
    private static final double SKILL_MISMATCH_WEIGHT = 0.33;
    private static final double DEADLINE_VIOLATION_WEIGHT = 0.34;

    /**
     * Calculates the total cost of aS solution.
     * Cost = α × (Overload Penalty) + β × (Skill Mismatch Penalty) + γ × (Deadline Violation Penalty)
     *
     * @param solution The Solution to evaluate
     * @param tasks The list of all tasks
     * @param employees The list of all employees
     * @return The total cost of the solution
     */
    public static double calculateTotalCost(int[] solution, List<Task> tasks, List<Employee> employees) {
        double overloadPenalty = calculateOverloadPenalty(solution, tasks, employees);
        double skillMismatchPenalty = calculateSkillMismatchPenalty(solution, tasks, employees);
        double deadlineViolationPenalty = calculateDeadlineViolationPenalty(solution, tasks, employees);

        return (OVERLOAD_WEIGHT * overloadPenalty) +
                (SKILL_MISMATCH_WEIGHT * skillMismatchPenalty) +
                (DEADLINE_VIOLATION_WEIGHT * deadlineViolationPenalty);
    }

    /**
     * Calculates the overload penalty.
     * This penalty occurs when an employee is assigned more work than their available hours.
     *
     * @param solution The Solution to evaluate
     * @param tasks The list of all tasks
     * @param employees The list of all employees
     * @return The overload penalty
     */
    public static double calculateOverloadPenalty(int[] solution, List<Task> tasks, List<Employee> employees) {
        double totalPenalty = 0;

        for (Employee employee : employees) {
            int totalWorkload = calculateEmployeeWorkload(solution, tasks, employees, employee.getId());
            int overload = Math.max(0, totalWorkload - employee.getAvailableHours());
            totalPenalty += overload;
        }

        return totalPenalty;
    }

    /**
     * Calculates the skill mismatch penalty.
     * This penalty occurs when tasks are assigned to employees who:
     * 1. Have a skill level lower than the task's difficulty
     * 2. Don't possess the specific skill required by the task
     *
     * @param solution The Solution to evaluate
     * @param tasks The list of all tasks
     * @param employees The list of all employees
     * @return The skill mismatch penalty
     */


    public static double calculateSkillMismatchPenalty(int[] solution, List<Task> tasks, List<Employee> employees) {
        int mismatchCount = 0;

        for (Task task : tasks) {
            String employeeId = employees.get(solution[task.getIdx()]).getId();
            if (employeeId != null) {
                Employee employee = findEmployeeById(employees, employeeId);

                if (employee != null) {
                    // Check skill level constraint
                    if (employee.getSkillLevel() < task.getDifficulty()) {
                        mismatchCount++;
                    }

                    // Check specialized skill constraint
                    if (!employee.hasSkill(task.getRequiredSkill())) {
                        mismatchCount++;
                    }
                }
            }
        }
        return mismatchCount;
    }

    /**
     * Calculates the deadline violation penalty.
     * This penalty occurs when a task is projected to be completed after its deadline.
     *
     * @param solution The Solution to evaluate
     * @param tasks The list of all tasks
     * @param employees The list of all employees
     * @return The deadline violation penalty
     */

    public static double calculateDeadlineViolationPenalty(int[] solution, List<Task> tasks, List<Employee> employees) {
        int violationCount = 0;

        // For each employee, track their current workload time
        Map<String, Integer> employeeWorkloadTimes = new HashMap<>();

        // For simplicity, assume tasks are processed in order by ID
        // A more sophisticated approach might involve scheduling algorithms
        List<Task> sortedTasks = tasks.stream()
                .sorted((t1, t2) -> t1.getId().compareTo(t2.getId()))
                .toList();

        for (Task task : sortedTasks) {
            String employeeId = employees.get(solution[task.getIdx()]).getId();
            if (employeeId != null) {
                // Initialize workload time if not already present
                employeeWorkloadTimes.putIfAbsent(employeeId, 0);

                // Get current workload time for the employee
                int currentWorkloadTime = employeeWorkloadTimes.get(employeeId);

                // Add the task's estimated time to the workload
                currentWorkloadTime += task.getEstimatedTime();

                // Check if the deadline is violated
                if (currentWorkloadTime > task.getDeadline()) {
                    violationCount++;
                }

                // Update the workload time
                employeeWorkloadTimes.put(employeeId, currentWorkloadTime);
            }
        }

        return violationCount;
    }

    /**
     * Calculates the total workload assigned to an employee.
     *
     * @param solution The Solution to evaluate
     * @param tasks The list of all tasks
     * @param employeeId The ID of the employee
     * @return The total workload in hours
     */

    public static int calculateEmployeeWorkload(int[] solution, List<Task> tasks, List<Employee> employees, String employeeId) {
        int totalWorkload = 0;

        for (Task task : tasks) {
            String assignedEmployeeId = employees.get(solution[task.getIdx()]).getId();
            if (employeeId.equals(assignedEmployeeId)) {
                totalWorkload += task.getEstimatedTime();
            }
        }

        return totalWorkload;
    }

    /**
     * Helper method to find an employee by ID.
     *
     * @param employees The list of all employees
     * @param employeeId The ID of the employee to find
     * @return The employee with the specified ID, or null if not found
     */

    private static Employee findEmployeeById(List<Employee> employees, String employeeId) {
        for (Employee employee : employees) {
            if (employee.getId().equals(employeeId)) {
                return employee;
            }
        }
        return null;
    }
}