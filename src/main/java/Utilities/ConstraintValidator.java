package Utilities;


import Model.Assignment;
import Model.Employee;
import Model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for validating constraints in the task assignment problem.
 */
public class ConstraintValidator {

    /**
     * Checks if an assignment satisfies all constraints.
     *
     * @param assignment The Assignment to validate
     * @param tasks The list of all tasks
     * @param employees The list of all employees
     * @return true if the assignment is feasible, false otherwise
     */
    public static boolean isAssignmentFeasible(Assignment assignment, List<Task> tasks, List<Employee> employees) {
        return isUniqueAssignment(assignment, tasks) &&
                isCapacityRespected(assignment, tasks, employees) &&
                isSkillLevelSufficient(assignment, tasks, employees) &&
                isSkillMatchingRespected(assignment, tasks, employees);
    }

    /**
     * Checks if each task is assigned to exactly one employee.
     *
     * @param assignment The Assignment to validate
     * @param tasks The list of all tasks
     * @return true if constraint is satisfied, false otherwise
     */
    public static boolean isUniqueAssignment(Assignment assignment, List<Task> tasks) {
        for (Task task : tasks) {
            if (assignment.getAssignedEmployee(task.getId()) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the capacity constraint is respected.
     * The total time assigned to an employee must not exceed their available hours.
     *
     * @param assignment The Assignment to validate
     * @param tasks The list of all tasks
     * @param employees The list of all employees
     * @return true if constraint is satisfied, false otherwise
     */
    public static boolean isCapacityRespected(Assignment assignment, List<Task> tasks, List<Employee> employees) {
        Map<String, Integer> employeeWorkload = new HashMap<>();

        // Initialize workload map
        for (Employee employee : employees) {
            employeeWorkload.put(employee.getId(), 0);
        }

        // Calculate workload for each employee
        for (Task task : tasks) {
            String employeeId = assignment.getAssignedEmployee(task.getId());
            if (employeeId != null) {
                int currentWorkload = employeeWorkload.getOrDefault(employeeId, 0);
                employeeWorkload.put(employeeId, currentWorkload + task.getEstimatedTime());
            }
        }

        // Check if workload exceeds capacity
        for (Employee employee : employees) {
            int workload = employeeWorkload.getOrDefault(employee.getId(), 0);
            if (workload > employee.getAvailableHours()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the skill level constraint is respected.
     * An employee can only be assigned a task if their skill level is at least equal to the task's difficulty.
     *
     * @param assignment The Assignment to validate
     * @param tasks The list of all tasks
     * @param employees The list of all employees
     * @return true if constraint is satisfied, false otherwise
     */
    public static boolean isSkillLevelSufficient(Assignment assignment, List<Task> tasks, List<Employee> employees) {
        for (Task task : tasks) {
            String employeeId = assignment.getAssignedEmployee(task.getId());
            if (employeeId != null) {
                Employee employee = findEmployeeById(employees, employeeId);
                if (employee != null && employee.getSkillLevel() < task.getDifficulty()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if the specialized skill matching constraint is respected.
     * A task requiring a specific skill may only be assigned to an employee who possesses that skill.
     *
     * @param assignment The Assignment to validate
     * @param tasks The list of all tasks
     * @param employees The list of all employees
     * @return true if constraint is satisfied, false otherwise
     */
    public static boolean isSkillMatchingRespected(Assignment assignment, List<Task> tasks, List<Employee> employees) {
        for (Task task : tasks) {
            String employeeId = assignment.getAssignedEmployee(task.getId());
            if (employeeId != null) {
                Employee employee = findEmployeeById(employees, employeeId);
                if (employee != null && !employee.hasSkill(task.getRequiredSkill())) {
                    return false;
                }
            }
        }

        return true;
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
