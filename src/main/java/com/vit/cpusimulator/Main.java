package com.vit.cpusimulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final int MAX_GENERATED_PROCESSES = 10000;

    private static final String SAMPLE_CSV =
            "id,arrivalTime,burstTime,priority\n"
                    + "P1,0,7,2\n"
                    + "P2,2,4,1\n"
                    + "P3,4,1,3\n"
                    + "P4,5,4,2\n"
                    + "P5,6,2,1\n";

    private static List<Process> workload = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                runCommandLine(args);
            } catch (IllegalArgumentException | IOException exception) {
                System.err.println("Error: " + exception.getMessage());
                System.exit(1);
            }
            return;
        }

        runInteractiveMenu();
    }

    private static void runCommandLine(String[] args) throws IOException {
        String algorithm = null;
        String inputFile = null;
        String exportFile = null;
        int quantum = 2;
        boolean compare = false;
        int generateCount = -1;
        int maxArrival = 20;
        int maxBurst = 15;
        int maxPriority = 5;
        int workloadSourceCount = 0;
        String selectedSource = null;

        for (int index = 0; index < args.length; index++) {
            String argument = args[index];

            if ("--sample".equalsIgnoreCase(argument)) {
                workloadSourceCount++;
                selectedSource = "sample";
            } else if ("--input".equalsIgnoreCase(argument)
                    || "--file".equalsIgnoreCase(argument)) {
                if (index + 1 >= args.length) {
                    throw new IllegalArgumentException(
                            "Missing file path after " + argument + ".");
                }
                workloadSourceCount++;
                selectedSource = "file";
                inputFile = args[++index];
            } else if ("--algorithm".equalsIgnoreCase(argument)) {
                if (index + 1 >= args.length) {
                    throw new IllegalArgumentException(
                            "Missing algorithm after --algorithm.");
                }
                algorithm = args[++index];
            } else if ("--quantum".equalsIgnoreCase(argument)) {
                if (index + 1 >= args.length) {
                    throw new IllegalArgumentException(
                            "Missing value after --quantum.");
                }
                quantum = parsePositiveArgument(args[++index], "quantum");
            } else if ("--export".equalsIgnoreCase(argument)) {
                if (index + 1 >= args.length) {
                    throw new IllegalArgumentException(
                            "Missing file path after --export.");
                }
                exportFile = args[++index];
            } else if ("--compare".equalsIgnoreCase(argument)) {
                compare = true;
            } else if ("--generate".equalsIgnoreCase(argument)) {
                if (index + 1 >= args.length) {
                    throw new IllegalArgumentException(
                            "Missing process count after --generate.");
                }
                workloadSourceCount++;
                selectedSource = "generate";
                generateCount = parseNonNegativeArgument(
                        args[++index], "process count");

                if (generateCount > MAX_GENERATED_PROCESSES) {
                    throw new IllegalArgumentException(
                            "Process count cannot exceed "
                                    + MAX_GENERATED_PROCESSES + ".");
                }
            } else if ("--max-arrival".equalsIgnoreCase(argument)) {
                if (index + 1 >= args.length) {
                    throw new IllegalArgumentException(
                            "Missing value after --max-arrival.");
                }
                maxArrival = parseNonNegativeArgument(
                        args[++index], "maximum arrival");
            } else if ("--max-burst".equalsIgnoreCase(argument)) {
                if (index + 1 >= args.length) {
                    throw new IllegalArgumentException(
                            "Missing value after --max-burst.");
                }
                maxBurst = parsePositiveArgument(
                        args[++index], "maximum burst");
            } else if ("--max-priority".equalsIgnoreCase(argument)) {
                if (index + 1 >= args.length) {
                    throw new IllegalArgumentException(
                            "Missing value after --max-priority.");
                }
                maxPriority = parseNonNegativeArgument(
                        args[++index], "maximum priority");
            } else {
                throw new IllegalArgumentException(
                        "Unknown or incomplete option: " + argument);
            }
        }

        if (workloadSourceCount != 1) {
            throw new IllegalArgumentException(
                    "Select exactly one workload source: --sample, "
                            + "--input/--file, or --generate.");
        }

        if ("sample".equals(selectedSource)) {
            workload = parseCsv(SAMPLE_CSV);
        } else if ("file".equals(selectedSource)) {
            workload = loadCsv(inputFile);
        } else {
            workload = generateWorkload(
                    generateCount, maxArrival, maxBurst, maxPriority);
        }

        validateTimelineCapacity(workload);

        if (compare) {
            runComparison(quantum, exportFile);
            return;
        }

        if (algorithm == null) {
            algorithm = "FCFS";
        }

        runSimulation(algorithm, quantum, exportFile);
    }

    private static void runInteractiveMenu() {
        System.out.println("CPU Scheduling Analysis & Simulation System");

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("Current workload: " + workload.size() + " process(es)");
            System.out.println("1. Add process manually");
            System.out.println("2. Load sample workload");
            System.out.println("3. Import workload from CSV");
            System.out.println("4. Export workload to CSV");
            System.out.println("5. Generate workload");
            System.out.println("6. Show workload statistics");
            System.out.println("7. Run scheduling simulation");
            System.out.println("8. Compare algorithms");
            System.out.println("9. Exit");

            int choice = readInt("Select an option: ", 1, 9);

            try {
                switch (choice) {
                    case 1 -> addProcess();
                    case 2 -> loadSample();
                    case 3 -> importWorkload();
                    case 4 -> exportWorkload();
                    case 5 -> generateInteractiveWorkload();
                    case 6 -> showStatistics();
                    case 7 -> simulateInteractively();
                    case 8 -> compareAlgorithms();
                    case 9 -> running = false;
                    default -> System.out.println("Invalid option.");
                }
            } catch (IOException | IllegalArgumentException exception) {
                System.out.println("Error: " + exception.getMessage());
            }
        }

        System.out.println("Goodbye.");
    }

    private static void addProcess() {
        String id = readText("Process ID: ");
        int arrival = readInt("Arrival time: ", 0, Integer.MAX_VALUE);
        int burst = readInt("Burst time: ", 1, Integer.MAX_VALUE);
        int priority = readInt("Priority: ", 0, Integer.MAX_VALUE);

        validateProcessFields(id, arrival, burst, priority);

        for (Process process : workload) {
            if (process.getId().equals(id.trim())) {
                throw new IllegalArgumentException("Duplicate process ID: " + id);
            }
        }

        List<Process> candidateWorkload = new ArrayList<>(workload);
        candidateWorkload.add(new Process(id, arrival, burst, priority));
        candidateWorkload.sort(processOrder());
        validateTimelineCapacity(candidateWorkload);

        workload = candidateWorkload;
        System.out.println("Process added.");
    }

    private static void loadSample() {
        workload = parseCsv(SAMPLE_CSV);
        System.out.println("Sample workload loaded.");
    }

    private static void importWorkload() throws IOException {
        String fileName = readText("CSV file path: ");
        List<Process> imported = loadCsv(fileName);
        validateTimelineCapacity(imported);
        workload = imported;
        System.out.println("Imported " + workload.size() + " process(es).");
    }

    private static void exportWorkload() throws IOException {
        if (workload.isEmpty()) {
            System.out.println("There is no workload to export.");
            return;
        }

        String fileName = readText("Output CSV file path: ");
        StringBuilder csv = new StringBuilder(
                "id,arrivalTime,burstTime,priority\n");

        for (Process process : workload) {
            csv.append(process.getId()).append(',')
                    .append(process.getArrivalTime()).append(',')
                    .append(process.getBurstTime()).append(',')
                    .append(process.getPriority()).append('\n');
        }

        Files.writeString(Path.of(fileName), csv.toString());
        System.out.println("Workload exported.");
    }

    private static void generateInteractiveWorkload() {
        int count = readInt(
                "Number of processes: ", 0, MAX_GENERATED_PROCESSES);
        int maxArrival = readInt(
                "Maximum arrival time: ", 0, Integer.MAX_VALUE);
        int maxBurst = readInt(
                "Maximum burst time: ", 1, Integer.MAX_VALUE);
        int maxPriority = readInt(
                "Maximum priority: ", 0, Integer.MAX_VALUE);

        workload = generateWorkload(
                count, maxArrival, maxBurst, maxPriority);
        System.out.println("Generated " + workload.size() + " process(es).");
    }

    private static void showStatistics() {
        if (workload.isEmpty()) {
            System.out.println("There is no workload.");
            return;
        }

        System.out.println(Analysis.workloadStatistics(workload));
    }

    private static void simulateInteractively() throws IOException {
        if (workload.isEmpty()) {
            System.out.println("Create or load a workload first.");
            return;
        }

        String algorithm = chooseAlgorithm();
        int quantum = 2;

        if ("RR".equals(algorithm)) {
            quantum = readInt(
                    "Round Robin quantum: ", 1, Integer.MAX_VALUE);
        }

        String exportFile = readText(
                "Export result to a text file, or press Enter to skip: ");

        runSimulation(
                algorithm,
                quantum,
                exportFile.isBlank() ? null : exportFile);
    }

    private static void compareAlgorithms() throws IOException {
        if (workload.isEmpty()) {
            System.out.println("Create or load a workload first.");
            return;
        }

        validateTimelineCapacity(workload);
        int quantum = readInt(
                "Round Robin quantum for comparison: ", 1, Integer.MAX_VALUE);
        String exportFile = readText(
                "Export comparison to a text file, or press Enter to skip: ");

        runComparison(
                quantum,
                exportFile.isBlank() ? null : exportFile);
    }

    private static void runComparison(
            int quantum, String exportFile) throws IOException {
        if (workload.isEmpty()) {
            throw new IllegalArgumentException("The workload is empty.");
        }

        Scheduler scheduler = new Scheduler();
        String result = Analysis.compareAlgorithms(
                workload, scheduler, quantum);

        System.out.println();
        System.out.println(result);

        if (exportFile != null) {
            Files.writeString(Path.of(exportFile), result);
            System.out.println("Comparison exported to " + exportFile);
        }
    }

    private static void runSimulation(
            String algorithm, int quantum, String exportFile)
            throws IOException {
        String canonicalAlgorithm = normalizeAlgorithm(algorithm);

        if (workload.isEmpty()) {
            throw new IllegalArgumentException("The workload is empty.");
        }

        validateTimelineCapacity(workload);

        Scheduler scheduler = new Scheduler();
        List<Process> result = scheduler.schedule(
                workload, canonicalAlgorithm, quantum);

        String displayedAlgorithm = canonicalAlgorithm;
        if ("RR".equals(canonicalAlgorithm)) {
            displayedAlgorithm = "RR (quantum=" + quantum + ")";
        }

        String report = Analysis.formatReport(
                result,
                displayedAlgorithm,
                scheduler.getGanttChart(),
                scheduler.getCpuUtilization(),
                scheduler.getThroughput());

        System.out.println();
        System.out.println(report);

        if (exportFile != null) {
            Files.writeString(Path.of(exportFile), report);
            System.out.println("Result exported to " + exportFile);
        }
    }

    private static String chooseAlgorithm() {
        System.out.println("1. FCFS");
        System.out.println("2. SJF");
        System.out.println("3. SRTF");
        System.out.println("4. Priority");
        System.out.println("5. Round Robin");

        int choice = readInt("Choose algorithm: ", 1, 5);

        return switch (choice) {
            case 1 -> "FCFS";
            case 2 -> "SJF";
            case 3 -> "SRTF";
            case 4 -> "PRIORITY";
            case 5 -> "RR";
            default -> "FCFS";
        };
    }

    private static String normalizeAlgorithm(String algorithm) {
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("An algorithm is required.");
        }

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
            default -> throw new IllegalArgumentException(
                    "Unknown scheduling algorithm: " + algorithm);
        };
    }

    private static List<Process> loadCsv(String fileName)
            throws IOException {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("CSV file path cannot be empty.");
        }

        Path path = Path.of(fileName);

        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + fileName);
        }

        if (!Files.isRegularFile(path)) {
            throw new IOException("Path is not a regular file: " + fileName);
        }

        return parseCsv(Files.readString(path));
    }

    private static List<Process> parseCsv(String content) {
        if (content == null) {
            throw new IllegalArgumentException("CSV content cannot be null.");
        }

        List<Process> processes = new ArrayList<>();
        String[] lines = content.split("\\R");
        boolean firstDataLineSeen = false;

        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber].trim();

            if (line.isEmpty()) {
                continue;
            }

            if (!firstDataLineSeen
                    && line.equalsIgnoreCase(
                            "id,arrivalTime,burstTime,priority")) {
                firstDataLineSeen = true;
                continue;
            }

            String[] fields = line.split(",", -1);

            if (fields.length != 4) {
                throw new IllegalArgumentException(
                        "CSV line " + (lineNumber + 1)
                                + " must contain four fields.");
            }

            String id = fields[0].trim();
            int arrival = parseCsvInteger(
                    fields[1], lineNumber + 1, "arrival time");
            int burst = parseCsvInteger(
                    fields[2], lineNumber + 1, "burst time");
            int priority = parseCsvInteger(
                    fields[3], lineNumber + 1, "priority");

            validateProcessFields(id, arrival, burst, priority);

            for (Process existing : processes) {
                if (existing.getId().equals(id)) {
                    throw new IllegalArgumentException(
                            "Duplicate process ID on CSV line "
                                    + (lineNumber + 1) + ": " + id);
                }
            }

            processes.add(new Process(id, arrival, burst, priority));
            firstDataLineSeen = true;
        }

        processes.sort(processOrder());
        return processes;
    }

    private static List<Process> generateWorkload(
            int count, int maxArrival, int maxBurst, int maxPriority) {
        if (count < 0) {
            throw new IllegalArgumentException(
                    "Process count cannot be negative.");
        }

        if (count > MAX_GENERATED_PROCESSES) {
            throw new IllegalArgumentException(
                    "Process count cannot exceed "
                            + MAX_GENERATED_PROCESSES + ".");
        }

        if (maxArrival < 0 || maxBurst <= 0 || maxPriority < 0) {
            throw new IllegalArgumentException(
                    "Generation limits are invalid.");
        }

        List<Process> generated = new ArrayList<>();
        Random random = new Random(42L);

        for (int index = 1; index <= count; index++) {
            int arrival = random.nextInt(maxArrival + 1);
            int burst = random.nextInt(maxBurst) + 1;
            int priority = random.nextInt(maxPriority + 1);

            generated.add(new Process(
                    "P" + index,
                    arrival,
                    burst,
                    priority));
        }

        generated.sort(processOrder());
        validateTimelineCapacity(generated);
        return generated;
    }

    private static void validateProcessFields(
            String id, int arrival, int burst, int priority) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Process ID cannot be empty.");
        }

        if (arrival < 0) {
            throw new IllegalArgumentException(
                    "Arrival time cannot be negative.");
        }

        if (burst <= 0) {
            throw new IllegalArgumentException(
                    "Burst time must be greater than zero.");
        }

        if (priority < 0) {
            throw new IllegalArgumentException(
                    "Priority cannot be negative.");
        }
    }

    private static void validateTimelineCapacity(
            List<Process> processes) {
        if (processes == null) {
            throw new IllegalArgumentException("The workload cannot be null.");
        }

        long latestArrival = 0L;
        long totalBurst = 0L;

        for (Process process : processes) {
            if (process == null) {
                throw new IllegalArgumentException(
                        "The workload contains a null process.");
            }

            latestArrival = Math.max(
                    latestArrival, process.getArrivalTime());
            totalBurst += process.getBurstTime();

            if (totalBurst + latestArrival > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "The workload exceeds the supported scheduling timeline.");
            }
        }
    }

    private static Comparator<Process> processOrder() {
        return Comparator.comparingInt(Process::getArrivalTime)
                .thenComparing(Process::getId);
    }

    private static int parseCsvInteger(
            String value, int lineNumber, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "CSV line " + lineNumber + " has a missing "
                            + fieldName + ".");
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "CSV line " + lineNumber + " has an invalid "
                            + fieldName + ": " + value.trim());
        }
    }

    private static int parsePositiveArgument(
            String value, String fieldName) {
        int parsed = parseIntegerArgument(value, fieldName);

        if (parsed <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than zero.");
        }

        return parsed;
    }

    private static int parseNonNegativeArgument(
            String value, String fieldName) {
        int parsed = parseIntegerArgument(value, fieldName);

        if (parsed < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative.");
        }

        return parsed;
    }

    private static int parseIntegerArgument(
            String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid " + fieldName + ": " + value);
        }
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return INPUT.nextLine().trim();
    }

    private static int readInt(
            String prompt, int minimum, int maximum) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();

            try {
                int parsed = Integer.parseInt(value);

                if (parsed < minimum || parsed > maximum) {
                    System.out.println(
                            "Enter a value from " + minimum + " to "
                                    + maximum + ".");
                } else {
                    return parsed;
                }
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid integer.");
            }
        }
    }
}