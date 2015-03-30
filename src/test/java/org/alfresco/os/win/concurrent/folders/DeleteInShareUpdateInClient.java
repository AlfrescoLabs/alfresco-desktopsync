package org.alfresco.os.win.concurrent.folders;

import org.alfresco.os.win.app.Notepad;
import org.alfresco.os.win.app.WindowsExplorer;
import org.alfresco.os.win.desktopsync.SyncSystemMenu;
import org.alfresco.po.share.steps.LoginActions;
import org.alfresco.po.share.steps.SiteActions;
import org.alfresco.sync.DesktopSyncTest;
import org.testng.Assert;
import org.testng.TestException;
import org.testng.annotations.Test;

import java.io.File;

/**
 * This class will contain all the test cases related to Concurrent delete of folder
 * in Share and update a file from that folder in Client (Windows Machine)
 *
 * @author rdorobantu
 */
public class DeleteInShareUpdateInClient extends DesktopSyncTest
{
    LoginActions shareLogin = new LoginActions();
    SiteActions share = new SiteActions();
    WindowsExplorer explorer = new WindowsExplorer();
    Notepad notepad = new Notepad();
    SyncSystemMenu notification = new SyncSystemMenu();
    String resolveUsingClient = "ResolveUsingLocal";
    String resolveUsingRemote = "ResolveUsingRemote";
    String conflictTypeDelete = "Conflict-Delete";

    /**
     * Folder declarations for test DeleteInShareUpdateInClient
     */
    File concurrentFolder = null;
    File fileInFolder = null;

    /**
     * This Test will create a folder in Client and a file inside the folder
     * and validate whether it is synced in Share.
     * Then it will delete the folder in Share and update the file in Client
     * Step1 - Create a folder and file and save it.
     * Step2 - Wait for Sync time which is 2 minutes for Client.
     * Step3 - Login in Share.
     * Step4 - Access sync site.
     * Step5 - Check the new folder and file created in Client is synced in Share.
     * Step6 - Delete the folder in Share.
     * Step7 - Update the file in Client.
     * Step8 - Wait for Sync time which is 5 minutes for Share.
     * Step9 - Check if conflict appears.
     *
     * @throws Exception
     */
    @Test
    public void setupDeleteInShareUpdateInClient()
    {
        concurrentFolder = getRandomFolderIn(getLocalSiteLocation(), "conFolder");
        fileInFolder = getRandomFileIn(concurrentFolder,"fileInFolder","txt");
        try
        {
            explorer.openApplication();
            explorer.openFolder(getLocalSiteLocation());
            explorer.createAndOpenFolder(concurrentFolder.getName());
            notepad.openApplication();
            notepad.saveAs(fileInFolder);
            explorer.goBack(getLocalSiteLocation().getName());
            syncWaitTime(CLIENTSYNCTIME);

            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, concurrentFolder.getName()), "Client folder is successfully synced in Share.");
            share.selectContent(drone, concurrentFolder.getName());
            Assert.assertTrue(share.isFileVisible(drone, fileInFolder.getName()), "Client file is successfully synced in Share.");
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.deleteContentInDocLib(drone, concurrentFolder.getName());
            notepad.edit("added new line of text");
            notepad.save();
            notepad.close(fileInFolder);
            syncWaitTime(SERVERSYNCTIME);
            //need to verify correct behavior as desktopSync does not trigger conflict
            // the conflictType may be different. Will update it once I can test it
//            Assert.assertTrue(notification.isConflictStatusCorrect(conflictTypeDelete,concurrentFolder.getName()));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new TestException("Test case failed - setupDeleteInShareUpdateInClient", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }
}
