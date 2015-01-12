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

import org.alfresco.application.windows.NotepadApplications;
import org.alfresco.explorer.WindowsExplorer;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
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
 * This class will contain all the test cases related to Rename of file in both client (Windows machine) and share
 * 
 * @author Sprasanna
 */
public class RenameContentSyncTest extends AbstractTest
{
    private static Log logger = LogFactory.getLog(RenameContentSyncTest.class);
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
     * Rename of file in client
     * Step1 - create a file in client sync location
     * Step2 - Wait for the sync time - Client sync time
     * Step3 - Validate in share whether the file is avilable correctly
     * Step4 - In client rename the file using right click rename and enter the new name
     * Step5 - wait for the sync time - Client sync time
     * Step6 - Validate the file name is changed in share
     */
    @Test
    public void renameFileInClient()
    {
        logger.info("Rename of file in client");
        String fileName = "renamefileclient" + fileAppend;
        String newName = "fileclientrename" + fileAppend;
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.rightClickCreate(siteName, fileName, Application.TEXTFILE);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
            explorer.renameFile(fileName + FILEEXT, newName);
            syncWaitTime(CLIENTSYNCTIME);
            share.navigateToDocuemntLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, newName + FILEEXT));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(siteName);
            explorer.closeExplorer();
        }
    }

    /**
     * rename a folder with file in client
     * Step1 - open the sync location
     * Step2 - Create a new folder (folderclientrename)
     * Step3 - Navigate to the newly created folder and upload a file (fileclientrename)
     * Step4 - Navigate back to the synclocation
     * Step5 - Wait for the sync time - Client sync time
     * Step6 - Check in share whether the folder and file is visible
     * Step7 - In client rename the folder (renameclientfolder)
     * Step8 - Wait for the sync time - client sync time
     * Step9 - validate in share whether the folder is renamed
     * Step10 - Validate whether the file is still present inside the folder.
     */
    @Test
    public void renameFolderInClient()
    {
        logger.info("rename a folder with file in client");
        String folderName = "folderclientrename" + fileAppend;
        String fileName = "fileclientrename" + fileAppend;
        String rename = "renameclientfolder" + fileAppend;
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.createNewFolderMenu(folderName);
            explorer.openFolderFromCurrent(folderName);
            explorer.rightClickCreate(folderName, fileName, Application.TEXTFILE);
            explorer.backButtonInExplorer(siteName);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, folderName));
            explorer.renameFolder(folderName, rename);
            syncWaitTime(CLIENTSYNCTIME);
            share.navigateToDocuemntLibrary(drone, siteName);
            Assert.assertTrue((share.isFileVisible(drone, rename)));
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + rename);
            Assert.assertTrue((share.isFileVisible(drone, fileName + FILEEXT)));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName() + e.getStackTrace());
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(siteName);
            explorer.closeExplorer();
        }
    }

    /**
     * Rename of file in share
     * Step1 - Login into share
     * Step2 - Upload the file in site document library
     * Step3 - Wait for the sync time - Server sync time
     * Step4 - Check in the client sync location the file is visible.
     * Step6 - In share rename the file
     * Step7 - Wait for the sync time - server sync time
     * step8 - In client synclocation the file is renamed correctly
     */
    @Test
    public void renameFileInShare()
    {
        logger.info("Rename a file in share");
        String fileName = "filesharerename"+fileAppend + FILEEXT;
        String rename = "renamesharefile"+fileAppend + FILEEXT;
        try
        {
            File file = share.newFile(fileName, fileName);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.uploadFile(drone, file);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + fileName));
            share.editContentNameInline(drone, fileName, rename, true);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(explorer.isFilePresent(syncLocation + File.separator + fileName));
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + rename));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName()+ e.getMessage());
        }
        finally
        {
            share.logout(drone);
        }
    }

    /**
     * Rename of empty folder in share
     * Step1 - login in to share
     * Step2 - Create a folder in the document library
     * Step3 - Wait for the sync time - server sync time
     * Step4 - Rename the folder in share
     * Step5 - Wait for the sync time - server sync time
     * Step6 - Validate in client the folder name is changed to the renamed name.
     */
    @Test
    public void renameFolderInShare()
    {
        logger.info("Rename of empty folder in share");
        String folderName = "foldersharerename" + fileAppend;
        String rename = "renamesharefolder" + fileAppend;
        try
        {
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderName, folderName, folderName);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFolderPresent(syncLocation + File.separator + folderName));
            share.editContentNameInline(drone, folderName, rename, true);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(explorer.isFolderPresent(syncLocation + File.separator + folderName));
            Assert.assertTrue(explorer.isFolderPresent(syncLocation + File.separator + rename));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName() + e.getStackTrace());
        }
        finally
        {
            share.logout(drone);
        }
    }

    /**
     * Rename a folder with File in share
     */
    @Test
    public void renameFolderWithFileInShare()
    {
        logger.info("Rename a folder with file in share");
        String folderName = "foldersharerename" + fileAppend;
        String rename = "renamefolder" + fileAppend;
        String fileName = "fileshare" + fileAppend + FILEEXT;
        String fileRename = "renamefile"+ fileAppend + FILEEXT;
        try
        {
            File file = share.newFile(fileName, fileName);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderName, folderName, folderName);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            share.uploadFile(drone, file);
            share.navigateToDocuemntLibrary(drone, siteName);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFolderPresent(syncLocation + File.separator + folderName));
            DocumentLibraryPage doclib = share.editContentNameInline(drone, folderName, rename, true);
            doclib.render();
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + rename);
            share.editContentNameInline(drone, fileName, fileRename, true);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(explorer.isFolderPresent(syncLocation + File.separator + folderName));
            Assert.assertTrue(explorer.isFolderPresent(syncLocation + File.separator + rename));
            Assert.assertFalse(explorer.isFilePresent(syncLocation + File.separator + rename + File.separator + fileName));
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + rename + File.separator + rename));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName() + e.getStackTrace());
        }
        finally
        {
            share.logout(drone);
        }
    }
}
