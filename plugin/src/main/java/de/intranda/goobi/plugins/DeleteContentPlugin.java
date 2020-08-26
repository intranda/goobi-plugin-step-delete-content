package de.intranda.goobi.plugins;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@Log4j2
public class DeleteContentPlugin implements IStepPluginVersion2 {

    @Getter
    private Step step;
    private Process process;

    @Getter
    private String title = "intranda_step_deleteContent";

    @Getter
    @Setter
    private boolean deleteAllContentFromImageDirectory;
    @Getter
    @Setter
    private boolean deleteAllContentFromThumbsDirectory;
    @Getter
    @Setter
    private boolean deleteAllContentFromOcrDirectory;

    @Getter
    @Setter
    private boolean deleteMediaDirectory;
    @Getter
    @Setter
    private boolean deleteMasterDirectory;
    @Getter
    @Setter
    private boolean deleteSourceDirectory;
    @Getter
    @Setter
    private boolean deleteFallbackDirectory;

    @Getter
    @Setter
    private boolean deleteAltoDirectory;
    @Getter
    @Setter
    private boolean deletePdfDirectory;
    @Getter
    @Setter
    private boolean deleteTxtDirectory;
    @Getter
    @Setter
    private boolean deleteWcDirectory;
    @Getter
    @Setter
    private boolean deleteXmlDirectory;

    @Getter
    @Setter
    private boolean deactivateProcess;

    @Getter
    @Setter
    private boolean deleteExportDirectory;
    @Getter
    @Setter
    private boolean deleteImportDirectory;
    @Getter
    @Setter
    private boolean deleteMetadataFiles;
    @Getter
    @Setter
    private boolean deleteProcesslogDirectory;

    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;
        this.process = step.getProzess();

