<div align="center">

# 🖥️ MIPS Pipeline Hazard Analyzer

[![Java Version](https://img.shields.io/badge/Java-11%2B-007396.svg?style=for-the-badge&logo=java&logoColor=white)](https://java.com)
[![License](https://img.shields.io/badge/License-MIT-4CAF50.svg?style=for-the-badge&logo=opensourceinitiative&logoColor=white)](LICENSE)
[![Version](https://img.shields.io/badge/Version-2.1-2196F3.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/yourusername/mips-pipeline-hazard-analyzer/releases)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-FF9800.svg?style=for-the-badge&logo=windows&logoColor=white)]()

> **A comprehensive Java-based tool for detecting, visualizing, and analyzing pipeline hazards in MIPS assembly code**

[Features](#✨-features) • [Quick Start](#🚀-quick-start) • [Screenshots](#📸-screenshots) • [Documentation](#📚-documentation) • [Contributing](#🤝-contributing)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Pipeline Hazards Detected](#-pipeline-hazards-detected)
- [System Requirements](#-system-requirements)
- [Quick Start](#-quick-start)
- [Usage Guide](#-usage-guide)
- [Screenshots](#-screenshots)
- [Performance Metrics](#-performance-metrics)
- [Project Structure](#-project-structure)
- [Technical Details](#-technical-details)
- [Documentation](#-documentation)
- [Built With](#-built-with)
- [Contributing](#-contributing)
- [Authors](#-authors)
- [License](#-license)
- [Acknowledgments](#-acknowledgments)

---

## 📖 Overview

The **MIPS Pipeline Hazard Analyzer** is a standalone Java desktop application designed for computer architecture students and professionals. It automates the detection, visualization, and analysis of pipeline hazards in MIPS assembly code, providing real-time feedback and comprehensive performance metrics.

### 🎯 Problem Solved

Traditional pipeline hazard learning requires manual, error-prone code tracing. This tool provides **instant, visual feedback** on hazard detection and performance impact, enabling students to understand complex computer architecture concepts effectively.

### ✨ Key Achievements

- ✅ Detects **5 types** of pipeline hazards (RAW, WAR, WAW, Control, Structural)
- 🎬 **Real-time pipeline simulation** with interactive animation
- 📊 **Comprehensive performance metrics** (CPI, throughput, stall analysis)
- 🎨 **Professional GUI** with responsive design and dark theme
- 📁 **Export capabilities** (TXT and PDF reports)
- 🎨 **Syntax-highlighted code editor**
- 🔍 **Real-time register tracking** and visualization
- 📈 **3500+ lines** of clean, modular Java code

---

## ✨ Features

### Core Analysis Features

| Feature | Description | Status |
|---------|-------------|--------|
| **5 Hazard Types** | RAW, WAR, WAW, Control, Structural | ✅ Complete |
| **Pipeline Simulation** | Cycle-by-cycle with real-time animation | ✅ Complete |
| **Performance Metrics** | CPI, throughput, stall percentage | ✅ Complete |
| **Register Tracking** | Monitor all 32 MIPS registers | ✅ Complete |
| **Syntax Highlighting** | Real-time MIPS code coloring | ✅ Complete |

### User Interface Features

| Feature | Description | Status |
|---------|-------------|--------|
| **Dark Theme** | Professional gradient design | ✅ Complete |
| **7 Info Tabs** | Pipeline, Hazards, Metrics, Visualizations | ✅ Complete |
| **File Operations** | Load/Save/Export functionality | ✅ Complete |
| **Interactive Animation** | Play/Pause/Step/Reset controls | ✅ Complete |
| **Responsive Design** | Scales from 800×600 to 4K | ✅ Complete |

### Export Features

| Format | Content | Status |
|--------|---------|--------|
| **TXT Report** | Complete analysis with timestamp | ✅ Complete |
| **PDF Report** | Professional formatted document | ✅ Optional* |

*PDF export requires iText7 library (see installation guide)

---

## 🚨 Pipeline Hazards Detected

### 1. RAW Hazard (Read After Write)
```assembly
add $t0, $t1, $t2    # Writes $t0 (finishes in cycle 5)
lw $t3, 0($t0)       # Reads $t0 (needs in cycle 2) ⚠️ HAZARD
```

Impact: 2-3 cycle stall | Solution: Data forwarding or NOP insertion

### 2. WAW Hazard (Write After Write)
```assembly
add $s0, $s1, $s2    # Writes $s0 first
sub $s0, $s3, $s4    # Writes $s0 again ⚠️ HAZARD
```

Impact: 0-1 cycle stall | Solution: Register renaming

### 3. WAR Hazard (Write After Read)
```assembly
lw $a0, 0($a1)       # Reads $a0 in cycle 2
add $a0, $a2, $a3    # Writes $a0 in cycle 3 ⚠️ HAZARD
```

Impact: 1 cycle stall | Solution: Register renaming

### 4. Control Hazard (Branch)
```assembly
beq $t0, $t1, target # Branch decision delayed
add $t2, $t3, $t4    # Should this execute? ⚠️ HAZARD
```

Impact: 2-3 cycle stall | Solution: Branch prediction

### 5. Structural Hazard (Resource Conflict)
```assembly
lw $k0, 0($k1)       # Memory read
sw $k2, 4($k3)       # Memory write (same cycle) ⚠️ HAZARD
```

Impact: 1 cycle stall | Solution: Dual-port memory

---

## 💻 System Requirements

### Minimum Requirements

| Component | Requirement |
|-----------|-------------|
| Operating System | Windows 7+, macOS 10.12+, Linux (Ubuntu 16.04+) |
| Java Version | Java SE 11 or higher |
| RAM | 512 MB minimum (1 GB recommended) |
| Disk Space | 50 MB free space |
| Display | 1400×900 resolution minimum |

### Recommended Setup

| Component | Recommendation |
|-----------|----------------|
| OS | Windows 10+ or Ubuntu 20.04+ |
| Java | Java SE 15+ |
| RAM | 2 GB |
| Display | 1920×1080 or higher |

---

## 🚀 Quick Start

### Step 1: Install Java

#### Windows
```powershell
# Download from: https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html
# Add to PATH: C:\Program Files\Java\jdk-11.0.x\bin

# Verify installation
java -version
javac -version
```

#### macOS
```bash
# Using Homebrew
brew install java11

# Verify
java -version
```

#### Linux (Ubuntu)
```bash
sudo apt-get update
sudo apt-get install openjdk-11-jdk

# Verify
java -version
```

### Step 2: Clone & Compile
```bash
# Clone the repository
git clone https://github.com/yourusername/mips-pipeline-hazard-analyzer.git

# Navigate to project directory
cd mips-pipeline-hazard-analyzer

# Compile the application
javac src/HazardAnalyzerComplete.java

# Run the application
java -cp src HazardAnalyzerComplete
```

### Step 3: With PDF Export (Optional)
```bash
# Download iText7 and place JARs in lib/ folder

# Compile with libraries
javac -cp "lib/*" src/HazardAnalyzerComplete.java

# Run with classpath
java -cp "src:lib/*" HazardAnalyzerComplete      # Linux/macOS
java -cp "src;lib\*" HazardAnalyzerComplete      # Windows
```

### Step 4: Verify Installation

✅ Success Indicators:

- Professional splash screen appears
- Main window opens at 1400×900 resolution
- All buttons are responsive
- Code editor accepts text input
- "Load Example" displays sample code

---

## 📖 Usage Guide

### Basic Workflow

```
┌─────────────────────────────────────────────────────────────┐
│                     USAGE WORKFLOW                           │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   1. WRITE CODE         2. ANALYZE         3. REVIEW        │
│      ┌────────┐            ┌─────┐            ┌─────┐       │
│      │  Type  │───────────▶│Click│───────────▶│ 7   │       │
│      │  MIPS  │            │Analyze│           │Tabs │       │
│      │  Code  │            └─────┘            └─────┘       │
│      └────────┘                                │            │
│                                                ▼            │
│   4. EXPORT                          5. ANIMATE             │
│      ┌────────┐                         ┌─────┐             │
│      │  Save  │◀────────────────────────│Play │             │
│      │ TXT/PDF│                         │Step │             │
│      └────────┘                         └─────┘             │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Step-by-Step Example

#### Step 1: Enter or Load Code

**Option A - Type manually:**

```assembly
add $t0, $s0, $s1    # Instruction 1
lw $t1, 0($t0)       # Instruction 2 (RAW hazard!)
add $t2, $t1, $s2    # Instruction 3 (RAW hazard!)
beq $t0, $t3, end    # Instruction 4 (Control hazard)
```

**Option B - Click "Load Example"** for pre-written demo code

**Option C - Click "Load File"** to open .asm or .s files

#### Step 2: Click "Analyze Hazards"

The analysis engine will:

- Parse MIPS instructions
- Detect all hazards
- Simulate pipeline execution
- Calculate performance metrics
- Populate all 7 result tabs

#### Step 3: Explore Results

| Tab | What You'll See |
|-----|------------------|
| Pipeline Simulation | Cycle-by-cycle execution table with animation |
| Hazard Report | Detailed list of all detected hazards |
| Performance Metrics | CPI, throughput, stall percentage |
| Hazard Visualization | Color-coded hazard grid |
| Pipeline Stage Gantt | Timeline view of instruction stages |
| Hazard Heatmap | Density analysis of hazards |
| Register File | Current state of all 32 MIPS registers |

#### Step 4: Export Results

- Export Report → Save as .txt file
- Export PDF → Generate professional PDF report (requires iText)

---

## Project Structure

```
HazardAnalyzerProject/
├─ lib/                 # Third-party jars (iText, etc.)
├─ src/                 # (optional) source folder
├─ HazardAnalyzerComplete.java
├─ HazardAnalyzerComplete_backup.java
├─ README.md
└─ .gitignore
```

---

## Technical Details

- Single-file Java Swing application implementing:
	- MIPS instruction parser and validator
	- Hazard detection engine (RAW/WAR/WAW/CONTROL/STRUCTURAL)
	- Pipeline simulator and visualization components
	- Export utilities (text + optional PDF via iText)

---

## 📚 Documentation

In-repo docs:

- `CODE_FUNCTIONALITY.md` — high-level design and module descriptions
- `COMPLETE_PROJECT_DOCUMENTATION.md` — extended documentation and diagrams
- `DFD_Diagram.md` — data flow diagrams

---

## 🛠️ Built With

- Java SE 11+
- Swing (Java desktop UI)
- Optional: iText 7 (PDF generation)

---

## 🤝 Contributing

Contributions are welcome — please open issues or PRs. Suggested improvements:

- Convert to a Maven/Gradle project for proper dependency management
- Add unit tests for parsing and hazard detection
- Create GitHub Actions CI to compile on push and generate artifacts

If you'd like, I can scaffold Gradle and a CI workflow.

---

## 🧑‍💻 Authors

- Rameesha (lead)
- Urva
- Asad

Maintainer: Repository mirror and packaging by the project maintainer.

---

## 📄 License

This project uses the MIT License — add a `LICENSE` file to the repository to apply it.

---

## 🙏 Acknowledgments

- Thanks to the Computer Architecture course contributors and testers.

---

For additional help (build scripts, runnable JAR, CI), reply here and I will implement it.

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
