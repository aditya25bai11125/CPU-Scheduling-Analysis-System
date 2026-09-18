package com.vit.cpusimulator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class Analysis {

    private static final int LONG_WAIT_MINIMUM = 10;
    private static final String PRIORITY_SEMANTICS =
            "Priority semantics: smaller numeric values indicate higher scheduling priority.";

    public static String workloadStatistics(List<Process> workload) {
        validateWorkload(workload);

        if (workload.isEmpty()) {
            return "Workload statistics\n"
                    + "-------------------\n"
                    + "No processes in the workload.\n"
                    + PRIORITY_SEMANTICS;
        }

        long totalBurst = 0;
        int earliestArrival = Integer.MAX_VALUE;
        int latestArrival = Integer.MIN_VALUE;
        int lowestPriority = Integer.MAX_VALUE;
        int highestPriority = Integer.MIN_VALUE;

        for (Process process : workload) {
            totalBurst += process.getBurstTime();
            earliestArrival = Math.min(earliestArrival, process.getArrivalTime());
            latestArrival = Math.max(latestArrival, process.getArrivalTime());
            lowestPriority = Math.min(lowestPriority, process.getPriority());
            highestPriority = Math.max(highestPriority, process.getPriority());
        }

        return "Workload statistics\n"
                + "-------------------\n"
                + "Process count: " + workload.size() + "\n"
                + "Total burst time: " + totalBurst + "\n"
                + "Arrival time range: " + earliestArrival + " to " + latestArrival + "\n"
                + "Priority range: " + lowestPriority + " to " + highestPriority + "\n"
                + PRIORITY_SEMANTICS;
    }

    public static String compareAlgorithms(
            List<Process> workload, Scheduler scheduler, int quantum) {

        validateWorkload(workload);

        if (scheduler == null) {
            throw new IllegalArgumentException("A scheduler is required.");
        }

        if (workload.isEmpty()) {
            throw new IllegalArgumentException("The workload is empty.");
        }

        if (quantum <= 0) {
            throw new IllegalArgumentException(
                    "Round Robin quantum must be greater than zero.");
        }

        String[] algorithms = {"FCFS", "SJF", "SRTF", "PRIORITY", "RR"};

        StringBuilder result = new StringBuilder();
        result.append("Algorithm comparison\n");
        result.append("--------------------\n");
        result.append(PRIORITY_SEMANTICS).append('\n');
        result.append("Round Robin quantum: ").append(quantum).append('\n');

        result.append(String.format(
                Locale.ROOT,
                "%-12s %12s %14s %12s %11s %14s %12s%n",
                "Algorithm",
                "Avg Waiting",
                "Avg Turnaround",
                "Avg Response",
                "CPU Util.",
                "Throughput",
                "Long Waits"
        ));

        for (String algorithm : algorithms) {
            List<Process> completed =
                    scheduler.schedule(workload, algorithm, quantum);

            double averageWaiting = averageWaitingTime(completed);
            double averageTurnaround = averageTurnaroundTime(completed);
            double averageResponse = averageResponseTime(completed);

            int longWaitCount =
                    findLongWaits(completed, averageWaiting).size();

            String displayName = algorithm.equals("RR")
                    ? "RR (q=" + quantum + ")"
                    : algorithm;

            result.append(String.format(
                    Locale.ROOT,
                    "%-12s %12.2f %14.2f %12.2f %10.2f%% %14.4f %12d%n",
                    displayName,
                    averageWaiting,
                    averageTurnaround,
                    averageResponse,
                    scheduler.getCpuUtilization(),
                    scheduler.getThroughput(),
                    longWaitCount
            ));
        }

        result.append('\n');
        result.append("Long-wait rule: waiting time must be greater than 10 time units ");
        result.append("and greater than twice the workload average waiting time. ");
        result.append("When the average is zero, only waits greater than 10 are reported.");

        return result.toString();
    }

    public static String formatReport(
            List<Process> processes,
            String algorithm,
            List<String> ganttChart,
            double cpuUtilization,
            double throughput) {

        validateWorkload(processes);

        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("An algorithm is required.");
        }

        double averageWaiting = averageWaitingTime(processes);
        double averageTurnaround = averageTurnaroundTime(processes);
        double averageResponse = averageResponseTime(processes);

        List<Process> longWaits =
                findLongWaits(processes, averageWaiting);

        StringBuilder report = new StringBuilder();

        report.append("CPU Scheduling Analysis Report\n");
        report.append("==============================\n");
        report.append("Algorithm: ").append(algorithm).append('\n');
        report.append(PRIORITY_SEMANTICS).append('\n');
        report.append("Process count: ").append(processes.size()).append('\n');

        report.append('\n');
        report.append("Gantt chart\n");
        report.append("-----------\n");

        if (ganttChart == null || ganttChart.isEmpty()) {
            report.append("No timeline available.\n");
        } else {
            for (String segment : ganttChart) {
                report.append(segment).append('\n');
            }
        }

        report.append('\n');
        report.append("Per-process results\n");
        report.append("-------------------\n");

        report.append(String.format(
                Locale.ROOT,
                "%-10s %8s %8s %8s %10s %10s %10s %10s%n",
                "ID",
                "Arrival",
                "Burst",
                "Priority",
                "Completion",
                "Turnaround",
                "Waiting",
                "Response"
        ));

        List<Process> orderedProcesses =
                new ArrayList<>(processes);

        orderedProcesses.sort(
                Comparator.comparingInt(Process::getArrivalTime)
                        .thenComparing(Process::getId)
        );

        for (Process process : orderedProcesses) {
            report.append(String.format(
                    Locale.ROOT,
                    "%-10s %8d %8d %8d %10d %10d %10d %10d%n",
                    process.getId(),
                    process.getArrivalTime(),
                    process.getBurstTime(),
                    process.getPriority(),
                    process.getCompletionTime(),
                    process.getTurnaroundTime(),
                    process.getWaitingTime(),
                    process.getResponseTime()
            ));
        }

        report.append('\n');
        report.append("Aggregate results\n");
        report.append("-----------------\n");

        report.append(String.format(
                Locale.ROOT,
                "Average waiting time: %.2f%n",
                averageWaiting
        ));

        report.append(String.format(
                Locale.ROOT,
                "Average turnaround time: %.2f%n",
                averageTurnaround
        ));

        report.append(String.format(
                Locale.ROOT,
                "Average response time: %.2f%n",
                averageResponse
        ));

        report.append(String.format(
                Locale.ROOT,
                "CPU utilization: %.2f%%%n",
                cpuUtilization
        ));

        report.append(String.format(
                Locale.ROOT,
                "Throughput: %.4f process(es) per time unit%n",
                throughput
        ));

        report.append('\n');
        report.append("Long-wait detection\n");
        report.append("-------------------\n");
        report.append("Rule: a process waits a long time when its waiting time is greater ");
        report.append("than 10 and greater than twice the average waiting time. ");
        report.append("If the average is zero, only waits greater than 10 are reported.\n");

        if (longWaits.isEmpty()) {
            report.append("No long waits detected.\n");
        } else {
            report.append("Long-wait processes: ");

            for (int i = 0; i < longWaits.size(); i++) {
                if (i > 0) {
                    report.append(", ");
                }

                Process process = longWaits.get(i);

                report.append(process.getId())
                        .append(" (")
                        .append(process.getWaitingTime())
                        .append(" time units)");
            }

            report.append('\n');
        }

        return report.toString().trim();
    }

    public static double averageWaitingTime(List<Process> processes) {
        validateWorkload(processes);
        return averageMetric(processes, "waiting");
    }

    public static double averageTurnaroundTime(List<Process> processes) {
        validateWorkload(processes);
        return averageMetric(processes, "turnaround");
    }

    public static double averageResponseTime(List<Process> processes) {
        validateWorkload(processes);
        return averageMetric(processes, "response");
    }

    public static List<Process> findLongWaits(
            List<Process> processes,
            double averageWaitingTime) {

        validateWorkload(processes);

        List<Process> longWaits = new ArrayList<>();

        for (Process process : processes) {
            int waitingTime = process.getWaitingTime();

            if (averageWaitingTime == 0.0) {
                if (waitingTime > LONG_WAIT_MINIMUM) {
                    longWaits.add(process);
                }
            } else if (waitingTime > LONG_WAIT_MINIMUM
                    && waitingTime > 2.0 * averageWaitingTime) {
                longWaits.add(process);
            }
        }

        longWaits.sort(
                Comparator.comparingInt(Process::getWaitingTime)
                        .reversed()
                        .thenComparing(Process::getId)
        );

        return longWaits;
    }

    private static double averageMetric(
            List<Process> processes,
            String metric) {

        if (processes.isEmpty()) {
            return 0.0;
        }

        long total = 0;

        for (Process process : processes) {
            switch (metric) {
                case "waiting" ->
                        total += process.getWaitingTime();

                case "turnaround" ->
                        total += process.getTurnaroundTime();

                case "response" ->
                        total += process.getResponseTime();

                default ->
                        throw new IllegalArgumentException(
                                "Unknown metric: " + metric);
            }
        }

        return (double) total / processes.size();
    }

    private static void validateWorkload(List<Process> workload) {
        if (workload == null) {
            throw new IllegalArgumentException(
                    "The workload cannot be null.");
        }

        for (Process process : workload) {
            if (process == null) {
                throw new IllegalArgumentException(
                        "The workload contains a null process.");
            }
        }
    }
}