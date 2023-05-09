# rooms
M242 Miniprojekt - Raumklima Management


# Java Backend

Das Java Backend verbindet sich beim starten automatisch mit dem MQTT-Server `cloud.tbz.ch` . Auf diesem Server schreiben die Sensoren die gemessenen Daten. Damit die Daten einem Sensor zugeordnet werden können gibt es für jeden Messwert von jedem Sensor ein eigenes Topic.

| Sensor | Messgrössen | Topic |
|--------|----------|-------|
|Sensor 1|Temperatur|`rooms/sens1/temp`|
|Sensor 1|Luftfeuchtigkeit|`rooms/sens1/hum`|
|Sensor 2|Temperatur|`rooms/sens2/temp`|
|Sensor 2|Luftfeuchtigkeit|`rooms/sens1/hum`|

Aus diesen Werten wird für beide Messgrössen ein Durchschnitt berechnet, welcher wiederum für auf ein eigenes MQTT-Topic geschrieben wird. 
| Messgrössen| Topic |
|----------|-------|
|Temperatur|`rooms/avg/temp`|
|Luftfeuchtigkeit|`rooms/avg/hum`|

## Benachrichtigungen
Im Backend können Zeiten festgelegt werden, an denen mittels Telegram Chat und visueller Zeichen die Benutzer  aufgefordert werden zu Lüften. Beim Telegram Chat wird jeweils nur ein Nutzer aufgefordert die Fenster zu öffnen. Nach 5 minuten werden die nutzer mittels Visueller Signale aufgefordert die Fenster wieder zu schliessen. 
Unabhänig von den definierten Zeiten werden die Nutzer nach 50 minuten wiederum per Telegram und Visueller Signale aufgefordert zu lüften. Dies wird im Code als "Alarm" bezeichnet

Die Zeiten, an denen gelüftet werden soll, können in der Klasse `VentingAlarmService` definiert werden. 

### Telegram
Um die Benachrigtigungen zu erhalten muss im Kanal des Telegram Bots `@tbzRoomBot` `/subscribe` gesendet werden. 

### Core2
Damit auch die Core2-Geräte die Aufforderungen zum Lüften anzeigen können. Werden ebenfalls MQTT-Nachrichten verwendet. Nachfolgend die Nachrichten für die Entsprechenden Ereignisse.
| Ereignis | Topic | Message |
|--------|----------|-------|
|Benachrichtigung nach Plan|`rooms/venting`|`notification`|
|Alarm nach 50 Minuten|`rooms/venting`|`alarm`|

Auf den Core2 kann der User quittieren, dass er die Fenster geöffnet bzw. geschlossen hat. Auch diese ereignisse werden auf ein entsprechendes MQTT-Topic geschrieben. Nachfolgend die Nachrichtend für die Entsorechenden Ereignisse.
| Ereignis | Topic | Message |
|--------|----------|-------|
|Fenster geöffnet|`rooms/windows`|`opened`|
|Fenster geschlossen|`rooms/venting`|`closed`|

Das backend für den Core2 wurde mit c++ gecoded mit der Arduino library. 

Auf dem Core2 ist implementiert:
Temperatur sowie humidity anzeige
Einen Button sobald das Fenster geöffnet werden muss/ geschlossen werden muss

Im backend selbst ist implementiert:
Wie oben erwähnt das Lesen sowie Schreiben auf die Topics.
Dies ist mithilfe des MqttClients möglich welcher die funktion callback sowie die funktion publish beinhaltet. Subscribe um ein Mqtt Topic zu überwachen und falls etwas passiert die callback funktion ausführen und publish um etwas auf das topic zu pushen.

Ebenfalls ist das ganze auslesen der Sensoren sowie die trennung dieser auf verschiedene Topics hier implementiert. Dies haben wir mithilfe der MAC-Adressen unseren Core2 gemacht. Hierbei wird gecheckt welche MAC-Adresse der verbundene Core hat und dann dementsprechend wird das richtige Topic zugewiesen.


# Grafana

Mittels Grafana werden die gemessenen Daten auf einem Dashboard dargestellt. Um es einfach zu halten liest das Dashboard die Werte direkt von den `rooms/avg/*`   Topics. Die Temperatur- und Luftfeuchtigkeitswerte werden in separaten Diagrammen dargestellt.

Da das Dashboard auch auf die Topics, welche Benachrichtigungen für die Benutzer beinhalten, höhrt, kann ich Grafana Dashboard auch abgelesen werden, wann eine Benachrichtigung versendet wurde.
