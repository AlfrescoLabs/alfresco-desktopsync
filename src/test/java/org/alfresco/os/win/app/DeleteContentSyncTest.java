/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.os.win.app;

import java.io.File;

import org.alfresco.po.share.steps.LoginActions;
import org.alfresco.po.share.steps.SiteActions;
import org.alfresco.sync.DesktopSyncTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to delete of file in both client (Windows machine) and share
 * 
 * @author Sprasanna
 * @author Paul Brodner
 */
public class DeleteContentSyncTest extends DesktopSyncTest
{
    Notepad notepad = new Notepad();
    LoginActions shareLogin = new LoginActions();
    SiteActions share = new SiteActions();
    WindowsExplorer explorer = new WindowsExplorer();

    /**
     * Test case to delete a file just created
     * Step1 - Open Windows explorer and navigate to the synclocation
     * Step2 - open notepad application
     * Step3 - Add a new line and Save the file to the sync location
     * step4 - close notepad
     * step5 - Wait for the sync time - 2 mins as it is client sync
     * Step6 - in the explorer window delete the file
     * Step7 - Say yes in the delete confirmation dialog
     * step8 - Wait for the sync time - 2 mins as it is client sync
     * Step9 - login in share
     * step10 - validate the file is not visible in document library
     * 
     * @throws InterruptedException
     */
    @Test
    public void deleteFileCreated() throws InterruptedException
    {
        File deleteFile = getRandomFileIn(getLocalSiteLocation(), "deleteFile", "txt");
        try
        {
            notepad.openApplication();
            notepad.edit("desktop Automated Testing");
            notepad.saveAs(deleteFile);
            notepad.close(deleteFile);
            syncWaitTime(CLIENTSYNCTIME);
            explorer.openApplication();
            explorer.deleteFile(deleteFile, true);
            explorer.closeExplorer();
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone, deleteFile.getName()), "File exists in Share after auto Sync.");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - deleteFileCreated", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * Test case to delete a folder which is already created by share
     * Step1 - Open Windows explorer and navigate to the synclocation
     * Step2 - open notepad application
     * Step3 - delete the folder created in the previous test case
     * step4 - Wait for the sync time - 2 mins as it is client sync
     * Step5 - login in share
     * step6 - validate the folder is not visible in document library
     */
    @Test
    public void deleteFolderInClient()
    {
        File deleteTestFolder = getRandomFolderIn(getLocalSiteLocation(), "deleteFolder");
        try
        {
            explorer.openApplication();
            explorer.openFolder(deleteTestFolder.getParentFile());
            explorer.createAndOpenFolder(deleteTestFolder.getName());
            explorer.goBack(deleteTestFolder.getParentFile().getName());
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, deleteTestFolder.getName()), "Folder exists in Share after auto Sync.");
            explorer.deleteFolder(deleteTestFolder.getName(), true);
            explorer.closeExplorer();
            syncWaitTime(CLIENTSYNCTIME);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone, deleteTestFolder.getName()), "Folder does not exist in Share after auto Sync.");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - deleteFileCreated", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * In share delete a folder with File which is synced
     * Step1 - create a new file
     * Step2 - login in to share
     * Step3 - open site document library
     * Step4 - create a folder
     * Step5 - Upload the created file inside the folder created in step4
     * Step6 - Wait for sync time - which is 5 mins in case of share
     * Step7 - Validate in the explorer that file is visible
     * Step8 - Now in share delete the folder
     * Step9 - Wait for sync time - which is 5 mins in case of share
     * Step10 - Validate that folder is not visible in sync location
     */
    @Test
    public void deleteFolderWithFileInShare()
    {
        File folderDelete = getRandomFolderIn(getLocalSiteLocation(), "deleteFoldeF");
        File fileDelete = getRandomFileIn(folderDelete, "fileDel", "txt");
        try
        {
            File shareFile = share.newFile(fileDelete.getName(), fileDelete.getName());
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderDelete.getName(), folderDelete.getName(), folderDelete.getName());
            share.navigateToFolder(drone, folderDelete.getName());
            share.uploadFile(drone, shareFile);
            share.navigateToDocuemntLibrary(drone, siteName);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(folderDelete.exists(), "Folder was synched in client");
            share.deleteContentInDocLib(drone, folderDelete.getName());
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(fileDelete.exists(), "File was successfuly deleted from client after share.");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - deleteFolderWithFileInShare", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * Delete folder with file in client
     * Step1 - Open windows explorer and access the sync location
     * Step2 - Create a new folder and open the created the folder
     * Step3 - Inside the folder create a text file
     * Step4 - Open the file and add a new line
     * Step5 - Save the notepad
     * Step6 - Wait for the sync - in this it is client sync time
     * Step7 - login in to share
     * step9 - Open site dashboard and navigate to the folder created in client
     * step10 - Check whether the file is present
     * Step11 - Now in client navigate back to sync set
     * Step12 - Right click on the folder and click on delete
     * Step13 - Wait for the sync time - client sync time
     * Step14 - Check in share the folder is deleted and file is also deleted.
     */
    @Test
    public void deleteFolderWithFileInClient()
    {
        File folderDelete = getRandomFolderIn(getLocalSiteLocation(), "deleteFolderWF");
        File fileDelete = getRandomFileIn(folderDelete, "fileDel", "txt");
        try
        {
            explorer.openApplication();
            explorer.openFolder(folderDelete.getParentFile());
            explorer.createAndOpenFolder(folderDelete.getName());
            explorer.goBack(folderDelete.getParentFile().getName());
            notepad.openApplication();
            notepad.edit("content");
            notepad.saveAs(fileDelete);
            notepad.exitApplication();
            syncWaitTime(CLIENTSYNCTIME);

            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, folderDelete.getName());
            Assert.assertTrue(share.isFileVisible(drone, fileDelete.getName()), "File exist in Share");

            explorer.deleteFolder(folderDelete.getName());
            explorer.closeExplorer();
            syncWaitTime(CLIENTSYNCTIME);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone, folderDelete.getName()), "Folder was deleted automaticaly after sync.");
        }
        catch (Throwable e)
        {
            throw new SkipException("test case failed - deleteFolderWithFileInClient", e.getCause());
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }
}
