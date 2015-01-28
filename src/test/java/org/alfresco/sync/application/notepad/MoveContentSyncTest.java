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
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.site.document.ContentType;
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
 * This class will contain all the test cases related to move of file in both client (Windows machine) and share
 * 
 * @author Sprasanna
 */
public class MoveContentSyncTest extends AbstractTest
{
    private static Log logger = LogFactory.getLog(MoveContentSyncTest.class);
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
        try
        {
        
        // This is to create sample file 
        explorer.openWindowsExplorer();
        explorer.openFolder("c:\\samplefile");
        explorer.rightClickCreate("samplefile", "movefileintosub"+fileAppend, Application.TEXTFILE);
        explorer.closeExplorer();
       
            // The below steps are to create data setup for all the test case
        explorer.openWindowsExplorer();
        explorer.openFolder(syncLocation);
        setupMoveFolderwithFileSubInClient();
        setupMoveFileInsideEmptyFolderInClient();
        explorer.closeExplorer();
        
        // Data setup in Share 
        setupInShare();
        syncWaitTime(SERVERSYNCTIME);
        
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName() +e.getMessage());
        }
        finally
        {
            share.logout(drone);
        }
    }

    /**
     * Data setup MoveFolderwithFileSubInClient
     */
    private void setupMoveFolderwithFileSubInClient()
    {
        logger.info("Data setup - Move folder with File in Subscription");
        String folderToMove = "movefolderwithfileclient" + fileAppend;
        String fileName = "fileclient" + fileAppend;
        String currentFolder = "curernt" + fileAppend;
        
        explorer.createNewFolderMenu(folderToMove);
        explorer.rightClickCreate(siteName, currentFolder, Application.FOLDER);
        explorer.openFolderFromCurrent(currentFolder);
        explorer.rightClickCreate(currentFolder, fileName, Application.TEXTFILE);
        explorer.oepnFileInCurrentFolder(fileName);
        notepad.setNotepadWindow(fileName);
        notepad.editNotepad("desktop sync testing", fileName);
        notepad.ctrlSSave();
        notepad.closeNotepad(fileName);
    }
   
    /**
     * Move folder with File in Subscription
     * Step1 - open window explorer and open sync folder
     * Step2 - Create two folders (one folder to move inside another)
     * Step3 - One the first folder and create a file
     * Step4 - Add a line of text inside the file
     * Step5 - Close the notepad
     * Step6 - Wait for it sync - Client wait time
     * Step7 - Hit back button in the explorer
     * Step8 - Move the folder with file inside the "movefolderClient"
     * Step9 - Validate whether the move is successful in share
     */
    @Test
    public void moveFolderwithFileWithInSubInClient()
    {
        logger.info("Move folder with File in Subscription");
        String folderToMove = "movefolderwithfileclient" + fileAppend;
        String fileName = "fileclient" + fileAppend;
        String currentFolder = "curernt" + fileAppend;
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.moveFolderInCurrent(currentFolder, siteName, folderToMove);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderToMove);
            Assert.assertTrue(share.isFileVisible(drone, currentFolder));
            share.selectContent(drone, currentFolder);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName() +e.getMessage());
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(siteName);
            explorer.closeExplorer();
        }
    }
