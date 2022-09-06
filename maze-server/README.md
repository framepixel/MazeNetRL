# Über

Dieses Projekt beinhaltet den Server und die GUI-Komponenten des Abschlusspraktikumsprojekt in Rechnernetze.
Das Programm startet ein Spiel für "Das verrückte Labyrinth" und wartet auf Spieler.
Nach jedem Zug wird überprüft, ob der Zug gültig ist, und im Fehlerfall wird erneut ein Zug angefragt.
Wenn ein Spieler gewinnt, endet die aktuelle Partie.
Der Server kommuniziert über Sockets und benutzt XML um Objekte zu serialisieren.
Die XML-Syntax wird definiert von der Schemadatei `src/main/resources/xsd/mazeCom.xsd`

# Build & Run

### Software-Voraussetzungen

Um das Projekt zu kompilieren wird folgende Software benötigt:

* Java11 - JDK
    * OpenJDK 11
* Maven 3.6

### Building:
```
mvn clean install
```

* `clean`: löscht alte Kompilate
* `install`: startet den kompletten life cycle mit validate, compile, test, install. Sorgt dafür, dass der Server als Dependency in anderen Maven Projekten verwendet werden kann

### Anwendung starten:
```
mvn [compile] exec:java [-Dexec.args="-c /home/max/myconfig.properties"]
```

* `compile` Kompiliert die aktuellen Änderungen
* `exec:java` führt das Maven-Plugin zum Starten aus. ACHTUNG: Es wird nicht kompiliert
* `-Dexec.args` gibt die Parameter bei der Ausführung an (String[] args)

# Releases  & Downloads

Die jar-Dateien der aktuellen und vorherigen Releases findet man unter [Tags](../../tags)

# Konfiguration

Details zur Konfiguration findet man [hier](../../wikis/doc/config.md)

# SSL/TLS

Details zum Einsatz verschlüsselter Verbindungen findet man [hier](../../wikis/doc/ssl.md)

# Contributions

Details wie Sie Code zum Projekt beitragen finden sie [hier](../../wikis/doc/contributions.md)

# Credits

* Application icon made by [Freepik](http://www.freepik.com) from [www.flaticon.com](http://www.flaticon.com) is licensed by [CC 3.0 BY](href="http://creativecommons.org/licenses/by/3.0/)
* A lot of the treasure-icons taken from [numix-Project](https://numixproject.org/)
* Gameidea orginally from [Ravensburger](https://www.ravensburger.de/produkte/spiele/familienspiele/das-verrueckte-labyrinth-26446/index.html)
* Music: "Cold Sober" Kevin MacLeod (incompetech.com) Licensed under [Creative Commons: By Attribution 3.0 License ](http://creativecommons.org/licenses/by/3.0/)