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
public class ImageDeletionPluginTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File processFolder;
    private File metadataFolder;
    private Process process;

    @Before
    public void setUp() throws Exception {
        metadataFolder = folder.newFolder("metadata");
        processFolder = new File(metadataFolder + File.separator + "1");
        processFolder.mkdirs();
        String metadataFolderName = metadataFolder.getAbsolutePath() + File.separator;

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
        EasyMock.expect(configurationHelper.getMediaDirectorySuffix()).andReturn("media").anyTimes();
        EasyMock.expect(configurationHelper.getMasterDirectoryPrefix()).andReturn("master").anyTimes();

        EasyMock.expect(configurationHelper.getMetadataFolder()).andReturn(metadataFolderName).anyTimes();

        EasyMock.expect(configurationHelper.getScriptCreateDirMeta()).andReturn("").anyTimes();
        EasyMock.replay(configurationHelper);

        PowerMock.mockStatic(StepManager.class);
        PowerMock.mockStatic(ProcessManager.class);
        PowerMock.mockStatic(PropertyManager.class);
        PowerMock.replay(ConfigPlugins.class);
        PowerMock.replay(ConfigurationHelper.class);

        process = getProcess();

    }

    private void createProcessFolder(boolean createSourceFolder, boolean createThumbsFolder, boolean createOcrFolder) throws IOException {

        // image folder
        File imageFolder = new File(processFolder.getAbsolutePath(), "images");
        imageFolder.mkdir();
        // master folder
        File masterFolder = new File(imageFolder.getAbsolutePath(), "master_fixture_media");
        masterFolder.mkdir();
        File masterImageFile = new File(masterFolder.getAbsolutePath(), "0001.tif");
        masterImageFile.createNewFile();
        // media folder
        File mediaFolder = new File(imageFolder.getAbsolutePath(), "fixture_media");
        mediaFolder.mkdir();
        File mediaImageFile = new File(mediaFolder.getAbsolutePath(), "0001.tif");
        mediaImageFile.createNewFile();
        if (createSourceFolder) {
            // source folder
            File sourceFolder = new File(imageFolder.getAbsolutePath(), "fixture_source");
            sourceFolder.mkdir();
            File sourceFile = new File(sourceFolder.getAbsolutePath(), "fixture.zip");
            sourceFile.createNewFile();
        }
        // thumbs
        if (createThumbsFolder) {
            File thumbsFolder = new File(processFolder.getAbsolutePath(), "thumbs");
            thumbsFolder.mkdir();
            File thumbsMediaFolder = new File(thumbsFolder.getAbsolutePath(), "fixture_media_800");
            thumbsMediaFolder.mkdir();
            File thumbsImageFile = new File(thumbsMediaFolder.getAbsolutePath(), "0001.tif");
            thumbsImageFile.createNewFile();
        }
        // ocr
        if (createOcrFolder) {
            File ocr = new File(processFolder.getAbsolutePath(), "ocr");
            ocr.mkdir();
            File altoFolder = new File(ocr.getAbsolutePath(), "fixture_alto");
            altoFolder.mkdir();
            File altoFile = new File(altoFolder.getAbsolutePath(), "0001.xml");
            altoFile.createNewFile();
        }
    }

    @Test
    public void testConstructor() {
        ImageDeletionPlugin plugin = new ImageDeletionPlugin();
        assertNotNull(plugin);
    }

    @Test
    public void testInitialize() {

        Step imageDeletionStep = process.getSchritte().get(1);

        ImageDeletionPlugin plugin = new ImageDeletionPlugin();
        plugin.initialize(imageDeletionStep, "somewhere");

        assertEquals(imageDeletionStep.getTitel(), plugin.getStep().getTitel());

        // test config
        assertEquals(false, plugin.isDeleteAllContentFromImageFolder());
        assertEquals(false, plugin.isDeleteAllContentFromOcrFolder());
        assertEquals(false, plugin.isDeleteAllContentFromThumbsFolder());

        assertEquals(false, plugin.isDeleteFallbackFolder());
        assertEquals(false, plugin.isDeleteMasterFolder());
        assertEquals(false, plugin.isDeleteMediaFolder());
        assertEquals(false, plugin.isDeleteSourceFolder());

        assertEquals(false, plugin.isDeactivateProcess());
    }

    @Test
    public void testDeleteNothing() throws Exception {
        createProcessFolder(true, true, true);

        ImageDeletionPlugin plugin = new ImageDeletionPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");
        plugin.run();
        String masterFolder = process.getImagesOrigDirectory(false);
        String mediaFolder = process.getImagesTifDirectory(false);
        String sourceFolder = process.getSourceDirectory();
        String thumbsFolder = process.getThumbsDirectory();
        String altoFolder = process.getOcrAltoDirectory();
        assertTrue(Files.exists(Paths.get(masterFolder)));
        assertTrue(Files.exists(Paths.get(mediaFolder)));
        assertTrue(Files.exists(Paths.get(sourceFolder)));
        assertTrue(Files.exists(Paths.get(thumbsFolder)));
        assertTrue(Files.exists(Paths.get(altoFolder)));
    }

    @Test
    public void testDeleteAllFiles() throws Exception {
        createProcessFolder(true, true, true);

        ImageDeletionPlugin plugin = new ImageDeletionPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");

        String masterFolder = process.getImagesOrigDirectory(false);
        String mediaFolder = process.getImagesTifDirectory(false);
        String sourceFolder = process.getSourceDirectory();
        String thumbsFolder = process.getThumbsDirectory();
        String altoFolder = process.getOcrAltoDirectory();
        assertTrue(Files.exists(Paths.get(masterFolder)));
        assertTrue(Files.exists(Paths.get(mediaFolder)));
        assertTrue(Files.exists(Paths.get(sourceFolder)));
        assertTrue(Files.exists(Paths.get(thumbsFolder)));
        assertTrue(Files.exists(Paths.get(altoFolder)));

        plugin.setDeleteAllContentFromImageFolder(true);
        plugin.setDeleteAllContentFromOcrFolder(true);
        plugin.setDeleteAllContentFromThumbsFolder(true);
        plugin.run();

        assertFalse(Files.exists(Paths.get(masterFolder)));
        assertFalse(Files.exists(Paths.get(mediaFolder)));
        assertFalse(Files.exists(Paths.get(sourceFolder)));
        assertFalse(Files.exists(Paths.get(thumbsFolder)));
        assertFalse(Files.exists(Paths.get(altoFolder)));
    }


    @Test
    public void testDeleteAllImagesFolder() throws Exception {
        createProcessFolder(true, true, true);

        ImageDeletionPlugin plugin = new ImageDeletionPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");

        String masterFolder = process.getImagesOrigDirectory(false);
        String mediaFolder = process.getImagesTifDirectory(false);
        String sourceFolder = process.getSourceDirectory();
        String thumbsFolder = process.getThumbsDirectory();
        String altoFolder = process.getOcrAltoDirectory();
        assertTrue(Files.exists(Paths.get(masterFolder)));
        assertTrue(Files.exists(Paths.get(mediaFolder)));
        assertTrue(Files.exists(Paths.get(sourceFolder)));
        assertTrue(Files.exists(Paths.get(thumbsFolder)));
        assertTrue(Files.exists(Paths.get(altoFolder)));

        plugin.setDeleteAllContentFromImageFolder(true);
        plugin.setDeleteAllContentFromOcrFolder(false);
        plugin.setDeleteAllContentFromThumbsFolder(false);
        plugin.run();

        assertFalse(Files.exists(Paths.get(masterFolder)));
        assertFalse(Files.exists(Paths.get(mediaFolder)));
        assertFalse(Files.exists(Paths.get(sourceFolder)));
        assertTrue(Files.exists(Paths.get(thumbsFolder)));
        assertTrue(Files.exists(Paths.get(altoFolder)));
    }


    @Test
    public void testDeleteMasterFolder() throws Exception {
        createProcessFolder(true, true, true);

        ImageDeletionPlugin plugin = new ImageDeletionPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");

        String masterFolder = process.getImagesOrigDirectory(false);
        String mediaFolder = process.getImagesTifDirectory(false);
        String sourceFolder = process.getSourceDirectory();
        String thumbsFolder = process.getThumbsDirectory();
        String altoFolder = process.getOcrAltoDirectory();
        assertTrue(Files.exists(Paths.get(masterFolder)));
        assertTrue(Files.exists(Paths.get(mediaFolder)));
        assertTrue(Files.exists(Paths.get(sourceFolder)));
        assertTrue(Files.exists(Paths.get(thumbsFolder)));
        assertTrue(Files.exists(Paths.get(altoFolder)));

        plugin.setDeleteMasterFolder(true);
        plugin.run();

        assertFalse(Files.exists(Paths.get(masterFolder)));
        assertTrue(Files.exists(Paths.get(mediaFolder)));
        assertTrue(Files.exists(Paths.get(sourceFolder)));
        assertTrue(Files.exists(Paths.get(thumbsFolder)));
        assertTrue(Files.exists(Paths.get(altoFolder)));
    }

    @Test
    public void testDeactivateProcess() throws Exception {
        ImageDeletionPlugin plugin = new ImageDeletionPlugin();
        Step imageDeletionStep = process.getSchritte().get(1);
        plugin.initialize(imageDeletionStep, "somewhere");
        Step stepToDeactivate = process.getSchritte().get(2);
        assertEquals(StepStatus.LOCKED, stepToDeactivate.getBearbeitungsstatusEnum());

        plugin.setDeactivateProcess(true);
        plugin.run();

        assertEquals(StepStatus.DEACTIVATED, stepToDeactivate.getBearbeitungsstatusEnum());

    }


    public Process getProcess() {
        Project project = new Project();
        project.setTitel("projectName");

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
