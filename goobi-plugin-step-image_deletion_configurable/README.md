---
description: >-
  Dieses Plugin löscht die Dateien eines Vorgangs.
---


# Löschen von Dateien eines Vorgangs

​
Dies ist eine technische Dokumentation für das Step-Plugin zum Löschen von Dateien eines Prozesses.

## Einführung

​
Das Plugin dient zum automatischen Löschen von Dateien eines Vorgangs. Hierzu kann in einer Konfigurationsdatei eingestellt werden, welche Daten gelöscht werden sollen.
​
| Details |  |
| :--- | :--- |
| Version | 1.0.0 |
| Identifier | intranda\_step\_imagedeletion |
| Source code |- Quellcode noch nicht öffentlich verfügbar  |
| Lizenz | GPL 2.0 oder neuer |
| Kompatibilität | Goobi workflow 2020.06 |
| Dokumentationsdatum | 19.06.2020 |
​

## Installation

​
Zur Nutzung des Plugins müssen diese beiden Dateien an folgende Orte kopiert werden:
​

```text
/opt/digiverso/goobi/plugins/step/plugin_intranda_step_image_deletion.jar
/opt/digiverso/goobi/config/plugin_intranda_step_imagedeletion.xml
```

## Konfiguration des Plugins

​
Zur Konfiguration dient die Datei `plugin_intranda_step_imagedeletion.xml`. Sie ist folgendermaßen aufgebaut:

```markup
<config_plugin>
    <!--
        order of configuration is:
          1.) project name and step name matches
          2.) step name matches and project is *
          3.) project name matches and step name is *
          4.) project name and step name are *
    -->
    <config>
        <project>*</project>
        <step>*</step>

        <!-- delete all data within the images/ folder -->
        <deleteAllContentFromImageDirectory>false</deleteAllContentFromImageDirectory>

        <!-- OR delete a single image folder - this is only used if deleteAllContentFromImageDirectory is set to false -->
        <deleteMediaDirectory>false</deleteMediaDirectory>
        <deleteMasterDirectory>false</deleteMasterDirectory>
        <deleteSourceDirectory>false</deleteSourceDirectory>
        <deleteFallbackDirectory>false</deleteFallbackDirectory>

        <!-- delete all data within the thumbs/ folder -->
        <deleteAllContentFromThumbsDirectory>false</deleteAllContentFromThumbsDirectory>

        <!-- delete all data within the ocr/ folder -->
        <deleteAllContentFromOcrDirectory>false</deleteAllContentFromOcrDirectory>

        <!-- OR delete a single ocr folder - this is only used if deleteAllContentFromOcrDirectory is set to false -->
        <deleteAltoDirectory>false</deleteAltoDirectory>
        <deletePdfDirectory>false</deletePdfDirectory>
        <deleteTxtDirectory>false</deleteTxtDirectory>
        <deleteWcDirectory>false</deleteWcDirectory>
        <deleteXmlDirectory>false</deleteXmlDirectory>

        <!-- delete export folder -->
        <deleteExportDirectory>false</deleteExportDirectory>

        <!-- delete import folder -->
        <deleteImportDirectory>false</deleteImportDirectory>

        <!-- delete processlog folder -->
        <deleteProcesslogDirectory>false</deleteProcesslogDirectory>

        <!-- delete metadata -->
        <deleteMetadataFiles>false</deleteMetadataFiles>

        <!-- deactivate all unfinished tasks -->
        <deactivateProcess>false</deactivateProcess>
    </config>
</config_plugin>
```

Die Parameter innerhalb dieser Konfigurationsdatei haben folgende Bedeutungen:

