# MIPS Pipeline Hazard Analyzer - Complete Project Documentation

## Version Information
- **Project Version:** 2.1
- **Last Updated:** May 2026
- **Document Version:** 1.0
- **Authors:** Rameesha, Urva, Asad
- **Course:** Computer Architecture (Semester 6)
- **Institution:** Comsats University Islamabad


## Executive Summary

The **MIPS Pipeline Hazard Analyzer** is a comprehensive, standalone Java-based application designed for computer architecture students and professionals. This tool automates the detection, visualization, and analysis of pipeline hazards in MIPS assembly code. 

**Key Achievements:**
- ✅ Detects 5 types of pipeline hazards (RAW, WAR, WAW, Control, Structural)
- ✅ Real-time pipeline simulation with interactive animation
- ✅ Comprehensive performance metrics (CPI, throughput, stall analysis)
- ✅ Professional GUI with responsive design and dark theme
- ✅ Export capabilities (TXT, PDF)
- ✅ Syntax-highlighted code editor
- ✅ Real-time register tracking and visualization
- ✅ 3000+ lines of clean, modular Java code

**Problem Solved:**
Traditional pipeline hazard learning requires manual, error-prone code tracing. This tool provides instant, visual feedback on hazard detection and performance impact, enabling students to understand complex concepts effectively.

---

## Project Overview

### 1.1 What is a Pipeline Hazard?

In modern CPUs, instruction pipelines execute multiple instructions simultaneously through different stages:

```
Stage 1: Instruction Fetch (IF)      - Retrieve instruction from memory
Stage 2: Instruction Decode (ID)     - Decode instruction and read registers
Stage 3: Execute (EX)                - Perform arithmetic/logic operations
Stage 4: Memory Access (MEM)         - Read/write data from/to memory
Stage 5: Write Back (WB)             - Write results back to registers
```

**Pipeline Hazards** are situations where the next instruction cannot proceed normally, causing pipeline stalls.

### 1.2 Types of Hazards Detected

#### **1. RAW Hazard (Read After Write)**
```
Cycle:  1    2    3    4    5    6
Instr1: IF   ID   EX   MEM  WB   
Instr2:      IF   ID   EX   MEM  WB
                  ❌ Reads $t0 before Instr1 writes

Example:
  add $t0, $t1, $t2    ← Writes $t0 (finishes in cycle 5)
  lw  $t3, 0($t0)      ← Reads $t0 (needs in cycle 2)
  Problem: $t0 contains stale data
  Solution: Wait 2-3 cycles or use forwarding
```

#### **2. WAW Hazard (Write After Write)**
```
Example:
  add $s0, $s1, $s2    ← Writes $s0 first
  sub $s0, $s3, $s4    ← Writes $s0 again
  Problem: Order must be preserved if out-of-order execution
  Solution: Enforce write-back ordering
```

#### **3. WAR Hazard (Write After Read)**
```
Example:
  lw  $a0, 0($a1)      ← Reads $a0 in cycle 2
  add $a0, $a2, $a3    ← Writes $a0 in cycle 3
  Problem: Register changes before read completes
  Solution: Register renaming or stalling
```

#### **4. Control Hazard (Branch)**
```
Example:
  beq $t0, $t1, target ← Branch decision delayed to MEM stage
  add $t2, $t3, $t4    ← Should this instruction execute?
  Problem: CPU doesn't know which instruction to fetch next
  Solution: Branch prediction or pipeline flush (3-4 cycle stall)
```

#### **5. Structural Hazard (Resource Conflict)**
```
Example:
  lw  $k0, 0($k1)      ← Memory read in cycle 4
  sw  $k2, 4($k3)      ← Memory write in cycle 4
  Problem: Both need memory unit simultaneously
  Solution: Dual-port memory or stall (1 cycle)
```

### 1.3 Project Objectives

| Objective | Status | Details |
|-----------|--------|---------|
| Detect all 5 hazard types | ✅ Complete | Automatic detection with cycle accuracy |
| Simulate pipeline execution | ✅ Complete | Real-time, cycle-by-cycle visualization |
| Calculate performance metrics | ✅ Complete | CPI, throughput, stall analysis |
| Professional GUI | ✅ Complete | Dark theme, responsive, 7 info tabs |
| Report generation | ✅ Complete | TXT and PDF export options |
| Register tracking | ✅ Complete | Monitor access patterns |
| Educational value | ✅ Complete | Immediate feedback for learning |

---

## System Architecture

### 2.1 High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     USER INTERFACE LAYER                    │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Swing GUI Components (JFrame, JPanel, JTabbedPane)  │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  Left Panel: Code Editor        Right Panel: Results  │   │
│  │  • Syntax Highlighting          • Pipeline Simulator  │   │
│  │  • File Operations              • Hazard Report       │   │
│  │  • Code Examples                • Performance Metrics │   │
│  │  • Export Buttons               • Visualization       │   │
│  │                                 • Register Viewer     │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                  ANALYSIS ENGINE LAYER                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Core Analysis Components                            │   │
│  ├──────────────────────────────────────────────────────┤   │
│  │  1. Instruction Parser       3. Pipeline Simulator   │   │
│  │     • Regex-based parsing       • Cycle tracking     │   │
│  │     • Type detection            • Stage assignment   │   │
│  │     • Dependency extraction     • Hazard mapping     │   │
│  │                                                      │   │
│  │  2. Hazard Detector          4. Performance Engine   │   │
│  │     • RAW/WAR/WAW logic         • CPI calculation    │   │
│  │     • Control detection         • Throughput calc    │   │
│  │     • Structural detection      • Stall analysis     │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    DATA MODEL LAYER                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  • Instruction Objects   • Hazard Objects           │   │
│  │  • Register State Maps   • Pipeline Cycle Data      │   │
│  │  • Performance Metrics   • Analysis Results         │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Component Interaction Flow

```
User Input (Code)
     ↓
[Code Parser] → Instruction List → [Hazard Detector]
     ↓                                   ↓
[Syntax Highlighter]          [Hazard List] → [Pipeline Simulator]
     ↓                                            ↓
[Display in Editor]      [Performance Calculator] ← [Pipeline Cycles]
                               ↓
                        [Report Generator]
                               ↓
                    [Display Results] ← [Register Tracker]
                               ↓
                    [Export Options (TXT/PDF)]
```

---

## Installation & Setup Guide

### 3.1 System Requirements

**Minimum Requirements:**
- Operating System: Windows 7+, macOS 10.12+, Linux (Ubuntu 16.04+)
- Java Version: Java SE 11 or higher
- RAM: 512 MB minimum (1 GB recommended)
- Disk Space: 50 MB free space
- Display: 1400×900 resolution minimum (recommended: 1920×1080)

**Recommended Setup:**
- OS: Windows 10+ or Ubuntu 20.04+
- Java: Java SE 15+
- RAM: 2 GB
- Display: 1920×1080 or higher

### 3.2 Java Installation

#### Windows:
```powershell
# Check if Java is installed
java -version

# If not installed, download from:
# https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html

# Extract to: C:\Program Files\Java\jdk-11.0.x
# Add to PATH: C:\Program Files\Java\jdk-11.0.x\bin

# Verify installation
java -version
javac -version
```

#### macOS:
```bash
# Check Java version
java -version

# If not installed, use Homebrew:
brew install java11

# Or download from Oracle JDK:
# https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html

# Verify
java -version
```

#### Linux (Ubuntu):
```bash
sudo apt-get update
sudo apt-get install openjdk-11-jdk

# Verify
java -version
javac -version
```

### 3.3 Project Setup

#### Step 1: Extract Project Files
```
Extract to: w:\Sem 6\CA\HazardAnalyzerProject\
Directory Structure:
├── HazardAnalyzerComplete.java
├── HazardAnalyzerComplete_backup.java
├── CODE_FUNCTIONALITY.md
├── DFD_Diagram.md
├── Research_Paper.md
├── COMPLETE_PROJECT_DOCUMENTATION.md (this file)
├── lib/
│   └── [optional iText7 JAR files for PDF export]
└── RESEARCH PAPER/
    └── LAB_FINAL_FA23-BCS-(...).docx
```

#### Step 2: Compilation

**Without PDF Export Support (Recommended for Quick Start):**
```bash
cd w:\Sem 6\CA\HazardAnalyzerProject
javac HazardAnalyzerComplete.java
```

**With PDF Export Support (Optional - requires iText7):**
```bash
# Download iText7 libraries and place in lib/ folder

# Then compile:
javac -cp lib/* HazardAnalyzerComplete.java
```

#### Step 3: Running the Application

**Basic Execution:**
```bash
cd w:\Sem 6\CA\HazardAnalyzerProject
java HazardAnalyzerComplete
```

**With Classpath (if using libraries):**
```bash
java -cp .:lib/* HazardAnalyzerComplete
# On Windows:
java -cp .;lib\* HazardAnalyzerComplete
```

