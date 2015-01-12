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

package org.alfresco.sync.application.notepad;

import java.io.File;
import java.util.regex.Pattern;

import org.alfresco.application.windows.NotepadApplications;
import org.alfresco.explorer.WindowsExplorer;
import org.alfresco.sync.AbstractTest;
import org.alfresco.sync.ShareUtil;
import org.alfresco.utilities.Application;
import org.alfresco.utilities.LdtpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to Create of file in both client (Windows machine) and share
 * 
 * @author Sprasanna
 */
public class CreateContentSyncTest extends AbstractTest
{
    private static Log logger = LogFactory.getLog(CreateContentSyncTest.class);
    NotepadApplications notepad = new NotepadApplications();
    ShareUtil share = new ShareUtil();
    WindowsExplorer explorer = new WindowsExplorer();
    LdtpUtil ldtpObject = new LdtpUtil();
    String[] userInfo = new String[2];
    String syncLocation = "";
    String shareFilePath = "";
    final String FILEEXT = ".txt";
    String clientCreatedFolder;

    @BeforeClass
    public void initialSetupOfShare()
    {
        userInfo[0] = username;
        userInfo[1] = password;
        syncLocation = location + File.separator + siteName;
        shareFilePath = downloadPath.toLowerCase();
    }

    /**
     * This test will create a notePad file in client and validate whether it is visible in Share
     * Step1 - Create a file in Note pad save it without any content
     * Step2 - Close Notepad
     * Step3 - Wait for Sync time which is 2 mins for client
     * Step4 - Login in share
     * Step5 - Access sync site
     * Step6 - Check the new file created in client is present in share.
     * 
     * @throws Exception
     */

    @Test
    public void createFileInClient()
    {
        logger.info("test to create file in client started");
        String fileName = "createfileclient" + fileAppend;
        try
        {
            notepad.openNotepadApplication();
            notepad.setNotepadWindow("Notepad");
            notepad.saveAsNotpad(syncLocation, fileName);
            notepad.closeNotepad(fileName);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));

        }
        catch (Throwable e)
        {
            throw new SkipException("create test case failed", e);
        }
        finally
        {
            share.logout(drone);
        }
    }

    /**
     * This test will create a Folder and then a file in Share and validate whether it is visible in client
     * Step1 - login in share
     * Step2 - Access sync site
     * Step3 - Create a Folder
     * Step4 - Open the folder created in Step3
     * Step5 - Upload File inside the folder
     * Step6 - Wait for Sync time which is 5 mins for share
     * Step6 - in the client access the sync folder
     * Step7 - Validate the folder with file created in share is synced correctly
     */

    @Test
    public void createFolderAndFileInShare()
    {
        logger.info("test to create a folder with file in share started");
        String name = "createfolderandfileshare" + fileAppend;
        try
        {
            String fileName = (name + FILEEXT).toLowerCase();
            String folderName = name;
            File file = share.newFile(fileName, fileName);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderName, "sync", "sync");
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            share.uploadFile(drone, file);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFolderPresent(syncLocation + File.separator + folderName));
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + folderName + File.separator + fileName));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
        }
    }

    /**
     * Test to create folder and sub Folder in client and validate whether folders is sync correctly in share.
     * Step1 - In client open the explorer , access the sync folder
     * Step2 - From the explorer menu create a new folder
     * Step3 - Open the created folder and using the context folder create another folder
     * Step4 - Wait for Sync time which is 2 mins for client
     * Step6 - Login in share
     * Step7 - open sync site document library
     * Step8 - Validate the folder is synced correctly
     * Step9 - Navigate to folder
     * Step10 - Validate whether the subFolder created is synced correctly
     */

    @Test
    public void createFolderTreeInClient()
    {
        logger.info("test to create a folder and then sub folder in client");
        String folderName = "createfolderclient" + fileAppend;
        String subFolderName = "createsubfolderclient" + fileAppend;
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.createandOpenFolder(folderName);
            explorer.rightClickCreate(folderName, subFolderName, Application.FOLDER);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, folderName));
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            Assert.assertTrue(share.isFileVisible(drone, subFolderName));

        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            ldtpObject.setOnWindow(folderName);
            explorer.closeExplorer();
            clientCreatedFolder = folderName + File.separator + subFolderName;
        }
    }

    /**
     * Test to add file inside the sub folder in the client
     * This test case is dependents on the previous method testToCreateFolderInClient
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
        logger.info("test to create a FILE inside the sub folder created in previous testcase ");
        String fileName = "createfileinsidefolderclient" + fileAppend;
        String syncPath = (syncLocation + File.separator + clientCreatedFolder +File.separator + fileName + FILEEXT).toLowerCase();
        String sharePath = shareFilePath + File.separator + fileName + FILEEXT;
        String[] folders = clientCreatedFolder.split(Pattern.quote(File.separator));
        String currentFolder = folders[(folders.length) - 1];
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation + File.separator + clientCreatedFolder);
            explorer.rightClickCreate(currentFolder, fileName, Application.TEXTFILE);
            explorer.oepnFileInCurrentFolder(fileName);
            notepad.editNotepad("desktop sync", fileName);
            notepad.ctrlSSave();
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + clientCreatedFolder);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
            share.shareDownloadFileFromDocLib(drone, fileName + FILEEXT, sharePath);
            Assert.assertTrue(compareTwoFiles(syncPath, sharePath));
            notepad.closeNotepad(fileName);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(clientCreatedFolder);
            explorer.closeExplorer();
        }
    }

    /**
     * Test cases to create empty sub folder in share without any files is sycned to the client correctly
     * Step1 - login in share
     * Step2 - Open the sync site
     * Step3 - Create a Folder
     * Step4 - open the created Folder
     * Step5 - Create a sub Folder
     * Step6 - Wait for Sync time which is 5 mins for share
     * Step7 - In client validate both the folder and sub folder is present
     */
    @Test
    public void createFolderInShare()
    {
        logger.info("test to create a folder in share"); 
        String shareCreatedFolder = "createsharefolder" + fileAppend;
        String subFolderName = "createsharesubfolder" + fileAppend;
        try
        {
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, shareCreatedFolder, shareCreatedFolder, shareCreatedFolder);
            share.selectContent(drone, shareCreatedFolder);
            share.createFolder(drone, subFolderName, subFolderName, subFolderName);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFolderPresent(syncLocation + File.separator + shareCreatedFolder));
            Assert.assertTrue(explorer.isFolderPresent(syncLocation + File.separator + shareCreatedFolder + File.separator + subFolderName));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
        }
    }
}
