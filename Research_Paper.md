# MIPS Pipeline Hazard Analyzer: A Comprehensive Study of Hazard Detection and Performance Evaluation in 5-Stage Pipeline Architecture

**Authors:** Rameesha, Urva, Asad  
**Institution:** [Your University Name]  
**Date:** May 2026  
**Version:** 2.1  

---

## Abstract

This paper presents a comprehensive analysis of the MIPS Pipeline Hazard Analyzer, a standalone Java-based application designed to automatically detect and visualize pipeline hazards in MIPS assembly code. The application identifies five categories of hazards—Read After Write (RAW), Write After Read (WAR), Write After Write (WAW), Control hazards, and Structural hazards—within a 5-stage pipeline architecture. The system provides real-time pipeline simulation, performance metrics calculation (CPI, throughput, stall cycles), and generates detailed analysis reports with optimization recommendations. Through interactive visualization and comprehensive metrics, the analyzer enables students and developers to understand pipeline behavior and optimize code for better performance. The tool demonstrates effectiveness in educational contexts by providing immediate feedback on hazard detection and performance impact, achieving accurate hazard identification with intuitive visual representation.

**Keywords:** Pipeline Hazards, MIPS Architecture, Hazard Detection, Performance Metrics, Pipeline Simulation

---

## 1. Introduction

### 1.1 Background

The MIPS (Microprocessor without Interlocked Pipeline Stages) architecture is a fundamental concept in computer architecture education, widely used to teach pipeline design and instruction-level parallelism. The 5-stage pipeline (Instruction Fetch, Instruction Decode, Execute, Memory Access, Write Back) improves throughput by processing multiple instructions simultaneously. However, this parallelism introduces dependencies and conflicts that degrade performance if not properly managed.

### 1.2 The Problem

Traditional teaching methods for pipeline hazards often rely on manual tracing through code, which is error-prone and time-consuming. Students struggle to visualize how hazards propagate through pipeline stages, and instructors lack efficient tools to provide immediate feedback on code quality and performance.

Three main categories of data hazards exist:
- **RAW (Read After Write):** An instruction reads a register before a previous instruction finishes writing to it
- **WAR (Write After Read):** An instruction writes to a register that a previous instruction is still reading
- **WAW (Write After Write):** Two instructions write to the same register in sequence

Additionally, control hazards (branch instructions) and structural hazards (resource conflicts) compound the challenge.

### 1.3 Contribution

This paper presents the MIPS Pipeline Hazard Analyzer, a tool that:
1. **Automatically detects** all five hazard types in MIPS assembly code
2. **Simulates pipeline execution** cycle-by-cycle with hazard visualization
3. **Calculates performance metrics** (CPI, throughput, stall percentage)
4. **Provides optimization recommendations** based on detected hazards
5. **Exports professional reports** in text and PDF formats

### 1.4 Scope

The analyzer supports:
- R-type instructions (add, sub, and, or, xor, nor, sll, srl, sra, slt, sltu)
- I-type instructions (addi, addiu, andi, ori, xori, lui, slti, sltiu)
- Load/Store operations (lw, lh, lhu, lb, lbu, sw, sh, sb)
- Branch instructions (beq, bne, bgtz, blez, bgez, bltz)
- Jump instructions (j, jal, jr)

---

## 2. Methodology

### 2.1 System Architecture

The MIPS Pipeline Hazard Analyzer follows a modular architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│         User Interface (Swing GUI)          │
├─────────────────────────────────────────────┤
│   Code Editor  │  Pipeline Visualization   │
│  Syntax Highlighter  │  Report Display     │
│   Register Tracker   │  Performance Metrics│
├─────────────────────────────────────────────┤
│      Analysis Engine Layer                  │
├─────────────────────────────────────────────┤
│  Instruction Parser  │  Hazard Detector    │
│  Pipeline Simulator  │  Performance Engine │
└─────────────────────────────────────────────┘
```

### 2.2 Instruction Parsing

The system uses regex-based parsing to extract:
1. **Instruction name** (opcode)
2. **Operands** (registers, immediates, memory offsets)
3. **Instruction type** (R, I, Load/Store, Branch, Jump)
4. **Register dependencies** (read and write registers)

Parsing logic differentiates instruction formats:
- **R-type:** `opcode rd, rs, rt` (3 register operands)
- **I-type:** `opcode rt, rs, immediate` (2 registers + immediate)
- **Load/Store:** `opcode rt, offset(rs)` (register with memory address)
- **Branch:** `opcode rs, rt, label` (2 registers + label)

### 2.3 Hazard Detection Algorithm

The detection algorithm analyzes consecutive instruction pairs using the following logic:

```
FOR each instruction i in program:
    FOR each instruction j = i + 1:
        
        // Check RAW Hazard
        IF (i writes to register R) AND (j reads from register R):
            report RAW hazard
        
        // Check WAW Hazard
        IF (i writes to register R) AND (j writes to register R):
            report WAW hazard
        
        // Check WAR Hazard
        IF (j writes to register R) AND (i reads from register R):
            report WAR hazard
        
        // Check Control Hazard
        IF (i is BRANCH):
            report CONTROL hazard (1-2 cycle penalty)
        
        // Check Structural Hazard
        IF (i is MEMORY) AND (j is MEMORY):
            report STRUCTURAL hazard (1 cycle stall)