| Wert | Beschreibung |
| :--- | :--- |
| `project` | Dieser Parameter legt fest, für welches Projekt der aktuelle Block `<config>` gelten soll. Verwendet wird hierbei der Name des Projektes. Dieser Parameter kann mehrfach pro `<config>` Block vorkommen. |
| `step` | Dieser Parameter steuert, für welche Arbeitsschritte der Block `<config>` gelten soll. Verwendet wird hier der Name des Arbeitsschritts. Dieser Parameter kann mehrfach pro `<config>` Block vorkommen. |
| `deleteAllContentFromImageDirectory` | Legen Sie hier fest, ob alle Daten aus dem `images` Ordner gelöscht werden sollen. |
| `deleteMediaDirectory` | Legen Sie hier fest, ob der derivate-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromImageDirectory` aktiviert ist. |
| `deleteMasterDirectory` | Legen Sie hier fest, ob der master-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromImageDirectory` aktiviert ist. |
| `deleteSourceDirectory` | Legen Sie hier fest, ob der source-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromImageDirectory` aktiviert ist. |
| `deleteFallbackDirectory` | Legen Sie hier fest, ob der konfigurierte fallback-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromImageDirectory` aktiviert ist. |
| `deleteAllContentFromThumbsDirectory` | Legen Sie hier fest, ob alle Daten aus dem `thumbs` Ordner gelöscht werden sollen. |
| `deleteAllContentFromOcrDirectory` | Legen Sie hier fest, ob alle Daten aus dem `ocr` Ordner gelöscht werden sollen. |
| `deleteAltoDirectory` | Legen Sie hier fest, ob der ALTO-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromOcrDirectory` aktiviert ist. |
| `deletePdfDirectory` | Legen Sie hier fest, ob der PDF-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromOcrDirectory` aktiviert ist. |
| `deleteTxtDirectory` | Legen Sie hier fest, ob der TXT-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromOcrDirectory` aktiviert ist. |
| `deleteWcDirectory` | Legen Sie hier fest, ob der WC-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromOcrDirectory` aktiviert ist. |
| `deleteXmlDirectory` | Legen Sie hier fest, ob der XML-Ordner gelöscht werden soll. Diese Option wird nicht ausgewertet, wenn `deleteAllContentFromOcrDirectory` aktiviert ist. |
| `deleteExportDirectory` | Legen Sie hier fest, ob der export-Ordner gelöscht werden soll. |
| `deleteImportDirectory` | Legen Sie hier fest, ob der import-Ordner gelöscht werden soll. |
| `deleteProcesslogDirectory` | Legen Sie hier fest, ob der Ordner gelöscht werden soll, in der die Dateien verwaltet werden, die im Vorgangslog hochgeladen wurden. |
| `deleteMetadataFiles` | Legen Sie hier fest, ob die Metadaten und dazugehörigen Backups gelöscht werden sollen. |
| `deactivateProcess` | Wenn diese Option aktiviert wurde, werden alle Schritte des Vorgangs deaktiviert, wenn sie noch nicht abgeschlossen sind |

## Integration des Plugins in den Workflow

​
Zur Inbetriebnahme des Plugins muss dieses für einen oder mehrere gewünschte Aufgaben im Workflow aktiviert werden. Dies erfolgt durch Auswahl des Plugins `intranda_step_imagedeletion` aus der Liste der installierten Plugins. Zusätzlich muss die Checkbox `Automatische Aufgabe` aktiviert sein.

## Arbeitsweise und Bedienung des Plugins

Das Plugin wird automatisch ausgeführt, wenn der automatische Schritt erreicht wurde. Das Plugin prüft nun, welcher `config` Block aus der Konfigurationsdatei für diesen Schritt ausgewertet werden soll. Anschließend werden die dort konfigurierten Ordner und Dateien gelöscht, sofern sie vorhanden sind. Nicht vorhandene Daten werden übersprungen.
Wenn konfiguriert wurde, dass der Vorgang deaktiviert werden soll, werden alle Schritte durchlaufen. Wenn ein Schritt noch nicht abgeschlossen wurde, wird er an dieser Stelle deaktiviert.
Als letztes wird im Vorgangslog eine Meldung über den Aufruf des Plugins und das Löschen der Daten hinzugefügt.
