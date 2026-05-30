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