#### Step 4: Verify Installation

✅ **Success Indicators:**
- Professional splash screen appears with loading progress
- Main window opens at 1400×900 resolution
- All buttons are responsive (clickable)
- Code editor accepts text input
- "Load Example" displays sample code

### 3.4 Optional: PDF Export Setup

To enable PDF export functionality:

1. Download iText7 Core from: https://mvnrepository.com/artifact/com.itextpdf/itext7-core

2. Extract JAR files to: `w:\Sem 6\CA\HazardAnalyzerProject\lib\`

3. Compile with libraries:
```bash
javac -cp lib/* HazardAnalyzerComplete.java
```

4. Run with classpath:
```bash
java -cp .;lib\* HazardAnalyzerComplete
```

---

## User Guide & Features

### 4.1 Main Application Window

**[SCREENSHOT PLACEHOLDER 1: Main Application GUI]**
*Insert screenshot showing:*
- *Full main window with both left and right panels*
- *Code editor with example MIPS code*
- *Right panel showing Pipeline Simulation tab*
- *All buttons and controls visible*
- *Status bar at bottom*

```
┌────────────────────────────────────────────────────────────────┐
│  MIPS Hazard Analyzer v3.0                          _ □ ✕     │
├─────────────────────────────────┬──────────────────────────────┤
│                                 │                              │
│  LEFT PANEL: Code Input         │  RIGHT PANEL: Results        │
│                                 │                              │
│  [Load File] [Example]          │  ╔════════════════════════╗  │
│  [Analyze] [Clear] [Export TXT] │  ║ Pipeline  Hazard  Perf ║  │
│  [Export PDF] [Benchmark]       │  ╠════════════════════════╣  │
│                                 │  ║ [Results Tab Content]  ║  │
│  ┌─────────────────────────┐    │  ║                        ║  │
│  │ # MIPS Assembly Code    │    │  ║ (Dynamic content      ║  │
│  │ add $t0, $t1, $t2      │    │  ║  based on analysis)    ║  │
│  │ lw  $t3, 0($t0)        │    │  ║                        ║  │
│  │ sw  $t3, 4($sp)        │    │  ║                        ║  │
│  │ ...                     │    │  ║                        ║  │
│  │                         │    │  ╚════════════════════════╝  │
│  └─────────────────────────┘    │                              │
│                                 │                              │
├─────────────────────────────────┴──────────────────────────────┤
│ Ready. Load or write MIPS code and click Analyze.            │
└────────────────────────────────────────────────────────────────┘
```

### 4.2 Feature List & Buttons

#### Left Panel: Code Input Features

| Button | Function | Shortcut | Description |
|--------|----------|----------|-------------|
| **Load File** | Opens file dialog | - | Load MIPS code from .asm or .s files |
| **Load Example** | Loads demo code | - | Insert pre-written code with all hazard types |
| **Analyze Hazards** | Runs analysis | Enter | Parse code, detect hazards, simulate pipeline |
| **Clear** | Reset all | - | Clear code, results, and reset animation |
| **Export Report** | Save as TXT | - | Export analysis as plain text file |
| **Export PDF** | Save as PDF | - | Generate professional PDF report (if iText7 installed) |
| **Benchmark** | Batch analysis | - | Compare multiple MIPS files (research mode) |

#### Right Panel: Tabbed Results Interface

| Tab | Purpose | Key Elements |
|-----|---------|--------------|
| **Pipeline Simulation** | Visual pipeline execution | Play/Pause/Step/Reset buttons, Cycle table, Speed slider |
| **Hazard Report** | Detected hazards list | Hazard descriptions, instruction pairs, summary stats |
| **Performance Metrics** | CPI & throughput | Performance table, interpretation, breakdown by hazard type |
| **Hazard Visualization** | Graphical grid view | Color-coded cells, legend, interactive tooltips |
| **Pipeline Stage Gantt** | Timeline view | Instruction bars, stage colors, zoom controls |
| **Hazard Heatmap** | Density analysis | Heatmap grid, color scale, statistical summary |
| **Register File** | Register state | All 32 registers, values, read/write counts, last written |

### 4.3 Step-by-Step Usage Example

#### Scenario: Analyzing Simple MIPS Code

**Step 1: Enter or Load Code**
```
Method A: Type manually in editor
Method B: Click "Load File" → Select .asm file
Method C: Click "Load Example" → Auto-populate demo code

Example code:
  add $t0, $s0, $s1      ; Instruction 1
  lw $t1, 0($t0)         ; Instruction 2 (RAW hazard!)
  add $t2, $t1, $s2      ; Instruction 3 (RAW hazard!)
  beq $t0, $t3, end      ; Instruction 4 (Control hazard)
```

**Step 2: Click "Analyze Hazards"**
- Compiler parses instructions
- System detects hazards
- Pipeline simulation runs
- Results populate all tabs
- Status bar updates with summary

**Step 3: Review Results**

*Pipeline Tab:*
```
Cycle | IF      | ID      | EX      | MEM     | WB      | Hazards
------|---------|---------|---------|---------|---------|----------
0     | add     | -       | -       | -       | -       | -
1     | lw      | add     | -       | -       | -       | RAW ⚠️
2     | add     | lw      | add     | -       | -       | RAW ⚠️
3     | beq     | add     | lw      | add     | -       | -
4     | [stall] | beq     | add     | lw      | add     | CONTROL ⚠️
5     | -       | [stall] | beq     | add     | lw      | -
```

*Hazard Report Tab:*
```
DETECTED HAZARDS (3 total)
═══════════════════════════════════════════════════════════

Hazard #1: RAW (Read After Write)
  Instruction 1: add $t0, $s0, $s1
  Instruction 2: lw $t1, 0($t0)
  Issue: Instruction 2 reads $t0 in ID stage (cycle 2)
         Instruction 1 writes $t0 in WB stage (cycle 5)
  Impact: 2-cycle stall (waiting for $t0 to be ready)
  Solution: Insert NOP or use data forwarding

Hazard #2: RAW (Read After Write)
  ...
```

*Performance Tab:*
```
PERFORMANCE METRICS
═══════════════════════════════════════════════════════════

Total Instructions:     4
Total Cycles:           7
Stall Cycles:           3
CPI (Cycles Per Instr): 1.75
Throughput:             0.57 instructions/cycle
Stall Percentage:       43%

Hazard Breakdown:
  RAW Hazards:          2 (50%)
  Control Hazards:      1 (25%)
  Structural Hazards:   0 (0%)
  WAW Hazards:          0 (0%)
  WAR Hazards:          0 (0%)

Performance Assessment: MODERATE
  CPI of 1.75 indicates 75% performance impact.
  Consider: data forwarding, instruction reordering, or NOP insertion.
```

**Step 4: Animation (Optional)**
- Click "Play" to animate pipeline execution
- Watch instructions flow through stages
- Hazards highlighted in red
- Click "Step" for manual control
- Speed slider adjusts animation rate

**Step 5: Export Results**
- Click "Export Report" → Save as .txt
- Click "Export PDF" → Save as professional PDF
- Use exported files for:
  - Assignment submission
  - Research papers
  - Code review
  - Documentation

### 4.4 Input Code Guidelines

**Valid MIPS Instructions:**
```mips
# R-Type (3 operands)
add $t0, $s0, $s1
sub $t1, $t2, $t3
and $t4, $t5, $t6

# I-Type (2 registers + immediate)
addi $t0, $s0, 100
andi $t1, $t2, 0xFF

# Load/Store
lw $t0, 0($sp)
sw $t1, 4($fp)

# Branch
beq $t0, $t1, label
bne $s0, $s1, target

# Jump
jal $ra
jr $ra
```

**Invalid / Unsupported:**
```
- Assembly directives (.data, .text, .globl)
- Undefined instructions
- Invalid syntax (will be skipped silently)
- Comments (automatically removed)
```

**Code Entry Tips:**
- One instruction per line
- Comments after # will be stripped
- Labels ending with : are ignored
- Whitespace is flexible
- Empty lines are skipped
- Case insensitive (add = ADD = Add)

---

## Technical Implementation

### 5.1 Technology Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| **Language** | Java | SE 11+ | Core implementation |
| **GUI Framework** | Swing | javax.swing.* | User interface |
| **Graphics** | Java 2D | java.awt.* | Drawing, animations |
| **File I/O** | Java NIO | java.nio.file | File operations |
| **Threading** | Java Threads | Swing Timer | Animation control |
| **PDF Export** | iText7 (optional) | 7.1+ | Report generation |
| **Build** | javac | JDK 11+ | Compilation |
| **Execution** | java | JRE 11+ | Runtime |

### 5.2 Compilation & Build Process

**Single-File Compilation:**
```bash
javac HazardAnalyzerComplete.java
```

**Result:**
- Creates: `HazardAnalyzerComplete.class`
- Also creates inner classes: 
  - `HazardAnalyzerComplete$Instruction.class`
  - `HazardAnalyzerComplete$Hazard.class`
  - `HazardAnalyzerComplete$RoundedPanel.class`
  - (and 10+ other inner classes)

**File Statistics:**
- Source code: ~3500 lines
- Comments: ~500 lines
- Blank lines: ~300 lines
- Compiled size: ~200 KB

### 5.3 Class Hierarchy

```
HazardAnalyzerComplete (Main Class, 3500+ lines)
│
├─ Inner Classes:
│  ├─ Instruction (Data Model)
│  │  ├─ original: String
│  │  ├─ name: String
│  │  ├─ type: String
│  │  ├─ writeReg: Integer
│  │  └─ readRegs: Set<Integer>
│  │
│  ├─ Hazard (Data Model)
│  │  ├─ type: String (RAW, WAR, WAW, CONTROL, STRUCTURAL)
│  │  ├─ instr1Idx: int
│  │  ├─ instr2Idx: int
│  │  └─ description: String
│  │
│  ├─ RoundedPanel (JPanel)
│  │  ├─ cornerRadius: int
│  │  └─ backColor: Color
│  │
│  ├─ GradientButton (JButton)
│  │  ├─ gradient rendering
│  │  └─ hover effects
│  │
│  ├─ HazardTableCellRenderer (TableCellRenderer)
│  │  ├─ Color coding logic
│  │  └─ Hazard highlighting
│  │
│  ├─ MIPSSyntaxDocument (DefaultStyledDocument)
│  │  ├─ Syntax highlighting
│  │  └─ Real-time formatting
│  │
│  ├─ ProfessionalSplashScreen (JWindow)
│  │  ├─ Progress bar
│  │  └─ Loading messages
│  │
│  └─ ThemeManager (Utility)
│     └─ Static color definitions
│
└─ Key Methods (100+):
   ├─ main(String[] args)
   ├─ createAndShowGUI()
   ├─ analyzeCode()
   ├─ parseInstructions(String)
   ├─ detectHazards(List<Instruction>)
   ├─ simulatePipeline(List, List)
   ├─ updatePerformanceTab(double, double, double)
   ├─ generateReport(List, List)
   └─ [... 100+ other methods ...]
```

---

## Code Structure & Components

### 6.1 Core Analysis Methods

#### Method: `parseInstructions(String sourceCode)`

**Purpose:** Convert MIPS assembly code into Instruction objects

**Input:** Raw MIPS assembly code as string

**Output:** List of Instruction objects

**Algorithm:**
```
1. Split source code by lines
2. For each line:
   a. Remove comments (everything after #)
   b. Trim whitespace
   c. Skip empty lines and labels
   d. Call parseSingleInstruction(line)
   e. If valid, add to list
3. Return instruction list
```

**Code:**
```java
private List<Instruction> parseInstructions(String sourceCode) {
    List<Instruction> instructions = new ArrayList<>();
    String[] lines = sourceCode.split("\\r?\\n");
    int lineNum = 0;
    
    for (String line : lines) {
        lineNum++;
        // Remove comments
        int commentIndex = line.indexOf('#');
        if (commentIndex != -1) {
            line = line.substring(0, commentIndex);
        }
        line = line.trim();
        
        // Skip empty lines and labels
        if (line.isEmpty() || line.endsWith(":")) {
            continue;
        }
        
        Instruction instr = parseSingleInstruction(line, lineNum);
        if (instr != null && instr.isValid) {
            instructions.add(instr);
        }
    }
    return instructions;
}
```

#### Method: `detectHazards(List<Instruction> instructions)`

**Purpose:** Identify all pipeline hazards

**Input:** List of parsed Instruction objects

**Output:** List of Hazard objects

**Algorithm:**
```
1. Initialize hazard list
2. For i = 0 to instructions.length-1:
   a. For j = i+1 to i+3 (lookahead 3 instructions):
      - Check RAW:
        If instr[i].writeReg in instr[j].readRegs AND (j-i) <= 2:
          Add RAW hazard
      - Check WAW:
        If instr[i].writeReg == instr[j].writeReg:
          Add WAW hazard
      - Check WAR:
        If instr[i].readRegs contains instr[j].writeReg:
          Add WAR hazard
      - Check CONTROL:
        If instr[i] is BRANCH:
          Add CONTROL hazard for cycle i+1
      - Check STRUCTURAL:
        If instr[i] and instr[j] both MEMORY access:
          Add STRUCTURAL hazard
3. Return hazard list
```

**Complexity:** O(n²) where n = number of instructions

**Code Snippet:**
```java
private List<Hazard> detectHazards(List<Instruction> instructions) {
    List<Hazard> hazards = new ArrayList<>();
    
    for (int i = 0; i < instructions.size(); i++) {
        Instruction instr1 = instructions.get(i);
        
        // Lookahead up to 3 instructions
        for (int j = i + 1; j <= i + 3 && j < instructions.size(); j++) {
            Instruction instr2 = instructions.get(j);
            int distance = j - i;
            
            // RAW Hazard
            if (instr1.writeReg != null && 
                instr2.readRegs.contains(instr1.writeReg) && 
                distance <= 2) {
                Hazard h = new Hazard();
                h.type = "RAW";
                h.instr1Idx = i;
                h.instr2Idx = j;
                h.description = String.format(
                    "RAW: Instr %d writes $%d, Instr %d reads $%d",
                    i+1, instr1.writeReg, j+1, instr1.writeReg);
                hazards.add(h);
            }
            
            // WAW Hazard
            if (instr1.writeReg != null && 
                instr1.writeReg.equals(instr2.writeReg)) {
                Hazard h = new Hazard();
                h.type = "WAW";
                h.instr1Idx = i;
                h.instr2Idx = j;
                hazards.add(h);
            }
            
            // ... WAR, CONTROL, STRUCTURAL checks ...
        }
    }
    return hazards;
}
```

#### Method: `simulatePipeline(List<Instruction> instructions, List<Hazard> hazards)`

**Purpose:** Create cycle-by-cycle pipeline execution table

**Input:** Instruction list, Hazard list

**Output:** Populates pipelineTable with execution data

**Algorithm:**
```
1. Calculate total cycles = instructions.size() + 4
2. Create table rows (one per cycle)
3. For each cycle c (0 to total-1):
   a. For each stage s (IF, ID, EX, MEM, WB):
      - Determine which instruction is in stage s at cycle c
      - Instruction = c - s (if positive and valid)
   b. Find hazards at this cycle
   c. Populate table row with instruction names and hazards
4. Return filled table
```

**Table Display:**
```
Cycle | IF    | ID    | EX    | MEM   | WB    | Hazards
0     | add   | -     | -     | -     | -     | -
1     | lw    | add   | -     | -     | -     | RAW ⚠️
2     | sw    | lw    | add   | -     | -     | -
3     | beq   | sw    | lw    | add   | -     | CONTROL ⚠️
4     | [END] | beq   | sw    | lw    | add   | -
5     | -     | [END] | beq   | sw    | lw    | -
6     | -     | -     | [END] | beq   | sw    | -
7     | -     | -     | -     | [END] | beq   | -
8     | -     | -     | -     | -     | [END] | -
```

---

## Data Flow & Processing

### 7.1 Complete Data Flow Diagram

**[DIAGRAM PLACEHOLDER 2: Data Flow Diagram Level 1]**
*Insert comprehensive DFD showing:*
- *User as external entity*
- *6 numbered processes (Parse, Detect, Simulate, Calculate, Report, Display)*
- *5 data stores (Code, Instructions, Hazards, Register State, Metrics)*
- *Labeled data flows between components*
- *Color coding: Blue (external), Orange (data stores), Green (processes), Purple (output)*

```
SYSTEM INPUTS:
  ├─ User loads MIPS assembly code
  ├─ User clicks "Analyze Hazards" button
  └─ User selects output format (TXT/PDF)

PROCESSING FLOW:
  1. Parse Instructions
     Input: Raw assembly code (string)
     Process: Regex parsing, line-by-line analysis
     Output: Instruction objects with metadata
     Storage: Instruction Cache (in-memory list)

  2. Detect Hazards
     Input: Instruction list
     Process: Pairwise comparison O(n²)
     Output: Hazard objects with types and locations
     Storage: Hazard Database (in-memory list)

  3. Simulate Pipeline
     Input: Instructions + Hazards
     Process: Cycle-by-cycle stage assignment
     Output: Pipeline table with cycle data
     Storage: Pipeline table model (DefaultTableModel)

  4. Calculate Performance
     Input: Total cycles, stall cycles, instructions
     Process: CPI = cycles/instructions, etc.
     Output: Performance metrics
     Storage: Performance metrics variables

  5. Generate Report
     Input: All analysis data
     Process: Format text report with details
     Output: Formatted report text
     Storage: Report text area

  6. Display Results
     Input: All processed data
     Process: Update UI components
     Output: Visual display
     Storage: GUI components

SYSTEM OUTPUTS:
  ├─ On-screen visualization
  ├─ Exported TXT file
  └─ Exported PDF file (optional)

DATA PERSISTENCE:
  • No persistent storage (all in-memory)
  • Results cleared when "Clear" button clicked
  • Can export to files for permanent storage
```

### 7.2 Register State Tracking

**Tracking Maps:**
```java
// Maps register numbers to values
private Map<Integer, Integer> registerValues;

// Count how many times each register is read
private Map<Integer, Integer> registerReadCounts;

// Count how many times each register is written
private Map<Integer, Integer> registerWriteCounts;

// Track which instruction last wrote to register
private Map<Integer, String> registerLastWritten;
```

**Update Process:**
```
1. During parsing, extract register accesses
2. For each READ operation:
   ├─ Increment registerReadCounts[regNum]
   └─ Update register table display

3. For each WRITE operation:
   ├─ Increment registerWriteCounts[regNum]
   ├─ Update registerValues[regNum]
   ├─ Update registerLastWritten[regNum]
   └─ Update register table display

4. Update register viewer table:
   For each of 32 registers:
   ├─ Register number
   ├─ Register name ($t0, $s1, etc.)
   ├─ Current value (hex format)
   ├─ Last instruction to write
   ├─ Read count
   └─ Write count
```

---

## Hazard Detection System

### 8.1 RAW Hazard Detection

**Definition:** Read After Write - An instruction reads a register before a previous instruction finishes writing to it.

**Detection Logic:**
```
For instructions i and j where j > i:
  IF instruction[i] writes to register R
  AND instruction[j] reads from register R
  AND (j - i) <= 2 (within pipeline depth)
  THEN: RAW Hazard detected
```

**Example:**
```mips
Cycle:  1  2  3  4  5
add     IF ID EX MEM WB   ← Writes $t0 in cycle 5
lw      IF ID EX MEM      ← Reads $t0 in cycle 2
         ❌ Hazard: $t0 not ready
```

**Impact:**
- Stall cycles: 1-2
- Solution: Data forwarding or wait

### 8.2 WAW Hazard Detection

**Definition:** Write After Write - Two instructions write to the same register.

**Detection Logic:**
```
For instructions i and j where j > i:
  IF instruction[i].writeReg == instruction[j].writeReg
  THEN: WAW Hazard detected
```

**Example:**
```mips
add $s0, $s1, $s2  ← First write to $s0
sub $s0, $s3, $s4  ← Second write to $s0
 ❌ Hazard: Order must be preserved
```

**Impact:**
- Stall cycles: 0-1 (depends on CPU design)
- Solution: Avoid consecutive writes or use register renaming

### 8.3 WAR Hazard Detection

**Definition:** Write After Read - An instruction writes to a register before a previous instruction finishes reading it.

**Detection Logic:**
```
For instructions i and j where j > i:
  IF instruction[j] writes to register R
  AND instruction[i] reads from register R
  AND (j - i) <= 2
  THEN: WAR Hazard detected
```

**Example:**
```mips
lw $a0, 0($a1)     ← Reads $a0 in cycle 2
add $a0, $a2, $a3  ← Writes $a0 in cycle 3
 ❌ Hazard: Register changes before read completes
```

**Impact:**
- Stall cycles: 1
- Solution: Register renaming or reordering

### 8.4 Control Hazard Detection

**Definition:** Branch instruction causes uncertainty about next instruction to fetch.

**Detection Logic:**
```
For instruction i:
  IF instruction[i] is a BRANCH instruction (beq, bne, etc.)
  THEN: Control Hazard detected for cycle = i+1
```

**Example:**
```mips
beq $t0, $t1, target  ← Branch decision made in MEM stage (cycle 4)
add $t2, $t3, $t4     ← May need to be flushed if branch taken
 ❌ Hazard: Wrong instructions may be fetched
```

**Impact:**
- Stall cycles: 2-3 (pipeline flush penalty)
- Solution: Branch prediction or delay slots

**Detected Branch Instructions:**
- beq, bne (branch equal/not equal)
- bgtz, blez (branch greater/less than zero)
- bgez, bltz (branch greater/less than equal zero)
- j, jal, jr (jumps)

### 8.5 Structural Hazard Detection

**Definition:** Multiple instructions need the same hardware resource simultaneously.

**Detection Logic:**
```
For instructions i and j where j = i+1:
  IF instruction[i] is MEMORY access (load/store)
  AND instruction[j] is MEMORY access
  AND (j - i) <= 2
  THEN: Structural Hazard detected
```

**Example:**
```mips
lw $k0, 0($k1)    ← Accesses memory
sw $k2, 4($k3)    ← Also accesses memory
 ❌ Hazard: Both need memory unit simultaneously
```

**Impact:**
- Stall cycles: 1
- Solution: Dual-port memory or reorder instructions

**Memory Instructions:**
- Load: lw, lh, lhu, lb, lbu
- Store: sw, sh, sb

### 8.6 Hazard Summary Statistics

**Output Display:**
```
═══════════════════════════════════════════════════
                 HAZARD STATISTICS
═══════════════════════════════════════════════════

Total Hazards Found:    5

Breakdown by Type:
  • RAW Hazards:        2 (40%)
  • WAW Hazards:        0 (0%)
  • WAR Hazards:        1 (20%)
  • Control Hazards:    1 (20%)
  • Structural Hazards: 1 (20%)

Average Hazards per Instruction: 1.25

Most Critical Hazard: Control (2-3 cycle stall)
Optimization Priority: High

Recommendations:
  1. Use branch prediction to reduce control stalls
  2. Apply data forwarding to eliminate RAW stalls
  3. Reorder instructions to reduce memory conflicts
  4. Consider VLIW or superscalar architecture
```

---

## Performance Metrics Calculation

### 9.1 Key Metrics Explained

#### **CPI (Cycles Per Instruction)**

**Formula:**
```
CPI = Total Cycles / Total Instructions
```

**Calculation Example:**
```
Total Instructions = 5
Stall Cycles (from hazards) = 2
Pipeline stages = 5
Basic cycles = Instructions + (pipeline depth - 1)
            = 5 + 4 = 9
Adjusted cycles = 9 + stall cycles = 9 + 2 = 11

Wait, the algorithm is: Total Cycles = Instructions + Stall Cycles + 4

So: Total Cycles = 5 + 2 + 4 = 11
CPI = 11 / 5 = 2.2
```

**Interpretation:**
```
CPI Value | Performance | Assessment
-----------|-------------|-------------
1.0       | Perfect     | No stalls, ideal conditions
1.1-1.3   | Excellent   | Minimal hazards, < 30% slowdown
1.3-1.6   | Good        | Moderate hazards, 30-60% slowdown
1.6-2.0   | Moderate    | Significant hazards, 60-100% slowdown
> 2.0     | Poor        | Critical hazards, severe slowdown
```

#### **Throughput**

**Formula:**
```
Throughput = Instructions per Cycle = 1 / CPI
```

**Example:**
```
CPI = 1.6
Throughput = 1 / 1.6 = 0.625 instructions/cycle

Interpretation:
  • Ideal throughput = 1 instruction/cycle
  • This code achieves 0.625 instr/cycle
  • Performance loss = 37.5%
```

#### **Stall Percentage**

**Formula:**
```
Stall % = (Stall Cycles / Total Cycles) × 100
```

**Example:**
```
Total Cycles = 11
Stall Cycles = 2
Stall % = (2 / 11) × 100 = 18.2%

This means 18.2% of pipeline cycles are wasted waiting for hazards
```

### 9.2 Performance Assessment Categories

```
╔═══════════════════════════════════════════════════════════════╗
║         PERFORMANCE ASSESSMENT SCALE                         ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  🟢 IDEAL (CPI = 1.0)                                        ║
║     No pipeline stalls, perfect execution                    ║
║     Typical for simple, hazard-free code                    ║
║                                                               ║
║  🟢 EXCELLENT (CPI < 1.3)                                    ║
║     Minimal performance impact (< 30% slowdown)              ║
║     Few hazards, mostly handled by forwarding                ║
║                                                               ║
║  🟡 GOOD (1.3 ≤ CPI < 1.6)                                   ║
║     Moderate performance impact (30-60% slowdown)            ║
║     Multiple hazards, but manageable                        ║
║                                                               ║
║  🟠 MODERATE (1.6 ≤ CPI < 2.0)                              ║
║     Significant performance impact (60-100% slowdown)        ║
║     Serious hazard issues, needs optimization                ║
║                                                               ║
║  🔴 POOR (CPI ≥ 2.0)                                         ║
║     Critical performance problems (>100% slowdown)           ║
║     Major hazard density, severe optimization needed         ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```

### 9.3 Performance Metrics Display Format

**[SCREENSHOT PLACEHOLDER 3: Performance Metrics Tab]**
*Insert screenshot showing:*
- *Formatted metrics table*
- *CPI value with interpretation color*
- *Throughput in instructions per cycle*
- *Stall percentage with bar chart*
- *Hazard breakdown pie chart or table*
- *Performance assessment text*

```
╔═══════════════════════════════════════════════════════════════╗
║           PERFORMANCE ANALYSIS REPORT                         ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║  EXECUTION STATISTICS                                        ║
║  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  ║
║                                                               ║
║  Total Instructions Executed:    5                           ║
║  Total Pipeline Cycles:          11                          ║
║  Pipeline Stall Cycles:          2                           ║
║                                                               ║
║  PERFORMANCE METRICS                                         ║
║  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  ║
║                                                               ║
║  CPI (Cycles Per Instruction):   2.20  [MODERATE]           ║
║  Throughput (IPC):               0.45 instructions/cycle     ║
║  Stall Percentage:               18.2%                       ║
║                                                               ║
║  HAZARD BREAKDOWN                                            ║
║  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  ║
║                                                               ║
║  RAW Hazards:        2 (40%)  ████████░░░░░░░░░░░░░░░░░░   ║
║  Control Hazards:    1 (20%)  ████░░░░░░░░░░░░░░░░░░░░░░░ ║
║  Structural Hazards: 1 (20%)  ████░░░░░░░░░░░░░░░░░░░░░░░ ║
║  WAW Hazards:        1 (20%)  ████░░░░░░░░░░░░░░░░░░░░░░░ ║
║  WAR Hazards:        0 (0%)   ░░░░░░░░░░░░░░░░░░░░░░░░░░░░ ║
║                                                               ║
║  ASSESSMENT & RECOMMENDATIONS                               ║
║  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━  ║
║                                                               ║
║  Performance Category: MODERATE                              ║
║  ⚠️  Code has significant hazard density                     ║
║                                                               ║
║  TOP OPTIMIZATION OPPORTUNITIES:                             ║
║  1. Data Forwarding: Eliminate 40% of RAW stalls            ║
║  2. Branch Prediction: Reduce 20% control stalls             ║
║  3. Instruction Reordering: Separate memory operations      ║
║  4. NOP Insertion: Stagger register dependencies             ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```

---

## UI/UX Design

### 10.1 Visual Design Principles

**Theme:**
- **Background:** Dark gradient (Blue #1a2a6c → Red #b21f1f)
- **Text Color:** White on dark (high contrast)
- **Accent Colors:**
  - Primary: Light Blue (#0078D7)
  - Secondary: Cyan (#0099BC)
- **UI Components:**
  - Rounded corners (15px radius)
  - Gradient buttons
  - Card-style panels
  - Modern typography (Poppins, Segoe UI)

**Color Palette:**
```
Background:         #1a2a6c (Dark Blue)
Gradient End:       #b21f1f (Dark Red)
Primary Accent:     #0078D7 (Light Blue)
Secondary Accent:   #0099BC (Cyan)
Text:               #FFFFFF (White)
Disabled:           #808080 (Gray)
Hazard (RAW):       #FF9999 (Light Red)
Hazard (Control):   #6699FF (Light Blue)
Success:            #4CAF50 (Green)
Warning:            #FFC107 (Amber)
```

### 10.2 Responsive Design

**Scaling Algorithm:**
```java
updateScaleFactor(int windowWidth, int windowHeight) {
    float scaleX = windowWidth / 1400.0f;      // Base width
    float scaleY = windowHeight / 900.0f;      // Base height
    currentScale = Math.min(scaleX, scaleY);   // Take minimum
    currentScale = Math.max(0.8f, Math.min(currentScale, 1.5f));  // Clamp
    
    refreshAllFonts();  // Update all components
}
```

**Font Scaling:**
```
Base Window: 1400 × 900
Scale Range: 0.8x to 1.5x

Example 1: Window → 700 × 450
  Scale = min(700/1400, 450/900) = 0.5 (clamped to 0.8)
  Button font = 12pt × 0.8 = 9.6pt

Example 2: Window → 2100 × 1350
  Scale = min(2100/1400, 1350/900) = 1.5
  Button font = 12pt × 1.5 = 18pt
```

### 10.3 Component Layout

**Left Panel (40% width):**
```
┌─────────────────────────────────┐
│ MIPS Assembly Code Input        │  Header
├─────────────────────────────────┤
│ [Load File] [Example] [Analyze] │  Toolbar
│ [Clear] [Export TXT] [Export PDF│  (can wrap)
│ [Benchmark]                      │
├─────────────────────────────────┤
│                                 │
│  [Code Editor Area]             │  Main content
│  (Scrollable text pane)         │  (90% height)
│  Syntax highlighted MIPS code   │
│                                 │
│                                 │
│                                 │
├─────────────────────────────────┤
└─────────────────────────────────┘
```

**Right Panel (60% width):**
```
┌──────────────────────────────────┐
│ Pipeline  Hazard  Performance... │  Tab bar
├──────────────────────────────────┤
│                                  │
│  [Tab 1 Content]                │  Active tab
│  Dynamic based on selected tab   │  (90% height)
│  Scrollable if needed            │
│  Color-coded elements            │
│                                  │
│                                  │
│                                  │
├──────────────────────────────────┤
└──────────────────────────────────┘
```

### 10.4 Interactive Elements

**Buttons:**
```
┌──────────────────────┐
│  Load File  ▼        │  Hover state: Darker gradient
├──────────────────────┤
│ Modern gradient bg   │
│ Rounded corners      │
│ Tooltip on hover     │
│ Click feedback       │
└──────────────────────┘
```

**Tables:**
```
┌─────────┬─────────┬─────────┬─────────────┐
│ Cycle   │ IF      │ ID      │ Hazards     │
├─────────┼─────────┼─────────┼─────────────┤
│ 0       │ add     │ -       │ -           │  Light gray
│ 1       │ lw      │ add     │ RAW ⚠️      │  Yellow (current)
│ 2       │ sw      │ lw      │ -           │  Light gray
│ 3       │ beq     │ sw      │ CONTROL ⚠️  │  Alternating
└─────────┴─────────┴─────────┴─────────────┘
```

**Text Areas:**
```
Dark background (#141519)
Light text (#C8DCFF)
Monospace font
Line numbers (optional)
Syntax highlighting (code area)
Read-only (report area)
```

---

## Example Analysis & Results

### 11.1 Simple Example: 3-Instruction Program

**Input Code:**
```mips
# Simple arithmetic with load
add $t0, $s0, $s1
lw  $t1, 0($t0)
add $t2, $t1, $s2
```

**Expected Analysis:**

**Parsed Instructions:**
```
Instruction 1: add $t0, $s0, $s1
  Type: R-Type
  Write Register: $t0 (8)
  Read Registers: {$s0 (16), $s1 (17)}

Instruction 2: lw $t1, 0($t0)
  Type: Load
  Write Register: $t1 (9)
  Read Registers: {$t0 (8)}

Instruction 3: add $t2, $t1, $s2
  Type: R-Type
  Write Register: $t2 (10)
  Read Registers: {$t1 (9), $s2 (18)}
```

**Detected Hazards:**
```
Hazard 1: RAW (Read After Write)
  Cycle: 2
  Instruction Pair: 1 → 2
  Issue: Instr 2 reads $t0 (written by Instr 1)
  Severity: HIGH (2-cycle dependency)

Hazard 2: RAW (Read After Write)
  Cycle: 3
  Instruction Pair: 2 → 3
  Issue: Instr 3 reads $t1 (written by Instr 2)
  Severity: HIGH (RAW + load latency)
```

**Pipeline Simulation:**
```
Cycle | IF  | ID  | EX  | MEM | WB  | Hazards
------|-----|-----|-----|-----|-----|----------
0     | add | -   | -   | -   | -   | -
1     | lw  | add | -   | -   | -   | RAW ⚠️
2     | add | lw  | add | -   | -   | RAW ⚠️
3     | -   | add | lw  | add | -   | -
4     | -   | -   | add | lw  | add | -
5     | -   | -   | -   | add | lw  | -
6     | -   | -   | -   | -   | add | -
```

**Performance Metrics:**
```
Total Instructions: 3
Total Cycles: 7
Stall Cycles: 2

CPI: 7 / 3 = 2.33
Throughput: 0.43 instr/cycle
Stall %: 28.6%

Assessment: MODERATE
Bottleneck: Load latency causing cascading stalls
```

**Recommendations:**
```
1. Insert NOP instructions between dependent ops
2. Use data forwarding to eliminate stalls
3. Reorder instructions to separate dependencies
4. Prefetch data into cache

Optimized code:
  add $t0, $s0, $s1
  nop                  # Wait for forwarding
  lw $t1, 0($t0)
  nop
  add $t2, $t1, $s2
```

### 11.2 Complex Example: All Hazard Types

**[SCREENSHOT PLACEHOLDER 4: Complete Example Analysis]**
*Insert 2-3 screenshots showing:*
- *Pipeline tab with full simulation table (8+ cycles)*
- *Hazard Report tab with 4-5 different hazard types listed*
- *Performance Metrics tab with CPI, throughput, breakdown charts*

**Input Code:**
```mips
# Complete hazard demonstration
add $t0, $s0, $s1       # Instruction 1
lw  $t1, 0($t0)         # Instruction 2 - RAW hazard with Instr 1
add $t2, $t1, $s2       # Instruction 3 - RAW hazard with Instr 2
sub $t0, $t3, $t4       # Instruction 4 - WAW hazard with Instr 1
beq $t0, $t1, target    # Instruction 5 - Control hazard
sw  $t2, 4($sp)         # Instruction 6 - Structural hazard
lw  $t3, 8($sp)         # Instruction 7 - Structural hazard
target:
add $v0, $zero, $zero   # Instruction 8
jr  $ra                 # Instruction 9
```

**Analysis Output:**

**Hazards Detected: 7 Total**
```
1. RAW: Instr 2 reads $t0 from Instr 1
2. RAW: Instr 3 reads $t1 from Instr 2
3. WAW: Instr 4 writes $t0 (also written by Instr 1)
4. CONTROL: Instr 5 is branch
5. STRUCTURAL: Instr 6 (store) and Instr 7 (load) both memory access
6. (Additional hazards from cascading effects)
```

**Performance Summary:**
```
Total Instructions: 9
Total Cycles: 14
Stall Cycles: 5

CPI: 14 / 9 = 1.56
Throughput: 0.64 instructions/cycle
Stall %: 35.7%

Assessment: GOOD (but could be better)
```

---

## API Reference

### 12.1 Public Methods

#### **Main Entry Point**
```java
public static void main(String[] args)
```
- **Purpose:** Application entry point
- **Parameters:** Command line arguments (unused)
- **Behavior:** Shows splash screen, initializes GUI
- **Returns:** void

#### **GUI Initialization**
```java
private void createAndShowGUI()
```
- **Purpose:** Create main application window
- **Responsibilities:**
  - Initialize frame (1400×900)
  - Create left/right panels
  - Setup tabbed interface
  - Add event listeners
- **Returns:** void

#### **Code Analysis**
```java
private void analyzeCode()
```
- **Purpose:** Main analysis orchestrator
- **Process:**
  1. Get code from editor
  2. Parse instructions
  3. Detect hazards
  4. Simulate pipeline
  5. Calculate metrics
  6. Generate reports
  7. Update displays
- **Returns:** void

### 12.2 Data Access Methods

#### **Instruction Parsing**
```java
private List<Instruction> parseInstructions(String sourceCode)
```
- **Input:** Raw MIPS assembly code
- **Output:** List of Instruction objects
- **Complexity:** O(n) where n = number of lines

#### **Hazard Detection**
```java
private List<Hazard> detectHazards(List<Instruction> instructions)
```
- **Input:** List of parsed Instruction objects
- **Output:** List of detected Hazard objects
- **Complexity:** O(n²) where n = number of instructions

#### **Pipeline Simulation**
```java
private void simulatePipeline(List<Instruction> instructions, 
                               List<Hazard> hazards)
```
- **Input:** Instructions and Hazards
- **Behavior:** Populates pipelineTable
- **Side Effect:** Updates pipelineModel

### 12.3 UI Update Methods

#### **Performance Display**
```java
private void updatePerformanceTab(double cpi, double throughput, 
                                   double stallPercentage)
```
- **Parameters:**
  - `cpi`: Cycles per instruction
  - `throughput`: Instructions per cycle
  - `stallPercentage`: Stall percentage (0-100)
- **Behavior:** Formats and displays metrics in performance tab
- **Returns:** void

#### **Report Generation**
```java
private void generateReport(List<Instruction> instructions, 
                             List<Hazard> hazards)
```
- **Purpose:** Create formatted analysis report
- **Behavior:** Populates reportArea with formatted text
- **Includes:** Instruction listing, hazard details, recommendations
- **Returns:** void

### 12.4 File Operations

#### **Load File**
```java
private void loadFile()
```
- **Behavior:** Opens file chooser, loads selected .asm or .s file
- **Updates:** codeArea
- **Exceptions:** IOException handled with dialog
- **Returns:** void

#### **Export Report**
```java
private void exportReport()
```
- **Behavior:** Saves analysis report as plain text (.txt)
- **Filename:** Hazard_Analysis_Report.txt (default)
- **Content:** Full analysis with timestamp
- **Returns:** void

#### **Export PDF**
```java
private void exportToPDF()
```
- **Behavior:** Generates formatted PDF report using iText7
- **Requirements:** iText7 library in classpath
- **Fallback:** Shows error dialog if iText7 not available
- **Returns:** void

---

## Troubleshooting Guide

### 13.1 Common Issues & Solutions

**Issue 1: Application won't start**

```
Problem:  java: command not found (or similar)
Solution:
  1. Verify Java is installed: java -version
  2. If not: Download from java.com
  3. Add Java to PATH:
     - Windows: System Properties → Environment Variables
     - macOS: Edit ~/.bash_profile or ~/.zshrc
     - Linux: export PATH=$PATH:/usr/lib/jvm/java-11/bin
  4. Restart terminal and try again
```

**Issue 2: Compilation errors**

```
Problem:  javac: command not found
Solution:
  1. Download JDK (not JRE): https://jdk.java.net/
  2. Extract JDK
  3. Set JAVA_HOME:
     - Windows: set JAVA_HOME=C:\path\to\jdk
     - Linux/Mac: export JAVA_HOME=/path/to/jdk
  4. Add to PATH: %JAVA_HOME%\bin (Windows) or $JAVA_HOME/bin (Linux/Mac)
  5. Retry: javac HazardAnalyzerComplete.java
```

**Issue 3: GUI doesn't appear (blank window)**

```
Problem:  Application starts but window is empty
Solution:
  1. Check screen resolution (minimum 1400×900)
  2. Maximize window to force repaint
  3. Resize window manually
  4. If persists: Restart application
  5. Last resort: Rebuild with: javac -g HazardAnalyzerComplete.java
```

**Issue 4: Code analysis produces no results**

```
Problem:  Click "Analyze" but no output
Solution:
  1. Verify code is valid MIPS:
     - Check syntax (one instruction per line)
     - Use Load Example to verify format
  2. Check for:
     - Empty editor
     - Comments only
     - Invalid instructions
  3. Try example code: Click "Load Example"
  4. Check console for error messages
  5. If still fails: Restart application
```

**Issue 5: PDF export not working**

```
Problem:  "PDF export not available" message
Solution:
  1. This is expected if iText7 not installed
  2. To enable PDF:
     a. Download iText7 from:
        https://mvnrepository.com/artifact/com.itextpdf/itext7-core
     b. Extract JAR to: lib/itext7-core.jar
     c. Recompile: javac -cp lib/* HazardAnalyzerComplete.java
     d. Run: java -cp .;lib\* HazardAnalyzerComplete
  3. For now: Use "Export Report" for TXT format
```

**Issue 6: Incorrect hazard detection**

```
Problem:  Hazards not detected or false positives
Solution:
  1. Verify instruction parsing:
     - Check "Instruction cache" (internal)
     - Use Load Example to test
  2. Common parsing issues:
     - Wrong register notation (use $t0, not t0)
     - Unsupported instruction (check supported list)
     - Label issues (labels ending with : are ignored)
  3. Report issue if pattern still fails
```

**Issue 7: Application runs slowly**

```
Problem:  GUI lag or slow analysis
Solution:
  1. For large code (50+ instructions):
     - This is expected O(n²) hazard detection
     - Normal: May take 1-2 seconds
  2. Performance tips:
     - Close other applications
     - Ensure 1+ GB RAM available
     - Use smaller code chunks for testing
  3. For production large-scale use:
     - Consider implementing hazard caching
     - Add parallel processing
     - Submit optimization request
```

### 13.2 Performance Optimization

**If application runs slowly:**

1. **Clear animation queue:**
   - Click "Stop" if animation running
   - Clear results: Click "Clear"

2. **Manage code size:**
   - Split large programs into sections
   - Test each section separately

3. **System resources:**
   - Close background applications
   - Allocate more RAM to JVM:
     ```bash
     java -Xmx2G HazardAnalyzerComplete
     ```

4. **GUI optimization:**
   - Minimize non-active tabs
   - Close Register Viewer if not needed

---

## Future Enhancements

### 14.1 Planned Features

**Version 3.0+ Roadmap:**

```
┌─────────────────────────────────────────────────────────┐
│ Q3 2026: Data Forwarding Simulation                     │
│ ├─ Simulate hardware forwarding units                  │
│ ├─ Show which hazards can be eliminated               │
│ ├─ Compare CPI with/without forwarding                │
│ └─ Educational: Demonstrate forwarding benefits       │
├─────────────────────────────────────────────────────────┤
│ Q4 2026: Branch Prediction                             │
│ ├─ Implement simple predictor (Always Not Taken)       │
│ ├─ Add advanced predictors (2-bit counter, etc.)      │
│ ├─ Show prediction accuracy metrics                    │
│ └─ Compare performance with/without prediction        │
├─────────────────────────────────────────────────────────┤
│ 2027: Superscalar Support                              │
│ ├─ Simulate 2-issue/4-issue processors                │
│ ├─ Model instruction-level parallelism                │
│ ├─ Multiple pipeline simulation                        │
│ └─ Compare CPI across CPU designs                     │
├─────────────────────────────────────────────────────────┤
│ 2027: Code Optimization Engine                         │
│ ├─ Automatic instruction reordering                    │
│ ├─ NOP insertion for hazard avoidance                  │
│ ├─ Unrolling loop optimization                         │
│ └─ Generate optimized MIPS code                        │
├─────────────────────────────────────────────────────────┤
│ 2027: Extended MIPS Support                            │
│ ├─ MIPS-32 and MIPS-64 extensions                      │
│ ├─ Floating-point operations                           │
│ ├─ Advanced branch types                               │
│ └─ System instructions                                 │
├─────────────────────────────────────────────────────────┤
│ 2027: Web Version                                       │
│ ├─ JavaScript/React implementation                     │
│ ├─ Cloud-based analysis                               │
│ ├─ Shared analysis database                            │
│ └─ Collaborative learning features                     │
└─────────────────────────────────────────────────────────┘
```

### 14.2 Feature Request Examples

**Requested Feature 1: Hazard Mitigation Suggestions**
```
Current: "RAW hazard detected"
Future:  "RAW hazard: Fix by:
          1. Insert 2 NOP instructions (insert code)
          2. Use data forwarding (change processor)
          3. Reorder: move independent instructions (rewrite code)
          → Cost/benefit analysis for each option"
```

**Requested Feature 2: Custom Pipeline**
```
Current: Fixed 5-stage pipeline (IF, ID, EX, MEM, WB)
Future:  Allow configuration:
         - Pipeline depth (3, 5, 7, 9+ stages)
         - Instruction issue width (1, 2, 4)
         - Forwarding availability
         - Branch prediction strategy
         - Memory latency
```

**Requested Feature 3: Batch Analysis**
```
Current: Analyze one program at a time
Future:  Compare multiple programs:
         - Load 5+ .asm files
         - Run analysis on all
         - Generate comparative report
         - Identify best/worst programs
         - Benchmark against reference
```

### 14.3 Technical Improvements

**Code Quality:**
- [ ] Refactor into separate classes (Model, View, Controller)
- [ ] Extract inner classes to separate files
- [ ] Add comprehensive unit tests
- [ ] Implement design patterns (MVC, Observer)
- [ ] Add JavaDoc for all public methods

**Performance:**
- [ ] Implement hazard detection caching
- [ ] Add parallel processing for large programs
- [ ] Optimize memory usage for big simulations
- [ ] Implement incremental analysis

**Architecture:**
- [ ] Support plugin architecture for new hazard types
- [ ] Modular UI components
- [ ] Configurable themes and color schemes
- [ ] International language support

---

## Appendices

### Appendix A: Supported MIPS Instruction Set

**R-Type Instructions (3 operands: rd, rs, rt)**
```
add      - Add: rd = rs + rt
addu     - Add unsigned: rd = rs + rt (no overflow)
sub      - Subtract: rd = rs - rt
subu     - Subtract unsigned: rd = rs - rt (no overflow)
and      - Bitwise AND: rd = rs & rt
or       - Bitwise OR: rd = rs | rt
xor      - Bitwise XOR: rd = rs ^ rt
nor      - Bitwise NOR: rd = ~(rs | rt)
slt      - Set less than: rd = (rs < rt) ? 1 : 0
sltu     - Set less than unsigned: rd = (rs < rt unsigned) ? 1 : 0
sll      - Shift left logical: rd = rt << sa
srl      - Shift right logical: rd = rt >> sa (logical)
sra      - Shift right arithmetic: rd = rt >> sa (arithmetic)
jr       - Jump register: PC = rs
```

**I-Type Instructions (2 operands + immediate)**
```
addi     - Add immediate: rt = rs + immediate
addiu    - Add immediate unsigned: rt = rs + immediate
andi     - AND immediate: rt = rs & immediate
ori      - OR immediate: rt = rs | immediate
xori     - XOR immediate: rt = rs ^ immediate
lui      - Load upper immediate: rt = immediate << 16
slti     - Set less than immediate: rt = (rs < immediate) ? 1 : 0
sltiu    - Set less than immediate unsigned
```

**Load/Store Instructions**
```
lw       - Load word: rt = memory[base + offset]
lh       - Load halfword (signed)
lhu      - Load halfword (unsigned)
lb       - Load byte (signed)
lbu      - Load byte (unsigned)
sw       - Store word: memory[base + offset] = rt
sh       - Store halfword
sb       - Store byte
```

**Branch Instructions**
```
beq      - Branch equal: if rs == rt goto label
bne      - Branch not equal: if rs != rt goto label
bgtz     - Branch greater than zero: if rs > 0 goto label
blez     - Branch less than or equal zero: if rs <= 0 goto label
bgez     - Branch greater than or equal zero: if rs >= 0 goto label
bltz     - Branch less than zero: if rs < 0 goto label
```

**Jump Instructions**
```
j        - Jump: PC = address
jal      - Jump and link: $ra = PC + 4; PC = address
jr       - Jump register: PC = rs
```

### Appendix B: Register Names and Numbers

**MIPS Register Conventions:**
```
Number | Name | Purpose | Preserved
--------|------|---------|----------
0      | $zero| Always 0| N/A
1      | $at  | Assembler temp | No
2-3    | $v0-$v1 | Return values | No
4-7    | $a0-$a3 | Arguments | No
8-15   | $t0-$t7 | Temporaries | No
16-23  | $s0-$s7 | Saved | Yes
24-25  | $t8-$t9 | Temporaries | No
26-27  | $k0-$k1 | Kernel | N/A
28     | $gp  | Global ptr | Yes
29     | $sp  | Stack ptr | Yes
30     | $fp  | Frame ptr | Yes
31     | $ra  | Return addr | Yes
```

**Register Groups:**
- **Temporary:** $t0-$t9 (not preserved across calls)
- **Saved:** $s0-$s7 (preserved across calls)
- **Arguments:** $a0-$a3 (passed to functions)
- **Return:** $v0-$v1 (returned from functions)
- **Special:** $zero, $ra, $sp, $fp, $gp

### Appendix C: Hazard Summary Quick Reference

**[DIAGRAM PLACEHOLDER 5: Hazard Quick Reference Chart]**
*Insert visual quick reference showing:*
- *5 hazard types with icons*
- *Example code for each*
- *Detection pattern*
- *Stall cycles*
- *Common solutions*

```
╔═══════════════════════════════════════════════════════════════╗
║              HAZARD DETECTION QUICK REFERENCE                ║
╠═══════════════════════════════════════════════════════════════╣
║                                                               ║
║ TYPE: RAW (Read After Write)                                 ║
║ Pattern: Write reg R, then read reg R within 2 cycles       ║
║ Stalls:  1-2 cycles                                          ║
║ Solution: Wait, forwarding, or reorder                       ║
║ Example: add $t0, ...  /  lw $t1, 0($t0)                     ║
║                                                               ║
║ TYPE: WAW (Write After Write)                                ║
║ Pattern: Write reg R, then write reg R again                 ║
║ Stalls:  0-1 cycles (depends on CPU)                         ║
║ Solution: Register renaming or enforce ordering             ║
║ Example: add $s0, ...  /  sub $s0, ...                       ║
║                                                               ║
║ TYPE: WAR (Write After Read)                                 ║
║ Pattern: Read reg R, then write reg R within 2 cycles       ║
║ Stalls:  1 cycle                                             ║
║ Solution: Register renaming or reorder                       ║
║ Example: lw $a0, ...  /  add $a0, ...                        ║
║                                                               ║
║ TYPE: CONTROL (Branch)                                       ║
║ Pattern: Branch instruction                                  ║
║ Stalls:  2-3 cycles (pipeline flush)                         ║
║ Solution: Branch prediction or delay slots                   ║
║ Example: beq $t0, $t1, label  /  add $t2, ...               ║
║                                                               ║
║ TYPE: STRUCTURAL (Resource Conflict)                         ║
║ Pattern: Two memory ops within 2 cycles                      ║
║ Stalls:  1 cycle                                             ║
║ Solution: Separate ops or dual-port memory                   ║
║ Example: lw $k0, ...  /  sw $k2, ...                         ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```

### Appendix D: Performance Assessment Guide

**Using CPI to Assess Code Quality:**

```
╔═════════════════════════════════════════════════════════════════╗
║           CPI-BASED PERFORMANCE ASSESSMENT                     ║
╠═════════════════════════════════════════════════════════════════╣
║                                                                 ║
║ CPI = 1.0   ████████░░░░░░░░░░░░  IDEAL                        ║
║             • Zero pipeline stalls                              ║
║             • Perfect code scheduling                           ║
║             • Expected for simple code with no dependencies    ║
║             • Achievable with: (rare - ideal case)              ║
║                                                                 ║
║ CPI = 1.1   ████████░░░░░░░░░░░░  EXCELLENT                    ║
║             • Minimal stalls (10% penalty)                      ║
║             • Minor hazards handled by forwarding              ║
║             • Achievable with: data forwarding                  ║
║                                                                 ║
║ CPI = 1.3   ██████████░░░░░░░░░░  GOOD                         ║
║             • Some stalls (30% penalty)                         ║
║             • Multiple RAW hazards                              ║
║             • Achievable with: careful scheduling               ║
║                                                                 ║
║ CPI = 1.6   ████████████░░░░░░░░  MODERATE                     ║
║             • Significant stalls (60% penalty)                  ║
║             • Many hazards present                              ║
║             • Needs optimization                                ║
║             • Achievable with: instruction reordering           ║
║                                                                 ║
║ CPI = 2.0   ████████████████░░░░  POOR                         ║
║             • Severe stalls (100% penalty)                      ║
║             • Critical hazard density                           ║
║             • Requires major optimization                       ║
║             • Achievable with: completely unoptimized code      ║
║                                                                 ║
║ CPI > 2.5   ████████████████████  CRITICAL                     ║
║             • Extreme stalls (>150% penalty)                    ║
║             • Code needs complete rewrite                       ║
║             • Throughput severely degraded                      ║
║             • Suggests algorithmic inefficiency                 ║
║                                                                 ║
╚═════════════════════════════════════════════════════════════════╝
```

### Appendix E: Glossary of Terms

```
Term              | Definition
─────────────────|────────────────────────────────────────
Instruction      | Single MIPS command (add, lw, beq, etc.)
Pipeline         | Parallel execution of multiple instructions
Stage            | One phase of pipeline (IF, ID, EX, MEM, WB)
Hazard           | Dependency preventing normal pipeline flow
Stall            | Pipeline pause waiting for hazard resolution
CPI              | Cycles Per Instruction (performance metric)
Throughput       | Instructions per cycle (IPC)
Forwarding       | Passing result directly to next instruction
Branch           | Conditional jump instruction
Register         | Fast storage location (32 in MIPS)
RAW              | Read After Write hazard
WAW              | Write After Write hazard
WAR              | Write After Read hazard
Control Hazard   | Uncertainty from branch instruction
Structural Hazard| Resource conflict (e.g., memory access)
Immediate        | Constant value in instruction
Opcode           | Instruction operation code
Register Number  | Integer 0-31 identifying register
Assembly         | Low-level programming language
Simulation       | Model of processor behavior
```

### Appendix F: File Manifest

**Project Directory Structure:**
```
w:\Sem 6\CA\HazardAnalyzerProject\
│
├─ HazardAnalyzerComplete.java          [Main application, 3500 lines]
│  └─ Compiled to: HazardAnalyzerComplete.class (+ 14 inner classes)
│
├─ HazardAnalyzerComplete_backup.java   [Backup copy]
│
├─ CODE_FUNCTIONALITY.md                [Technical documentation]
│  └─ Detailed method documentation
│  └─ Algorithm explanations
│  └─ Data structure descriptions
│
├─ DFD_Diagram.md                       [Data Flow Diagram]
│  └─ Mermaid diagram code
│  └─ Diagram rendering instructions
│  └─ Component descriptions
│
├─ Research_Paper.md                    [Academic paper, 600+ lines]
│  └─ Abstract, introduction, methodology
│  └─ Implementation details
│  └─ Results and conclusions
│
├─ COMPLETE_PROJECT_DOCUMENTATION.md    [This file]
│  └─ Comprehensive guide (1500+ lines)
│  └─ User guide, technical details
│  └─ Troubleshooting, appendices
│
├─ lib/                                 [Library directory]
│  └─ (Empty by default)
│  └─ Optional: iText7 JARs for PDF
│
├─ RESEARCH PAPER/                      [Additional documents]
│  └─ LAB_FINAL_FA23-BCS-(...).docx
│
└─ Screenshots/ (to be created)
   ├─ GUI_Main_Window.png               [Main application window]
   ├─ Hazard_Report.png                 [Hazard detection output]
   ├─ Performance_Metrics.png           [CPI and throughput display]
   ├─ Pipeline_Simulation.png           [Pipeline table animation]
   ├─ Heatmap_Visualization.png         [Hazard density heatmap]
   ├─ Register_Viewer.png               [Register file display]
   └─ Code_Syntax_Highlighting.png      [Editor with formatting]
```

**File Statistics:**
```
Component                  | Lines  | Size
─────────────────────────|--------|──────────
HazardAnalyzerComplete.java| 3500  | 120 KB
CODE_FUNCTIONALITY.md      | 400   | 20 KB
DFD_Diagram.md            | 150   | 8 KB
Research_Paper.md         | 600   | 40 KB
This Documentation        | 1500  | 80 KB
─────────────────────────|--------|──────────
TOTAL                     | 6150  | 268 KB
```

---

## Additional Resources

### Recommended Learning Materials

**Books:**
- "Computer Organization and Design" by Patterson & Hennessy
- "Computer Architecture: A Quantitative Approach" by Hennessy & Patterson
- "MIPS Assembly Language Programming" by Robert Britton

**Online Resources:**
- MIPS Reference Manual: https://www.mips.com/
- Stack Overflow MIPS tag: https://stackoverflow.com/questions/tagged/mips
- Computer Architecture courses on Coursera, edX, etc.

**Related Tools:**
- MARS (MIPS Assembler and Runtime Simulator)
- SPIM (MIPS Simulator)
- QtSpim (Qt version of SPIM)

### Contact & Support

**Project Authors:**
- Rameesha
- Urva
- Asad

**Course Information:**
- Course: Computer Architecture (CA)
- Semester: 6
- Institution: [Your University Name]

**For Bug Reports or Feature Requests:**
Please provide:
1. Version number: 2.1
2. Operating system: Windows/macOS/Linux
3. Java version: (output of `java -version`)
4. Detailed description of issue
5. Steps to reproduce
6. Example MIPS code if applicable

---

## Document Revision History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | May 2026 | Initial complete documentation |
| | | • 1500+ lines of detailed guide |
| | | • All 16 sections complete |
| | | • Placeholders for 5 screenshots |
| | | • Full API reference |
| | | • Troubleshooting guide |

---

## License & Attribution

This project is created for educational purposes as part of the Computer Architecture course (Semester 6).

**Authors:** Rameesha, Urva, Asad

**Institution:** [Your University Name]

**Date:** May 2026

**Version:** 2.1

All intellectual property, source code, and documentation are the property of the authors.

---

**END OF DOCUMENTATION**

---

## Screenshot & Diagram Insertion Guide

This documentation includes spaces for 5 required screenshots/diagrams:

1. **Figure 1 (Placeholder 1):** Main Application GUI
   - **Location:** Section 4.1 (Main Application Window)
   - **Dimensions:** 1400×900 pixels
   - **Content:** Full application window with code editor and results panel
   - **Filename suggestion:** GUI_Main_Window.png

2. **Figure 2 (Placeholder 2):** Data Flow Diagram Level 1
   - **Location:** Section 7.1 (Complete Data Flow Diagram)
   - **Dimensions:** 1200×800 pixels
   - **Content:** Mermaid DFD with processes, data stores, and flows
   - **Filename suggestion:** DFD_Level1.png

3. **Figure 3 (Placeholder 3):** Performance Metrics Tab
   - **Location:** Section 9.3 (Performance Metrics Display Format)
   - **Dimensions:** 800×600 pixels
   - **Content:** Performance metrics table with charts
   - **Filename suggestion:** Performance_Metrics.png

4. **Figure 4 (Placeholder 4):** Complete Example Analysis
   - **Location:** Section 11.2 (Complex Example)
   - **Dimensions:** 1200×900 pixels (2-3 screenshots)
   - **Content:** Multiple tabs showing full analysis results
   - **Filename suggestion:** Example_Analysis_Complete.png

5. **Figure 5 (Placeholder 5):** Hazard Quick Reference Chart
   - **Location:** Appendix C (Hazard Summary Quick Reference)
   - **Dimensions:** 1000×600 pixels
   - **Content:** Visual reference for 5 hazard types with examples
   - **Filename suggestion:** Hazard_Reference_Chart.png

**To Insert Screenshots:**
1. Create folder: `w:\Sem 6\CA\HazardAnalyzerProject\Screenshots\`
2. Place screenshots in this folder
3. In Markdown editor, replace placeholders with: `![Description](Screenshots/filename.png)`

---

**Generated:** May 2026  
**Version:** 1.0 (Complete)  
**Status:** Ready for Academic Submission
