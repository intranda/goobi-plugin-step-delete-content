package de.intranda.goobi.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfigPlugins.class, StepManager.class, ConfigurationHelper.class, ProcessManager.class, PropertyManager.class })
@PowerMockIgnore({ "javax.management.*" })
public class DeleteContentPluginTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File processDirectory;
    private File metadataDirectory;
    private Process process;

    @Before
    public void setUp() throws Exception {
        metadataDirectory = folder.newFolder("metadata");
        processDirectory = new File(metadataDirectory + File.separator + "1");
        processDirectory.mkdirs();
        String metadataDirectoryName = metadataDirectory.getAbsolutePath() + File.separator;

        XMLConfiguration config = getConfig();
        PowerMock.mockStatic(ConfigPlugins.class);
        EasyMock.expect(ConfigPlugins.getPluginConfig(EasyMock.anyString())).andReturn(config).anyTimes();
        PowerMock.replay(ConfigPlugins.class);

        PowerMock.mockStatic(ConfigurationHelper.class);
        ConfigurationHelper configurationHelper = EasyMock.createMock(ConfigurationHelper.class);
        EasyMock.expect(ConfigurationHelper.getInstance()).andReturn(configurationHelper).anyTimes();
        EasyMock.expect(configurationHelper.getMetsEditorLockingTime()).andReturn(1800000l).anyTimes();
        EasyMock.expect(configurationHelper.isAllowWhitespacesInFolder()).andReturn(false).anyTimes();
        EasyMock.expect(configurationHelper.useS3()).andReturn(false).anyTimes();
        EasyMock.expect(configurationHelper.isUseMasterDirectory()).andReturn(true).anyTimes();
        EasyMock.expect(configurationHelper.isCreateMasterDirectory()).andReturn(false).anyTimes();
        EasyMock.expect(configurationHelper.isCreateSourceFolder()).andReturn(false).anyTimes();
        EasyMock.expect(configurationHelper.getProcessImagesMasterDirectoryName()).andReturn("master_fixture_media").anyTimes();
        EasyMock.expect(configurationHelper.getProcessImagesMainDirectoryName()).andReturn("fixture_media").anyTimes();
        EasyMock.expect(configurationHelper.getProcessImagesSourceDirectoryName()).andReturn("fixture_source").anyTimes();

        EasyMock.expect(configurationHelper.getProcessOcrAltoDirectoryName()).andReturn("fixture_alto").anyTimes();
        EasyMock.expect(configurationHelper.getProcessOcrPdfDirectoryName()).andReturn("fixture_pdf").anyTimes();
        EasyMock.expect(configurationHelper.getProcessExportDirectoryName()).andReturn("export").anyTimes();
        EasyMock.expect(configurationHelper.getProcessImportDirectoryName()).andReturn("import").anyTimes();

        EasyMock.expect(configurationHelper.getFolderForInternalJournalFiles()).andReturn("intern").anyTimes();
        EasyMock.expect(configurationHelper.getMetadataFolder()).andReturn(metadataDirectoryName).anyTimes();

        EasyMock.expect(configurationHelper.getScriptCreateDirMeta()).andReturn("").anyTimes();
        EasyMock.replay(configurationHelper);

        PowerMock.mockStatic(StepManager.class);
        PowerMock.mockStatic(ProcessManager.class);
        PowerMock.mockStatic(PropertyManager.class);
        PowerMock.replay(ConfigPlugins.class);
        PowerMock.replay(ConfigurationHelper.class);

        process = getProcess();

    }

    private void createProcessDirectory(boolean createSourceDirectory, boolean createThumbsDirectory, boolean createOcrDirectory) throws IOException {

        // image folder
        File imageDirectory = new File(processDirectory.getAbsolutePath(), "images");
        imageDirectory.mkdir();
        // master folder
        File masterDirectory = new File(imageDirectory.getAbsolutePath(), "master_fixture_media");
        masterDirectory.mkdir();
        File masterImageFile = new File(masterDirectory.getAbsolutePath(), "0001.tif");
        masterImageFile.createNewFile();
        // media folder
        File mediaDirectory = new File(imageDirectory.getAbsolutePath(), "fixture_media");
        mediaDirectory.mkdir();
        File mediaImageFile = new File(mediaDirectory.getAbsolutePath(), "0001.tif");
        mediaImageFile.createNewFile();
        if (createSourceDirectory) {
            // source folder
            File sourceDirectory = new File(imageDirectory.getAbsolutePath(), "fixture_source");
            sourceDirectory.mkdir();
            File sourceFile = new File(sourceDirectory.getAbsolutePath(), "fixture.zip");
            sourceFile.createNewFile();
        }
        // thumbs
        if (createThumbsDirectory) {
            File thumbsDirectory = new File(processDirectory.getAbsolutePath(), "thumbs");
            thumbsDirectory.mkdir();
            File thumbsMediaDirectory = new File(thumbsDirectory.getAbsolutePath(), "fixture_media_800");
            thumbsMediaDirectory.mkdir();
            File thumbsImageFile = new File(thumbsMediaDirectory.getAbsolutePath(), "0001.tif");
            thumbsImageFile.createNewFile();
        }
        // ocr
        if (createOcrDirectory) {
            File ocr = new File(processDirectory.getAbsolutePath(), "ocr");
            ocr.mkdir();
            File altoDirectory = new File(ocr.getAbsolutePath(), "fixture_alto");
            altoDirectory.mkdir();
            File altoFile = new File(altoDirectory.getAbsolutePath(), "0001.xml");
            altoFile.createNewFile();
        }
    }

    @Test
    public void testConstructor() {
        DeleteContentPlugin plugin = new DeleteContentPlugin();
        assertNotNull(plugin);
    }

    @Test
    public void testInitialize() {

        Step imageDeletionStep = process.getSchritte().get(1);

        DeleteContentPlugin plugin = new DeleteContentPlugin();
        plugin.initialize(imageDeletionStep, "somewhere");

        assertEquals(imageDeletionStep.getTitel(), plugin.getStep().getTitel());

        // test config
        assertEquals(false, plugin.isDeleteAllContentFromImageDirectory());
        assertEquals(false, plugin.isDeleteAllContentFromOcrDirectory());
        assertEquals(false, plugin.isDeleteAllContentFromThumbsDirectory());

        assertEquals(false, plugin.isDeleteFallbackDirectory());
        assertEquals(false, plugin.isDeleteMasterDirectory());
        assertEquals(false, plugin.isDeleteMediaDirectory());
        assertEquals(false, plugin.isDeleteSourceDirectory());

        assertEquals(false, plugin.isDeactivateProcess());
    }

    @Test
    public void testDeleteNothing() throws Exception {
        createProcessDirectory(true, true, true);

        DeleteContentPlugin plugin = new DeleteContentPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");
        plugin.run();
        String masterDirectory = process.getImagesOrigDirectory(false);
        String mediaDirectory = process.getImagesTifDirectory(false);
        String sourceDirectory = process.getSourceDirectory();
        String thumbsDirectory = process.getThumbsDirectory();
        String altoDirectory = process.getOcrAltoDirectory();
        assertTrue(Files.exists(Paths.get(masterDirectory)));
        assertTrue(Files.exists(Paths.get(mediaDirectory)));
        assertTrue(Files.exists(Paths.get(sourceDirectory)));
        assertTrue(Files.exists(Paths.get(thumbsDirectory)));
        assertTrue(Files.exists(Paths.get(altoDirectory)));
    }

    @Test
    public void testDeleteAllFiles() throws Exception {
        createProcessDirectory(true, true, true);

        DeleteContentPlugin plugin = new DeleteContentPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");

        String masterDirectory = process.getImagesOrigDirectory(false);
        String mediaDirectory = process.getImagesTifDirectory(false);
        String sourceDirectory = process.getSourceDirectory();
        String thumbsDirectory = process.getThumbsDirectory();
        String altoDirectory = process.getOcrAltoDirectory();
        assertTrue(Files.exists(Paths.get(masterDirectory)));
        assertTrue(Files.exists(Paths.get(mediaDirectory)));
        assertTrue(Files.exists(Paths.get(sourceDirectory)));
        assertTrue(Files.exists(Paths.get(thumbsDirectory)));
        assertTrue(Files.exists(Paths.get(altoDirectory)));

        plugin.setDeleteAllContentFromImageDirectory(true);
        plugin.setDeleteAllContentFromOcrDirectory(true);
        plugin.setDeleteAllContentFromThumbsDirectory(true);
        plugin.run();

        assertFalse(Files.exists(Paths.get(masterDirectory)));
        assertFalse(Files.exists(Paths.get(mediaDirectory)));
        assertFalse(Files.exists(Paths.get(sourceDirectory)));
        assertFalse(Files.exists(Paths.get(thumbsDirectory)));
        assertFalse(Files.exists(Paths.get(altoDirectory)));
    }

    @Test
    public void testDeleteAllImagesDirectory() throws Exception {
        createProcessDirectory(true, true, true);

        DeleteContentPlugin plugin = new DeleteContentPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");

        String masterDirectory = process.getImagesOrigDirectory(false);
        String mediaDirectory = process.getImagesTifDirectory(false);
        String sourceDirectory = process.getSourceDirectory();
        String thumbsDirectory = process.getThumbsDirectory();
        String altoDirectory = process.getOcrAltoDirectory();
        assertTrue(Files.exists(Paths.get(masterDirectory)));
        assertTrue(Files.exists(Paths.get(mediaDirectory)));
        assertTrue(Files.exists(Paths.get(sourceDirectory)));
        assertTrue(Files.exists(Paths.get(thumbsDirectory)));
        assertTrue(Files.exists(Paths.get(altoDirectory)));

        plugin.setDeleteAllContentFromImageDirectory(true);
        plugin.setDeleteAllContentFromOcrDirectory(false);
        plugin.setDeleteAllContentFromThumbsDirectory(false);
        plugin.run();

        assertFalse(Files.exists(Paths.get(masterDirectory)));
        assertFalse(Files.exists(Paths.get(mediaDirectory)));
        assertFalse(Files.exists(Paths.get(sourceDirectory)));
        assertTrue(Files.exists(Paths.get(thumbsDirectory)));
        assertTrue(Files.exists(Paths.get(altoDirectory)));
    }

    @Test
    public void testDeleteMasterDirectory() throws Exception {
        createProcessDirectory(true, true, true);

        DeleteContentPlugin plugin = new DeleteContentPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");

        String masterDirectory = process.getImagesOrigDirectory(false);
        String mediaDirectory = process.getImagesTifDirectory(false);
        String sourceDirectory = process.getSourceDirectory();
        String thumbsDirectory = process.getThumbsDirectory();
        String altoDirectory = process.getOcrAltoDirectory();
        assertTrue(Files.exists(Paths.get(masterDirectory)));
        assertTrue(Files.exists(Paths.get(mediaDirectory)));
        assertTrue(Files.exists(Paths.get(sourceDirectory)));
        assertTrue(Files.exists(Paths.get(thumbsDirectory)));
        assertTrue(Files.exists(Paths.get(altoDirectory)));

        plugin.setDeleteMasterDirectory(true);
        plugin.run();

        assertFalse(Files.exists(Paths.get(masterDirectory)));
        assertTrue(Files.exists(Paths.get(mediaDirectory)));
        assertTrue(Files.exists(Paths.get(sourceDirectory)));
        assertTrue(Files.exists(Paths.get(thumbsDirectory)));
        assertTrue(Files.exists(Paths.get(altoDirectory)));
    }

    @Test
    public void testDeleteAltoDirectory() throws Exception {
        createProcessDirectory(true, true, true);

        DeleteContentPlugin plugin = new DeleteContentPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");

        String masterDirectory = process.getImagesOrigDirectory(false);
        String mediaDirectory = process.getImagesTifDirectory(false);
        String sourceDirectory = process.getSourceDirectory();
        String thumbsDirectory = process.getThumbsDirectory();
        String altoDirectory = process.getOcrAltoDirectory();
        assertTrue(Files.exists(Paths.get(masterDirectory)));
        assertTrue(Files.exists(Paths.get(mediaDirectory)));
        assertTrue(Files.exists(Paths.get(sourceDirectory)));
        assertTrue(Files.exists(Paths.get(thumbsDirectory)));
        assertTrue(Files.exists(Paths.get(altoDirectory)));

        plugin.setDeleteAltoDirectory(true);
        plugin.run();

        assertTrue(Files.exists(Paths.get(masterDirectory)));
        assertTrue(Files.exists(Paths.get(mediaDirectory)));
        assertTrue(Files.exists(Paths.get(sourceDirectory)));
        assertTrue(Files.exists(Paths.get(thumbsDirectory)));
        assertFalse(Files.exists(Paths.get(altoDirectory)));
    }

    @Test
    public void testDeleteNonExistingDirectory() throws Exception {
        createProcessDirectory(true, true, true);

        DeleteContentPlugin plugin = new DeleteContentPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");

        String masterDirectory = process.getImagesOrigDirectory(false);
        String mediaDirectory = process.getImagesTifDirectory(false);
        String sourceDirectory = process.getSourceDirectory();
        String thumbsDirectory = process.getThumbsDirectory();
        String altoDirectory = process.getOcrAltoDirectory();
        assertTrue(Files.exists(Paths.get(masterDirectory)));
        assertTrue(Files.exists(Paths.get(mediaDirectory)));
        assertTrue(Files.exists(Paths.get(sourceDirectory)));
        assertTrue(Files.exists(Paths.get(thumbsDirectory)));
        assertTrue(Files.exists(Paths.get(altoDirectory)));

        plugin.setDeletePdfDirectory(true);
        plugin.run();

        assertTrue(Files.exists(Paths.get(masterDirectory)));
        assertTrue(Files.exists(Paths.get(mediaDirectory)));
        assertTrue(Files.exists(Paths.get(sourceDirectory)));
        assertTrue(Files.exists(Paths.get(thumbsDirectory)));
        assertTrue(Files.exists(Paths.get(altoDirectory)));
    }

    @Test
    public void testDeactivateProcess() throws Exception {
        DeleteContentPlugin plugin = new DeleteContentPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");
        Step stepToDeactivate = process.getSchritte().get(2);
        assertEquals(StepStatus.LOCKED, stepToDeactivate.getBearbeitungsstatusEnum());

        plugin.setDeactivateProcess(true);
        plugin.run();

        assertEquals(StepStatus.DEACTIVATED, stepToDeactivate.getBearbeitungsstatusEnum());

    }

    @Test
    public void testDeleteExportImportDirectories() throws Exception {
        createProcessDirectory(true, true, true);

        File exportDirectory = new File(processDirectory.getAbsolutePath(), "export");
        exportDirectory.mkdir();
        File importDirectory = new File(processDirectory.getAbsolutePath(), "import");
        importDirectory.mkdir();
        File internalDirectory = new File(processDirectory.getAbsolutePath(), "intern");
        internalDirectory.mkdir();

        DeleteContentPlugin plugin = new DeleteContentPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");

        String exportName = process.getExportDirectory();
        String importName = process.getImportDirectory();
        String internName = internalDirectory.getAbsolutePath();
        assertTrue(Files.exists(Paths.get(exportName)));
        assertTrue(Files.exists(Paths.get(importName)));
        assertTrue(Files.exists(Paths.get(internName)));

        plugin.setDeleteExportDirectory(true);
        plugin.setDeleteImportDirectory(true);
        plugin.setDeleteProcesslogDirectory(true);
        plugin.run();

        assertFalse(Files.exists(Paths.get(exportName)));
        assertFalse(Files.exists(Paths.get(importName)));
        assertFalse(Files.exists(Paths.get(internName)));

    }

    @Test
    public void testDeleteMetadata() throws Exception {
        createProcessDirectory(true, true, true);

        File metaXml = new File(processDirectory.getAbsolutePath(), "meta.xml");
        metaXml.createNewFile();
        File metaXmlBackup = new File(processDirectory.getAbsolutePath(), "meta.xml.1");
        metaXmlBackup.createNewFile();
        File anchorXml = new File(processDirectory.getAbsolutePath(), "meta_anchor.xml");
        anchorXml.createNewFile();
        File anchorXmlBackup = new File(processDirectory.getAbsolutePath(), "meta_anchor.xml.1");
        anchorXmlBackup.createNewFile();

        DeleteContentPlugin plugin = new DeleteContentPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");

        assertTrue(Files.exists(Paths.get(metaXml.getAbsolutePath())));
        assertTrue(Files.exists(Paths.get(metaXmlBackup.getAbsolutePath())));
        assertTrue(Files.exists(Paths.get(anchorXml.getAbsolutePath())));
        assertTrue(Files.exists(Paths.get(anchorXmlBackup.getAbsolutePath())));

        plugin.setDeleteMetadataFiles(true);
        plugin.run();

        assertFalse(Files.exists(Paths.get(metaXml.getAbsolutePath())));
        assertFalse(Files.exists(Paths.get(metaXmlBackup.getAbsolutePath())));
        assertFalse(Files.exists(Paths.get(anchorXml.getAbsolutePath())));
        assertFalse(Files.exists(Paths.get(anchorXmlBackup.getAbsolutePath())));

    }

    public Process getProcess() {
        Project project = new Project();
        project.setTitel("projectName");
        project.setId(1);

        Process process = new Process();
        process.setTitel("fixture");
        process.setProjekt(project);
        process.setId(1);
        List<Step> steps = new ArrayList<>();
        Step s1 = new Step();
        s1.setReihenfolge(1);
        s1.setProzess(process);
        s1.setTitel("closed step");
        s1.setBearbeitungsstatusEnum(StepStatus.DONE);
        User user = new User();
        user.setVorname("Firstname");
        user.setNachname("Lastname");
        s1.setBearbeitungsbenutzer(user);
        steps.add(s1);

        Step s2 = new Step();
        s2.setReihenfolge(2);
        s2.setProzess(process);
        s2.setTitel("Image deletion step");
        s2.setStepPlugin("intranda_step_imagedeletion");
        s2.setBearbeitungsstatusEnum(StepStatus.OPEN);
        s2.setTypAutomatisch(true);
        steps.add(s2);

        Step s3 = new Step();
        s3.setReihenfolge(3);
        s3.setProzess(process);
        s3.setTitel("test step to deactivate");
        s3.setBearbeitungsstatusEnum(StepStatus.LOCKED);
        steps.add(s3);

        process.setSchritte(steps);

        return process;
    }

    private XMLConfiguration getConfig() throws Exception {
        XMLConfiguration config = new XMLConfiguration("src/test/resources/plugin_intranda_step_imagedeletion.xml");
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;

    }
}
