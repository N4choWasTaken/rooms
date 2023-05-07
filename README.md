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
Unabhänig von den definierten Zeiten werden die Nutzer nach 50 minuten wiederum per Telegram und Visueller Signale aufgefordert zu lüften. 

Um die Benachrigtigungen zu erhalten muss im Kanal des Telegram Bots `@tbzRoomBot` `/subscribe` gesendet werden. 

Die Zeiten, an denen gelüftet werden soll, können in der Klasse `VentingAlarmService` definiert werden. 
