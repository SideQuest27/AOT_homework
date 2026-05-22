# Multi-Agenten-System: Ameisenkolonie-Simulation
---



##  Projekt- und Ordnerstruktur

Das Projekt ist nach dem standardisierten Maven-Layout aufgebaut:



```text

ameisen-simulation/

├── src/
│   └── main/
│       ├── java/
│       │   └── org/
│       │       └── example/
│       │           ├── Main.java                 # Einstiegspunkt (Parsing & Simulationsschleife)
│       │           ├── agent/
│       │           │   └── Ant.java              # Ameisen-Agent (Sense-Reason-Act Logik)
│       │           ├── config/
│       │           │   ├── ModelConfig.java      # Mapping-Klasse für die JSON-Konfiguration
│       │           │   └── modelConfig.json      # JSON-Datei, die die Experimentkonfiguration enthält
│       │           ├── enums/
│       │           │   ├── ActionType.java       # UP, DOWN, LEFT, RIGHT, PICK_FOOD, DROP_FOOD
│       │           │   ├── AntState.java         # SEARCHING, RETURNING
│       │           │   └── MessageType.java      # SUCCESS, FAILED
│       │           ├── environment/
│       │           │   ├── Grid.java             # Das 2D-Koordinatensystem
│       │           │   └── Cell.java             # Einzelne Zelle (Kapazitäten, Pheromone, Futter)
│       │           ├── model/
│       │           │   ├── Action.java           # Container für Agenten-Aktionen
│       │           │   ├── CellPerception.java   # Zellwahrnehmung einer einzelnen Gitterzelle
│       │           │   └── Perception.java       # Kombinierte Zellwahrnehmung der Wahrnehmungen benachbarter Zellen.
│       │           └── simulation/
│       │               └── Manager.java          # Koordinator, Telemetrie-Logger & Scoring-System
│       └── resources/
│           ├── log4j2.xml                        # Konfiguration für das CSV-Logging Target
│           └── config.json                       # Experiment-Konfigurationsprofile
│   
├── pom.xml                                       # Maven Project Object Model (Abhängigkeiten)
├── README.md                                     # Diese Dokumentation
└── simulation_metrics.csv                        # Generierter Output für die Auswertung

```

##  Kompilieren und Ausführen

1. Java-Umgebung einrichten
   Installieren Sie Java JDK 17 (oder eine neuere Version), um den Java-Code kompilieren und ausführen zu können.

2. Dateipfad in Main.java anpassen
   Öffnen Sie die Datei Main.java (zu finden unter src/main/java/org/example/Main.java). Tragen Sie dort in Zeile 42 den absoluten Dateipfad zu Ihrer Konfigurationsdatei (modelConfig.json) ein, damit der JSON-Parser das gewünschte Profil beim Start fehlerfrei laden kann.

3. Experiment-Konfiguration definieren
   Hinterlegen Sie die gewünschten Simulationsparameter (Grid-Größe, Nest- und Futterkoordinaten, Ameisenanzahl, Evaporationsrate etc.) in der Datei modelConfig.json im Verzeichnis src/main/java/org/example/config/, um das jeweilige Experimentzenario zu definieren.
