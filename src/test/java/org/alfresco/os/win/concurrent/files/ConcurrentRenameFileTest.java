package org.alfresco.os.win.concurrent.files;

import org.alfresco.os.win.app.Notepad;
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
 * This class will contain all the test cases related to Concurrent rename of file
 * in Client (Windows machine) and Share
 *
 * @author rdorobantu
 */
public class ConcurrentRenameFileTest extends DesktopSyncTest
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
     * File declarations for test concurrentRenameFile
     */
    File concurrentRenameFile = null;
    File concurrentRenamedShare = null;
    File concurrentRenamedClient = null;

    /**
     * This BeforeMethod will create a Notepad file in Client and validate whether it is
     * synced in Share. Then it will rename the file simultaneously in Share and in Client
     * and trigger a conflict.
     * Step1 - Create a file in Notepad and save it without any content.
     * Step2 - Close Notepad.
     * Step3 - Wait for Sync time which is 2 minutes for Client.
     * Step4 - Login in Share.
     * Step5 - Access sync site.
     * Step6 - Check the new file created in Client is synced in Share.
     * Step7 - Rename the file in Share.
     * Step8 - Rename the file in Client.
     * Step9 - Wait for Sync time which is 5 minutes for Share.
     * Step10 - Check if conflict appears.
     *
     * @throws Exception
     */
    @BeforeMethod
    public void setupConcurrentRenameFile()
    {
        concurrentRenameFile = getRandomFileIn(getLocalSiteLocation(), "concRename", "txt");
        concurrentRenamedShare = getRandomFileIn(getLocalSiteLocation(), "concRenamedShare", "txt");
        concurrentRenamedClient = getRandomFileIn(getLocalSiteLocation(), "concRenamedClient", "txt");
        try
        {
            notepad.openApplication();
            notepad.saveAs(concurrentRenameFile);
            notepad.close(concurrentRenameFile);
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, concurrentRenameFile.getName()), "Client notepad file is successfully synced in Share.");
            explorer.openApplication();
            explorer.openFolder(concurrentRenameFile.getParentFile());
            share.editContentNameInline(drone, concurrentRenameFile.getName(), concurrentRenamedShare.getName(), true);
            explorer.rename(concurrentRenameFile, concurrentRenamedClient);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(notification.isConflictStatusCorrect(conflictTypeRename,concurrentRenameFile.getName()));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - setupConcurrentRenameFile", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * This test will resolve the conflict created in the BeforeClass using Remote
     * Step1 - Resolve conflict using Remote for the file created in the BeforeClass.
     * Step2 - Wait for Sync time which is 5 minutes for Share.
     * Step3 - Verify the renamed file in Share is synced in Client.
     *
     * @throws Exception
     */
    @Test
    public void resolveConflictUsingRemote()
    {
        try
        {
            notification.resolveConflictingFilesWithoutOpeningWindow(concurrentRenameFile.getName(),resolveUsingRemote);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(concurrentRenamedShare.exists(), "Renamed file in Share is now synced in Client.");
            Assert.assertFalse(concurrentRenamedClient.exists(), "Renamed file in Client is not present in Client.");
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
     * Step1 - Resolve conflict using Local for the file created in the BeforeClass.
     * Step2 - Wait for Sync time which is 2 minutes for Client.
     * Step3 - Login in Share.
     * Step4 - Navigate to the sync site.
     * Step5 - Verify the renamed file in Client is synced in Share.
     *
     * @throws Exception
     */
    @Test
    public void resolveConflictUsingLocal()
    {
        try
        {
            notification.resolveConflictingFilesWithoutOpeningWindow(concurrentRenameFile.getName(),resolveUsingClient);
            syncWaitTime(SERVERSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, concurrentRenamedClient.getName()), "Client renamed file is successfully synced in Share.");
            Assert.assertFalse(share.isFileVisible(drone, concurrentRenamedShare.getName()), "Renamed file in Share is not present in Share.");
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
