package Model;

import Utilities.CostCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an assignment of tasks to employees.
 * This class is used to store and manipulate a solution for the task assignment problem.
 */

public class Assignment
{
    private final Map<String, String> taskToEmployeeMap;

    // Cache for storing calculated values like overload, skill mismatch, etc.
    private double cachedCost = -1;

    // Penalty weights
    private static final double OVERLOAD_WEIGHT = 0.33;
    private static final double SKILL_MISMATCH_WEIGHT = 0.33;
    private static final double DEADLINE_VIOLATION_WEIGHT = 0.34;


    public Assignment() {
        this.taskToEmployeeMap = new HashMap<>();
    }

    public Assignment(Assignment other) {
        this.taskToEmployeeMap = new HashMap<>(other.taskToEmployeeMap);
    }

    // Assign a task to employee
    public void assign(String taskId, String employeeId) {
        taskToEmployeeMap.put(taskId, employeeId);
        // Invalidate cache when assignment changes
        cachedCost = -1;
    }

    public String getAssignedEmployee(String taskId) {
        return taskToEmployeeMap.get(taskId);
    }

    public List<Task> getAssignedTasks(String employeeId, List<Task> tasks) {
        List<Task> assignedTasks = new ArrayList<>();

        for (Task task : tasks) {
            String assignedEmployeeId = getAssignedEmployee(task.getId());
            if (employeeId.equals(assignedEmployeeId)) {
                assignedTasks.add(task);
            }
        }

        return assignedTasks;
    }

    public boolean isComplete(List<Task> tasks) {
        for (Task task : tasks) {
            if (!taskToEmployeeMap.containsKey(task.getId())) {
                return false;
            }
        }
        return true;
    }

    public int calculateTotalWorkingTime(String employeeId, List<Task> tasks) {
        List<Task> assignedTasks = getAssignedTasks(employeeId, tasks);

        int totalTime = 0;
        for (Task task : assignedTasks) {
            totalTime += task.getEstimatedTime();
        }

        return totalTime;
    }

    public double calculateOverloadPenalty(Employee employee, List<Task> tasks) {
        int totalTime = calculateTotalWorkingTime(employee.getId(), tasks);
        int overload = Math.max(0, totalTime - employee.getAvailableHours());

        return overload;
    }

    public double calculateSkillMismatchPenalty(List<Employee> employees, List<Task> tasks) {
        return CostCalculator.calculateSkillMismatchPenalty(this, tasks, employees);
    }

    public double calculateDeadlineViolationPenalty(List<Employee> employees, List<Task> tasks) {
        return CostCalculator.calculateDeadlineViolationPenalty(this, tasks, employees);
    }

    public double calculateCost(List<Employee> employees, List<Task> tasks) {
        if (cachedCost >= 0) {
            return cachedCost;
        }

        cachedCost = CostCalculator.calculateTotalCost(this, tasks, employees);

        return cachedCost;
    }


    private Employee findEmployeeById(List<Employee> employees, String employeeId) {
        for (Employee employee : employees) {
            if (employee.getId().equals(employeeId)) {
                return employee;
            }
        }
        return null;
    }


    public Map<String, String> getTaskToEmployeeMap() {
        return Map.copyOf(taskToEmployeeMap);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Assignment:\n");

        for (Map.Entry<String, String> entry : taskToEmployeeMap.entrySet()) {
            sb.append("  Task ").append(entry.getKey())
                    .append(" -> Employee ").append(entry.getValue())
                    .append("\n");
        }

        return sb.toString();
    }

}

