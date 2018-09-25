# db-consistency-checker

Tool, um NoSQL-Datenbanken auf die Einhaltung verschiedener clientorientierter Konsistenzmodelle zu testen.

Folgende Konsistenzmodelle können getestet werden:
- Read Your Writes Consistency (RYWC)
- Monotonic Read Consistency (MRC)
- Monotonic Write Consistency (MWC)

Aktuell werden folgende NoSQL-Datenbanken unterstützt:
- Cassandra
- MongoDB
- Redis

## Voraussetzungen

- Docker Maschine mit mind. 10GB RAM

## Konfiguration

Im Verzeichnis src/main/resources liegen die Konfigurationsdateien im JSON-Format. Diese enthalten die Konfigurationen für die jeweils getestete NoSQL-Datenbank und das zu überprüfende Konsistenzmodell.
Folgende Konfigurationen sind möglich:

**Allgemeine Konfigurationen**

| Key | Bedeutung | mögliche Werte |
| ------------------ | ------------------ | ------------------ |
| ClusterIP | IP-Adresse des Clusters. | |
| ClusterPorts | Ports der Knoten im Cluster. | |
| ConsistencyModel | Zu überprüfendes Konsistenzmodell. | RYWC, MRC, MWC |
| MWCWrite (optional)| true: Schreiben von Daten für die spätere Überprüfung von MWC; false: Überprüfen der geschriebenen Daten auf Einhaltung von MWC | true, false |
| NumberOfTests | Anzahl der Testdurchläufe. | &gt; =  0 |
| Database | Zu überprüfende Datenbank. | Cassandra, MongoDB, Redis |

**Konfigurationen Cassandra**

| Key | Bedeutung | mögliche Werte |
| ------------------ | ------------------ | ------------------ |
| ReplicationStrategy | Cassandra Replikations-Strategie. | SimpleStrategy, NetworkTopologyStrategy |
| ReplicationFactor | Cassandra Replikations-Faktor. | &gt; =  1 |
| WriteConsistencyLevel | Konsistenzlevel für Schreibzugriffe. | ALL, QUORUM, LOCAL_QUORUM, EACH_QUORUM, THREE, TWO, ONE, LOCAL_ONE, ANY |
| ReadConsistencyLevel | Konsistenzlevel für Lesezugriffe. | ALL, QUORUM, LOCAL_QUORUM, EACH_QUORUM, THREE, TWO, ONE, LOCAL_ONE |


**Konfigurationen MongoDB**

| Key | Bedeutung | mögliche Werte |
| ------------------ | ------------------ | ------------------ |
| WriteConcern: w | Anzahl an MongoDB Instanzen, die einen Schreibzugriff bestätigen müssen. | 0, 1, 2, 3, majority |
| WriteConcern: j | Persistieren des Schreibzugriffs im Journal. | true, false |
| WriteConcern: wtimeout | Maximale Dauer in Millisekunden für die Bestätigung eines Schreibzugriffs. | &gt; =  0 |
| ReadConcern: level | Rückgabe des aktuellsten Wertes, der von einer gewissen Anzahl an MongoDB Instanzen als geschrieben bestätigt wurde. | local, majority, linearizable |
| ReadConcern: causalConsistent | Client Session mit garantierter CC. Nur bei ReadConcern Level "local" oder "majority" möglich. | true, false |
| ReadPreference: mode | Bevorzugter Knoten, an welchen Lesezugriffe gesendet werden sollen. | primary, primaryPreferred, secondary, secondaryPreferred, nearest |
| ReadPreference: maxStalenessSeconds (optional) | Maximale Staleness gelesener Daten in Sekunden. | &gt; =  90 |


**Konfigurationen Redis**

| Key | Bedeutung | mögliche Werte |
| ------------------ | ------------------ | ------------------ |
| Replication | Synchrone oder asynchrone Replikation von Schreibzugriffen. | synchron, asynchron |
| numslaves | Anzahl an Slaves, die bei synchroner Replikation einen Schreibzugriff bestätigen müssen. | 1, 2 |
| timeout | Timeout für synchrone Replikation in Sekunden. | &gt; =  0 |
| ReadMode | Bevorzugter Knoten, an welchen Lesezugriffe gesendet werden sollen. | MASTER, SLAVE, MASTER_SLAVE |

## Beispiel: Überprüfung von Cassandra auf Einhaltung von RYWC

Folgende Schritte sind durchzuführen:

- In der `configurationCassandra.json` das Konsistenzmodell `"ConsistencyModel": "RYWC"` sowie die gewünschten Konfigurationen für Cassandra einstellen.

- Mit Hilfe der docker-compose.yml im Verzeichnis src/main/resources/docker/cassandra das Cluster aufsetzen und starten:

```bash
docker-compose up -d
```

- Überprüfen des erfolgreichen Starts des Clusters. Das Cluster ist einsatzbereit, wenn alle Nodes mit `Status=Up, State=Normal` angezeigt werden.

```bash
docker exec node1 nodetool status
```
 
- Den consistency-checker starten und dabei die Konfigurationsdatei übergeben.
```bash
java -jar conchecker.jar configurationCassandra.json
```