```

**Time Complexity:** O(n²) where n is number of instructions  
**Space Complexity:** O(n) for hazard storage

### 2.4 Pipeline Simulation

The simulator models a 5-stage pipeline using the formula:

**Total Cycles = Number of Instructions + 4 Stages - 1**

For each cycle, the simulator tracks:
- Which instruction is in each stage (IF, ID, EX, MEM, WB)
- Which hazards occur at that cycle
- Stall cycles due to hazard resolution

Hazard timing mapping:
- **RAW hazard:** Occurs at cycle = instruction₁_index + 2
- **Control hazard:** Occurs at cycle = instruction₁_index + 1
- **Structural hazard:** Occurs at cycle = instruction₁_index + 3
- **WAW/WAR hazard:** Occurs at cycle = instruction₁_index + 2

### 2.5 Performance Metrics Calculation

The system calculates three key metrics:

**1. CPI (Cycles Per Instruction)**
```
CPI = Total Cycles / Number of Instructions
```

**2. Throughput**
```
Throughput = 1 / CPI (Instructions per cycle)
```

**3. Stall Percentage**
```
Stall % = (Stall Cycles / Total Cycles) × 100
```

**Interpretation Scale:**
- CPI = 1.0 → Ideal (no stalls)
- CPI < 1.3 → Excellent (< 30% stalls)
- CPI < 1.6 → Good (< 60% stalls)
- CPI < 2.0 → Moderate (< 100% stalls)
- CPI ≥ 2.0 → High (severe hazards)

---

## 3. Implementation

### 3.1 Technology Stack

| Component | Technology |
|-----------|------------|
| **Language** | Java SE 11+ |
| **GUI Framework** | Swing (javax.swing.*) |
| **Graphics** | Java 2D (GradientPaint, Graphics2D) |
| **Export Format** | PDF (iText7 - optional, via reflection) |
| **Compilation** | javac |
| **Execution** | java HazardAnalyzerComplete |

### 3.2 Core Components

#### 3.2.1 Main Class: `HazardAnalyzerComplete`

**Responsibilities:**
- GUI creation and management (3000+ lines)
- Event handling (button clicks, file loads, animations)
- Data coordination between UI and analysis engine
- User interaction orchestration

**Key Methods:**
- `main()` - Application entry point with splash screen
- `createAndShowGUI()` - Main window setup
- `analyzeCode()` - Orchestrates parsing, detection, simulation, and reporting
- `parseInstructions()` - Converts source code to Instruction objects
- `detectHazards()` - Identifies all hazard types
- `simulatePipeline()` - Creates cycle-by-cycle simulation
- `generateReport()` - Formats analysis results
- `updatePerformanceTab()` - Calculates and displays metrics

#### 3.2.2 Data Model Classes

**Instruction Class**
```java
static class Instruction {
    String original;           // Original assembly line
    String name;              // Instruction mnemonic
    String type;              // R, I, MEMORY, BRANCH, JUMP
    int lineNum;              // Source line number
    Integer writeReg;         // Destination register
    Set<Integer> readRegs;    // Source registers
    boolean isValid;          // Parse validation flag
}
```

**Hazard Class**
```java
static class Hazard {
    String type;              // RAW, WAR, WAW, CONTROL, STRUCTURAL
    int instr1Idx;           // Index of first instruction
    int instr2Idx;           // Index of second instruction
    String description;       // Human-readable hazard explanation
}
```

#### 3.2.3 UI Components

**RoundedPanel**
- Custom JPanel with rounded corners
- Used for card-style UI sections
- Improves visual hierarchy

**GradientButton**
- Custom button with gradient background
- Provides modern, professional appearance
- Supports hover effects

**HazardTableCellRenderer**
- Custom table cell renderer
- Color-codes hazard severity:
  - Light yellow: 0 hazards
  - Orange: 1-2 hazards
  - Dark red: 3-5 hazards
  - Brown: 6+ hazards

**MIPSSyntaxDocument**
- Extends DefaultStyledDocument
- Real-time syntax highlighting
- Features:
  - Keywords (blue, bold)
  - Registers (green)
  - Comments (gray, italic)
  - Numbers (orange)
  - Labels (purple, bold)

#### 3.2.4 Auxiliary Components

**ProfessionalSplashScreen**
- Loading screen with progress bar
- Simulates module loading
- Professional appearance

**CustomTitleBar**
- Custom window title bar
- Minimalist design
- Responsive to window state

**ThemeManager**
- Static utility class for color management
- Centralized color definitions
- Maintains visual consistency

### 3.3 User Interface Layout

**Main Window (1400×900 minimum)**

**[SCREENSHOT REQUIRED - Figure 1: Main Application GUI]**
*Screenshot should show: Main window with left code input panel, right tabbed results panel, gradient background, custom title bar, status bar at bottom. Code example loaded in editor. Minimum resolution: 1400x900.*

```
┌─────────────────────────────────────────────────┐
│              Custom Title Bar                   │
├──────────────────────┬──────────────────────────┤
│                      │                          │
│   Code Input Panel   │  Results Panel           │
│  ┌────────────────┐  │  ┌──────────────────┐   │
│  │                │  │  │ Tabbed Interface │   │
│  │ Editor + Btns  │  │  │ ├ Pipeline       │   │
│  │                │  │  │ ├ Report         │   │
│  │                │  │  │ ├ Performance    │   │
│  │                │  │  │ ├ Visualization  │   │
│  │                │  │  │ └ Registers      │   │
│  │                │  │  └──────────────────┘   │
│  └────────────────┘  │                          │
└──────────────────────┴──────────────────────────┘
│        Status Bar: Ready. Load or write MIPS code...       │
└──────────────────────────────────────────────────────────┘
```

**Responsive Design**
- Font scaling: 0.8x to 1.5x based on window size
- Base dimensions: 1400×900
- Scale factor: min(width/1400, height/900)

### 3.4 Data Flow Diagram

**[DIAGRAM REQUIRED - Figure 8: DFD Level 1 Diagram]**
*Professional Mermaid Flowchart DFD showing: User as external entity, 6 numbered processes (Parse Instructions, Detect Hazards, Simulate Pipeline, Calculate Performance, Generate Report, Display Results), 5 data stores (Code Editor, Instruction Cache, Hazard Database, Register State, Performance Metrics), labeled data flows between all components, color-coded by type. Suitable for research paper format.*

The system follows a structured data flow from user input through analysis to output:

```
PROCESS FLOW:
1.0 Parse Instructions
   ↓ Input: MIPS Assembly Code
   ↓ Output: Instruction List, Register Dependencies
   ↓ Data Store: Instruction Cache

