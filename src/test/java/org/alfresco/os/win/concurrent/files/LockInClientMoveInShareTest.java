package org.alfresco.os.win.concurrent.files;

import org.alfresco.os.win.app.Notepad;
import org.alfresco.os.win.app.WindowsExplorer;
import org.alfresco.os.win.app.office.MicrosoftOffice2010;
import org.alfresco.os.win.app.office.MicrosoftOfficeBase;
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
 * This class will contain all the test cases related to Concurrent
 * lock of file in Client (Windows machine)
 * and move of same file in Share
 *
 * @author rdorobantu
 */
public class LockInClientMoveInShareTest extends DesktopSyncTest
{
    LoginActions shareLogin = new LoginActions();
    SiteActions share = new SiteActions();
    WindowsExplorer explorer = new WindowsExplorer();
    Notepad notepad = new Notepad();
    SyncSystemMenu notification = new SyncSystemMenu();
    MicrosoftOffice2010 office = new MicrosoftOffice2010(MicrosoftOfficeBase.VersionDetails.EXCEL);

    /**
     * File declaration for test LockInClientMoveInShare
     */
    File folderToMoveShare = null;
    File concurrentUpdateMove = null;

    /**
     * This Test will create an Excel file in Client and validate whether it is
     * visible in Share. Then it will keep the file opened (locked for editing) while
     * it is being moved in Share to another location.
     * Step1 - Create a file in Excel and save it.
     * Step2 - Create a folder in Share for the file to be moved into.
     * Step3 - Wait for Sync time which is 5 minutes for Share.
     * Step4 - Login in Share.
     * Step5 - Access sync site.
     * Step6 - Check the new file created in Client is synced in Share.
     * Step7 - Check the folder created in Share is synced in Client.
     * Step8 - Move the file in Share to the folder created in Step2 while it is open in Client.
     * Step9 - Wait for Sync time which is 5 minutes for Share.
     * Step10 - Add a new line of text in the Excel file in Client and save.
     * Step11 - Close the file in Client.
     * Step12 - Wait for Sync time which is 2 minutes for Client.
     * Step13 - Check if moved file in Share has been also moved in Client.
     *
     * @throws Exception
     */
    @Test
    public void LockInClientMoveInShare()
    {
        folderToMoveShare = getRandomFolderIn(getLocalSiteLocation(), "folderToMoveShare");
        concurrentUpdateMove = getRandomFileIn(getLocalSiteLocation(), "concUpdateMove", "xlsx");
        try
        {
            explorer.openApplication();
            explorer.openFolder(getLocalSiteLocation());
            explorer.createAndOpenFolder(folderToMoveShare.getName());
//            explorer.goBack(getLocalSiteLocation().getName());
            explorer.closeExplorer();
            office.openApplication();
            office.saveAsOffice(concurrentUpdateMove.getPath());
            office.closeApplication(concurrentUpdateMove);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);

            syncWaitTime(CLIENTSYNCTIME);
            office.openApplication();
            office.openOfficeFromFileMenu(concurrentUpdateMove.getPath());
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(folderToMoveShare.exists(),"Created folder in Share is synced to Client.");
            Assert.assertTrue(share.isFileVisible(drone,concurrentUpdateMove.getName()),"Created file in Client is synced in Share.");
            share.copyOrMoveArtifact(drone,"All Sites",siteName,folderToMoveShare.getName(), concurrentUpdateMove.getName(),"Move");

            syncWaitTime(SERVERSYNCTIME);
            office.editOffice("added line of text in Client.");
            office.saveOffice();
            office.closeApplication(concurrentUpdateMove);

            syncWaitTime(CLIENTSYNCTIME);
            //toDo - check type of conflict
            Assert.assertTrue(LdtpUtils.isFilePresent(getLocalSiteLocation().getAbsolutePath() + File.separator + concurrentUpdateMove.getName()),"Move was successful in Client.");
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
