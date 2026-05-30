# MIPS Pipeline Hazard Analyzer

A comprehensive Java-based tool for detecting, visualizing, and analyzing pipeline hazards in MIPS assembly code. Detects 5 hazard types (RAW, WAR, WAW, Control, Structural) with real-time pipeline simulation and performance metrics.

## ✨ Features

- **5 Hazard Types Detected** - RAW, WAR, WAW, Control, and Structural hazards
- **Pipeline Stage Simulation** - IF, ID, EX, MEM, WB stages with cycle-by-cycle view
- **Interactive Animation** - Play, pause, step through pipeline execution
- **Performance Metrics** - CPI, throughput, stall percentage calculation
- **Multiple Visualizations** - Gantt chart, heatmap, and hazard grid views
- **Register File Viewer** - Track all 32 MIPS registers with read/write counts
- **Export Reports** - TXT export (always available) and PDF export (optional)

### Seven Analysis Views

| Tab | Purpose |
|-----|---------|
| Pipeline Simulation | Cycle-by-cycle execution with animation controls |
| Hazard Report | Detailed list of all detected hazards with descriptions |
| Performance Metrics | CPI, throughput, stall percentage with assessment |
| Hazard Visualization | Color-coded hazard grid |
| Pipeline Stage Gantt | Timeline view of instruction stages |
| Hazard Heatmap | Density analysis of hazards across instructions |
| Register File | Current state of all 32 MIPS registers |

## 🚀 Quick Start

### Prerequisites
- Java Development Kit (JDK) 11 or newer
- `javac` and `java` available on PATH

### Build and Run

Open a terminal in the project root and run:

```bash
# 1. Compile the program
javac HazardAnalyzerComplete.java

# 2. Run the program
java HazardAnalyzerComplete
```

### Basic Usage

- Load or write MIPS code - Use "Load File" or "Load Example" buttons
- Click "Analyze Hazards" - Run the detection engine
- Explore results - Navigate through the 7 tabs to see different views
- Export report - Save analysis as TXT file

## 📁 Project Layout

```
MIPS-Pipeline-Hazard-Analyzer/
├── HazardAnalyzerComplete.java          # Main application (single-file Swing app)
├── HazardAnalyzerComplete_backup.java   # Previous version backup
├── CODE_FUNCTIONALITY.md                # Technical documentation
├── DFD_Diagram.md                       # Data flow diagrams
├── lib/                                 # Third-party JARs (empty by default)
└── README.md                            # This file
```

## 💡 Usage Tips

- Use Load Example to see a pre-written MIPS program with multiple hazard types

- The Pipeline Simulation tab lets you play/step through cycles and see hazards in real-time

- Hazard Report shows detailed descriptions of each detected hazard with instruction pairs

- Performance Metrics displays CPI, throughput, and stall percentage with color-coded assessment

- Use Export Report to save analysis results as a text file

## 📄 PDF Export (Optional)

PDF export uses iText via reflection. To enable it:

1. Download iText 7 JAR(s) and place them in the lib/ folder

