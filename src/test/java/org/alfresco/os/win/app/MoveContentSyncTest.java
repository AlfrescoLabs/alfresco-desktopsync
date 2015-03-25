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

import org.alfresco.os.win.Application.type;
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.site.document.ContentType;
import org.alfresco.sync.DesktopSyncTest;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utilities.LdtpUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to move of file in both client (Windows machine) and share
 * 
 * @author Sprasanna
 */
public class MoveContentSyncTest extends DesktopSyncTest
{
    private static final Logger logger = Logger.getLogger(MoveContentSyncTest.class);
    Notepad notepad = new Notepad();
    WindowsExplorer explorer = new WindowsExplorer();
    File moveFileOutOFSub = null;
    File downloadLocation = null;

    /**
     * File and Folder deceleration for test MoveFolderwithFileSubInClient
     */
    File folderToMoveInFolderwithFile = null;
    File fileNameInFolderwithFile = null;
    File currentFolderInFolderwithFile = null;

    /**
     * File and Folder deceleration for share related move
     */
    String siteNameToMoveShare = null;
    File folderNameShare = null;
    File fileNameShare = null;
    File folderName_2Share = null;
    File folderToMoveShare = null;

    /**
     * File and Folder deceleration for MoveFileInsideEmptyFolderInClient
     */
    File emptyFolderName = null;
    File emptyFileName = null;

    @BeforeClass
    public void initialSetupOfShare()
    {
        downloadLocation = new File(downloadPath);
        moveFileOutOFSub = getRandomFileIn(downloadLocation, "moveFileOutOFSub", "txt");

        try
        {

            // This is to create sample file
            explorer.openApplication();
            explorer.openFolder(downloadLocation);
            explorer.rightClickCreate(downloadLocation.getName(), moveFileOutOFSub.getName(), type.TEXTFILE);

            // The below steps are to create data setup for all the test case
            explorer.openFolder(getLocalSiteLocation());
            setupMoveFolderwithFileSubInClient();
            // ** set up for test file inside empty folder in client
            setupMoveFileInsideEmptyFolderInClient();
            explorer.closeExplorer();

            // Data setup in Share
            setupMoveFolderOutOfSubInShare();
            setupMoveFolderWithInSubInShare();
            syncWaitTime(SERVERSYNCTIME);

        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("test case failed -  initialSetupOfShare ", e);
        }
    }

