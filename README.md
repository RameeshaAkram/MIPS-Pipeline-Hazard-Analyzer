# MIPS Pipeline Hazard Analyzer

Java Swing application that analyses MIPS assembly for pipeline hazards (RAW, WAR, WAW, control, structural), simulates pipeline stages, and exports reports.

## Build & Run
Requirements: JDK 11+ installed and `javac`/`java` on PATH.

Compile:

```powershell
javac HazardAnalyzerComplete.java
```

Run:

```powershell
java HazardAnalyzerComplete
```

## Notes
- `lib/` can hold third-party JARs (e.g. iText) if you want PDF export.
- To build a runnable JAR or add CI, contact me and I can scaffold Gradle/Maven and GitHub Actions.
