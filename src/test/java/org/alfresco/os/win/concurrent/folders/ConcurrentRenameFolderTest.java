package org.alfresco.os.win.concurrent.folders;

import org.alfresco.os.win.app.WindowsExplorer;
import org.alfresco.os.win.desktopsync.SyncSystemMenu;
import org.alfresco.po.share.steps.LoginActions;
import org.alfresco.po.share.steps.SiteActions;
import org.alfresco.sync.DesktopSyncTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

/**
 * This class will contain all the test cases related to Concurrent rename of folder
 * in Client (Windows machine) and Share
 *
 * @author rdorobantu
 */
public class ConcurrentRenameFolderTest extends DesktopSyncTest
{
    LoginActions shareLogin = new LoginActions();
    SiteActions share = new SiteActions();
    WindowsExplorer explorer = new WindowsExplorer();
    SyncSystemMenu notification = new SyncSystemMenu();
    String resolveUsingClient = "ResolveUsingLocal";
    String resolveUsingRemote = "ResolveUsingRemote";
    String conflictTypeRename = "Conflict-Rename";

    /**
     * Folder declarations for test concurrentRenameFolder
     */
    File concurrentRenameFolder = null;
    File concurrentFldInShareRenamed = null;
    File concurrentFldInClientRenamed = null;

    /**
     * This BeforeMethod will create a folder in Client and validate whether it is
     * synced in Share. Then it will rename the folder simultaneously in Share and in Client
     * and trigger a conflict.
     * Step1 - Create a folder and save it.
     * Step2 - Wait for Sync time which is 2 minutes for Client.
     * Step3 - Login in Share.
     * Step4 - Access sync site.
     * Step5 - Check the new folder created in Client is synced in Share.
     * Step6 - Rename the folder in Share.
     * Step7 - Rename the folder in Client.
     * Step8 - Wait for Sync time which is 5 minutes for Share.
     * Step9 - Check if conflict appears.
     *
     * @throws Exception
     */
    @BeforeMethod
    public void setupConcurrentRenameFolder()
    {
        concurrentRenameFolder = getRandomFolderIn(getLocalSiteLocation(), "conFolder");
        concurrentFldInShareRenamed = getRandomFolderIn(getLocalSiteLocation(), "renamedFldInShare");
        concurrentFldInClientRenamed = getRandomFolderIn(getLocalSiteLocation(), "renamedFldInClient");
        try
        {
            explorer.openApplication();
            explorer.openFolder(getLocalSiteLocation());
            explorer.createAndOpenFolder(concurrentRenameFolder.getName());
            explorer.goBack(getLocalSiteLocation().getName());
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, concurrentRenameFolder.getName()), "Client folder is successfully synced in Share.");
            share.editContentNameInline(drone, concurrentRenameFolder.getName(), concurrentFldInShareRenamed.getName(), true);
            explorer.rename(concurrentRenameFolder, concurrentFldInClientRenamed);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(notification.isConflictStatusCorrect(conflictTypeRename,concurrentRenameFolder.getName()));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - setupConcurrentRenameFolder", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * This test will resolve the conflict created in the BeforeClass using Remote
     * Step1 - Resolve conflict using Remote for the folder created in the BeforeClass.
     * Step2 - Wait for Sync time which is 5 minutes for Share.
     * Step3 - Verify the renamed folder in Share is synced in Client.
     *
     * @throws Exception
     */
    @Test
    public void resolveConflictUsingRemote()
    {
        try
        {
            notification.resolveConflictingFilesWithoutOpeningWindow(concurrentRenameFolder.getName(),resolveUsingRemote);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(concurrentFldInShareRenamed.exists(), "Renamed folder in Share is now synced in Client.");
            Assert.assertFalse(concurrentFldInClientRenamed.exists(), "Renamed folder in Client is not present in Client.");
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
     * This test will resolve the conflict created in the BeforeClass using Local
     * Step1 - Resolve conflict using Local for the folder created in the BeforeClass.
     * Step2 - Wait for Sync time which is 2 minutes for Client.
     * Step3 - Login in Share.
     * Step4 - Navigate to the sync site.
     * Step5 - Verify the renamed folder in Client is synced in Share.
     *
     * @throws Exception
     */
    @Test
    public void resolveConflictUsingLocal()
    {
        try
        {
            notification.resolveConflictingFilesWithoutOpeningWindow(concurrentRenameFolder.getName(),resolveUsingClient);
            syncWaitTime(SERVERSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, concurrentFldInClientRenamed.getName()), "Client renamed folder is successfully synced in Share.");
            Assert.assertFalse(share.isFileVisible(drone, concurrentFldInShareRenamed.getName()), "Renamed folder in Share is not present in Share.");
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
