package be.ugent.benchmark;

public class Summary {
    private final long executionTime;
    private final int weight;

    public Summary(long executionTime, int weight) {
        this.executionTime = executionTime;
        this.weight = weight;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public int getWeight() {
        return weight;
    }
}