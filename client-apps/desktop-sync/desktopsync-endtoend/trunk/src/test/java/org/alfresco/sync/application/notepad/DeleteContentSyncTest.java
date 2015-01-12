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
 * This class will contain all the test cases related to delete of file in both client (Windows machine) and share
 * 
 * @author Sprasanna
 */
public class DeleteContentSyncTest extends AbstractTest
{
    private static Log logger = LogFactory.getLog(DeleteContentSyncTest.class);
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
     * @throws InterruptedException 
     */
    @Test
    public void deleteFileCreated() throws InterruptedException 
    {
        logger.info("Test case to delete a file just created");
        // String fileName = share.getFileName(share.getTestName() + "10").toLowerCase();
        String fileName = "clientdelete6";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            notepad.openNotepadApplication();
            notepad.setNotepadWindow("Notepad");
            notepad.saveAsNotpad(syncLocation, fileName);
            notepad.editNotepad("desktop Automated Testing", fileName);
            notepad.ctrlSSave();
            notepad.closeNotepad(fileName);
            syncWaitTime(CLIENTSYNCTIME);
            explorer.activateApplicationWindow(siteName);
            explorer.deleteFile(fileName, true);
            explorer.activateApplicationWindow(siteName);
            explorer.closeExplorer();
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone, fileName + FILEEXT));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            syncWaitTime(SERVERSYNCTIME);
        //    explorer.activateApplicationWindow(siteName);
        //    explorer.closeExplorer();
        //    syncWaitTime(CLIENTSYNCTIME);
          
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
    @Test(dependsOnMethods = "deleteFileCreated")
    public void deleteFolderInClient()
    {
        logger.info("Test case to delete a folder which is already created by share");
        String folderName = "sharecreatedfolder";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderName, folderName, folderName);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFolderPresent(syncLocation + File.separator + folderName));
            explorer.activateApplicationWindow(siteName);
            explorer.deleteFolder(folderName, true);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone, folderName));
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
            try
            {
                syncWaitTime(SERVERSYNCTIME);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
        logger.info("Test share delete a folder with File which is synced");
        // String folderName = share.getFileName(share.getTestName()).toLowerCase();
        // String fileName = share.getFileName(share.getTestName() + FILEEXT).toLowerCase();
        String folderName = "sharefolderdelete";
        String fileName = "sharefolderfiledelete" + FILEEXT;
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
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + folderName + File.separator + fileName));
            share.deleteContentInDocLib(drone, folderName);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(explorer.isFilePresent(syncLocation + File.separator + folderName + File.separator + fileName));

        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            try
            {
                syncWaitTime(SERVERSYNCTIME);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
        logger.info("Test delete folder with file in client");
        // String folderName = share.getFileName(share.getTestName()+ "3").toLowerCase();
        // String fileName = share.getFileName(share.getTestName()).toLowerCase();
        String folderName = "clientdeletefolder";
        String fileName = "clientdeletefile";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.createandOpenFolder(folderName);
            explorer.rightClickCreate(folderName, fileName, Application.TEXTFILE);
            explorer.oepnFileInCurrentFolder(fileName);
            notepad.editNotepad("sync client testing", fileName);
            notepad.ctrlSSave();
            notepad.closeNotepad(fileName);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
            explorer.activateApplicationWindow(folderName);
            explorer.backButtonInExplorer(siteName);
            explorer.activateApplicationWindow(siteName);
            explorer.deleteFolder(folderName, true);
            syncWaitTime(CLIENTSYNCTIME);
            Assert.assertFalse(share.isFileVisible(drone, fileName + FILEEXT));
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
            try
            {
                syncWaitTime(SERVERSYNCTIME);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
