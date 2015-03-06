package org.alfresco.os.win.concurrent;

import org.alfresco.os.win.app.Notepad;
import org.alfresco.os.win.app.WindowsExplorer;
import org.alfresco.os.win.app.office.MicrosoftOffice2013;
import org.alfresco.os.win.app.office.MicrosoftOfficeBase;
import org.alfresco.os.win.desktopsync.SyncSystemMenu;
import org.alfresco.po.share.steps.LoginActions;
import org.alfresco.po.share.steps.SiteActions;
import org.alfresco.sync.DesktopSyncTest;
import org.alfresco.utilities.LdtpUtils;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
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
    Notepad notepad = new Notepad();
    MicrosoftOffice2013 office = new MicrosoftOffice2013(MicrosoftOfficeBase.VersionDetails.WORD);
    SyncSystemMenu notification = new SyncSystemMenu();

    /**
     * File declaration for test LockInClientRenameInShare
     */
    File concurrentUpdateRename = null;
    File renamedConcurrentUpdate = null;

    /**
     * This Test will create a Notepad file in Client and validate whether it is
     * visible in Share. Then it will keep the file opened (locked for editing) while
     * it is being renamed in Share.
     * Step1 - Create a file in Notepad and save it without any content without closing it.
     * Step2 - Wait for Sync time which is 2 minutes for Client.
     * Step3 - Login in Share.
     * Step4 - Access sync site.
     * Step5 - Check the new file created in Client is synced in Share.
     * Step6 - Rename the file in Share while it is still open in Client.
     * Step7 - Wait for Sync time which is 5 minutes for Share.
     * Step8 - Add a new line of text in the Notepad file in Client and save.
     * Step9 - Close the file in Client.
     * Step10 - Wait for Sync time which is 2 minutes for Client.
     * Step11 - Check if the renamed file in Share has been synced in Client.
     * Step12 - Check if the old file (before rename) is deleted from Client.
     *
     * @throws Exception
     */
    @Test
    public void LockInClientRenameInShare()
    {
        concurrentUpdateRename = getRandomFileIn(getLocalSiteLocation(), "concUpdateRename", "txt");
        renamedConcurrentUpdate = getRandomFileIn(getLocalSiteLocation(), "renamedConcUpdate", "txt");
        try
        {
            notepad.openApplication();
            notepad.saveAs(concurrentUpdateRename);

            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone,concurrentUpdateRename.getName()),"Created file in Client is synced in Share.");
            share.editContentNameInline(drone, concurrentUpdateRename.getName(), renamedConcurrentUpdate.getName(), true);

            syncWaitTime(SERVERSYNCTIME);
            notepad.edit("added line of text in Client.");
            notepad.save();
            notepad.close(concurrentUpdateRename);
            syncWaitTime(SERVERSYNCTIME);
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
