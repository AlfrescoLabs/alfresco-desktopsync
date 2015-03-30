package org.alfresco.os.win.concurrent.folders;

import org.alfresco.os.win.app.Notepad;
import org.alfresco.os.win.app.WindowsExplorer;
import org.alfresco.os.win.desktopsync.SyncSystemMenu;
import org.alfresco.po.share.steps.LoginActions;
import org.alfresco.po.share.steps.SiteActions;
import org.alfresco.sync.DesktopSyncTest;
import org.alfresco.utilities.LdtpUtils;
import org.testng.Assert;
import org.testng.TestException;
import org.testng.annotations.Test;

import java.io.File;

/**
 * This class will contain all the test cases related to Concurrent delete of folder
 * in Client (Windows machine) and move the same folder in Share
 *
 * @author rdorobantu
 */
public class DeleteInClientMoveInShare extends DesktopSyncTest
{
    LoginActions shareLogin = new LoginActions();
    SiteActions share = new SiteActions();
    WindowsExplorer explorer = new WindowsExplorer();
    Notepad notepad = new Notepad();
    SyncSystemMenu notification = new SyncSystemMenu();

    /**
     * File declaration for test deleteInClientMoveInShare
     */
    File concurrentDeleteMove = null;
    File concurrentMoveInto = null;

    /**
     * This Test will create a folder in Client and validate whether it is
     * visible in Share. Then it will move the folder to another folder in Share
     * and delete it in Client.
     * Step1 - Create a folder in Client.
     * Step2 - Create a second folder in Client to move into.
     * Step3 - Wait for Sync time which is 5 minutes for Share.
     * Step4 - Login in Share.
     * Step5 - Access sync site.
     * Step6 - Check the new folders created in Client are synced in Share.
     * Step7 - Move the file in Share to the folder created in Step2 and delete it in Client.
     * Step8 - Wait for Sync time which is 5 minutes for Share.
     * Step9 - Check if moved folder in Share has been also moved in Client.
     *
     * @throws Exception
     */
    @Test
    public void deleteInClientMoveInShare()
    {
        concurrentDeleteMove = getRandomFolderIn(getLocalSiteLocation(), "concUpdateMove");
        concurrentMoveInto = getRandomFolderIn(getLocalSiteLocation(), "concUpdateMoveInto");
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            explorer.openApplication();
            explorer.openFolder(getLocalSiteLocation());
            explorer.createAndOpenFolder(concurrentDeleteMove.getName());
            explorer.goBack(getLocalSiteLocation().getName());
            explorer.createAndOpenFolder(concurrentMoveInto.getName());
            explorer.goBack(getLocalSiteLocation().getName());

            syncWaitTime(CLIENTSYNCTIME);
            Assert.assertTrue(concurrentDeleteMove.exists(), "Created folder in Share is synced to Client.");
            Assert.assertTrue(concurrentMoveInto.exists(), "Created folder in Share is synced to Client.");
            share.copyOrMoveArtifact(drone,"All Sites",siteName,concurrentMoveInto.getName(),concurrentDeleteMove.getName(),"Move");
            explorer.deleteFolder(concurrentDeleteMove.getName(),true);

            syncWaitTime(SERVERSYNCTIME);
            //to do - verify folder is moved .. or conflict
            Assert.assertTrue(LdtpUtils.isFilePresent(getLocalSiteLocation().getAbsolutePath() + File.separator + concurrentDeleteMove.getName()),"Move was successful in Client.");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new TestException("Test case failed - LockInClientMoveInShare", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }
}
