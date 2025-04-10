package Utilities;

import Model.Assignment;
import Model.Employee;
import Model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for calculating costs and penalties in the task assignment problem.
 */
public class CostCalculator {

    // Penalty weighting factors as defined in the problem specification
    private static final double OVERLOAD_WEIGHT = 0.33;
    private static final double SKILL_MISMATCH_WEIGHT = 0.33;
    private static final double DEADLINE_VIOLATION_WEIGHT = 0.34;

    /**
     * Calculates the total cost of an assignment.
     * Cost = α × (Overload Penalty) + β × (Skill Mismatch Penalty) + γ × (Deadline Violation Penalty)
     *
     * @param assignment The Assignment to evaluate
     * @param tasks The list of all tasks
     * @param employees The list of all employees
     * @return The total cost of the assignment
     */
    public static double calculateTotalCost(Assignment assignment, List<Task> tasks, List<Employee> employees) {
        double overloadPenalty = calculateOverloadPenalty(assignment, tasks, employees);
        double skillMismatchPenalty = calculateSkillMismatchPenalty(assignment, tasks, employees);
        double deadlineViolationPenalty = calculateDeadlineViolationPenalty(assignment, tasks, employees);

        return (OVERLOAD_WEIGHT * overloadPenalty) +
                (SKILL_MISMATCH_WEIGHT * skillMismatchPenalty) +
                (DEADLINE_VIOLATION_WEIGHT * deadlineViolationPenalty);
    }

    /**
     * Calculates the overload penalty.
     * This penalty occurs when an employee is assigned more work than their available hours.
     *
     * @param assignment The Assignment to evaluate
     * @param tasks The list of all tasks
     * @param employees The list of all employees
     * @return The overload penalty
     */
    public static double calculateOverloadPenalty(Assignment assignment, List<Task> tasks, List<Employee> employees) {
        double totalPenalty = 0;

        for (Employee employee : employees) {
            int totalWorkload = calculateEmployeeWorkload(assignment, tasks, employee.getId());
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
     * @param assignment The Assignment to evaluate
     * @param tasks The list of all tasks
     * @param employees The list of all employees
     * @return The skill mismatch penalty
     */
    public static double calculateSkillMismatchPenalty(Assignment assignment, List<Task> tasks, List<Employee> employees) {
        int mismatchCount = 0;

        for (Task task : tasks) {
            String employeeId = assignment.getAssignedEmployee(task.getId());
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
     * @param assignment The Assignment to evaluate
     * @param tasks The list of all tasks
     * @param employees The list of all employees
     * @return The deadline violation penalty
     */
    public static double calculateDeadlineViolationPenalty(Assignment assignment, List<Task> tasks, List<Employee> employees) {
        int violationCount = 0;

        // For each employee, track their current workload time
        Map<String, Integer> employeeWorkloadTimes = new HashMap<>();

        // For simplicity, assume tasks are processed in order by ID
        // A more sophisticated approach might involve scheduling algorithms
        List<Task> sortedTasks = tasks.stream()
                .sorted((t1, t2) -> t1.getId().compareTo(t2.getId()))
                .toList();

        for (Task task : sortedTasks) {
            String employeeId = assignment.getAssignedEmployee(task.getId());
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
     * @param assignment The Assignment to evaluate
     * @param tasks The list of all tasks
     * @param employeeId The ID of the employee
     * @return The total workload in hours
     */
    public static int calculateEmployeeWorkload(Assignment assignment, List<Task> tasks, String employeeId) {
        int totalWorkload = 0;

        for (Task task : tasks) {
            String assignedEmployeeId = assignment.getAssignedEmployee(task.getId());
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