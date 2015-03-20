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

package org.alfresco.os.mac.app;

import java.io.File;

import org.alfresco.os.mac.DesktopSyncMacTest;
import org.testng.Assert;
import org.testng.TestException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to delete of a file and folder
 * in both environments: Client (MAC machine) and Share
 * 
 * @author Paul Brodner
 */
public class DeleteContentSyncTest extends DesktopSyncMacTest
{
    TextEdit notepad = new TextEdit();
    FinderExplorer explorer = new FinderExplorer();
    File _deleteFileClient;
    File _deleteFolderClient;
    File _deleteFileShareMisc;
    File _deleteFolderShareMisc;
    File _deleteFolderClientMisc;
    File _deleteFileClientMisc;

    /**
     * Prepare the files and folders in Client and Share prior to the tests
     * We use this approach in order to gain more time and just sync once the files.
     */
    @BeforeClass
    public void prepareData() throws Exception
    {
        super.initialSetupOfShare();

        _deleteFileClient = getRandomFileIn(getLocalSiteLocationClean(), "deleteFile", "rtf");
        notepad.openApplication();
        notepad.save(_deleteFileClient);
        notepad.close(_deleteFileClient);

        _deleteFolderClient = getRandomFolderIn(getLocalSiteLocationClean(), "deleteFolder");
        _deleteFolderClientMisc = getRandomFolderIn(getLocalSiteLocationClean(), "deleteFolderClientMisc");
        explorer.openApplication();
        explorer.createFolder(_deleteFolderClient);
        explorer.createFolder(_deleteFolderClientMisc);
        explorer.closeExplorer();

        _deleteFolderShareMisc = getRandomFolderIn(getLocalSiteLocationClean(), "deleteShareFolder");
        _deleteFileShareMisc = getRandomFileIn(_deleteFolderShareMisc, "shareFile", "rtf");
        _deleteFileClientMisc = getRandomFileIn(_deleteFolderClientMisc, "fileShareMisc", "rtf");

        notepad.openApplication();
        notepad.save(_deleteFileClientMisc);
        notepad.close(_deleteFileClientMisc);

        shareLogin.loginToShare(drone, userInfo, shareUrl);
        share.openSitesDocumentLibrary(drone, siteName);
        share.createFolder(drone, _deleteFolderShareMisc.getName(), _deleteFolderShareMisc.getName(), "data");
        share.navigateToFolder(drone, _deleteFolderShareMisc.getName());
        File tmpFile = share.newFile(_deleteFileShareMisc.getName(), _deleteFileShareMisc.getName());
        share.uploadFile(drone, tmpFile);
        shareLogin.logout(drone);

        syncWaitTime(SERVERSYNCTIME);
    }

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
    @Test(groups = { "MacOnly"})
    public void deleteFileInClient()
    {
        File deleteFile = _deleteFileClient;
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, deleteFile.getName()),
                    String.format("File {%s} exists in Share before deletion.", deleteFile.getName()));

            explorer.openApplication();
            explorer.deleteFile(deleteFile);
            explorer.closeExplorer();
            syncWaitTime(CLIENTSYNCTIME);
            share.refreshSharePage(drone);
            Assert.assertFalse(share.isFileVisible(drone, deleteFile.getName()),
                    String.format("File {%s} was removed from Share after Client deletion.", deleteFile.getName()));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new TestException(e.getMessage(), e.getCause());
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
    @Test(groups = { "MacOnly"})
    public void deleteFolderInClient()
    {
        File deleteTestFolder = _deleteFolderClient;
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, deleteTestFolder.getName()),
                    String.format("Folder [%s] exists in Share before deletion", deleteTestFolder.getName()));

            explorer.openApplication();
            explorer.deleteFolder(deleteTestFolder);
            explorer.closeExplorer();

            syncWaitTime(CLIENTSYNCTIME);
            share.refreshSharePage(drone);
            Assert.assertFalse(share.isFileVisible(drone, deleteTestFolder.getName()),
                    String.format("Folder [%s] was deleted fromShare after deletion in client", deleteTestFolder.getName()));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new TestException(e.getMessage(), e.getCause());
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
    @Test(groups = { "MacOnly"})
    public void deleteFolderWithFileInShare()
    {
        try
        {
            Assert.assertTrue(_deleteFolderShareMisc.exists(),
                    String.format("Folder [%s] is synched in Client prior to test", _deleteFolderShareMisc.getPath()));

            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.navigateToDocuemntLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, _deleteFolderShareMisc.getName()),
                    String.format("Folder [%s] exists in Share prior to deletion.", _deleteFolderShareMisc.getName()));

            share.deleteContentInDocLib(drone, _deleteFolderShareMisc.getName());
            syncWaitTime(SERVERSYNCTIME);

            Assert.assertFalse(_deleteFolderShareMisc.exists(),
                    String.format("Folder [%s] was successfuly deleted from Client after sync.", _deleteFolderShareMisc.getPath()));
            Assert.assertFalse(_deleteFileShareMisc.exists(),
                    String.format("File [%s] was successfuly deleted from Client after sync.", _deleteFileShareMisc.getPath()));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new TestException(e.getMessage(), e.getCause());
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
    @Test(groups = { "MacOnly"})
    public void deleteFolderWithFileInClient()
    {
        File folderDelete = _deleteFolderClientMisc;
        File fileDelete = _deleteFileClientMisc;
        try
        {
            logger.info("Testing using: " + fileDelete.getPath());
            Assert.assertTrue(fileDelete.exists(), String.format("File [%s] exist in Client prior to test", fileDelete.getPath()));

            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, folderDelete.getName());
            Assert.assertTrue(share.isFileVisible(drone, fileDelete.getName()), String.format("File [%s] exist in Share", fileDelete.getName()));

            explorer.openApplication();
            explorer.deleteFolder(folderDelete);
            explorer.closeExplorer();

            syncWaitTime(CLIENTSYNCTIME);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone, folderDelete.getName()),
                    String.format("Folder [%s] was deleted automaticaly after sync.", folderDelete.getName()));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new TestException(e.getMessage(), e.getCause());
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }
}