2. Compile and run with classpath including lib/*

Windows:
```bash
javac -cp "lib/*" HazardAnalyzerComplete.java
java -cp ".;lib/*" HazardAnalyzerComplete
```

Linux/macOS:
```bash
javac -cp "lib/*" HazardAnalyzerComplete.java
java -cp ".:lib/*" HazardAnalyzerComplete
```

Note: If iText is not found, the app falls back to text-only exports.



## 📝 Example MIPS Code

```assembly
# Simple program with RAW hazards
add $t0, $s0, $s1    # Instruction 1 - writes $t0
lw $t1, 0($t0)       # Instruction 2 - RAW hazard (reads $t0)
add $t2, $t1, $s2    # Instruction 3 - RAW hazard (reads $t1)
beq $t0, $t3, end    # Instruction 4 - Control hazard
add $v0, $zero, $zero
end:
jr $ra
```

## 📊 Performance Metrics Explained

| Metric | Formula | Ideal Value |
|--------|---------:|:------------|
| CPI (Cycles Per Instruction) | Total Cycles ÷ Instructions | 1.0 |
| Throughput | 1 ÷ CPI | 1.0 IPC |
| Stall Percentage | (Stall Cycles ÷ Total Cycles) × 100 | 0% |

### Performance Assessment Scale

| CPI Range | Assessment |
|:---------:|:-----------|
| 1.0 | IDEAL - No pipeline stalls |
| 1.1 - 1.3 | EXCELLENT - <30% slowdown |
| 1.3 - 1.6 | GOOD - 30-60% slowdown |
| 1.6 - 2.0 | MODERATE - 60-100% slowdown |
| > 2.0 | POOR - >100% slowdown |

## 📚 Documentation

- `CODE_FUNCTIONALITY.md` - Technical specifications and algorithms

- `DFD_Diagram.md` - Data flow diagrams and system architecture

## 👥 Authors

Rameesha, Urva, Asad

Course: Computer Architecture (Semester 6)
Version: 2.1
Last Updated: May 2026

⭐ Show Your Support

If you find this project useful for learning computer architecture, please consider giving it a star on GitHub!


# MIPS Pipeline Hazard Analyzer

Professional Java Swing application for analyzing MIPS assembly code for pipeline hazards, visualizing pipeline execution, and producing detailed performance reports.

---

## Key Features
- Detects data hazards: RAW (Read-After-Write), WAR (Write-After-Read), WAW (Write-After-Write)
- Detects control hazards (branches) and reports branch-related stalls
- Pipeline stage simulation and animated visualization (IF, ID, EX, MEM, WB)
- Gantt chart and heatmap visualizations for hazard distribution
- Register file viewer showing read/write counts and last writes
- Export analysis report as text; optional PDF export via iText (when provided)

## Quick Start (Windows)
Prerequisites:
- Java Development Kit (JDK) 11 or newer installed and `javac`/`java` available on PATH

Build and run from the project folder:

```powershell
# Compile
javac HazardAnalyzerComplete.java

# Run
java HazardAnalyzerComplete
```

If you plan to use the PDF export feature, add the iText 7 JAR(s) into the `lib/` directory and compile/run including `lib/*` on the classpath:

```powershell
javac -cp "lib/*" HazardAnalyzerComplete.java
java -cp ".;lib/*" HazardAnalyzerComplete
```

## Project Layout
- `HazardAnalyzerComplete.java` — main application source file (single-file Swing app)
- `HazardAnalyzerComplete_backup.java` — previous version backup
- `lib/` — third-party JARs (empty by default)
- `README.md` — this file

## Usage Tips
- Use the **Load File** or **Load Example** buttons to populate the editor with MIPS code.
- Click **Analyze Hazards** to run detection, populate the pipeline table, and generate reports.
- Use the **Pipeline Simulation** tab to play/step through cycles and inspect hazards.
- Export the textual report via **Export Report**; for PDF export, ensure iText is available in `lib/`.

## Contributing
Contributions are welcome. Suggested improvements:
- Split the single-file app into a Maven/Gradle project for dependency management.
- Add unit tests for instruction parsing and hazard detection logic.
- Add a build script to produce an executable JAR.

If you'd like, I can scaffold a Gradle build and CI workflow (GitHub Actions) to compile on push and create artifacts.

## Troubleshooting
- "ClassNotFoundException" for iText: place the iText JAR(s) in `lib/` and run with `-cp .;lib/*`.
- GUI not appearing: ensure you run on a desktop environment (not headless) and Java can open windows.

## License & Attribution
Add a license file (`LICENSE`) to this repository. If you want MIT, Apache-2.0, or similar, tell me and I will add it.

---

Maintained by: Original authors (Rameesha, Urva, Asad) — repository mirror and packaging by the project maintainer.

For help or to request additional changes (runnable JAR, Gradle, CI), reply here and I will implement them.
