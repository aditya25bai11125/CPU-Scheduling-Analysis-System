package com.vit.cpusimulator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Scheduler {
    private final List<String> ganttChart = new ArrayList<>();
    private double cpuUtilization;
    private double throughput;
    private int busyTime;
    private int elapsedTime;

    public List<Process> schedule(List<Process> workload, String algorithm, int quantum) {
        if (workload == null || workload.isEmpty()) {
            throw new IllegalArgumentException("The workload is empty.");
        }
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("An algorithm is required.");
        }

        String selectedAlgorithm = normalizeAlgorithm(algorithm);

        switch (selectedAlgorithm) {
            case "FCFS", "SJF", "SRTF", "PRIORITY" -> {
            }
            case "RR" -> {
                if (quantum <= 0) {
                    throw new IllegalArgumentException(
                            "Round Robin quantum must be greater than zero.");
                }
            }
            default -> throw new IllegalArgumentException(
                    "Unknown scheduling algorithm: " + algorithm);
        }

        List<Process> processes = copyWorkload(workload);
        resetResults();

        switch (selectedAlgorithm) {
            case "FCFS" -> runFcfs(processes);
            case "SJF" -> runShortestJobFirst(processes);
            case "SRTF" -> runShortestRemainingTimeFirst(processes);
            case "PRIORITY" -> runPriority(processes);
            case "RR" -> runRoundRobin(processes, quantum);
            default -> throw new IllegalArgumentException(
                    "Unknown scheduling algorithm: " + algorithm);
        }

        calculateSummary(processes);
        return processes;
    }

    public List<String> getGanttChart() {
        return new ArrayList<>(ganttChart);
    }

    public double getCpuUtilization() {
        return cpuUtilization;
    }

    public double getThroughput() {
        return throughput;
    }

    private List<Process> copyWorkload(List<Process> workload) {
        List<Process> copies = new ArrayList<>();
        Set<String> processIds = new HashSet<>();

        for (Process process : workload) {
            if (process == null) {
                throw new IllegalArgumentException("The workload contains a null process.");
            }
            if (!processIds.add(process.getId())) {
                throw new IllegalArgumentException(
                        "Duplicate process ID: " + process.getId());
            }

            copies.add(process.copy());
        }

        copies.sort(arrivalOrder());

        long horizon = 0L;
        for (Process process : copies) {
            horizon = Math.max(horizon, process.getArrivalTime());
            horizon += process.getBurstTime();

            if (horizon > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "The workload exceeds the supported scheduling timeline.");
            }
        }

        return copies;
    }

    private String normalizeAlgorithm(String algorithm) {
        String value = algorithm.trim()
                .toUpperCase(Locale.ROOT)
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");

        return switch (value) {
            case "FCFS", "FIRSTCOMEFIRSTSERVED" -> "FCFS";
            case "SJF", "SHORTESTJOBFIRST" -> "SJF";
            case "SRTF", "SHORTESTREMAININGTIMEFIRST" -> "SRTF";
            case "PRIORITY", "PRIORITYSCHEDULING" -> "PRIORITY";
            case "RR", "ROUNDROBIN" -> "RR";
            default -> value;
        };
    }

    private void runFcfs(List<Process> processes) {
        int currentTime = 0;

        for (Process process : processes) {
            if (currentTime < process.getArrivalTime()) {
                addSegment("IDLE", currentTime, process.getArrivalTime());
                currentTime = process.getArrivalTime();
            }

            startProcess(process, currentTime);
            int completion = currentTime + process.getBurstTime();
            addSegment(process.getId(), currentTime, completion);
            process.setRemainingTime(0);
            process.setCompletionTime(completion);
            currentTime = completion;
        }
    }

    private void runShortestJobFirst(List<Process> processes) {
        List<Process> waiting = new ArrayList<>();
        int nextProcess = 0;
        int completed = 0;
        int currentTime = 0;

        while (completed < processes.size()) {
            while (nextProcess < processes.size()
                    && processes.get(nextProcess).getArrivalTime() <= currentTime) {
                waiting.add(processes.get(nextProcess));
                nextProcess++;
            }

            if (waiting.isEmpty()) {
                int nextArrival = processes.get(nextProcess).getArrivalTime();
                addSegment("IDLE", currentTime, nextArrival);
                currentTime = nextArrival;
                continue;
            }

            waiting.sort(Comparator
                    .comparingInt(Process::getBurstTime)
                    .thenComparing(arrivalOrder()));

            Process process = waiting.remove(0);
            startProcess(process, currentTime);

            int completion = currentTime + process.getBurstTime();
            addSegment(process.getId(), currentTime, completion);
            process.setRemainingTime(0);
            process.setCompletionTime(completion);

            currentTime = completion;
            completed++;
        }
    }

    private void runPriority(List<Process> processes) {
        List<Process> waiting = new ArrayList<>();
        int nextProcess = 0;
        int completed = 0;
        int currentTime = 0;

        while (completed < processes.size()) {
            while (nextProcess < processes.size()
                    && processes.get(nextProcess).getArrivalTime() <= currentTime) {
                waiting.add(processes.get(nextProcess));
                nextProcess++;
            }

            if (waiting.isEmpty()) {
                int nextArrival = processes.get(nextProcess).getArrivalTime();
                addSegment("IDLE", currentTime, nextArrival);
                currentTime = nextArrival;
                continue;
            }

            waiting.sort(Comparator
                    .comparingInt(Process::getPriority)
                    .thenComparing(arrivalOrder()));

            Process process = waiting.remove(0);
            startProcess(process, currentTime);

            int completion = currentTime + process.getBurstTime();
            addSegment(process.getId(), currentTime, completion);
            process.setRemainingTime(0);
            process.setCompletionTime(completion);

            currentTime = completion;
            completed++;
        }
    }

    private void runShortestRemainingTimeFirst(List<Process> processes) {
        int currentTime = 0;
        int completed = 0;

        while (completed < processes.size()) {
            Process selected = null;

            for (Process process : processes) {
                if (process.getArrivalTime() <= currentTime
                        && process.getRemainingTime() > 0
                        && (selected == null || compareRemaining(process, selected) < 0)) {
                    selected = process;
                }
            }

            if (selected == null) {
                int nextArrival = Integer.MAX_VALUE;

                for (Process process : processes) {
                    if (process.getRemainingTime() > 0
                            && process.getArrivalTime() > currentTime) {
                        nextArrival = Math.min(nextArrival, process.getArrivalTime());
                    }
                }

                addSegment("IDLE", currentTime, nextArrival);
                currentTime = nextArrival;
                continue;
            }

            if (!selected.hasStarted()) {
                selected.setStartTime(currentTime);
            }

            addSegment(selected.getId(), currentTime, currentTime + 1);
            selected.decrementRemainingTime();
            currentTime++;

            if (selected.getRemainingTime() == 0) {
                selected.setCompletionTime(currentTime);
                completed++;
            }
        }
    }

    private void runRoundRobin(List<Process> processes, int quantum) {
        Deque<Process> readyQueue = new ArrayDeque<>();
        int nextProcess = 0;
        int completed = 0;
        int currentTime = 0;

        while (completed < processes.size()) {
            while (nextProcess < processes.size()
                    && processes.get(nextProcess).getArrivalTime() <= currentTime) {
                readyQueue.addLast(processes.get(nextProcess));
                nextProcess++;
            }

            if (readyQueue.isEmpty()) {
                if (nextProcess < processes.size()) {
                    int nextArrival = processes.get(nextProcess).getArrivalTime();
                    addSegment("IDLE", currentTime, nextArrival);
                    currentTime = nextArrival;
                    continue;
                }
            }

            Process process = readyQueue.removeFirst();

            if (!process.hasStarted()) {
                process.setStartTime(currentTime);
            }

            int runTime = Math.min(quantum, process.getRemainingTime());
            addSegment(process.getId(), currentTime, currentTime + runTime);
            process.reduceRemainingTime(runTime);
            currentTime += runTime;

            while (nextProcess < processes.size()
                    && processes.get(nextProcess).getArrivalTime() <= currentTime) {
                readyQueue.addLast(processes.get(nextProcess));
                nextProcess++;
            }

            if (process.getRemainingTime() == 0) {
                process.setCompletionTime(currentTime);
                completed++;
            } else {
                readyQueue.addLast(process);
            }
        }
    }

    private int compareRemaining(Process first, Process second) {
        int result = Integer.compare(first.getRemainingTime(), second.getRemainingTime());

        if (result != 0) {
            return result;
        }

        return arrivalOrder().compare(first, second);
    }

    private void startProcess(Process process, int currentTime) {
        if (!process.hasStarted()) {
            process.setStartTime(currentTime);
        }
    }

    private Comparator<Process> arrivalOrder() {
        return Comparator.comparingInt(Process::getArrivalTime)
                .thenComparing(Process::getId);
    }

    private void addSegment(String processId, int startTime, int endTime) {
        if (endTime <= startTime) {
            return;
        }

        if (!ganttChart.isEmpty()) {
            String previous = ganttChart.get(ganttChart.size() - 1);
            int separator = previous.lastIndexOf(" [");

            if (separator > 0) {
                String previousId = previous.substring(0, separator);
                String range = previous.substring(separator + 2, previous.length() - 1);
                int dash = range.indexOf('-');

                if (dash > 0) {
                    int previousEnd = Integer.parseInt(range.substring(dash + 1));

                    if (previousId.equals(processId) && previousEnd == startTime) {
                        int previousStart = Integer.parseInt(range.substring(0, dash));
                        ganttChart.set(
                                ganttChart.size() - 1,
                                processId + " [" + previousStart + "-" + endTime + "]");
                        return;
                    }
                }
            }
        }

        ganttChart.add(processId + " [" + startTime + "-" + endTime + "]");
    }

    private void resetResults() {
        ganttChart.clear();
        cpuUtilization = 0.0;
        throughput = 0.0;
        busyTime = 0;
        elapsedTime = 0;
    }

    private void calculateSummary(List<Process> processes) {
        int latestCompletion = 0;

        for (Process process : processes) {
            busyTime += process.getBurstTime();
            latestCompletion = Math.max(latestCompletion, process.getCompletionTime());
        }

        elapsedTime = latestCompletion;

        if (elapsedTime > 0) {
            cpuUtilization = busyTime * 100.0 / elapsedTime;
            throughput = processes.size() / (double) elapsedTime;
        }
    }
}