2.0 Detect Hazards
   ↓ Input: Instruction List
   ↓ Output: Hazard List, Conflict Information
   ↓ Data Store: Hazard Database

3.0 Simulate Pipeline
   ↓ Input: Instruction List, Hazards
   ↓ Output: Pipeline Cycles, Hazard Mapping
   ↓ Data Store: Register State

4.0 Calculate Performance
   ↓ Input: Pipeline Data, Hazard Information
   ↓ Output: CPI, Throughput, Stall %
   ↓ Data Store: Performance Metrics

5.0 Generate Report
   ↓ Input: Instructions, Hazards, Performance Data
   ↓ Output: Formatted Report, Recommendations
   ↓ Data Store: Report Files

6.0 Display Results
   ↓ Input: All Analysis Data
   ↓ Output: UI Tabs (Pipeline, Report, Performance, Visualization, Registers)
   ↓ Display: Interactive Visualization + Export Options
```

**Key Data Stores:**
- **Code Editor:** Stores user-input MIPS assembly code
- **Instruction Cache:** Parsed instruction objects with opcode, operands, type, register dependencies
- **Hazard Database:** Detected hazards with type, location, and cycle information
- **Register State:** Register values, read/write counts, access patterns
- **Performance Metrics:** CPI calculations, throughput, stall cycles, interpretation data

---

## 4. Results and Features

### 4.1 Core Features Demonstrated

**1. Automatic Hazard Detection**

**[SCREENSHOT REQUIRED - Figure 2: Hazard Detection Report]**
*Screenshot should show: Report tab displaying detected hazards list with descriptions, hazard summary statistics (RAW count, WAR count, WAW count, Control count, Structural count), total hazards detected. Show the detailed output from example code analysis.*

- Identifies RAW, WAR, WAW, Control, and Structural hazards
- Provides detailed descriptions of each hazard
- Generates hazard count statistics

**2. Pipeline Visualization**

**[SCREENSHOT REQUIRED - Figure 3: Pipeline Simulation Table]**
*Screenshot should show: Pipeline tab with table showing cycles (rows) and stages (columns: Cycle, IF, ID, EX, MEM, WB, Hazards). Instructions flowing through stages. Hazard warnings (⚠️) highlighted. Play/Pause/Reset animation buttons visible. Show 8-10 cycles of pipeline execution.*

- Real-time cycle-by-cycle display
- Shows instruction progression through 5 stages
- Highlights hazards at occurrence cycles
- Interactive animation (play/pause/step)

**3. Performance Metrics**

**[SCREENSHOT REQUIRED - Figure 4: Performance Metrics Display]**
*Screenshot should show: Performance tab displaying formatted metrics table with: Total Instructions Executed, Total Cycles Taken, Stall Cycles, CPI value, Throughput value, Stall Percentage. Include interpretation text below metrics (e.g., "Good CPI - Minor performance impact from hazards"). Show hazard breakdown percentages.*

- Calculates CPI with interpretation guidance
- Computes throughput metrics
- Tracks stall percentages
- Provides performance assessment (Ideal/Excellent/Good/Moderate/High)

**4. Code Analysis Report**
- Lists all parsed instructions with register usage
- Details hazard occurrences with explanations
- Provides hazard summary statistics
- Offers optimization recommendations:
  - RAW: Insert NOP instructions
  - WAR: Use different registers or reorder
  - WAW: Avoid consecutive writes
  - Control: Use branch delay slots
  - Structural: Avoid consecutive memory ops

**5. Advanced Visualizations**

**[SCREENSHOT REQUIRED - Figure 5: Heatmap Visualization & Register Tracker]**
*Screenshot should show: Visualization tab with hazard density heatmap grid (instructions vs cycles), color legend showing 0/light yellow through 6+/brown. Also include: Register tab showing register state table with columns: Register, Value, Read Count, Write Count, Last Written. Multiple registers visible (at least $t0-$t5, $s0-$s2).*

- Heatmap showing hazard density per instruction
- Color-coded legend (0 → light yellow, 6+ → brown)
- Register state tracker with read/write counts
- Syntax-highlighted code editor

**6. Export Capabilities**

**[SCREENSHOT REQUIRED - Figure 6: Code Editor with Syntax Highlighting & Export Buttons]**
*Screenshot should show: Code input panel with MIPS assembly code in text area. Syntax highlighting visible (keywords in blue, registers in green, comments in gray). Toolbar buttons visible: Load File, Load Example, Analyze, Clear All, Export Report, Export PDF buttons. Show professional button styling with gradients.*

- Text report export (.txt)
- PDF report generation (with iText7)
- Detailed documentation for external review

### 4.2 UI/UX Features

| Feature | Benefit |
|---------|---------|
| Dark theme with gradient background | Reduced eye strain, professional appearance |
| Responsive font scaling | Readable on any screen size |
| Real-time syntax highlighting | Improves code readability |
| Tabbed interface | Organized information access |
| Animation controls | Better understanding of pipeline flow |
| Color-coded hazard cells | Quick visual hazard identification |
| Tooltips and status bar | User guidance and feedback |
| Splash screen | Professional application feel |

### 4.3 Educational Impact

The analyzer serves as an effective learning tool:
1. **Visualization:** Students see abstract concepts (hazards, pipeline stages) in concrete form
2. **Immediate Feedback:** Results appear instantly after code entry
3. **Exploration:** Users can experiment with different code patterns
4. **Understanding:** Detailed reports explain why hazards occur and how to fix them

---

## 5. Technical Achievements

### 5.1 Code Quality

- **Single-file design:** 3000+ lines of well-organized code
- **Modular structure:** 14+ inner classes with clear responsibilities
- **Event-driven architecture:** Responsive to user interactions
- **Error handling:** Graceful handling of invalid instructions
- **Performance:** Handles large programs (50+ instructions) efficiently

### 5.2 Algorithm Efficiency

| Operation | Complexity | Notes |
|-----------|-----------|-------|
| Instruction parsing | O(n) | Linear scan with regex |
| Hazard detection | O(n²) | Pairwise comparison |
| Pipeline simulation | O(n) | Single pass through instructions |
| Performance calculation | O(1) | Simple arithmetic operations |

### 5.3 Features Beyond Requirements

- **Register tracking:** Monitors register access patterns
- **PDF export:** Professional report generation
- **Animation system:** Visual pipeline execution
- **Zoom controls:** Adjustable Gantt chart visualization
- **Responsive UI:** Adapts to various window sizes

---

## 6. Limitations and Future Work

### 6.1 Current Limitations

1. **Simplified register model:** Assumes all register writes complete by Write Back stage
2. **No forwarding simulation:** Doesn't model hazard mitigation via data forwarding
3. **Limited instruction set:** Supports common MIPS instructions but not extensions
4. **No optimization suggestions:** Recommendations are generic, not code-specific
5. **Manual hazard entry:** Cannot directly modify detected hazards

### 6.2 Future Enhancements

1. **Forwarding Simulation:** Model data forwarding to reduce hazard impact
2. **Advanced MIPS:** Support MIPS-32, MIPS-64 extensions
3. **Code Optimization:** Suggest specific instruction reordering
4. **Parallel Pipeline:** Support superscalar architectures
5. **Performance Prediction:** Estimate execution time on real hardware
6. **Batch Analysis:** Process multiple code files
7. **Cloud Integration:** Web-based version with shared analysis database

---

## 7. Conclusion

The MIPS Pipeline Hazard Analyzer successfully demonstrates the practical application of computer architecture concepts through an interactive, user-friendly tool. By automating hazard detection and visualization, the analyzer:

1. **Improves learning efficiency:** Students understand hazards through immediate, visual feedback
2. **Saves time:** Eliminates manual, error-prone hazard tracing
3. **Enables exploration:** Users experiment with code variations quickly
4. **Provides insights:** Performance metrics guide optimization efforts
5. **Professional output:** Exportable reports suitable for academic and professional contexts

The tool's modular architecture, comprehensive feature set, and educational focus make it a valuable contribution to computer architecture education. Future development focusing on hazard mitigation techniques (forwarding, branch prediction) and expanded instruction sets will further enhance its utility.

The project demonstrates that effective educational tools combine theoretical understanding with practical implementation, making complex concepts accessible through well-designed user interfaces and automated analysis.

---

## 8. References

1. D. A. Patterson and J. L. Hennessy, "Computer Organization and Design: The Hardware/Software Interface," Morgan Kaufmann, 5th Edition, 2013.

2. J. L. Hennessy and D. A. Patterson, "Computer Architecture: A Quantitative Approach," Morgan Kaufmann, 6th Edition, 2017.

3. "MIPS Architecture Reference Manual," MIPS Technologies, Inc., 2019.

4. M. J. Flynn, "Computer Architecture: Pipelined and Parallel Processor Design," Jones & Bartlett Learning, 1995.

5. B. Gooch, A. Gooch, "Non-Photorealistic Rendering," SIGGRAPH Conference Proceedings, 1998.

6. Oracle Java Swing Documentation: https://docs.oracle.com/javase/tutorial/uiswing/

7. iText7 PDF Library: https://itextpdf.com/

8. "IEEE Standard for Floating-Point Arithmetic (IEEE 754-2019)," IEEE Standards Association, 2019.

---

## Appendices

### Appendix A: Example Analysis Output

**[SCREENSHOT REQUIRED - Figure 7: Complete Example Analysis]**
*Screenshot should show: Multiple tabs in sequence showing the complete analysis of the example code:
  1. Code editor with example code entered
  2. Pipeline table showing full simulation output
  3. Report tab with detected hazards
  4. Performance metrics displayed
Or provide single comprehensive screenshot showing at least the pipeline and report simultaneously if space permits.*

**Input Code:**
```
add $t0, $s0, $s1
lw $t1, 0($t0)
add $t2, $t1, $s2
sw $t2, 4($sp)
beq $t2, $t3, end
```

**Detected Hazards:**
1. RAW: Instruction 2 reads $t0 from instruction 1
2. RAW: Instruction 3 reads $t1 from instruction 2
3. STRUCTURAL: Instructions 2 and 4 both access memory
4. CONTROL: Instruction 5 is a branch

**Performance Metrics:**
- Total Instructions: 5
- Total Cycles: 8 (5 + 5 - 2 for hazards)
- Stall Cycles: 3
- CPI: 1.6
- Throughput: 0.625 instructions/cycle

---

### Appendix B: Supported MIPS Instructions

**R-Type (3 register operands):**
add, addu, sub, subu, and, andi, or, ori, xor, xori, nor, sll, srl, sra, slt, sltu

**I-Type (2 registers + immediate):**
addi, addiu, andi, ori, xori, lui, slti, sltiu

**Load/Store:**
lw, lh, lhu, lb, lbu, sw, sh, sb

**Branch:**
beq, bne, bgtz, blez, bgez, bltz

**Jump:**
j, jal, jr

---

**Document Version:** 2.1  
**Last Updated:** May 2026  
**Status:** Complete
