---
title: Inhalte löschen
identifier: intranda_step_deleteContent
published: true
description: Dieses Step Plugin ermöglicht das automatische selektive Löschen von Inhalten aus einem Vorgang.
---
## Einführung
Das Plugin dient zum automatischen Löschen von Daten eines Vorgangs. Hierzu kann in einer Konfigurationsdatei sehr granular festgelegt werden, welche Daten genau gelöscht werden sollen.


## Installation
Zur Installation des Plugins muss die folgende Datei installiert werden:

```bash
/opt/digiverso/goobi/plugins/step/plugin_intranda_step_deleteContent-base.jar
```

Um zu konfigurieren, wie sich das Plugin verhalten soll, können verschiedene Werte in der Konfigurationsdatei angepasst werden. Die Konfigurationsdatei befindet sich üblicherweise hier:

```bash
/opt/digiverso/goobi/config/plugin_intranda_step_deleteContent.xml
```


## Überblick und Funktionsweise
Zur Inbetriebnahme des Plugins muss dieses für einen oder mehrere gewünschte Aufgaben im Workflow aktiviert werden. Dies erfolgt wie im folgenden Screenshot aufgezeigt durch Auswahl des Plugins `intranda_step_deleteContent` aus der Liste der installierten Plugins.

![Zuweisung des Plugins zu einer bestimmten Aufgabe](screen1_de.png)

Da dieses Plugin üblicherweise automatisch ausgeführt werden soll, sollte der Arbeitsschritt im Workflow als automatisch konfiguriert werden.

Nachdem das Plugin vollständig installiert und eingerichtet wurde, wird es üblicherweise automatisch innerhalb des Workflows ausgeführt, so dass keine manuelle Interaktion mit dem Nutzer erfolgt. Stattdessen erfolgt der Aufruf des Plugins durch den Workflow im Hintergrund und startet die Löschung der konfigurierten Daten. Hierbei werden die konfigurierten Ordner und Daten gelöscht, sofern diese vorhanden sind. Nicht vorhandene Daten werden übersprungen. Wenn konfiguriert wurde, dass der Vorgang deaktiviert werden soll, werden alle Arbeitsschritte durchlaufen und geprüft, ob diese bereits innerhalb des Workflows regulär geschlossen wurden. Sollte dies nicht der Fall sein, wird der Arbeitsschritt deaktiviert.

Nach Abschluss der Löschung wird im Vorgangslog eine Meldung über den Aufruf dieses Plugins und das Löschen der Daten hinzugefügt.


## Konfiguration
Die Konfiguration des Plugins ist folgendermaßen aufgebaut:

{{CONFIG_CONTENT}}

{{CONFIG_DESCRIPTION_PROJECT_STEP}}

| Parameter | Erläuterung |
| :--- | :--- |
| `deleteAllContentFromImageDirectory` | Legen Sie hier fest, ob alle Daten aus dem `images` Ordner gelöscht werden sollen. |
| `deleteMediaDirectory` | Legen Sie hier fest, ob der `media`-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromImageDirectory` aktiviert ist. |
| `deleteMasterDirectory` | Legen Sie hier fest, ob der `master`-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromImageDirectory` aktiviert ist. |
| `deleteSourceDirectory` | Legen Sie hier fest, ob der `source`-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromImageDirectory` aktiviert ist. |
| `deleteFallbackDirectory` | Legen Sie hier fest, ob der konfigurierte fallback-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromImageDirectory` aktiviert ist. |
| `deleteAllContentFromThumbsDirectory` | Legen Sie hier fest, ob alle Daten aus dem `thumbs` Ordner gelöscht werden sollen. |
| `deleteAllContentFromOcrDirectory` | Legen Sie hier fest, ob alle Daten aus dem `ocr` Ordner gelöscht werden sollen. |
| `deleteAltoDirectory` | Legen Sie hier fest, ob der `alto`-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromOcrDirectory` aktiviert ist. |
| `deletePdfDirectory` | Legen Sie hier fest, ob der `pdf`-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromOcrDirectory` aktiviert ist. |
| `deleteTxtDirectory` | Legen Sie hier fest, ob der `txt`-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromOcrDirectory` aktiviert ist. |
| `deleteWcDirectory` | Legen Sie hier fest, ob der `wc`-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromOcrDirectory` aktiviert ist. |
| `deleteXmlDirectory` | Legen Sie hier fest, ob der `xml`-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromOcrDirectory` aktiviert ist. |
| `deleteExportDirectory` | Legen Sie hier fest, ob der `export`-Ordner gelöscht werden soll. |
| `deleteImportDirectory` | Legen Sie hier fest, ob der `import`-Ordner gelöscht werden soll. |
| `deleteProcesslogDirectory` | Legen Sie hier fest, ob der Ordner gelöscht werden soll, in dem die Dateien verwaltet werden, die im Vorgangslog hochgeladen wurden. |
| `deleteMetadataFiles` | Legen Sie hier fest, ob die Metadaten und dazugehörigen Backups gelöscht werden sollen. |
| `deactivateProcess` | Wenn diese Option aktiviert wurde, werden alle Schritte des Vorgangs deaktiviert, wenn diese zuvor nicht bereits abgeschlossen wurden. |
| `deleteMetadata` | Hier kann ein bestimmtes Metadatum gelöscht werden, das sich auf der Ebene des Werkes in der Metadatendatei befindet. Das Element ist wiederholbar und muss einen gültigen Namen für ein Metadatentyp aus dem Regelsatz verwenden. |
| `deleteProperty` | Hier kann eine bestimmte Vorgangseigenschaft gelöscht werden., Das Element ist wiederholbar und muss den Namen der Eigenschaft aufführen. |
