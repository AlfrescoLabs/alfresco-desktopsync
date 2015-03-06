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
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to Create of a file and folder
 * in both environments: client (MAC machine) and share
 * 
 * @author Paul Brodner
 */
public class CreateContentSyncTest extends DesktopSyncMacTest
{
    TextEdit notepad = new TextEdit();
    File folderChild = null;
    FinderExplorer explorer = new FinderExplorer();

    /**
     * This test will create a notePad file in client and validate whether it is
     * visible in Share
     * Step1 - Create a file in NotePad save it without any
     * content
     * Step2 - Close NotePad
     * Step3 - Wait for Sync time which is 2 minutes for client
     * Step4 - Login in share
     * Step5 - Access sync site
     * Step6 - Check the new file created in client is present in share.
     * 
     * @throws Exception
     */
    @Test(groups = { "MacOnly", "Create" })
    public void createFileInClient()
    {
        File clientTestFile = getRandomFileIn(getLocalSiteLocationClean(), "createFile", "rtf");
        try
        {
            notepad.openApplication();
            notepad.save(clientTestFile);
            notepad.close(clientTestFile);
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);

            Assert.assertTrue(share.isFileVisible(drone, clientTestFile.getName()),
                    String.format("Client Notepad File [%s] is successfuly synched in site.", clientTestFile.getName()));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new TestException(e.getMessage(), e.getCause());
        }
        finally
        {
            shareLogin.logout(drone);
            clientTestFile.delete();
        }
    }

    /**
     * This test will create a Folder and then a file in Share and validate
     * whether it is visible in client
     * Step1 - login in share
     * Step2 - Access sync site
     * Step3 - Create a Folder
     * Step4 - Open the folder created in Step3
     * Step5 - Upload File inside the folder
     * Step6 - Wait for Sync time which is 5 mins for share
     * Step6 - in the client access the sync folder
     * Step7 - Validate the folder with file created in share is synced
     * correctly
     */
    @Test(groups = { "MacOnly", "Create" })
    public void createFolderAndFileInShare()
    {
        File folderToCreate = getRandomFolderIn(getLocalSiteLocationClean(), "createFolder");
        File fileInClient = getRandomFileIn(folderToCreate, "fileSynched", "rtf");
        File fileToUpload = share.newFile(fileInClient.getName(), "empty");

        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderToCreate.getName(), folderToCreate.getName(), folderToCreate.getName());
            share.navigateToFolder(drone, folderToCreate.getName());
            share.uploadFile(drone, fileToUpload);
            Assert.assertTrue(share.isFileVisible(drone, fileToUpload.getName()),
                    String.format("File {%s} was uploaded successfuly in Share", fileToUpload.getName()));

            syncWaitTime(SERVERSYNCTIME);

            Assert.assertTrue(folderToCreate.exists(), String.format("Folder synched from Remote [%s], exists in client", folderToCreate.getPath()));
            Assert.assertTrue(fileInClient.exists(), String.format("File uploaded on Remote [%s], was synched on client", fileInClient.getPath()));
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new TestException(e.getMessage(), e.getCause());
        }
        finally
        {
            shareLogin.logout(drone);
            folderToCreate.delete();
            fileToUpload.delete();
        }
    }

    /**
     * Test to create folder and sub Folder in client and validate whether
     * folders is sync correctly in share.
     * Step1 - In client open the explorer , access the sync folder
     * Step2 - From the explorer menu create a new folder
     * Step3 - Open the created folder and using the context folder create
     * another folder
     * Step4 - Wait for Sync time which is 2 minutes for client
     * Step6 - Login in share
     * Step7 - open sync site document library
     * Step8 - Validate the folder is synched correctly
     * Step9 - Navigate to folder
     * Step10 - Validate whether the subFolder created is synched correctly
     */
    @Test(groups = { "MacOnly", "Create" })
    public void createFolderTreeInClient()
    {
        File folderParent = getRandomFolderIn(getLocalSiteLocationClean(), "folderParent");
        folderChild = getRandomFolderIn(folderParent, "folderChild1");
        try
        {
            explorer.openApplication();
            explorer.createAndOpenFolder(folderParent);
            explorer.createAndOpenFolder(folderChild);
            explorer.closeExplorer();

            syncWaitTime(CLIENTSYNCTIME);

            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, folderParent.getName()),
                    String.format("Folder Parent [%s] is visible in Share", folderParent.getName()));
            share.navigateToFolder(drone, folderParent.getName());
            Assert.assertTrue(share.isFileVisible(drone, folderChild.getName()), String.format("Subfolder [%s] is visible in Share", folderChild.getName()));
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
     * Test to add file inside the sub folder in the client
     * This test case is dependents on the previous method
     * testToCreateFolderInClient
     * Step1 - open Windows Explorer
     * Step2 - open the sub folder
     * Step3 - Create a note pad inside the sub folder
     * Step5 - Open the note pad and add a line of text called desktop sync
     * Step6 - Wait for Sync time which is 2 mins for client
     * Step7 - login in share
     * Step8 - open the sync site dashboard
     * Step9 - Navigate to the sub Folder
     * step10 - Validate the file created in client is synced correctly
     */
    @Test(groups = { "MacOnly", "Create" }, dependsOnMethods = "createFolderTreeInClient")
    public void createFileInsideFolderInClient()
    {
        folderChild = folderChild.getParentFile();
        File fileName = getRandomFileIn(folderChild, "createFile", "rtf");
        File downloadFile = new File(downloadPath, fileName.getName());

        try
        {
            notepad.openApplication();
            notepad.edit("desktop sync");
            notepad.save(fileName);
            notepad.close(fileName);

            syncWaitTime(CLIENTSYNCTIME);

            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, folderChild.getName());
            Assert.assertTrue(share.isFileVisible(drone, fileName.getName()), String.format("File [%s] is synched in Share", fileName.getName()));
            share.shareDownloadFileFromDocLib(drone, fileName.getName(), downloadFile.getPath());

            Assert.assertTrue(compareTwoFiles(fileName.getPath(), downloadFile.getPath()), "File uploaded is the same as file downloaded.");
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
     * Test cases to create empty sub folder in share without any files is
     * Synched to the client correctly
     * Step1 - login in share
     * Step2 - Open the sync site
     * Step3 - Create a Folder
     * Step4 - open the created Folder
     * Step5 - Create a sub Folder
     * Step6 - Wait for Sync time which is 5 mins for share
     * Step7 - In client validate both the folder and sub folder is present
     */
    @Test(groups = { "MacOnly", "Create" })
    public void createFolderInShare()
    {
        File shareFolderParent = getRandomFolderIn(getLocalSiteLocationClean(), "createShareParentFldr");
        File shareFolderChild = getRandomFolderIn(shareFolderParent, "creatSharedChildFldr");
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, shareFolderParent.getName(), shareFolderParent.getName(), shareFolderParent.getName());
            share.navigateToFolder(drone, shareFolderParent.getName());
            share.createFolder(drone, shareFolderChild.getName(), shareFolderChild.getName(), shareFolderChild.getName());
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(shareFolderParent.exists(), String.format("Shared folder [%s] parent was synched in client", shareFolderParent.getPath()));
            Assert.assertTrue(shareFolderChild.exists(), String.format("Shared folder [%s] was synched in client", shareFolderChild.getPath()));
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
