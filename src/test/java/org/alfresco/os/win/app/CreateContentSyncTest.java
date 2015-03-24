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

import org.alfresco.po.share.site.document.ContentType;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.po.share.site.document.EditInGoogleDocsPage;
import org.alfresco.po.share.site.document.GoogleDocsAuthorisation;
import org.alfresco.po.share.site.document.GoogleDocsRenamePage;
import org.alfresco.po.share.site.document.GoogleSignUpPage;
import org.alfresco.sync.DesktopSyncTest;
import org.alfresco.test.AlfrescoTest;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.TestException;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to Create of file in both
 * client (Windows machine) and share
 * 
 * @author Sprasanna
 * @author Paul Brodner
 */
public class CreateContentSyncTest extends DesktopSyncTest
{
    private static final Logger logger = Logger.getLogger(CreateContentSyncTest.class);
    Notepad notepad = new Notepad();

    File folderChild = null;

    WindowsExplorer explorer = new WindowsExplorer();

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
    @AlfrescoTest(testlink = "ALF-2569")
    @Test
    public void createFileInClient()
    {
        File clientTestFile = getRandomFileIn(getLocalSiteLocation(), "createFile", "txt");
        try
        {
            notepad.openApplication();
            notepad.saveAs(clientTestFile);
            notepad.close(clientTestFile);
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, clientTestFile.getName()), "Client Notepad file is successfuly synched in site.");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("File was not created or synched on Share", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * This test case will create a file in share and sync to the client
     * Step1 - Login in share
     * Step2 - Access the sync site
     * Step3 - Create a file in Document library
     * Step4 - Wait for the share sync time
     * Step5 - Validate the file is synced to client
     */
    @AlfrescoTest(testlink = "ALF-2570")
    @Test
    public void createFileInShare()
    {
        File shareTestFile = getRandomFileIn(getLocalSiteLocation(), "sharecreatefile", "txt");
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            shareTestFile = share.newFile(shareTestFile.getName(), "share created file for sync");
            share.uploadFile(drone, shareTestFile);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(shareTestFile.exists(), "Share created file exist");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new TestException("test case failed - createFileInShare ", e);
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
    @AlfrescoTest(testlink = "ALF-2571")
    @Test
    public void createFolderAndFileInShare()
    {
        File folderToCreate = getRandomFolderIn(getLocalSiteLocation(), "createFolder");
        File fileInClient = getRandomFileIn(folderToCreate, "fileSynched", "txt");
        File fileToUpload = share.newFile(fileInClient.getName(), "empty");

        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.createFolder(drone, folderToCreate.getName(), folderToCreate.getName(), folderToCreate.getName());
            share.navigateToFolder(drone, folderToCreate.getName());
            share.uploadFile(drone, fileToUpload);

            syncWaitTime(SERVERSYNCTIME);

            Assert.assertTrue(folderToCreate.exists(), "Folder synched from Remote, exists in client");
            Assert.assertTrue(fileInClient.exists(), "File uploaded on Remote, was synched on client");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("test case failed - createFolderAndFileInShare ", e);
        }
        finally
        {
            shareLogin.logout(drone);
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

    @AlfrescoTest(testlink = "ALF-2573")
    @Test
    public void createFolderTreeInClient()
    {
        File folderParent = getRandomFolderIn(getLocalSiteLocation(), "folderParent");
        folderChild = getRandomFolderIn(folderParent, "folderChild1");
        try
        {
            explorer.openApplication();
            explorer.openFolder(getLocalSiteLocation());
            explorer.createAndOpenFolder(folderParent.getName());
            explorer.createAndOpenFolder(folderChild.getName());
            explorer.closeExplorer();

            syncWaitTime(CLIENTSYNCTIME);

            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, folderParent.getName()), "Folder parent is visible in Share");
            share.navigateToFolder(drone, folderParent.getName());
            Assert.assertTrue(share.isFileVisible(drone, folderChild.getName()), "Subfolder is visible in Share");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("test case failed - createFolderTreeInClient", e);
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
    @Test(dependsOnMethods = "createFolderTreeInClient")
    public void createFileInsideFolderInClient()
    {
        File fileName = getRandomFileIn(folderChild, "createFile", "txt");
        File downloadFile = new File(downloadPath, fileName.getName());

        try
        {
            notepad.openApplication();
            notepad.edit("desktop sync");
            notepad.saveAs(fileName);
            notepad.close(fileName);

            syncWaitTime(CLIENTSYNCTIME);

            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.navigateToFolder(drone, folderChild.getParentFile().getName());
            share.navigateToFolder(drone, folderChild.getName());

            Assert.assertTrue(share.isFileVisible(drone, fileName.getName()));
            share.shareDownloadFileFromDocLib(drone, fileName.getName(), downloadFile.getPath());

            Assert.assertTrue(compareTwoFiles(fileName.getPath(), downloadFile.getPath()));
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("Test Case Failed - createFileInsideFolderInClient", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * Test cases to create empty sub folder in share without any files is
     * sycned to the client correctly
     * Step1 - login in share
     * Step2 - Open the sync site
     * Step3 - Create a Folder
     * Step4 - open the created Folder
     * Step5 - Create a sub Folder
     * Step6 - Wait for Sync time which is 5 mins for share
     * Step7 - In client validate both the folder and sub folder is present
     */
    @AlfrescoTest(testlink = "ALF-2574")
    @Test
    public void createFolderInShare()
    {
        File shareFolderParent = getRandomFolderIn(getLocalSiteLocation(), "createShareFolder");
        File shareFolderChild = getRandomFolderIn(shareFolderParent, "creatChildFolder");
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.createFolder(drone, shareFolderParent.getName(), shareFolderParent.getName(), shareFolderParent.getName());
            share.selectContent(drone, shareFolderParent.getName());
            share.createFolder(drone, shareFolderChild.getName(), shareFolderChild.getName(), shareFolderChild.getName());
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(shareFolderChild.exists(), "Shared folder parent was synched in client");
            Assert.assertTrue(shareFolderChild.exists(), "Shared folder child was synched in client");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("test case failed - createFolderInShare ", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    @AlfrescoTest(testlink = "ALF-2572")
    // @Test
    public void createGoogleDocInShare()
    {
        File shareTestFile = getRandomFileIn(getLocalSiteLocation(), "createGoogleDoc", "docx");
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            DocumentLibraryPage docPage = new DocumentLibraryPage(drone);
            GoogleDocsAuthorisation googleAuth = docPage.getNavigation().selectCreateContent(ContentType.GOOGLEDOCS).render();
            GoogleSignUpPage signUpPage = googleAuth.submitAuth().render();
            EditInGoogleDocsPage googleDocsPage = signUpPage.signUp(googleusername, googlepassword).render();
            GoogleDocsRenamePage renameDocs = googleDocsPage.renameDocumentTitle().render();
            googleDocsPage = renameDocs.updateDocumentName(shareTestFile.getName()).render();
            googleDocsPage.selectSaveToAlfresco().render();
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(shareTestFile.exists(), "GoogleDoc created on Remote was synched on client");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("test case failed - createGoogleDocInShare ", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }
}
