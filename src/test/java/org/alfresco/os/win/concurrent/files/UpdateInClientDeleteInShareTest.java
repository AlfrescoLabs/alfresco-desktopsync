package org.alfresco.os.win.concurrent.files;

import java.io.File;

import org.alfresco.os.win.app.Notepad;
import org.alfresco.os.win.desktopsync.SyncSystemMenu;
import org.alfresco.po.share.steps.LoginActions;
import org.alfresco.po.share.steps.SiteActions;
import org.alfresco.sync.DesktopSyncTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to Concurrent
 * lock of file in Client (Windows machine) then update it
 * and delete of same file in Share
 *
 * @author rdorobantu
 */
public class UpdateInClientDeleteInShareTest extends DesktopSyncTest
{
    LoginActions shareLogin = new LoginActions();
    SiteActions share = new SiteActions();
    Notepad notepad = new Notepad();
    SyncSystemMenu notification = new SyncSystemMenu();
    String resolveUsingClient = "ResolveUsingLocal";
    String resolveUsingRemote = "ResolveUsingRemote";
    String conflictTypeDelete = "Conflict-Delete";

    /**
     * File declaration for test UpdateInClientDeleteInShare
     */
    File concurrentUpdateDelete = null;

    /**
     * This BeforeMethod will create a Notepad file in Client and validate whether it is
     * synced in Share. Then it will keep the file opened (locked for editing) while
     * it is being deleted in Share and trigger a conflict.
     * Step1 - Create a file in Notepad and save it without any content without closing it.
     * Step2 - Wait for Sync time which is 2 minutes for Client.
     * Step3 - Login in Share.
     * Step4 - Access sync site.
     * Step5 - Check the new file created in Client is synced in Share.
     * Step6 - Delete the file in Share while it is still open in Client.
     * Step7 - Wait for Sync time which is 5 minutes for Share.
     * Step8 - Add a new line of text in the file in Client and save.
     * Step9 - Close the file in Client.
     * Step10 - Wait for Sync time which is 2 minutes for Client.
     * Step11 - Check if conflict appears.
     *
     * @throws Exception
     */
    @BeforeMethod
    public void setupUpdateInClientDeleteInShare()
    {
        concurrentUpdateDelete = getRandomFileIn(getLocalSiteLocation(), "concUpdateDelete", "txt");
        try
        {
            notepad.openApplication();
            notepad.saveAs(concurrentUpdateDelete);

            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone,concurrentUpdateDelete.getName()),"Created file in Client is synced in Share.");
            share.deleteContentInDocLib(drone,concurrentUpdateDelete.getName());

            syncWaitTime(SERVERSYNCTIME);
            notepad.edit("Added line in Client");
            notepad.save();
            notepad.close(concurrentUpdateDelete);

            syncWaitTime(CLIENTSYNCTIME);
            Assert.assertTrue(notification.isConflictStatusCorrect(conflictTypeDelete, concurrentUpdateDelete.getName()));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - setupUpdateInClientDeleteInShare", e);
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
     * Step3 - Verify the deleted file in Share is synced in Client.
     *
     * @throws Exception
     */
    @Test
    public void resolveConflictUsingRemote()
    {
        try
        {
            notification.resolveConflictingFilesWithoutOpeningWindow(concurrentUpdateDelete.getName(), resolveUsingRemote);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(concurrentUpdateDelete.exists(), "Deleted file in Share is now also deleted in Client.");
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
     * Step5 - Verify the file in Client is synced in Share.
     *
     * @throws Exception
     */
    @Test
    public void resolveConflictUsingLocal()
    {
        try
        {
            notification.resolveConflictingFilesWithoutOpeningWindow(concurrentUpdateDelete.getName(),resolveUsingClient);
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, concurrentUpdateDelete.getName()), "Client locked file is successfully synced in Share.");
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
