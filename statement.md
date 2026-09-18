# CPU Scheduling Analysis & Simulation System

## Problem Statement

CPU scheduling algorithms decide the order in which processes receive processor time. Different algorithms can produce different waiting times, response times, turnaround times, CPU utilization, and throughput. Manually calculating these values for several workloads is time-consuming and makes algorithm comparison difficult.

This project provides a small terminal-based Java system for creating process workloads, simulating common scheduling algorithms, and reporting their results in a consistent format. It helps users observe how FCFS, SJF, SRTF, Priority Scheduling, and Round Robin behave on the same workload.

## Scope

The project covers:

- Manual creation of processes
- Sample workload loading
- Simple CSV import and export
- Random workload generation
- Workload validation and statistics
- FCFS scheduling
- Non-preemptive SJF scheduling
- Preemptive SRTF scheduling
- Non-preemptive Priority Scheduling
- Round Robin scheduling with a configurable quantum
- Text Gantt charts
- Completion, turnaround, waiting, and response time
- CPU utilization and throughput
- Algorithm comparison
- Long-wait detection
- Text result export

The project does not cover graphical interfaces, operating-system-level process control, databases, multi-core scheduling, context-switch overhead, or real-time scheduling guarantees.

## Target Users

- Programming in Java students
- Operating Systems students learning CPU scheduling
- Teachers demonstrating scheduling algorithms
- Users who need a small command-line scheduling calculator
- Reviewers evaluating algorithm implementation and Java collections

## High-Level Features

1. **Workload Builder** creates or loads process data and validates it before simulation.
2. **Scheduling Simulation** executes five scheduling algorithms using deterministic tie-breaking.
3. **Analysis & Reporting** displays timelines, process metrics, aggregate values, comparisons, statistics, long waits, and exportable reports.

The application is implemented with exactly four Java classes: `Main`, `Process`, `Scheduler`, and `Analysis`.
