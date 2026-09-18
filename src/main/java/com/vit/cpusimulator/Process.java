package com.vit.cpusimulator;

public class Process {
    private final String id;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority;

    private int remainingTime;
    private int startTime;
    private int completionTime;

    public Process(String id, int arrivalTime, int burstTime, int priority) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Process ID cannot be empty.");
        }

        String normalizedId = id.trim();
        if ("IDLE".equalsIgnoreCase(normalizedId)) {
            throw new IllegalArgumentException(
                    "Process ID 'IDLE' is reserved for CPU idle periods.");
        }

        if (arrivalTime < 0) {
            throw new IllegalArgumentException("Arrival time cannot be negative.");
        }
        if (burstTime <= 0) {
            throw new IllegalArgumentException("Burst time must be greater than zero.");
        }
        if (priority < 0) {
            throw new IllegalArgumentException("Priority cannot be negative.");
        }

        this.id = normalizedId;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        resetSimulation();
    }

    public Process(Process other) {
        if (other == null) {
            throw new IllegalArgumentException("Process cannot be null.");
        }

        this.id = other.id;
        this.arrivalTime = other.arrivalTime;
        this.burstTime = other.burstTime;
        this.priority = other.priority;
        resetSimulation();
    }

    public String getId() {
        return id;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getOriginalBurstTime() {
        return burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        if (remainingTime < 0) {
            throw new IllegalArgumentException("Remaining time cannot be negative.");
        }
        this.remainingTime = remainingTime;
    }

    public void reduceRemainingTime(int amount) {
        if (amount < 0 || amount > remainingTime) {
            throw new IllegalArgumentException("Invalid remaining time reduction.");
        }
        remainingTime -= amount;
    }

    public void decrementRemainingTime() {
        if (remainingTime <= 0) {
            throw new IllegalStateException("The process has no remaining time.");
        }
        remainingTime--;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        if (startTime < 0) {
            throw new IllegalArgumentException("Start time cannot be negative.");
        }
        this.startTime = startTime;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(int completionTime) {
        if (completionTime < 0) {
            throw new IllegalArgumentException("Completion time cannot be negative.");
        }
        this.completionTime = completionTime;
    }

    public boolean hasStarted() {
        return startTime >= 0;
    }

    public boolean isCompleted() {
        return remainingTime == 0 && completionTime >= 0;
    }

    public int getTurnaroundTime() {
        if (completionTime < 0) {
            return 0;
        }
        return completionTime - arrivalTime;
    }

    public int getWaitingTime() {
        if (completionTime < 0) {
            return 0;
        }
        return getTurnaroundTime() - burstTime;
    }

    public int getResponseTime() {
        if (startTime < 0) {
            return 0;
        }
        return startTime - arrivalTime;
    }

    public void resetSimulation() {
        remainingTime = burstTime;
        startTime = -1;
        completionTime = -1;
    }

    public Process copy() {
        return new Process(this);
    }

    @Override
    public String toString() {
        return id + " (arrival=" + arrivalTime
                + ", burst=" + burstTime
                + ", priority=" + priority + ")";
    }
}