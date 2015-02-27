package org.alfresco.os.win.app;

import org.alfresco.os.win.app.office.MicrosoftOffice2013;
import org.alfresco.os.win.app.office.MicrosoftOfficeBase;
import org.alfresco.os.win.desktopsync.DesktopSyncNotification;
import org.alfresco.po.share.steps.LoginActions;
import org.alfresco.po.share.steps.SiteActions;
import org.alfresco.sync.DesktopSyncTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created by rdorobantu on 2/25/2015.
 */
public class ConcurrentContentSyncTest extends DesktopSyncTest
{
    LoginActions shareLogin = new LoginActions();
    SiteActions share = new SiteActions();
    WindowsExplorer explorer = new WindowsExplorer();
    Notepad notepad = new Notepad();
    MicrosoftOffice2013 office = new MicrosoftOffice2013(MicrosoftOfficeBase.VersionDetails.WORD);
    DesktopSyncNotification notification = new DesktopSyncNotification();
    String resolveUsingClient = "ResolveUsingLocal";
    String resolveUsingRemote = "ResolveUsingRemote";
    String conflictTypeRename = "Conflict-Rename";
    String conflictTypeDelete = "Conflict-Delete";

    /**
     * File declarations for test concurrentRename
    */
    File concurrentRename = null;
    File concurrentRenamedShare = null;
    File concurrentRenamedClient = null;

    /**
     * Folder declarations for test concurrentRenameFolder
     */
    File concurrentRenameFolder = null;
    File concurrentFldInShareRenamed = null;
    File concurrentFldInClientRenamed = null;

    /**
     * File declaration for test concurrentUpdateClientDeleteShare
     */
    File concurrentUpdateDelete = null;

    /**
     * File declaration for test concurrentUpdateClientRenameShare
     */
    File concurrentUpdateRename = null;
    File renamedConcurrentUpdate = null;

    /**
     *  All the files and folders required for the test cases
     */
    @BeforeClass
   public void setupConcurrent()
    {
        concurrentRename = getRandomFileIn(getLocalSiteLocation(), "concRename", "txt");
        concurrentRenameFolder = getRandomFolderIn(getLocalSiteLocation(), "conFolder");
        concurrentRenamedShare = getRandomFileIn(getLocalSiteLocation(), "concRenamedShare", "txt");
        concurrentRenamedClient = getRandomFileIn(getLocalSiteLocation(), "concRenamedClient", "txt");
        concurrentFldInShareRenamed = getRandomFolderIn(getLocalSiteLocation(), "renamedFldInShare");
        concurrentFldInClientRenamed = getRandomFolderIn(getLocalSiteLocation(), "renamedFldInClient");
        concurrentUpdateDelete = getRandomFileIn(getLocalSiteLocation(), "concUpdateDelete", "txt");
        concurrentUpdateRename = getRandomFileIn(getLocalSiteLocation(), "concUpdateRename", "txt");
        renamedConcurrentUpdate = getRandomFileIn(getLocalSiteLocation(), "renamedConcUpdate", "txt");
        try
        {
            explorer.openApplication();
            explorer.openFolder(getLocalSiteLocation());
            explorer.createAndOpenFolder(concurrentRenameFolder.getName());
            explorer.closeExplorer();
            notepad.openApplication();
            notepad.saveAs(concurrentRename);
            notepad.close(concurrentRename);
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, concurrentRename.getName()), "Client notepad file is successfully synced in Share.");
            Assert.assertTrue(share.isFileVisible(drone, concurrentRenameFolder.getName()), "Client folder is successfully synced in Share.");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - setupConcurrent", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }


    @Test
    public void concurrentRename()
    {
        try
        {
            explorer.openApplication();
            explorer.openFolder(concurrentRename.getParentFile());
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.editContentNameInline(drone, concurrentRename.getName(), concurrentRenamedShare.getName(), true);
            explorer.rename(concurrentRename, concurrentRenamedClient);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(notification.isConflictStatusCorrect(conflictTypeRename));
            notification.resolveConflictingFiles(concurrentRename.getName(),resolveUsingRemote);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(concurrentRenamedShare.exists(), "Renamed file in Share is now synced in Client.");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - concurrentRename", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    @Test
    public void concurrentRenameFolder()
    {
        try
        {
            explorer.openApplication();
            explorer.openFolder(concurrentRenameFolder.getParentFile());
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.editContentNameInline(drone, concurrentRenameFolder.getName(), concurrentFldInShareRenamed.getName(), true);
            explorer.rename(concurrentRenameFolder, concurrentFldInClientRenamed);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(notification.isConflictStatusCorrect(conflictTypeRename));
            notification.resolveConflictingFiles(concurrentRenameFolder.getName(),resolveUsingClient);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(concurrentFldInClientRenamed.exists(), "Renamed file in Share is now synced in Client.");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - concurrentRenameFolder", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    @Test
    public void concurrentUpdateClientDeleteShare()
    {
        try
        {
            notepad.openApplication();
            notepad.saveAs(concurrentUpdateDelete);

            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.deleteContentInDocLib(drone,concurrentUpdateDelete.getName());
            notepad.edit("Added line in Client");
            notepad.save();
            notepad.close(concurrentUpdateDelete);

            syncWaitTime(CLIENTSYNCTIME);
            Assert.assertTrue(notification.isConflictStatusCorrect(conflictTypeDelete));
            notification.resolveConflictingFiles(concurrentRenameFolder.getName(),resolveUsingClient);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue((share.isFileVisible(drone, concurrentUpdateDelete.getName())), "Updated file in Client was synced in Share");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - concurrentRename", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }
}
