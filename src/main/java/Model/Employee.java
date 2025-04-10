package Model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Employee
{
    private final String id;
    private final int availableHours;
    private final int skillLevel;
    private final Set<String> skills;

    public Employee(String ID, int hours, int skillLevel, Set<String> skills)
    {
        this.id = ID;
        this.availableHours = hours;
        this.skillLevel = skillLevel;
        this.skills = new HashSet<>(skills);
    }

    public String getId()
    {
        return id;
    }

    public int getAvailableHours()
    {
        return availableHours;
    }

    public int getSkillLevel()
    {
        return skillLevel;
    }

    public Set<String> getSkills()
    {
        return Collections.unmodifiableSet(skills);
    }

    public boolean hasSkill(String skill)
    {
        return skills.contains(skill);
    }

    public boolean canPerformTask(Task task)
    {
        return skillLevel >= task.getDifficulty() && hasSkill(task.getRequiredSkill());
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", availableHours=" + availableHours +
                ", skillLevel=" + skillLevel +
                ", skills=" + skills +
                '}';
    }

}
