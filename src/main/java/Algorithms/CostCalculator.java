package Algorithms;

import Model.Employee;
import Model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * Utility class for calculating costs and penalties in the task solution problem.
 */

public class CostCalculator
{

    // Penalty weighting factors as defined in the problem specification
    private static final double OVERLOAD_WEIGHT = 0.2;
    private static final double SKILL_MISMATCH_WEIGHT = 0.2;
    private static final double DEADLINE_VIOLATION_WEIGHT = 0.2;
    private static final double DIFFICULTY_VIOLATION_WEIGHT = 0.2;
    private static final double UNIQUE_ASSIGNMENT_WEIGHT = 0.2;

    // Every constraint except deadline violation is a hard constraint; violations should be prioritised
    private static final int HARD_CONSTRAINT_MULTIPLIER = 100000;


    /**
     * Calculates the total cost of aS solution.
     * Cost = α × (Overload Penalty) + β × (Skill Mismatch Penalty) + γ × (Deadline Violation Penalty)
     *
     * @param solution  The Solution to evaluate
     * @param tasks     The list of all tasks
     * @param employees The list of all employees
     * @return The total cost of the solution
     */
    public static double calculateTotalCost(int[] solution, List<Task> tasks, List<Employee> employees)
    {
        double overloadPenalty = calculateOverloadPenalty(solution, tasks, employees);
        double skillMismatchPenalty = calculateSkillMismatchPenalty(solution, tasks, employees);
        double deadlineViolationPenalty = calculateDeadlineViolationPenalty(solution, tasks, employees);
        double skillLvlViolationPenalty = calculateSkillLevelPenalty(solution, tasks, employees);
        double uniqueAssignmentViolationPenalty = calculateUniqueAssignmentViolationPenalty(solution, tasks, employees);

        return (
                OVERLOAD_WEIGHT * overloadPenalty +
                        SKILL_MISMATCH_WEIGHT * skillMismatchPenalty +
                        DIFFICULTY_VIOLATION_WEIGHT * skillLvlViolationPenalty +
                        UNIQUE_ASSIGNMENT_WEIGHT * uniqueAssignmentViolationPenalty +
                        DEADLINE_VIOLATION_WEIGHT * deadlineViolationPenalty);
    }

    /**
     * Calculate the unique assignment penalty
     * If a task is assigned to more than one employee, increments count
     *
     * @param solution  The Solution to evaluate
     * @param tasks     The list of all tasks
     * @param employees The list of all employees
     * @return The number of violations
     */

    public static double calculateUniqueAssignmentViolationPenalty(int[] solution, List<Task> tasks, List<Employee> employees)
    {
        int violationCount = 0;
        for (Task task : tasks)
        {
            if (employees.get(solution[task.getIdx()]) == null)
            {
                violationCount++;
            }
        }
        return HARD_CONSTRAINT_MULTIPLIER * violationCount;
    }

    /**
     * Calculates the overload penalty.
     * This penalty occurs when an employee is assigned more work than their available hours.
     *
     * @param solution  The Solution to evaluate
     * @param tasks     The list of all tasks
     * @param employees The list of all employees
     * @return The number of hours in total that an employees are overworked.
     */

    public static double calculateOverloadPenalty(int[] solution, List<Task> tasks, List<Employee> employees)
    {
        double totalPenalty = 0;

        for (Employee employee : employees)
        {
            int totalWorkload = calculateEmployeeWorkload(solution, tasks, employees, employee.getId());
            int overload = Math.max(0, totalWorkload - employee.getAvailableHours());
            totalPenalty += overload;
        }

        return HARD_CONSTRAINT_MULTIPLIER * totalPenalty;
    }

    /**
     * Calculates the skill mismatch penalty.
     * This penalty occurs when tasks are assigned to employees who:
     * 1. Have a skill level lower than the task's difficulty
     * 2. Don't possess the specific skill required by the task
     *
     * @param solution  The Solution to evaluate
     * @param tasks     The list of all tasks
     * @param employees The list of all employees
     * @return The skill mismatch penalty
     */


