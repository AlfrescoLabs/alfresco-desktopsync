package org.alfresco.os.win.concurrent.files;

import org.alfresco.os.win.app.Notepad;
import org.alfresco.os.win.app.WindowsExplorer;
import org.alfresco.os.win.desktopsync.SyncSystemMenu;
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.steps.LoginActions;
import org.alfresco.po.share.steps.SiteActions;
import org.alfresco.sync.DesktopSyncTest;
import org.testng.Assert;
import org.testng.TestException;
import org.testng.annotations.Test;

import java.io.File;

/**
 * This class will contain all the test cases related to Concurrent
 * update of file in Share
 * and rename of same file in Client (Windows Machine)
 *
 * @author rdorobantu
 */
public class UpdateInShareRenameInClient extends DesktopSyncTest
{
    LoginActions shareLogin = new LoginActions();
    SiteActions share = new SiteActions();
    WindowsExplorer explorer = new WindowsExplorer();
    Notepad notepad = new Notepad();
    SyncSystemMenu notification = new SyncSystemMenu();

    /**
     * File declaration for test UpdateInShareRenameInClient
     */
    File concurrentUpdateRename = null;
    File renamedConcurrentUpdate = null;

    /**
     * This Test will create a Notepad file in Client and validate whether it is
     * visible in Share. Then it will update the file in Share and rename the same file in Client.
     * Step1 - Create a file in Notepad and save it.
     * Step2 - Wait for Sync time which is 2 minutes for Client.
     * Step3 - Login in Share.
     * Step4 - Access sync site.
     * Step5 - Check the new file created in Client is synced in Share.
     * Step6 - Update the file in Share.
     * Step7 - Rename the same file in Client.
     * Step8 - Wait for Sync time which is 5 minutes for Share.
     * Step9 - Check if conflict appears.
     *
     * @throws Exception
     */
    @Test
    public void updateInShareRenameInClient()
    {
        concurrentUpdateRename = getRandomFileIn(getLocalSiteLocation(), "concUpdateRename", "txt");
        renamedConcurrentUpdate = getRandomFileIn(getLocalSiteLocation(), "renamedConcUpdate", "txt");
        File fileUpdated = getRandomFileIn(getLocalSiteLocation(), "updatedFile", "txt");
        ContentDetails content = new ContentDetails();
        content.setName(fileUpdated.getName());
        content.setDescription(fileUpdated.getName());
        content.setTitle(fileUpdated.getName());
        content.setContent("share created file");
        try
        {
            explorer.openApplication();
            explorer.openFolder(concurrentUpdateRename.getParentFile());
            notepad.openApplication();
            notepad.edit("new line of text");
            notepad.saveAs(concurrentUpdateRename);
            notepad.close(concurrentUpdateRename);
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone,concurrentUpdateRename.getName()), "File created in Client is synced in Share.");
            explorer.rename(concurrentUpdateRename, renamedConcurrentUpdate);
            share.uploadNewVersionOfDocument(drone, concurrentUpdateRename.getName(), fileUpdated.getName(), "test sync update");
            explorer.closeExplorer();
            syncWaitTime(SERVERSYNCTIME);
            //check correct behavior
            Assert.assertTrue(share.isFileVisible(drone,renamedConcurrentUpdate.getName()), "File renamed in Client is synced in Share.");
            Assert.assertEquals(share.getVersionNumber(drone,concurrentUpdateRename.getName()), "2.0", "Version is not updated.");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new TestException("Test case failed - updateInShareRenameInClient", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }
}
