package Model;

public class Task
{
    private final String id;
    private final int estimatedTime;  // in hours
    private final int difficulty;     // skill level required
    private final int deadline;       // hours from now
    private final String requiredSkill;
    private final int idx;


    public Task(String id, int estimatedTime, int difficulty, int deadline, String requiredSkill, int idx) {
        this.id = id;
        this.estimatedTime = estimatedTime;
        this.difficulty = difficulty;
        this.deadline = deadline;
        this.requiredSkill = requiredSkill;
        this.idx = idx;
    }

    public int getIdx()
    {
        return idx;
    }

    public String getId()
    {
        return id;
    }

    public int getEstimatedTime() {
        return estimatedTime;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getDeadline() {
        return deadline;
    }

    public String getRequiredSkill() {
        return requiredSkill;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", estimatedTime=" + estimatedTime +
                ", difficulty=" + difficulty +
                ", deadline=" + deadline +
                ", requiredSkill='" + requiredSkill + '\'' +
                '}';
    }
}