    public static double calculateSkillMismatchPenalty(int[] solution, List<Task> tasks, List<Employee> employees)
    {
        int mismatchCount = 0;

        for (Task task : tasks)
        {
            int employeeIdx = solution[task.getIdx()];
            if (employeeIdx != -1)
            {
                Employee employee = employees.get(employeeIdx);
                if (employee != null)
                {
                    // Check specialized skill constraint
                    if (!employee.hasSkill(task.getRequiredSkill()))
                    {
                        mismatchCount++;
                    }
                }
            }
        }
        return HARD_CONSTRAINT_MULTIPLIER * mismatchCount;
    }

    /**
     * Calculates difficulty level violation penalty
     * The number of assignments with correct skill matches that don't have the adequate skill level
     *
     * @param solution  The Solution to evaluate
     * @param tasks     The list of all tasks
     * @param employees The list of all employees
     * @return The difficulty violation penalty
     */

    public static double calculateSkillLevelPenalty(int[] solution, List<Task> tasks, List<Employee> employees)
    {
        int skillLvlViolationCount = 0;

        for (Task task : tasks)
        {
            int employeeIdx = solution[task.getIdx()];
            if (employeeIdx != -1)
            {
                Employee employee = employees.get(employeeIdx);
                if (employee != null)
                {
                    if (task.getDifficulty() > employee.getSkillLevel())
                    {
                        skillLvlViolationCount++;
                    }
                }
            }
        }
        return HARD_CONSTRAINT_MULTIPLIER * skillLvlViolationCount;
    }


    /**
     * Calculates the deadline violation penalty.
     * The number of total hours tasks overshoot their deadline in a solution
     *
     * @param solution  The Solution to evaluate
     * @param tasks     The list of all tasks
     * @param employees The list of all employees
     * @return The deadline violation penalty
     */

    public static double calculateDeadlineViolationPenalty(int[] solution, List<Task> tasks, List<Employee> employees)
    {
        int violationHrs = 0;
        Queue<Task> queue = new PriorityQueue<>(Comparator.comparingInt(task -> task.getEstimatedTime()));

        // For each employee, track their current workload time
        Map<String, Integer> employeeWorkloadTimes = new HashMap<>();

        for (Task task : tasks)
        {
            queue.add(task);
        }

        while(queue.peek() != null)
        {
            Task task = queue.poll();
            String employeeId = employees.get(solution[task.getIdx()]).getId();
                if (employeeId != null)
                {
                    // Initialize workload time if not already present
                    employeeWorkloadTimes.putIfAbsent(employeeId, 0);

                    // Get current workload time for the employee
                    int currentWorkloadTime = employeeWorkloadTimes.get(employeeId);

                    // Add the task's estimated time to the workload
                    currentWorkloadTime += task.getEstimatedTime();

                    // Check if the deadline is violated
                    if (currentWorkloadTime > task.getDeadline())
                    {
                        violationHrs += (currentWorkloadTime - task.getDeadline());
                    }

                    // Update the workload time
                    employeeWorkloadTimes.put(employeeId, currentWorkloadTime);
                }
        }
        return violationHrs;
    }

    /**
     * Calculates the total workload assigned to an employee.
     *
     * @param solution   The Solution to evaluate
     * @param tasks      The list of all tasks
     * @param employeeId The ID of the employee
     * @return The total workload in hours
     */

    public static int calculateEmployeeWorkload(int[] solution, List<Task> tasks, List<Employee> employees, String employeeId)
    {
        int totalWorkload = 0;

        for (Task task : tasks)
        {
            String assignedEmployeeId = employees.get(solution[task.getIdx()]).getId();
            if (employeeId.equals(assignedEmployeeId))
            {
                totalWorkload += task.getEstimatedTime();
            }
        }

        return totalWorkload;
    }

    /**
     * Checks if all hard constraints are satisfied
     * @param solution   The Solution to evaluate
     * @param tasks      The list of all tasks
     * @param employees  The list of all employees
     * @return True if feasible, false if else.
     */

    public static boolean isFeasible(int[] solution, List<Task> tasks, List<Employee> employees)
    {
        return  (calculateTotalCost(solution, tasks, employees) -
                calculateDeadlineViolationPenalty(solution, tasks, employees) == 0);
    }
}