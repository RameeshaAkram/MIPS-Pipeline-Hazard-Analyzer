# MIPS Pipeline Hazard Analyzer

A compact, open-source Java Swing application for detecting and visualizing pipeline hazards in MIPS assembly code. This repository contains the complete application as a set of Java source files (no build system required).

If you'd like, I can add a license (MIT recommended), screenshots, or a Gradle/Maven build. Tell me which you prefer and I'll add them.

## What this repository actually contains

- `HazardAnalyzerComplete.java` — main application (single-file Swing app)
- `HazardAnalyzerComplete_backup.java` — backup copy
- `CODE_FUNCTIONALITY.md`, `COMPLETE_PROJECT_DOCUMENTATION.md`, `DFD_Diagram.md` — project docs
- `Research_Paper.md` and `RESEARCH PAPER/` — research artifacts
- `lib/` — third-party jars (currently empty)

## Quick facts

- Language: Java (Swing)
- Entry point: `HazardAnalyzerComplete.main`
- Build: No build tool required; compile with `javac` and run with `java`
- PDF export: optional; requires adding iText JAR(s) to `lib/`

## Quick Start

1. Open a terminal in the project root (where `HazardAnalyzerComplete.java` is located).
2. Compile:

```powershell
javac HazardAnalyzerComplete.java
```

3. Run:

```powershell
java HazardAnalyzerComplete
```

4. Use the GUI: Load code, click **Analyze Hazards**, inspect the tabs, export text report.

## Notes about PDF export

PDF export uses iText via reflection if the JARs are present. To enable it:

1. Download the iText 7 JAR(s) and place them in `lib/`.
2. Compile and run with the classpath including `lib/*`:

Windows:
```powershell
javac -cp "lib/*" HazardAnalyzerComplete.java
java -cp ".;lib/*" HazardAnalyzerComplete
```

Linux/macOS:
```bash
javac -cp "lib/*" HazardAnalyzerComplete.java
java -cp ".:lib/*" HazardAnalyzerComplete
```

If iText is not found, the app will fall back to exporting text-only reports and show a helpful message.

## Minimal README — what I changed

- Removed large promotional sections and screenshots placeholder.
- Kept accurate build/run instructions for this repo layout.
- Noted that the project is open-source and offered to add a license file you prefer.

## Next steps I can do for you (pick any)

- Add an `LICENSE` file (MIT by default).
- Add a small `build.gradle` to produce a runnable JAR.
- Add GitHub Actions workflow to compile on push and create a release artifact.

Tell me which of the next steps you want and I'll implement it.

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
