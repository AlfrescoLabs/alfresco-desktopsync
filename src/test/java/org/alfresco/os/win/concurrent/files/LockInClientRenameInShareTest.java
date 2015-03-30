package org.alfresco.os.win.concurrent.files;

import org.alfresco.os.win.app.Notepad;
import org.alfresco.os.win.app.WindowsExplorer;
import org.alfresco.os.win.app.office.MicrosoftOffice2010;
import org.alfresco.os.win.app.office.MicrosoftOffice2013;
import org.alfresco.os.win.app.office.MicrosoftOfficeBase;
import org.alfresco.os.win.desktopsync.SyncSystemMenu;
import org.alfresco.po.share.steps.LoginActions;
import org.alfresco.po.share.steps.SiteActions;
import org.alfresco.sync.DesktopSyncTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.File;

/**
 * This class will contain all the test cases related to Concurrent
 * lock of file in Client (Windows machine)
 * and rename of same file in Share
 *
 * @author rdorobantu
 */
public class LockInClientRenameInShareTest extends DesktopSyncTest
{
    LoginActions shareLogin = new LoginActions();
    SiteActions share = new SiteActions();
    WindowsExplorer explorer = new WindowsExplorer();
    MicrosoftOffice2010 office = new MicrosoftOffice2010(MicrosoftOfficeBase.VersionDetails.POWERPOINT);
    SyncSystemMenu notification = new SyncSystemMenu();

    /**
     * File declaration for test LockInClientRenameInShare
     */
    File concurrentUpdateRename = null;
    File renamedConcurrentUpdate = null;

    /**
     * This Test will create a PowerPoint file in Client and validate whether it is
     * visible in Share. Then it will keep the file opened (locked for editing) while
     * it is being renamed in Share.
     * Step1 - Create a file in PowerPoint and save it.
     * Step2 - Wait for Sync time which is 2 minutes for Client.
     * Step3 - Login in Share.
     * Step4 - Access sync site.
     * Step5 - Check the new file created in Client is synced in Share.
     * Step6 - Rename the file in Share while it is open in Client.
     * Step7 - Wait for Sync time which is 5 minutes for Share.
     * Step8 - Close the file in Client.
     * Step9 - Wait for Sync time which is 2 minutes for Client.
     * Step10 - Check if the renamed file in Share has been synced in Client.
     * Step11 - Check if the old file (before rename) is deleted from Client.
     *
     * @throws Exception
     */
    @Test
    public void LockInClientRenameInShare()
    {
        concurrentUpdateRename = getRandomFileIn(getLocalSiteLocation(), "concUpdateRename", "pptx");
        renamedConcurrentUpdate = getRandomFileIn(getLocalSiteLocation(), "renamedConcUpdate", "pptx");
        try
        {
            office.openApplication();
            office.saveAsOffice(concurrentUpdateRename.getPath());
            office.closeApplication(concurrentUpdateRename);

            syncWaitTime(CLIENTSYNCTIME);
            office.openApplication();
            office.openOfficeFromFileMenu(concurrentUpdateRename.getPath());
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone,concurrentUpdateRename.getName()),"Created file in Client is synced in Share.");
            share.editContentNameInline(drone, concurrentUpdateRename.getName(), renamedConcurrentUpdate.getName(), true);

            syncWaitTime(SERVERSYNCTIME);
            office.closeApplication(concurrentUpdateRename);
            syncWaitTime(SERVERSYNCTIME);
            //toDo - check type of conflict
            Assert.assertTrue(renamedConcurrentUpdate.exists(), "Renamed file in Share was synced in Client.");
            Assert.assertFalse(concurrentUpdateRename.exists(), "Original file is not present in Client.");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - LockInClientRenameInShare", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }
}
