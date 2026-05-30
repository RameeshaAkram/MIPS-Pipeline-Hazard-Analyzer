# MIPS Hazard Analyzer - Code Functionality Guide

## Overview
The **MIPS Hazard Analyzer** is a comprehensive Java GUI application designed to detect, analyze, and visualize pipeline hazards in MIPS assembly code. It provides performance metrics, pipeline simulation, and detailed reporting capabilities for computer architecture studies.

---

## Table of Contents
1. [Core Architecture](#core-architecture)
2. [Main Functionalities](#main-functionalities)
3. [Hazard Detection System](#hazard-detection-system)
4. [Performance Metrics](#performance-metrics)
5. [UI Components](#ui-components)
6. [Data Structures](#data-structures)
7. [File Operations](#file-operations)

---

## Core Architecture

### Application Entry Point (`main` method)
- Displays a professional splash screen showing progress (0-100%)
- Shows loading messages: "Loading MIPS parser", "Initializing hazard detector", etc.
- Initializes the main GUI after splash screen closes
- **Responsive Design**: Adapts font sizes based on window dimensions

### Main Window Layout
```
┌─────────────────────────────────────────────────────┐
│            MIPS Hazard Analyzer v3.0                │
├──────────────────────┬──────────────────────────────┤
│   LEFT PANEL         │      RIGHT PANEL (Tabs)      │
│  (Code Input)        │  ├─ Pipeline Simulation      │
│                      │  ├─ Hazard Report           │
│  • Load File         │  ├─ Performance Metrics     │
│  • Load Example      │  ├─ Hazard Visualization   │
│  • Analyze Hazards   │  ├─ Pipeline Gantt Chart   │
│  • Clear             │  ├─ Hazard Heatmap        │
│  • Export Report     │  └─ Register File          │
│  • Export PDF        │                             │
│  • Benchmark         │                             │
│                      │                             │
│  [Code Area]         │  [Results Tabs]            │
└──────────────────────┴──────────────────────────────┘
│                    Status Bar                       │
└─────────────────────────────────────────────────────┘
```

---

## Main Functionalities

### 1. **Code Input & Parsing** (`createCodeInputPanel`, `parseInstructions`)

#### How It Works:
- Users input MIPS assembly code in the left panel's text editor
- Code can be loaded from:
  - `.asm` or `.s` files (via "Load File" button)
  - Built-in example code (via "Load Example" button)
  - Manual typing in the editor

#### Parsing Process:
```java
parseInstructions(String sourceCode)
├─ Split source code by lines
├─ For each non-empty, non-comment line:
│  ├─ Extract instruction name (e.g., "add", "lw", "beq")
│  ├─ Extract operands
│  ├─ Determine instruction type (R, I, Load/Store, Branch)
│  └─ Parse registers involved
└─ Return List<Instruction> with parsed data
```

#### Supported Instruction Types:
- **R-Type**: `add, addu, sub, subu, and, or, xor, nor, slt, sltu, sll, srl, sra, jr`
- **I-Type**: `addi, addiu, andi, ori, xori, slti, sltiu, lui`
- **Load/Store**: `lw, lh, lhu, lb, lbu, sw, sh, sb`
- **Branch**: `beq, bne, bgtz, blez, bltz, bgez`
- **Jump**: `j, jal`

#### Register Parsing:
- Converts register names (`$t0`, `$a0`, etc.) to numbers (0-31)
- Handles offset notation: `lw $t0, 0($sp)` → extracts base register
- Tracks which registers are read and written by each instruction

---

### 2. **Hazard Detection System** (`detectHazards`)

#### What Are Pipeline Hazards?
Pipeline hazards prevent the next instruction from executing during its designated clock cycle. They cause pipeline stalls.

#### Five Types Detected:

##### **RAW (Read After Write) Hazard** ❌
- **Occurs When**: An instruction reads a register before a previous instruction finishes writing to it
- **Example**:
  ```mips
  add $t0, $t1, $t2    # Writes to $t0 (finishes in WB stage, cycle 5)
  lw  $t3, 0($t0)      # Reads $t0 (needs in ID stage, cycle 2)
  ```
- **Problem**: $t0 contains old data when `lw` tries to use it
- **Solution**: Forwarding units, data hazard stalls, or code scheduling

##### **WAW (Write After Write) Hazard** ⚠️
- **Occurs When**: Two instructions write to the same register
- **Example**:
  ```mips
  add $s0, $s1, $s2    # Writes to $s0
  sub $s0, $s3, $s4    # Writes to $s0 again
  ```
- **Problem**: If executed out-of-order, results could be incorrect
- **Solution**: Write-back ordering enforcement

##### **WAR (Write After Read) Hazard** ⚠️
- **Occurs When**: An instruction writes to a register before a previous instruction finishes reading it
- **Example**:
  ```mips
  lw  $a0, 0($a1)      # Reads from memory
  add $a0, $a2, $a3    # Writes to $a0
  ```
- **Problem**: Register value changes before read completes
- **Solution**: Register renaming or stalling

##### **Control Hazard** 🔀
- **Occurs When**: Branch instruction causes uncertainty about next instruction
- **Example**:
  ```mips
  beq $t0, $t1, target  # Branch decision not known until MEM stage
  add $t2, $t3, $t4     # Should this execute?
  ```
- **Problem**: Fetched instructions may be wrong path
- **Solution**: Branch prediction, delay slots, or pipeline flush

##### **Structural Hazard** 💥
- **Occurs When**: Multiple instructions need same hardware resource simultaneously
- **Example**:
  ```mips
  lw  $k0, 0($k1)       # Accesses memory (IF/MEM stage)
  sw  $k2, 4($k3)       # Also accesses memory (IF/MEM stage)
  ```
- **Problem**: Both can't access memory in same cycle
- **Solution**: Dual-port memory or stalling

#### Detection Algorithm:
```java
detectHazards(List<Instruction> instructions)
├─ For each instruction pair (i, i+1), (i, i+2), (i, i+3):
│  │
│  ├─ Check RAW: Does instruction_j write to a register that instruction_i reads?
│  │  └─ If distance ≤ 2 cycles → Hazard detected
│  │
│  ├─ Check WAW: Do both write to same register?
│  │  └─ If distance ≤ 4 cycles → Hazard detected
│  │
│  ├─ Check WAR: Does instruction_i write before instruction_j reads?
│  │  └─ If distance ≤ 2 cycles → Hazard detected
│  │
│  ├─ Check Control: Is one instruction a branch?
│  │  └─ Hazard detected immediately after branch
│  │
│  └─ Check Structural: Are both Load/Store instructions?
│     └─ If within 2 cycles → Hazard detected
│
└─ Increment hazard counters (rawCount, warCount, etc.)
```

---

### 3. **Pipeline Simulation** (`simulatePipeline`)

#### What It Does:
Creates a visual representation of how instructions flow through the 5-stage MIPS pipeline:

```
Stages:  IF (Instruction Fetch)
         ↓
         ID (Instruction Decode)
         ↓
         EX (Execute)
         ↓
        MEM (Memory Access)
         ↓
         WB (Write Back)
```

#### Simulation Logic:
```
Total Cycles = Number of Instructions + 4 (pipeline depth - 1)

For each cycle 0 to totalCycles-1:
├─ Determine which instruction is in each stage
├─ Mark stages with instruction name (e.g., "add", "lw")
├─ Highlight hazardous cycles in red/orange
├─ Track which hazards occur in this cycle
└─ Display in pipeline table

Example: 5 instructions = 5 + 4 = 9 cycles needed
```

#### Pipeline Table Structure:
| Cycle | IF | ID | EX | MEM | WB | Hazards |
|-------|----|----|----|----|-----|---------|
| 0 | add | - | - | - | - | - |
| 1 | lw | add | - | - | - | RAW |
| 2 | sw | lw | add | - | - | - |

#### Animation Controls:
- **Play/Pause**: Cycles through pipeline states automatically
- **Step**: Advances one cycle manually
- **Reset**: Returns to cycle 0
- **Speed Slider**: Adjusts animation speed (100-1000ms per step)

---

### 4. **Performance Metrics Calculation** (`updatePerformanceTab`)

#### Key Metrics:

##### **CPI (Cycles Per Instruction)**
```
CPI = Total Cycles / Total Instructions

Interpretation:
├─ CPI = 1.0 → Perfect (ideal pipeline)
├─ CPI = 1.5 → 50% slowdown due to stalls
├─ CPI = 2.0 → 100% slowdown (significant hazards)
└─ CPI > 2.0 → Severe hazards detected
```

##### **Throughput**
```
Throughput = Total Instructions / Total Cycles

Units: Instructions per cycle (IPC)

Example: 10 instructions in 15 cycles
└─ Throughput = 10/15 = 0.67 IPC
```

##### **Stall Percentage**
```
Stall % = (Stall Cycles / Total Cycles) × 100

Interpretation:
├─ 0% → No hazards, full pipeline efficiency
├─ 10-20% → Minor hazards, acceptable performance
├─ 20-50% → Significant hazards, needs optimization
└─ >50% → Critical hazards, major performance impact
```

#### Register Statistics:
- **Read Count**: Number of times register is read
- **Write Count**: Number of times register is written
- **Last Written**: Instruction that last modified register
- **Value**: Current value in register (initialized to 0)

---

## Hazard Detection System

### Register Tracking (`initializeRegisterTracking`, `updateRegisterTracking`)

#### Initialization:
- All 32 MIPS registers initialized to value 0
- Read/write counters reset to 0

#### During Analysis:
- **Read Operations**: When instruction reads a register:
  - Increment `registerReadCounts[regNum]`
  - Check if value is current (no hazard) or stale (hazard exists)
  
- **Write Operations**: When instruction writes a register:
  - Increment `registerWriteCounts[regNum]`
  - Update `registerLastWritten[regNum]` with instruction name
  - Update `registerValues[regNum]` with new value

---

## Performance Metrics

### Cycle Calculation
```
Formula: Total Cycles = N + (Pipeline Depth - 1) + Stall Cycles

Where:
├─ N = Number of instructions
├─ Pipeline Depth = 5 (IF, ID, EX, MEM, WB)
└─ Stall Cycles = Number of hazards detected
```

### Hazard Impact
Each detected hazard typically adds **1-2 stall cycles** depending on type:
- **RAW Hazard** → 1-2 cycle stall (waiting for write to complete)
- **Control Hazard** → 2-3 cycle stall (pipeline flush + refill)
- **Structural Hazard** → 1 cycle stall (waiting for resource)

---

## UI Components

### 1. **Tabbed Results Panel**

#### Tab 1: Pipeline Simulation
- Animated visualization of instruction stages
- Color-coded hazard indicators
- Real-time cycle counter
- Play/pause/step controls

#### Tab 2: Hazard Report
- Text-based list of all detected hazards
- Instruction pairs involved
- Hazard type classification
- Recommendations for optimization

#### Tab 3: Performance Metrics
- CPI, throughput, stall percentage
- Detailed hazard breakdown by type
- Performance interpretation guide
- Bottleneck identification

#### Tab 4: Hazard Visualization
- Grid showing each cycle and stage
- Color-coded cells:
  - **Green**: Normal execution
  - **Orange**: RAW hazard
  - **Pink**: WAW/WAR hazard
  - **Blue**: Control hazard
  - **Gray**: Structural hazard
- Interactive tooltips on hover

#### Tab 5: Pipeline Gantt Chart
- Timeline visualization of instruction execution
- Each instruction shown as horizontal bar
- Color changes when hazard occurs
- Zoom in/out controls
- Click instruction for details

#### Tab 6: Hazard Heatmap
- 2D grid: Cycles × Instructions
- Color intensity = hazard severity
- Bar chart showing hazard distribution
- Legend for hazard count ranges

#### Tab 7: Register File Viewer
- All 32 MIPS registers displayed
- Shows: Name, Value, Read Count, Write Count
- $zero register highlighted (read-only)
- Updated after code analysis

### 2. **Professional UI Elements**

#### GradientButton
- Modern gradient background
- Hover effects
- Rounded corners
- Better visual hierarchy

#### RoundedPanel
- Card-style panels with rounded corners
- Consistent spacing
- Clean visual grouping

#### Responsive Scaling
```java
updateScaleFactor(width, height)
├─ Calculates scale based on window size
├─ Clamps between 0.8x and 1.5x
└─ Updates all fonts and components
```

---

## Data Structures

### Instruction Class
```java
class Instruction {
    String original;           // Original code line
    String name;              // Instruction name (add, lw, etc.)
    String type;              // R, I, Load/Store, Branch, Jump
    int lineNum;              // Line number in source
    Integer writeReg;         // Register being written (null if none)
    Set<Integer> readRegs;    // Registers being read
    boolean isValid;          // Successfully parsed?
}
```

### Hazard Class
```java
class Hazard {
    String type;              // RAW, WAW, WAR, Control, Structural
    int instr1Idx;           // Index of first instruction
    int instr2Idx;           // Index of second instruction
    String description;       // Human-readable hazard description
}
```

### Tracking Maps
```java
Map<Integer, String> cycleHazards;           // Cycle → Hazard types
Map<Integer, Integer> instructionHazardCount; // Instruction → Hazard count
Map<Integer, Integer> registerValues;         // Register → Current value
Map<Integer, Integer> registerReadCounts;     // Register → Read count
Map<Integer, Integer> registerWriteCounts;    // Register → Write count
Map<Integer, String> registerLastWritten;     // Register → Last written by
```

---

## File Operations

### Load File (`loadFile`)
- Opens file chooser dialog
- Reads `.asm` or `.s` files
- Displays content in code editor
- Automatically parses on load

### Export Report (`exportReport`)
- Saves analysis results to `.txt` file
- Includes:
  - Instruction listing
  - Detected hazards with descriptions
  - Hazard summary counts
  - Optimization recommendations

### Export PDF (`exportToPDF`)
- Exports formatted report as PDF
- Requires iText7 library (optional)
- Uses reflection for dynamic library detection
- Falls back to text export if library unavailable

### Export Heatmap (`exportHeatmapAsPNG`)
- Generates PNG image of hazard heatmap
- Includes legend and color scale
- Renders to BufferedImage then saves to disk

### Benchmark Mode (`runBenchmarkMode`)
- Multi-file analysis capability
- Compare hazard metrics across files
- Useful for research and optimization studies

---

## Syntax Highlighting

### MIPSSyntaxDocument Class
Custom syntax highlighting for MIPS code:

- **Keywords** (Green): `add, lw, beq, j`, etc.
- **Registers** (Blue): `$t0, $a1, $sp`, etc.
- **Comments** (Gray): Lines starting with `#`
- **Numbers** (Orange): Numeric literals
- **Labels** (Yellow): Lines ending with `:`
- **Normal** (White): Other text

---

## Animation System

### Pipeline Animation (`toggleAnimation`, `advanceOneCycle`)

```
Animation Flow:
├─ User clicks Play button
├─ Timer fires every `animationSpeed` milliseconds
├─ Increment currentCycle
├─ Update pipeline table visualization
├─ Repaint table with new cycle
├─ Stop when currentCycle reaches totalCycles
└─ User can pause, reset, or adjust speed
```

---

## Error Handling

- **Empty Code**: Validates input before analysis
- **Invalid Instructions**: Skipped with warning
- **Missing Registers**: Logged as invalid
- **File I/O Errors**: Dialog with error message
- **PDF Library**: Graceful fallback if iText7 unavailable

---

## Performance Optimizations

1. **Responsive Scaling**: Fonts scale with window, not recreated constantly
2. **Cached References**: UI component references stored for quick updates
3. **Efficient Parsing**: Single-pass instruction parsing
4. **Lazy Visualization**: Hazards only highlighted where detected
5. **Event Optimization**: Batch repaints instead of continuous updates

---

## Key Algorithms Summary

| Component | Algorithm | Time Complexity |
|-----------|-----------|-----------------|
| Parsing | Regex-based instruction matching | O(n) |
| Hazard Detection | Pairwise instruction comparison | O(n²) |
| Pipeline Simulation | Stage calculation per cycle | O(n+c) |
| Performance Metrics | Direct calculation | O(1) |
| Visualization | Grid rendering | O(c×s) |

Where:
- n = number of instructions
- c = total cycles
- s = pipeline stages (5)

---

## Usage Example

```
1. User clicks "Load Example" → Loads test MIPS code
2. User clicks "Analyze Hazards" → Parses and detects hazards
3. System displays:
   - Pipeline table with 9 cycles (5 instructions + 4)
   - 2 RAW hazards detected
   - CPI = 1.4 (40% slowdown)
   - Heatmap showing red cells at conflict points
4. User clicks "Play" → Animation shows execution flow
5. User exports report → Saves detailed analysis to file
```

---

## Conclusion

This MIPS Hazard Analyzer demonstrates comprehensive compiler and architecture concepts including:
- **Instruction parsing and semantic analysis**
- **Pipeline hazard detection and classification**
- **Performance metrics calculation**
- **Interactive visualization and UI design**
- **File I/O and report generation**
- **Advanced Swing GUI programming**

It's an excellent educational tool for understanding MIPS pipeline behavior and performance optimization techniques.