        readConfiguration(process.getProjekt().getTitel(), step.getTitel());
    }

    private void readConfiguration(String projectName, String stepName) {
        SubnodeConfiguration config = ConfigPlugins.getProjectAndStepConfig(title, step);
        
        deleteAllContentFromImageDirectory = config.getBoolean("/deleteAllContentFromImageDirectory", false);
        deleteAllContentFromThumbsDirectory = config.getBoolean("/deleteAllContentFromThumbsDirectory", false);
        deleteAllContentFromOcrDirectory = config.getBoolean("/deleteAllContentFromOcrDirectory", false);
        deleteMediaDirectory = config.getBoolean("/deleteMediaDirectory", false);
        deleteMasterDirectory = config.getBoolean("/deleteMasterDirectory", false);
        deleteSourceDirectory = config.getBoolean("/deleteSourceDirectory", false);
        deleteFallbackDirectory = config.getBoolean("/deleteFallbackDirectory", false);

        deleteAltoDirectory = config.getBoolean("/deleteAltoDirectory", false);
        deletePdfDirectory = config.getBoolean("/deleteAltoDirectory", false);
        deleteTxtDirectory = config.getBoolean("/deleteTxtDirectory", false);
        deleteWcDirectory = config.getBoolean("/deleteWcDirectory", false);
        deleteXmlDirectory = config.getBoolean("/deleteXmlDirectory", false);

        deleteExportDirectory = config.getBoolean("/deleteExportDirectory", false);
        deleteImportDirectory = config.getBoolean("/deleteImportDirectory", false);
        deleteProcesslogDirectory = config.getBoolean("/deleteProcesslogDirectory", false);
        deleteMetadataFiles = config.getBoolean("/deleteMetadataFiles", false);

        deactivateProcess = config.getBoolean("/deactivateProcess", false);
    }

    @Override
    public boolean execute() {

        try {
            // list data in images/
            if (deleteAllContentFromImageDirectory) {
                String imageDirectoryName = process.getImagesDirectory();
                List<Path> contentOfImageDirectory = StorageProvider.getInstance().listFiles(imageDirectoryName);
                for (Path path : contentOfImageDirectory) {
                    if (StorageProvider.getInstance().isDirectory(path)) {
                        StorageProvider.getInstance().deleteDir(path);
                    } else {
                        StorageProvider.getInstance().deleteFile(path);
                    }
                }
            }

            // list data in thumbs/
            if (deleteAllContentFromThumbsDirectory) {
                String thumbDirectoryName = process.getThumbsDirectory();
                Path thumbs = Paths.get(thumbDirectoryName);
                if (StorageProvider.getInstance().isDirectory(thumbs)) {
                    StorageProvider.getInstance().deleteDir(thumbs);
                }
            }

            // list data in ocr/
            if (deleteAllContentFromOcrDirectory) {
                String orcDirectoryName = process.getOcrDirectory();
                Path ocr = Paths.get(orcDirectoryName);
                if (StorageProvider.getInstance().isDirectory(ocr)) {
                    StorageProvider.getInstance().deleteDir(ocr);
                }
            }
            // or delete single directories
            if (!deleteAllContentFromImageDirectory) {
                //                master
                if (deleteMasterDirectory) {
                    Path path = Paths.get(process.getImagesOrigDirectory(false));
                    if (StorageProvider.getInstance().isDirectory(path)) {
                        StorageProvider.getInstance().deleteDir(path);
                    }
                }
                //                media
                if (deleteMediaDirectory) {
                    Path path = Paths.get(process.getImagesTifDirectory(false));
                    if (StorageProvider.getInstance().isDirectory(path)) {
                        StorageProvider.getInstance().deleteDir(path);
                    }
                }
                //                fallback image folder
                if (deleteFallbackDirectory) {
                    Path path = Paths.get(process.getImagesTifDirectory(true));
                    if (StorageProvider.getInstance().isDirectory(path)) {
                        StorageProvider.getInstance().deleteDir(path);
                    }
                }

                //                source
                if (deleteSourceDirectory) {
                    Path path = Paths.get(process.getSourceDirectory());
                    if (StorageProvider.getInstance().isDirectory(path)) {
                        StorageProvider.getInstance().deleteDir(path);
                    }
                }
            }
            if (!deleteAllContentFromOcrDirectory) {
                if (deleteAltoDirectory) {
                    Path path = Paths.get(process.getOcrAltoDirectory());
                    if (StorageProvider.getInstance().isDirectory(path)) {
                        StorageProvider.getInstance().deleteDir(path);
                    }
                }
                if (deletePdfDirectory) {
                    Path path = Paths.get(process.getOcrPdfDirectory());
                    if (StorageProvider.getInstance().isDirectory(path)) {
                        StorageProvider.getInstance().deleteDir(path);
                    }
                }

                if (deleteTxtDirectory) {
                    Path path = Paths.get(process.getOcrTxtDirectory());
                    if (StorageProvider.getInstance().isDirectory(path)) {
                        StorageProvider.getInstance().deleteDir(path);
                    }
                }

                if (deleteWcDirectory) {
                    Path path = Paths.get(process.getOcrWcDirectory());
                    if (StorageProvider.getInstance().isDirectory(path)) {
                        StorageProvider.getInstance().deleteDir(path);
                    }
                }

                if (deleteXmlDirectory) {
                    Path path = Paths.get(process.getOcrXmlDirectory());
                    if (StorageProvider.getInstance().isDirectory(path)) {
                        StorageProvider.getInstance().deleteDir(path);
                    }
                }
            }
            if (deleteExportDirectory) {

                Path path = Paths.get(process.getExportDirectory());
                if (StorageProvider.getInstance().isDirectory(path)) {
                    StorageProvider.getInstance().deleteDir(path);
                }
            }
            if (deleteImportDirectory) {
                Path path = Paths.get(process.getImportDirectory());
                if (StorageProvider.getInstance().isDirectory(path)) {
                    StorageProvider.getInstance().deleteDir(path);
                }
            }

            if (deleteMetadataFiles) {
                List<Path> filesInFolder = StorageProvider.getInstance().listFiles(process.getProcessDataDirectory(), NIOFileUtils.fileFilter);
                for (Path path : filesInFolder) {
                    StorageProvider.getInstance().deleteFile(path);
                }
            }
            if (deleteProcesslogDirectory) {
                Path path = Paths.get(process.getProcessDataDirectory(), ConfigurationHelper.getInstance().getFolderForInternalProcesslogFiles());
                if (StorageProvider.getInstance().isDirectory(path)) {
                    StorageProvider.getInstance().deleteDir(path);
                }
            }

        } catch (IOException | InterruptedException | SwapException | DAOException e) {
            log.error(e);
            Helper.setFehlerMeldung("Error during deletion", e);
            LogEntry.build(process.getId())
            .withContent("Error during file deletion in task " + step.getTitel() + ": " + e.getMessage())
            .withType(LogType.ERROR)
            .persist();
            return false;
        }

        if (deactivateProcess) {
            for (Step other : process.getSchritte()) {
                if (!other.getTitel().equals(step.getTitel()) && other.getBearbeitungsstatusEnum() != StepStatus.DONE) {
                    other.setBearbeitungsstatusEnum(StepStatus.DEACTIVATED);
                }
            }
            try {
                ProcessManager.saveProcess(process);
            } catch (DAOException e) {
                log.error(e);
                Helper.setFehlerMeldung("Error process deactivation", e);
                LogEntry.build(process.getId()).withContent("Error process deactivation: " + e.getMessage()).withType(LogType.ERROR).persist();
                return false;
            }
        }

        LogEntry.build(process.getId()).withContent("Files where automatically deleted in task " + step.getTitel()).withType(LogType.INFO).persist();

        return true;
    }

    @Override
    public String cancel() {
        return null;
    }

    @Override
    public String finish() {
        return null;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.NONE;
    }

    @Override
    public String getPagePath() {
        return null;
    }

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public PluginReturnValue run() {
        return execute() == true ? PluginReturnValue.FINISH : PluginReturnValue.ERROR;
    }

    @Override
    public int getInterfaceVersion() {
        return 0;
    }

}
