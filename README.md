# CPU Scheduling Analysis & Simulation System

A compact terminal-based Java 17 project for creating process workloads, simulating CPU scheduling algorithms, and comparing their results.

## Quick Start

The repository already contains sample process data. You do **not** need to enter any process values to perform the main test.

### 1. Open a terminal in the project root

You should be in the folder containing:

```text
README.md
statement.md
PROJECT_REPORT.md
compile.ps1
compile.bat
run.ps1
run.bat
data/
src/
```

### 2. Check Java

```powershell
java -version
javac -version
```

JDK 17 or newer is required.

### 3. Compile the project

PowerShell:

```powershell
.\compile.ps1
```

Command Prompt:

```bat
compile.bat
```

You should see:

```text
Compilation successful.
```

### 4. Run the sample workload automatically

The recommended first test is:

```powershell
.
un.ps1 --sample --compare --quantum 2
```

This command uses the sample workload already present in:

```text
data/sample_processes.csv
```

No keyboard input is required.

The program should run and display results for:

```text
FCFS
SJF
SRTF
Priority Scheduling
Round Robin
```

It should also display the execution timelines and scheduling metrics.

---

## Sample Workload

The sample file is:

```text
data/sample_processes.csv
```

It contains:

```csv
PID,ArrivalTime,BurstTime,Priority
P1,0,7,2
P2,2,4,1
P3,4,1,3
P4,5,4,2
P5,6,2,1
```

You do not need to edit this file for the first test.

---

## Test Each Scheduling Algorithm

After compiling, run these commands one at a time.

### FCFS

```powershell
.
un.ps1 --sample --algorithm FCFS
```

### SJF

```powershell
.
un.ps1 --sample --algorithm SJF
```

### SRTF

```powershell
.
un.ps1 --sample --algorithm SRTF
```

### Priority Scheduling

```powershell
.
un.ps1 --sample --algorithm PRIORITY
```

Lower numerical priority values represent higher priority.

### Round Robin

```powershell
.
un.ps1 --sample --algorithm RR --quantum 2
```

---

## Compare All Algorithms

Run:

```powershell
.
un.ps1 --sample --compare --quantum 2
```

This compares:

```text
FCFS
SJF
SRTF
Priority Scheduling
Round Robin
```

The comparison includes metrics such as:

- Average Waiting Time
- Average Turnaround Time
- Average Response Time
- CPU Utilization
- Throughput

---

## Export Results

To save a comparison report:

```powershell
.
un.ps1 --sample --compare --quantum 2 --export comparison.txt
```

View the generated file with:

```powershell
Get-Content comparison.txt
```

To export one algorithm's result:

```powershell
.
un.ps1 --sample --algorithm SRTF --export srtf-report.txt
```

---

## Test CSV Loading

The sample file can also be supplied directly:

```powershell
.
un.ps1 --input data\sample_processes.csv --algorithm SRTF
```

This tests the CSV input path.

---

## Generate a New Workload

Example:

```powershell
.
un.ps1 --generate 10 --max-arrival 20 --max-burst 15 --max-priority 5
```

This creates a workload using the specified limits.

---

## Interactive Mode

The project also has an interactive terminal menu.

Run:

```powershell
.
un.ps1
```

The menu allows you to:

```text
1. Add process manually
2. Load sample workload
3. Import workload from CSV
4. Export workload to CSV
5. Generate a workload
6. Show workload statistics
7. Run a scheduling simulation
8. Compare algorithms
9. Exit
```

To use the sample data from the menu, select:

```text
2
```

Then you can inspect statistics, run a scheduler, or compare algorithms.

---

## Direct CLI Examples

The `run.ps1` script is the easiest way to run commands because it already sets the Java classpath.

Examples:

```powershell
.
un.ps1 --sample --algorithm FCFS
```

```powershell
.
un.ps1 --sample --algorithm SJF
```

```powershell
.
un.ps1 --sample --algorithm SRTF
```

```powershell
.
un.ps1 --sample --algorithm PRIORITY
```

```powershell
.
un.ps1 --sample --algorithm RR --quantum 2
```

```powershell
.
un.ps1 --sample --compare --quantum 2
```

For CSV input:

```powershell
.
un.ps1 --input data\sample_processes.csv --algorithm SRTF
```

For generated input:

