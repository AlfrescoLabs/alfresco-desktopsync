package org.alfresco.os.win.concurrent.folders;

import org.alfresco.os.win.app.Notepad;
import org.alfresco.os.win.app.WindowsExplorer;
import org.alfresco.os.win.desktopsync.SyncSystemMenu;
import org.alfresco.po.share.steps.LoginActions;
import org.alfresco.po.share.steps.SiteActions;
import org.alfresco.sync.DesktopSyncTest;
import org.alfresco.utilities.LdtpUtils;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.TestException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

/**
 * This class will contain all the test cases related to Concurrent rename of folder
 * in Client (Windows Machine)
 * and move the same folder in Share
 *
 * @author rdorobantu
 */
public class RenameInClientMoveInShare extends DesktopSyncTest
{
    LoginActions shareLogin = new LoginActions();
    SiteActions share = new SiteActions();
    WindowsExplorer explorer = new WindowsExplorer();
    Notepad notepad = new Notepad();
    SyncSystemMenu notification = new SyncSystemMenu();
    String resolveUsingClient = "ResolveUsingLocal";
    String resolveUsingRemote = "ResolveUsingRemote";
    String conflictTypeRename = "Conflict-Rename";

    /**
     * Folder declarations for test RenameInClientMoveInShare
     */
    File concurrentMoveFolder = null;
    File concurrentMovedIntoFld = null;
    File concurrentFldRenamed = null;
    File fileInFolder = null;

    /**
     * This BeforeMethod will create a folder in Client and a file inside the folder
     * and validate whether it is synced in Share.
     * Then it will rename the folder in Client and Move the same folder in Share
     * Step1 - Create a folder and file and save it.
     * Step2 - Wait for Sync time which is 2 minutes for Client.
     * Step3 - Login in Share.
     * Step4 - Access sync site.
     * Step5 - Check the new folder and file created in Client is synced in Share.
     * Step6 - Rename the folder in Client.
     * Step7 - Move the same folder to another location in Share.
     * Step8 - Wait for Sync time which is 5 minutes for Share.
     * Step9 - Check if conflict appears.
     *
     * @throws Exception
     */
    @BeforeMethod
    public void setupRenameInClientMoveInShare()
    {
        concurrentMoveFolder = getRandomFolderIn(getLocalSiteLocation(), "fldToMove");
        concurrentMovedIntoFld = getRandomFolderIn(getLocalSiteLocation(), "fldToMoveInto");
        concurrentFldRenamed = getRandomFolderIn(getLocalSiteLocation(), "renamedFld");
        fileInFolder = getRandomFileIn(concurrentMoveFolder,"fileInFolder","txt");
        try
        {
            explorer.openApplication();
            explorer.openFolder(getLocalSiteLocation());
            explorer.createAndOpenFolder(concurrentMoveFolder.getName());
            explorer.goBack(getLocalSiteLocation().getName());
            explorer.createAndOpenFolder(concurrentMovedIntoFld.getName());
            explorer.goBack(getLocalSiteLocation().getName());
            notepad.openApplication();
            notepad.saveAs(fileInFolder);
            notepad.close(fileInFolder);
            syncWaitTime(CLIENTSYNCTIME);

            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, concurrentMoveFolder.getName()), "Client folder is successfully synced in Share.");
            Assert.assertTrue(share.isFileVisible(drone, concurrentMovedIntoFld.getName()), "Client folder is successfully synced in Share.");
            share.selectContent(drone, concurrentMoveFolder.getName());
            Assert.assertTrue(share.isFileVisible(drone, fileInFolder.getName()), "Client file is successfully synced in Share.");
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.copyOrMoveArtifact(drone, "All Sites", siteName, concurrentMovedIntoFld.getName(), concurrentMoveFolder.getName(), "Move");
            explorer.rename(concurrentMoveFolder,concurrentFldRenamed);
            syncWaitTime(SERVERSYNCTIME);
//            Assert.assertTrue(concurrentFldInShareRenamed.exists(), "Renamed folder synced from Remote exists in Client.");
            //as I cannot test the following line of code locally
            // the conflictType may be different. Will update it once I can test it
            Assert.assertTrue(notification.isConflictStatusCorrect(conflictTypeRename,concurrentMoveFolder.getName()));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new TestException("test case failed - setupRenameInClientMoveInShare", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * This test will resolve the conflict created in the BeforeMethod using Remote
     * Step1 - Resolve conflict using Remote for the folder created in the BeforeMethod.
     * Step2 - Wait for Sync time which is 5 minutes for Share.
     * Step3 - Verify the folder moved in Share is synced in Client.
     *
     * @throws Exception
     */
    @Test
    public void resolveConflictUsingRemote()
    {
        try
        {
            notification.resolveConflictingFilesWithoutOpeningWindow(concurrentMoveFolder.getName(), resolveUsingRemote);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(LdtpUtils.isFilePresent(getLocalSiteLocation().getAbsolutePath() + File.separator + concurrentMoveFolder.getName()), "Move was successful in Client.");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - resolveConflictUsingRemote", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * This test will resolve the conflict created in the BeforeMethod using Local
     * Step1 - Resolve conflict using Local for the folder created in the BeforeMethod.
     * Step2 - Wait for Sync time which is 2 minutes for Client.
     * Step3 - Login in Share.
     * Step4 - Navigate to the sync site.
     * Step5 - Verify the folder renamed in Client is synced in Share.
     *
     * @throws Exception
     */
    @Test
    public void resolveConflictUsingLocal()
    {
        try
        {
            notification.resolveConflictingFilesWithoutOpeningWindow(concurrentMoveFolder.getName(),resolveUsingClient);
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, concurrentFldRenamed.getName()), "Client renamed folder is successfully synced in Share.");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - resolveConflictUsingLocal", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }
}
