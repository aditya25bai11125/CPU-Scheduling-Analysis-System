# CPU Scheduling Analysis & Simulation System

## 1. Cover Page

**Project Title:** CPU Scheduling Analysis & Simulation System  
**Subject:** Programming in Java  
**Technology:** Java 17+ standard library  
**Project Type:** Terminal-based scheduling simulation and analysis system  
**Student:** [Add student name]  
**Registration Number:** [Add registration number]  
**Institution:** [Add institution name]  
**Submission Date:** [Add date]

## 2. Introduction

CPU scheduling is an important operating-system concept because the selected scheduling policy affects how quickly processes start, how long they wait, and how efficiently the processor is used. This project implements a compact Java application that allows a user to create a process workload and observe the results of several scheduling algorithms.

The system is intentionally limited to four Java source files. `Main` manages interaction, `Process` stores process data, `Scheduler` performs simulation, and `Analysis` formats results and calculates reports. This keeps the implementation understandable while demonstrating Java classes, collections, validation, file handling, control flow, and algorithmic problem solving.

## 3. Problem Statement

Students often understand individual scheduling formulas but find it difficult to compare several algorithms on the same workload. Manual calculations also become error-prone when processes arrive at different times or when preemption and Round Robin queues are involved.

The project addresses this problem by accepting a validated workload, simulating multiple scheduling policies, producing a text Gantt chart, calculating standard scheduling metrics, and presenting a comparison report.

## 4. Functional Requirements

### Workload Builder

- The system shall allow manual entry of process ID, arrival time, burst time, and priority.
- The system shall load a built-in sample CSV workload.
- The system shall import workloads from CSV files.
- The system shall export the current workload to CSV.
- The system shall generate workloads using configurable count and maximum values.
- The system shall reject empty IDs, reserved ID `IDLE`, duplicate IDs, negative values, invalid numbers, and invalid burst times.
- The system shall provide simple workload statistics.

### Scheduling Simulation

- The system shall implement FCFS.
- The system shall implement non-preemptive SJF.
- The system shall implement preemptive SRTF.
- The system shall implement non-preemptive Priority Scheduling.
- The system shall implement Round Robin with a positive configurable quantum.
- The system shall preserve arrival constraints.
- The system shall include idle periods in the Gantt chart when no process is ready.
- The system shall calculate completion, turnaround, waiting, and response time.
- The system shall calculate CPU utilization and throughput.
- The system shall use independent workload copies for separate simulations.

### Analysis and Reporting

- The system shall display per-process simulation results.
- The system shall display average waiting, turnaround, and response times.
- The system shall compare all supported algorithms.
- The system shall identify long waits using a documented rule.
- The system shall export simulation and comparison results as text files.

## 5. Non-functional Requirements

### Usability

The terminal menu shall use clear numbered options, readable prompts, validation messages, and a straightforward workflow from workload creation to reporting.

### Correctness

Each algorithm shall respect process arrival times, handle idle CPU intervals, calculate metrics consistently, and use deterministic tie-breaking for equal scheduling keys.

### Maintainability

The system shall contain exactly four Java source files with meaningful class responsibilities, small understandable methods, standard collections, and Java 17-compatible syntax.

### Portability and Reliability

The application shall run with a standard Java 17 or newer runtime without Maven, Gradle, a database, a graphical interface, or third-party runtime libraries. Malformed input shall produce an error message rather than an uncontrolled crash.

### Performance

The implementation shall use ordinary in-memory collections and should be suitable for the simple educational workloads targeted by the project. Generated workloads are bounded by the application to avoid unreasonable input sizes.

## 6. System Architecture

The project uses a simple four-class layered responsibility split:

- **Main:** Presentation and application-flow layer. Handles menus, command-line options, file operations, input parsing, and calls to other classes.
- **Process:** Domain-data layer. Stores process attributes and mutable simulation state and calculates process-level metrics.
- **Scheduler:** Simulation layer. Copies workloads, runs algorithms, creates Gantt entries, and calculates CPU summary values.
- **Analysis:** Reporting layer. Calculates workload statistics, averages, comparisons, long waits, and formatted output.

The application starts in `Main`. A workload is built or loaded, then passed to `Scheduler` for simulation. The returned process results and scheduler summary values are passed to `Analysis`. Reports may be printed and optionally written to a text file.

## 7. Design Diagrams

The repository includes the following Mermaid diagrams:

- `docs/system-architecture.mmd` shows the four-class responsibility structure.
- `docs/workflow.mmd` shows the normal user workflow.
- `docs/use-case.mmd` shows user interactions with the three functional modules.
- `docs/sequence.mmd` shows a simulation request sequence.
- `docs/class-diagram.mmd` shows the actual Java classes and their principal relationships.

No additional Java classes are represented in the diagrams.

## 8. Design Decisions & Rationale

1. **Exactly four source files:** The source limit keeps the project compact and prevents artificial helper classes. Each class has a clear responsibility.
2. **Independent simulation copies:** Scheduling changes remaining time and metric fields, so `Scheduler` copies the input workload before every run.
3. **Simple text Gantt representation:** Timeline entries use strings such as `P1 [0-7]`. Consecutive entries for the same process are merged when possible, and `IDLE` entries represent CPU idle time.
4. **Deterministic tie-breaking:** Equal scheduling keys use earliest arrival time followed by lexicographically smallest process ID. Round Robin preserves ready-queue order and appends unfinished processes after their slice.
5. **Priority interpretation:** Smaller numeric priority values represent higher priority.
6. **CSV format:** The project uses `id,arrivalTime,burstTime,priority` to keep file handling understandable with standard Java APIs.
7. **Long-wait rule:** A wait is long when it is greater than 10 and greater than twice the average waiting time. If the average is zero, only waits greater than 10 are reported.
8. **Standard library only:** File access uses `java.nio.file`, input uses `Scanner`, and workload generation uses `Random`.
9. **Interactive and CLI modes:** Interactive mode supports student demonstration, while CLI mode supports repeatable execution and export.