```powershell
.
un.ps1 --generate 10 --max-arrival 20 --max-burst 15 --max-priority 5
```

---

## Metrics

For every process, the program reports:

```text
Completion Time
Turnaround Time
Waiting Time
Response Time
```

The formulas are:

```text
Turnaround Time = Completion Time - Arrival Time

Waiting Time = Turnaround Time - Burst Time

Response Time = First CPU Start Time - Arrival Time
```

The simulator also calculates:

```text
Average Waiting Time
Average Turnaround Time
Average Response Time
CPU Utilization
Throughput
```

---

## Gantt Chart

The execution timeline is displayed as text.

Example:

```text
|  P1  |  P2  |  P3  |  P4  |
0      7     11     12     16
```

CPU idle periods are represented when no process is ready.

---

## Testing and Validation

For a complete basic test, use:

```powershell
.\compile.ps1

.
un.ps1 --sample --algorithm FCFS

.
un.ps1 --sample --algorithm SJF

.
un.ps1 --sample --algorithm SRTF

.
un.ps1 --sample --algorithm PRIORITY

.
un.ps1 --sample --algorithm RR --quantum 2

.
un.ps1 --sample --compare --quantum 2
```

Then test CSV loading:

```powershell
.
un.ps1 --input data\sample_processes.csv --algorithm SRTF
```

Then test export:

```powershell
.
un.ps1 --sample --compare --quantum 2 --export comparison.txt
```

For error handling, try:

```text
a missing CSV file
an unknown algorithm
a duplicate process ID
a zero or negative Round Robin quantum
a malformed CSV row
```

The program should display an error rather than continue with invalid input.

---

## Manual Compilation Without Scripts

PowerShell:

```powershell
New-Item -ItemType Directory -Force out | Out-Null

javac --release 17 -encoding UTF-8 -d out `
src/main/java/com/vit/cpusimulator/Main.java `
src/main/java/com/vit/cpusimulator/Process.java `
src/main/java/com/vit/cpusimulator/Scheduler.java `
src/main/java/com/vit/cpusimulator/Analysis.java
```

Then run:

```powershell
java -cp out com.vit.cpusimulator.Main --sample --compare --quantum 2
```

Command Prompt:

```bat
if not exist out mkdir out

javac --release 17 -encoding UTF-8 -d out src\main\java\com\vit\cpusimulator\Main.java src\main\java\com\vit\cpusimulator\Process.java src\main\java\com\vit\cpusimulator\Scheduler.java src\main\java\com\vit\cpusimulator\Analysis.java
```

Then:

```bat
java -cp out com.vit.cpusimulator.Main --sample --compare --quantum 2
```

---

## Troubleshooting

### PowerShell blocks the script

Run:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
```

Then:

```powershell
.\compile.ps1
```

### Java is not recognized

Run:

```powershell
java -version
```

A JDK 17+ installation must be available in PATH.

### javac is not recognized

Run:

```powershell
javac -version
```

A JDK is required; a JRE alone is not enough.

### You do not want to enter any values

Use:

```powershell
.
un.ps1 --sample --compare --quantum 2
```

This uses the repository's sample workload automatically.

---

## Project Structure

```text
CPU-Scheduling-Analysis-System/
│
├── README.md
├── statement.md
├── PROJECT_REPORT.md
│
├── compile.bat
├── compile.ps1
├── run.bat
├── run.ps1
│
├── data/
│   └── sample_processes.csv
│
├── docs/
│   ├── class-diagram.mmd
│   ├── sequence.mmd
│   ├── system-architecture.mmd
│   ├── use-case.mmd
│   └── workflow.mmd
│
└── src/
    └── main/
        └── java/
            └── com/
                └── vit/
                    └── cpusimulator/
                        ├── Main.java
                        ├── Process.java
                        ├── Scheduler.java
                        └── Analysis.java
```

The Java application intentionally uses four source files:

```text
Main.java
Process.java
Scheduler.java
Analysis.java
```

---

## Technologies

- Java 17+
- Java standard library
- javac
- java
- PowerShell / Command Prompt
- Git
- Mermaid for design diagrams

No Maven, Gradle, database, GUI framework, or third-party Java runtime library is required.

---

## Author

**Aditya Shukla**

VITyarthi Programming in Java — Flipped Course Project
