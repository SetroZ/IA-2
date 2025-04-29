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
    private final int idx;

    public Employee(String ID, int hours, int skillLevel, Set<String> skills, int idx)
    {
        this.id = ID;
        this.availableHours = hours;
        this.skillLevel = skillLevel;
        this.skills = new HashSet<>(skills);
        this.idx = idx;
    }

    public int getIdx(){
        return idx;
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


    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", availableHours=" + availableHours +
                ", skillLevel=" + skillLevel +
                ", skill/s=" + skills +
                '}';
    }

}