/**
 *  All the file and folder required for the test in share 
 */
    private void setupInShare()
    {
        String siteNameToMove = "moveSite" + fileAppend;
        String folderName = "sharemovefolder" + fileAppend;
        String fileName = "sharemovefile" +fileAppend+ FILEEXT;
        String folderName_2 = "sharemovefolder" + fileAppend;
        String folderToMove = "sharefoldertomove" + fileAppend;
        ContentDetails content = new ContentDetails();
        content.setName(fileName);
        content.setDescription(fileName);
        content.setTitle(fileName);
        content.setContent("share created file");
        try
        {
            share.loginToShare(drone, userInfo, shareUrl);
            share.createSite(drone, siteNameToMove, "movesite", "public");
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderName, folderName, folderName);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            share.createContent(drone, content, ContentType.PLAINTEXT);
            share.navigateToDocuemntLibrary(drone, siteName);
            share.createFolder(drone, folderName_2, folderName_2, folderName_2);
            share.createFolder(drone, folderToMove, folderToMove, folderToMove);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName_2);
            share.createContent(drone, content, ContentType.PLAINTEXT);
        }
        catch(Exception e)
        {
            
        }
    }  
    
    /**
     * Move folder in share to a different site which is out of subscription
     * Step1 - Login in share
     * Step2 - Create a new site (movesite)
     * Step3 - Open the sync site
     * Step4 - Create a folder and a file inside the folder
     * Step5 - Move the folder to moveSite
     * Step6 - Wait for the sync time of 5 mins
     * Step7 - In client check whether the folder is not synced
     */
    @Test
    public void moveFolderOutOfSubInShare()
    {
        logger.info("Move folder in share to a different site which is out of subscription");
        String siteNameToMove = "moveSite" + fileAppend;
        String folderName = "sharemovefolder" + fileAppend;
        try
        {
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.copyOrMoveArtifact(drone, "All Sites", siteNameToMove, null, folderName, "Move");
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(explorer.isFolderPresent(syncLocation + File.separator + folderName));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName() + e.getMessage());
        }
        finally
        {
            share.logout(drone);
        }
    }

    /**
     * Move folder to a folder in share within the subscription
     * Step1 - Login in to share
     * Step2 - Open site Document library
     * Step3 - Create two folder ("sharemovefolder" and "sharefoldertomove")
     * Step4 - Open the sharemovefolder and create a text file
     * Step5 - move sharemovefolder to sharefoldertomove
     * Step6 - Wait for the sync time - in share it is 5 mins
     * Step7 - Check in the client explorer the folder is moved insided sharefoldertomove
     * step8 - Validate that the folder is not directly visible.
     */
    @Test
    public void moveFolderWithInSubInShare()
    {
        logger.info("Move folder to a folder in share within the subscription");
        String folderName = "sharemovefolder" + fileAppend;
        String fileName = "sharemovefile" + fileAppend + FILEEXT;
        String folderToMove = "sharefoldertomove" + fileAppend;
        try
        {
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.copyOrMoveArtifact(drone, "All Sites", siteName, folderToMove, folderName, "Move");
            syncWaitTime(SERVERSYNCTIME);
            share.navigateToDocuemntLibrary(drone, siteName);
            Assert.assertFalse(explorer.isFolderPresent(syncLocation + File.separator + folderName));
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + folderToMove + File.separator + folderName + File.separator + fileName));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName() + e.getMessage());
        }
        finally
        {
            share.logout(drone);

        }
    }

    private void setupMoveFileInsideEmptyFolderInClient()
    {
        logger.info("Move file inside a empty folder within the subscription in client");
        String fileName = "movefileemptyclient" +fileAppend;
        String folderNameToMove = "moveemptyfolderclient" + fileAppend;
        explorer.rightClickCreate(siteName, folderNameToMove, Application.FOLDER);
        explorer.rightClickCreate(siteName, fileName, Application.TEXTFILE);
    }
    /**
     * Move file inside a empty folder within the subscription in client
     * Step1 - open windows explorer
     * Step2 - open the sync location
     * Step3 - create a folder
     * Step4 - Create a text file
     * Step5 - Wait for the sync time - Client sync time
     * Step6 - Now login in to share and check whether the file is visible
     * Step7 - In the client move the file into the folder created in step 3
     * Step8 - wait for the sync time - Client sync time
     * Step9 - Validate in share the file is moved and it is not visible in document library
     */
    @Test
    public void moveFileInsideEmptyFolderInClient()
    {
        logger.info("Move file inside a empty folder within the subscription in client");
        String fileName = "movefileemptyclient" +fileAppend;
        String folderNameToMove = "moveemptyfolderclient" + fileAppend;
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
            explorer.activateApplicationWindow(siteName);
            explorer.moveFileInCurrent(fileName, siteName, folderNameToMove);
            syncWaitTime(CLIENTSYNCTIME);
            share.navigateToDocuemntLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone, fileName + FILEEXT));
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderNameToMove);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName()+ e.getMessage());
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(folderNameToMove);
            explorer.closeExplorer();
        }
    }

    /**
     * Create and move immediately
     * Step1 - Open windows explorer
     * Step2 - Go to the sync location
     * Step3 - create a new folder and text file
     * Step4 - Immediately move the text file inside the folder
     * Step5 - Wait for the sync time - Client sync time
     * Step6 - Login in to share
     * Step7 - Validate whether the file is present inside the folder and not in the document library
     */
    @Test
    public void moveFileInsideFolderInClient()
    {
        logger.info("Test to Create and move immediately in client");
        String fileName = "movefileclient" + fileAppend;
        String folderNameToMove = "movefolderclient" + fileAppend;
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.createNewFolderMenu(folderNameToMove);
            explorer.rightClickCreate(siteName, fileName, Application.TEXTFILE);
            explorer.moveFileInCurrent(fileName, siteName, folderNameToMove);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone, fileName + FILEEXT));
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderNameToMove);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName() + e.getMessage());
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(folderNameToMove);
            explorer.closeExplorer();
        }
    }

    /**
     * Move File out of the subscription
     * Step1 - Open explorer
     * Step2 - Open the c:\\test folder
     * Step3 - Open the sync folder
     * Step4 - Right click and create a file
     * Step5 - Wait for the sync time - Client sync time
     * Step6 - Login into share
     * Step7 - Open site document library and check whether the file created in step4 is present
     * Step8 - Move the file to test folder
     * Step9 - Wait for the sync time - Client sync time
     * Step10 - Check the file is removed from document library
     */
    @Test
    public void moveFileOutOfSubInClient()
    {
        logger.info("Move file out of subscription in client");
        String fileToMove = "filetomove"+ fileAppend;
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder("c:\\moveout");
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.rightClickCreate(siteName, fileToMove, Application.TEXTFILE);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, fileToMove + FILEEXT));
            String windowName = ldtpObject.findWindowName(siteName);
            explorer.activateApplicationWindow(windowName);
            explorer.moveFileBetweenFolders(siteName, "moveout", fileToMove);
            syncWaitTime(CLIENTSYNCTIME);
            share.navigateToDocuemntLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone, fileToMove + FILEEXT));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName() +  e.getMessage());
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(siteName);
            explorer.closeExplorer();
            explorer.activateApplicationWindow("moveout");
            explorer.closeExplorer();
        }
    }

    /**
     * Move a File into the subscription
     * step1 - Open the sync location and sample file in an explorer
     * Step2 - move "movefileintosub" file from sample file to the sync location
     * step3 - wait for the sync time - Client sync time
     * Step4 - In share validate whether the newly moved file is present.
     */
    @Test
    public void moveFileIntoSubClient()
    {
        logger.info(" Move a  File into the subscription");
        String fileName = "movefileintosub" + fileAppend;
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.openWindowsExplorer();
            explorer.openFolder("c:\\samplefile");
            String windowName = ldtpObject.findWindowName("samplefile");
            explorer.activateApplicationWindow(windowName);
            explorer.moveFileBetweenFolders("samplefile", siteName, fileName);
            Thread.sleep(3000);
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + fileName + FILEEXT));
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName() + e.getMessage());
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow("samplefile");
            explorer.closeExplorer();
            explorer.activateApplicationWindow(siteName);
            explorer.closeExplorer();
        }
    }
}