    /**
     * Data setup MoveFolderwithFileSubInClient
     * This will create a Two folder moveFolder
     * 
     * @throws Exception
     */
    private void setupMoveFolderwithFileSubInClient()
    {

        folderToMoveInFolderwithFile = getRandomFolderIn(getLocalSiteLocation(), "movefolder");
        fileNameInFolderwithFile = getRandomFileIn(folderToMoveInFolderwithFile, "fileclient", "txt");
        currentFolderInFolderwithFile = getRandomFolderIn(getLocalSiteLocation(), "currentfolder");

        explorer.createNewFolderMenu(folderToMoveInFolderwithFile.getName());
        explorer.createAndOpenFolder(currentFolderInFolderwithFile.getName());
        explorer.rightClickCreate(currentFolderInFolderwithFile.getName(), fileNameInFolderwithFile.getName(), type.TEXTFILE);
        explorer.openFileInCurrentFolder(fileNameInFolderwithFile);
        notepad.focus(fileNameInFolderwithFile);
        notepad.edit("desktop sync testing");
        notepad.save();
        notepad.close();
        explorer.goBack(getLocalSiteLocation().getName());

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
        try
        {
            explorer.openApplication();
            explorer.openFolder(getLocalSiteLocation());
            explorer.moveFolder(currentFolderInFolderwithFile, folderToMoveInFolderwithFile);
            explorer.closeExplorer();
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.navigateToFolder(drone, folderToMoveInFolderwithFile.getName());
            Assert.assertTrue(share.isFileVisible(drone, currentFolderInFolderwithFile.getName()), "current folder is not present in doc lib");
            share.selectContent(drone, currentFolderInFolderwithFile.getName());
            Assert.assertTrue(share.isFileVisible(drone, fileNameInFolderwithFile.getName()), "folder and file is moved successfully");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("test case failed - moveFolderwithFileWithInSubInClient ", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * All the file and folder required for the test in share
     * MoveFolderWithInSubInShare
     */
    private void setupMoveFolderWithInSubInShare()
    {
        folderName_2Share = getRandomFolderIn(getLocalSiteLocation(), "folderName_2Share");
        folderToMoveShare = getRandomFolderIn(getLocalSiteLocation(), "folderToMoveShare");
        fileNameShare = getRandomFile("sharemovefile", "txt");
        ContentDetails content = new ContentDetails();
        content.setName(fileNameShare.getName());
        content.setDescription(fileNameShare.getName());
        content.setTitle(fileNameShare.getName());
        content.setContent("share created file");
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.createFolder(drone, folderName_2Share.getName(), folderName_2Share.getName(), folderName_2Share.getName());
            share.createFolder(drone, folderToMoveShare.getName(), folderToMoveShare.getName(), folderToMoveShare.getName());
            share.navigateToFolder(drone, folderName_2Share.getName());
            share.createContent(drone, content, ContentType.PLAINTEXT);
            shareLogin.logout(drone);
        }
        catch (Exception e)
        {
            logger.error(e);
            throw new SkipException("share creation failed ", e);
        }
    }

    /**
     * Data setup for move folder out of MoveFolderOutOfSubInShare
     */
    public void setupMoveFolderOutOfSubInShare()
    {
        siteNameToMoveShare = "movesite" + RandomStringUtils.randomAlphanumeric(5);
        folderNameShare = getRandomFolderIn(getLocalSiteLocation(), "sharemovefolder");
        fileNameShare = getRandomFile("sharemovefile", "txt");
        ContentDetails content = new ContentDetails();
        content.setName(fileNameShare.getName());
        content.setDescription(fileNameShare.getName());
        content.setTitle(fileNameShare.getName());
        content.setContent("share created file");
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.createSite(drone, siteNameToMoveShare, "movesite", "public");
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.createFolder(drone, folderNameShare.getName(), folderNameShare.getName(), folderNameShare.getName());
            share.navigateToFolder(drone, folderNameShare.getName());
            share.createContent(drone, content, ContentType.PLAINTEXT);
            shareLogin.logout(drone);
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("Share data setup in share failed ", e);
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
    @AlfrescoTest(testlink = "ALF-261")
    @Test
    public void moveFolderOutOfSubInShare()
    {
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.copyOrMoveArtifact(drone, "All Sites", siteNameToMoveShare, folderNameShare.getName(), "Move",null);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(folderNameShare.exists(), "Folder move performed in share is synced correctly");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("test case failed - moveFolderOutOfSubInShare ", e);
        }
        finally
        {
            shareLogin.logout(drone);
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
    @AlfrescoTest(testlink = "ALF-2609")
    @Test
    public void moveFolderWithInSubInShare()
    {
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.copyOrMoveArtifact(drone, "All Sites", siteName,folderName_2Share.getName()  , "Move",getLocalSiteLocation().getName(),folderToMoveShare.getName());
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(
                    LdtpUtils.isFilePresent(folderToMoveShare.getAbsolutePath() + File.separator + folderName_2Share.getName() + File.separator
                            + fileNameShare.getName()), "Folder is moved correctly");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed - moveFolderWithInSubInShare", e);
        }
        finally
        {
            shareLogin.logout(drone);

        }
    }

    /**
     * Data set up for MoveFileInsideEmptyFolderInClient
     */

    private void setupMoveFileInsideEmptyFolderInClient()
    {
        emptyFileName = getRandomFile("movefileemptyclient", "txt");
        emptyFolderName = getRandomFolderIn(getLocalSiteLocation(), "moveemptyfolderclient");
        explorer.rightClickCreate(getLocalSiteLocation().getName(), emptyFolderName.getName(), type.FOLDER);
        explorer.rightClickCreate(getLocalSiteLocation().getName(), emptyFileName.getName(), type.TEXTFILE);
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
    @AlfrescoTest(testlink = "ALF-2588")
    @Test
    public void moveFileInsideEmptyFolderInClient()
    {
        try
        {
            explorer.openApplication();
            explorer.openFolder(getLocalSiteLocation());
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, emptyFileName.getName()), "File got sycned to the client");
            explorer.activateApplicationWindow(getLocalSiteLocation().getName());
            explorer.moveFolder(emptyFileName, emptyFolderName);
            explorer.closeExplorer();
            syncWaitTime(CLIENTSYNCTIME);
            share.navigateToDocuemntLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertFalse(share.isFileVisible(drone, emptyFileName.getName()), "file is not visible in doc lib");
            share.navigateToFolder(drone, emptyFolderName.getName());
            Assert.assertTrue(share.isFileVisible(drone, emptyFileName.getName()), "File is moved inside the empty folder");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("test case failed - setupMoveFileInsideEmptyFolderInClient ", e);
        }
        finally
        {
            shareLogin.logout(drone);
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
    @AlfrescoTest(testlink = "ALF-2590")
    @Test
    public void moveFileInsideFolderInClient()
    {
        File fileName = getRandomFile("movefileclient", "txt");
        File folderNameToMove = getRandomFolderIn(getLocalSiteLocation(), "foldertomoveclient");
        try
        {
            explorer.openApplication();
            explorer.openFolder(getLocalSiteLocation());
            explorer.createNewFolderMenu(folderNameToMove.getName());
            explorer.rightClickCreate(getLocalSiteLocation().getName(), fileName.getName(), type.TEXTFILE);
            explorer.moveFolder(fileName, folderNameToMove);
            explorer.closeExplorer();
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertFalse(share.isFileVisible(drone, fileName.getName()), "The file is not present in doc lib as it is moved");
            share.navigateToFolder(drone, folderNameToMove.getName());
            Assert.assertTrue(share.isFileVisible(drone, fileName.getName()), "File is moved correctly inside the folder");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("test case failed - moveFileInsideFolderInClient", e);
        }
        finally
        {
            shareLogin.logout(drone);
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
    @AlfrescoTest(testlink = "ALF-2610")
    @Test
    public void moveFileOutOfSubInClient()
    {
        File fileToMove = getRandomFileIn(getLocalSiteLocation(), "filetomove", "txt");
        try
        {
            explorer.openApplication();
            explorer.openFolder(getLocalSiteLocation());
            explorer.rightClickCreate(getLocalSiteLocation().getName(), fileToMove.getName(), type.TEXTFILE);
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, fileToMove.getName()), "the file created is visible in share");
            explorer.activateApplicationWindow(getLocalSiteLocation().getName());
            explorer.moveFolder(fileToMove, downloadLocation);
            explorer.closeExplorer();
            syncWaitTime(CLIENTSYNCTIME);
            share.navigateToDocuemntLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertFalse(share.isFileVisible(drone, fileToMove.getName()), "The file is moved out successfully");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("test case failed - moveFileOutOfSubInClient ", e);
        }
        finally
        {
            shareLogin.logout(drone);
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
        try
        {
            explorer.openApplication();
            logger.info("download location " + downloadLocation);
            explorer.openFolder(downloadLocation);
            explorer.moveFolder(moveFileOutOFSub, getLocalSiteLocation());
            explorer.closeExplorer();
            Assert.assertTrue(LdtpUtils.isFilePresent(getLocalSiteLocation().getAbsolutePath() + File.separator + moveFileOutOFSub.getName()),
                    "move was successful in client");
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, moveFileOutOFSub.getName()), "move intp of subscription was successful");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("test case failed - moveFileIntoSubClient ", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }
}