## 9. Implementation Details

### Process Data

`Process` stores immutable input fields for ID, arrival time, burst time, and priority. It also stores remaining time, start time, and completion time. Turnaround time is completion time minus arrival time. Waiting time is turnaround time minus original burst time. Response time is start time minus arrival time.

### Scheduling Algorithms

- **FCFS:** Processes are sorted by arrival time and ID and run to completion.
- **SJF:** Ready processes are selected by shortest original burst time without preemption.
- **SRTF:** At each time unit, the ready process with the shortest remaining time is selected.
- **Priority:** Ready processes are selected by the smallest priority value without preemption.
- **Round Robin:** Ready processes are managed with a queue. Each process receives at most the configured quantum before an unfinished process returns to the queue.

### Metrics

Busy time is the sum of original burst times. Elapsed time is the latest completion time from time zero. CPU utilization is calculated as busy time divided by elapsed time and expressed as a percentage. Throughput is the process count divided by elapsed time.

### Validation

The application validates empty workloads, duplicate IDs, null processes, invalid algorithm names, invalid quantum values, malformed CSV fields, missing files, and unsupported numeric values. Timeline capacity is also checked before simulation to avoid integer overflow in the supported time representation.

## 10. Screenshots / Results

The report should contain screenshots captured from the actual implementation. Do not use invented or simulated screenshots.

[ADD REAL SCREENSHOT: Interactive menu]

[ADD REAL SCREENSHOT: Sample workload statistics]

[ADD REAL SCREENSHOT: FCFS or SRTF report with Gantt chart]

[ADD REAL SCREENSHOT: Algorithm comparison output]

[ADD REAL SCREENSHOT: Exported text result opened in a text editor]

Results should be generated by running the commands in `README.md`. Any numerical observations added here should be copied from an actual run rather than invented in advance.

## 11. Testing Approach

Testing is based on compilation checks, normal usage, boundary cases, malformed input, and comparison of output against manually calculated small examples.

### Build Testing

```powershell
.\compile.ps1
```

The project should compile with Java 17 or newer and should create the `out` directory.

### Functional Testing

```powershell
.\run.ps1 --sample --algorithm FCFS
.\run.ps1 --sample --algorithm SJF
.\run.ps1 --sample --algorithm SRTF
.\run.ps1 --sample --algorithm PRIORITY
.\run.ps1 --sample --algorithm RR --quantum 2
.\run.ps1 --sample --compare --quantum 3 --export comparison.txt
```

### Input and Validation Testing

- Import `data/sample_processes.csv`.
- Try a missing CSV path.
- Try a CSV row with fewer or more than four fields.
- Try duplicate process IDs.
- Try a negative arrival time.
- Try burst time zero.
- Try an invalid algorithm name.
- Try Round Robin quantum zero.
- Try an empty workload in interactive mode.
- Try generated workloads with different process counts and ranges.

### Algorithm Verification

Use small workloads whose results can be calculated by hand. Check that idle intervals appear when the first arrival is greater than zero, that SRTF can preempt a running process, that Round Robin preserves queue order, and that all reported metrics are non-negative and internally consistent.

The student should add actual test observations and any discovered defects here after running the program.

## 12. Challenges Faced

**Student personalization required.** Replace this section with challenges actually experienced during implementation. Suitable topics may include handling preemption in SRTF, maintaining Round Robin queue order when new processes arrive, representing idle time, validating CSV input, or keeping the four-file source restriction manageable.

Possible reflection prompts:

- Which algorithm was hardest to implement and why?
- How was the ready queue handled?
- Which invalid inputs required additional validation?
- Was any scheduling result initially different from a hand calculation?

## 13. Learnings & Key Takeaways

**Student personalization required.** Describe the Java and scheduling concepts learned from the real development process. Possible areas include:

- Designing classes around responsibilities
- Using `ArrayList`, `ArrayDeque`, `Comparator`, and `HashSet`
- Copying mutable state before independent simulations
- Handling files with `java.nio.file.Files`
- Converting scheduling definitions into deterministic control flow
- Testing edge cases involving idle CPU time and simultaneous arrivals
- Using Git commits to track incremental work

The final submission should include personal examples rather than copying these prompts as completed claims.

## 14. Future Enhancements

- Add automated tests while relaxing the current four-source-file restriction.
- Add support for quoted CSV fields.
- Add optional context-switch overhead.
- Add multi-core scheduling simulation.
- Add configurable priority direction.
- Add graphical chart output while keeping the terminal mode.
- Add JSON or HTML report export.
- Add a stronger statistical analysis of generated workloads.
- Add a persistent workload library using a database or structured files.

## 15. References

- Java Platform Standard Edition API documentation for Java 17 or newer, especially `java.util`, `java.nio.file`, and `java.util.Scanner`.
- Standard operating-systems course material covering FCFS, SJF, SRTF, Priority Scheduling, Round Robin, turnaround time, waiting time, response time, throughput, and CPU utilization.
- Mermaid documentation for flowcharts, sequence diagrams, class diagrams, and use-case-style diagrams.

References should be updated with the exact course books, lecture notes, or web resources used by the student.
