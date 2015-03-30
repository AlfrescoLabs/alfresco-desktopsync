package org.alfresco.os.win.concurrent.files;

import org.alfresco.os.win.app.Notepad;
import org.alfresco.os.win.app.WindowsExplorer;
import org.alfresco.os.win.app.office.MicrosoftOffice2010;
import org.alfresco.os.win.app.office.MicrosoftOfficeBase;
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
 * This class will contain all the test cases related to Concurrent
 * lock of file in Client (Windows machine)
 * and delete of same file in Share
 *
 * @author rdorobantu
 */
public class LockInClientDeleteInShareTest extends DesktopSyncTest
{
    LoginActions shareLogin = new LoginActions();
    SiteActions share = new SiteActions();
    WindowsExplorer explorer = new WindowsExplorer();
    Notepad notepad = new Notepad();
    SyncSystemMenu notification = new SyncSystemMenu();
    String resolveUsingClient = "ResolveUsingLocal";
    String resolveUsingRemote = "ResolveUsingRemote";
    String conflictTypeUpdate = "Conflict-Update";
    MicrosoftOffice2010 office = new MicrosoftOffice2010(MicrosoftOfficeBase.VersionDetails.WORD);
    /**
     * File declaration for test LockInClientDeleteInShare
     */
    File concurrentLockDelete = null;

    /**
     * This BeforeMethod will create a Word file in Client and validate whether it is
     * synced in Share. Then it will keep the file opened (locked for editing) while
     * it is being deleted in Share and trigger a conflict.
     * Step1 - Create a file in Word and save it.
     * Step2 - Wait for Sync time which is 2 minutes for Client.
     * Step3 - Login in Share.
     * Step4 - Access sync site.
     * Step5 - Check the new file created in Client is synced in Share.
     * Step6 - Delete the file in Share while it is open in Client.
     * Step7 - Wait for Sync time which is 5 minutes for Share.
     * Step8 - Add a new line of text and then close the file in Client.
     * Step9 - Wait for Sync time which is 2 minutes for Client.
     * Step10 - Check if conflict appears.
     *
     * @throws Exception
     */
    @Test
    public void setupLockInClientDeleteInShare()
    {
        concurrentLockDelete = getRandomFileIn(getLocalSiteLocation(), "concLockDelete", "docx");
        try
        {
            office.openApplication();
            office.saveAsOffice(concurrentLockDelete.getPath());
            office.closeApplication(concurrentLockDelete);
            syncWaitTime(CLIENTSYNCTIME);

            office.openApplication();
            office.openOfficeFromFileMenu(concurrentLockDelete.getPath());
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone,concurrentLockDelete.getName()), "Created file in Client is synced in Share.");
            share.deleteContentInDocLib(drone,concurrentLockDelete.getName());

            syncWaitTime(SERVERSYNCTIME);
            office.editOffice("new line of text.");
            office.closeApplication(concurrentLockDelete);

            syncWaitTime(CLIENTSYNCTIME);
            //toDo - check type of conflict
            notification.isConflictStatusCorrect(conflictTypeUpdate, concurrentLockDelete.getName()); //or Conflict-Delete
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - setupLockInClientDeleteInShare", e);
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
            notification.resolveConflictingFilesWithoutOpeningWindow(concurrentLockDelete.getName(),resolveUsingRemote);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(concurrentLockDelete.exists(), "Deleted file in Share is now also deleted in Client.");
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
            notification.resolveConflictingFilesWithoutOpeningWindow(concurrentLockDelete.getName(),resolveUsingClient);
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, concurrentLockDelete.getName()), "Client locked file is successfully synced in Share.");
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
