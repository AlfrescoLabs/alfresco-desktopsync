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
import org.alfresco.test.AlfrescoTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to move of files in client (MAC OS machine)
 * 
 * @author Paul Brodner
 */
public class MoveContentSyncClientTest extends DesktopSyncMacTest
{
    TextEdit notepad = new TextEdit();
    FinderExplorer explorer = new FinderExplorer();

    @BeforeClass
    public void start() throws Exception
    {
        shareLogin.loginToShare(drone, userInfo, shareUrl);
    }

    @AfterClass
    public void cleanUp()
    {
        shareLogin.logout(drone);
    }

    /**
     * Step1 - in client subscription: create one folder (A) with a file (f) and another empty folder (B)
     * Step2 - verify that this structure is created on client prior to tests
     * Step3 - wait for the client sync
     * Step4 - login to Share and check that this files are created
     * Step5 - move file (f) to folder (B)
     * Step6 - wait for the client sync
     * Step7 - login to Share and check that this file (f) exists now ONLY in folder (B)
     * ALF-2588: Move content in client with in empty folder
     * 
     * @throws Exception
     */
    @AlfrescoTest(testlink = "ALF-2588")
    @Test(groups = { "MacOnly"})
    public void inClientMoveFileFromFolderInOtherEmptyFolder() throws Exception
    {
        // inClientMoveFileFromFolderInOtherEmptyFolder
        File clFolderWithFile   = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clFolder"), explorer);
        File clFileFromFolder   = addDataInClient(getRandomFileIn(clFolderWithFile, "clFile", "rtf"), notepad);
        File clFolderMove       = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clMoveFolder"), explorer);
        runDataCreationInClient();
        // end prepare data

        explorer.openApplication();
        explorer.moveFile(clFileFromFolder, clFolderMove);
        explorer.closeExplorer();
        syncWaitTime(CLIENTSYNCTIME);

        share.navigateToDocuemntLibrary(drone, siteName);

        Assert.assertTrue(share.isFileVisible(drone, clFolderMove.getName()),
                String.format("Folder {%s} exists in Share, prior to tests", clFolderMove.getName()));
        share.navigateToFolder(drone, clFolderMove.getName());
        Assert.assertTrue(share.isFileVisible(drone, clFileFromFolder.getName()),
                String.format("File{%s} from moved folder, was found in Share", clFileFromFolder.getName()));

        share.navigateToDocuemntLibrary(drone, siteName);
        share.navigateToFolder(drone, clFolderWithFile.getName());
        Assert.assertFalse(share.isFileVisible(drone, clFileFromFolder.getName()),
                String.format("File {%s} moved in client, does not exists in original folder {%s}", clFileFromFolder.getName(), clFolderWithFile.getName()));
    }

    /**
     * Prerequisites:
     * In Client create folder A with file B and folder C with file D
     * Sync content and validate if this structure exists in Share
     * Test:
     * Move file B in Folder C
     * Wait for the client sync and validate in Share that:
     * File B does not exists in Folder A
     * File B exists in Folder C
     * File D exist in Folder C
     * 
     * @throws Exception
     *             ALF-2589 Move content in client not an empty folder
     */
    @AlfrescoTest(testlink = "ALF-2589")
    @Test(groups = { "MacOnly"})
    public void inClientMoveFileFromFolderInOtherNonEmptyFolder() throws Exception
    {
        // inClientMoveFileFromFolderInOtherNonEmptyFolder
        File clFolderWithFile           = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clFolder"), explorer);
        File clFileInFolder             = addDataInClient(getRandomFileIn(clFolderWithFile, "clFile", "rtf"), notepad);
        File clFolderMoveDestination    = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clMoveFolder"), explorer);
        File clFileInMoveFolder         = addDataInClient(getRandomFileIn(clFolderMoveDestination, "clFile", "rtf"), notepad);
        runDataCreationInClient();
        // end prepare data

        explorer.openApplication();
        explorer.moveFile(clFileInFolder, clFolderMoveDestination);
        explorer.closeExplorer();
        syncWaitTime(CLIENTSYNCTIME);
        share.navigateToDocuemntLibrary(drone, siteName);

        Assert.assertTrue(share.isFileVisible(drone, clFolderMoveDestination.getName()),
                String.format("Folder {%s} moved exists in Share prior to move operation.", clFolderMoveDestination.getName()));

        share.navigateToFolder(drone, clFolderMoveDestination.getName());

        Assert.assertTrue(share.isFileVisible(drone, clFileInFolder.getName()),
                String.format("File {%s} moved in Client exists in new destination: {%s}.", clFileInFolder.getName(), clFolderMoveDestination.getName()));
        Assert.assertTrue(share.isFileVisible(drone, clFileInMoveFolder.getName()),
                String.format("Existing file {%s} still exists in destination: {%s}.", clFileInMoveFolder.getName(), clFolderMoveDestination.getName()));
    }

    /**
     * Prerequisites:
     * In client create folder A with file B and empty folder C
     * Wait for share and validate existence of this structure in share, prior to tests
     * Test:
     * move folder A in folder C
     * wait for sync and check that folder A with file B exists in folde C in Share
     * ALF-2590: move content in client with some files
     * 
     * @throws Exception
     */
    @AlfrescoTest(testlink = "ALF-2590")
    @Test(groups = { "MacOnly"})
    public void inClientMoveFolderWithFileInOtherEmptyFolder() throws Exception
    {
        // inClientMoveFolderWithFileInOtherEmptyFolder
        File clFolderWithFile   = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clFolder"), explorer);
        File clFileInFolder     = addDataInClient(getRandomFileIn(clFolderWithFile, "clFile", "rtf"), notepad);
        File clFolderMoved      = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clMoveFolder"), explorer);
        runDataCreationInClient();
        // //end prepare data

        explorer.openApplication();
        explorer.moveFolder(clFolderWithFile, clFolderMoved);
        explorer.closeExplorer();
        syncWaitTime(CLIENTSYNCTIME);

        share.navigateToDocuemntLibrary(drone, siteName);

        Assert.assertTrue(share.isFileVisible(drone, clFolderMoved.getName()),
                String.format("Destination folder {%s} exists in Share.", clFolderMoved.getName()));
        Assert.assertFalse(share.isFileVisible(drone, clFolderWithFile.getName()),
                String.format("Folder {%s} moved from root, is not visible in share.", clFolderWithFile.getName()));

        share.navigateToFolder(drone, clFolderMoved.getName());
        Assert.assertTrue(share.isFileVisible(drone, clFolderWithFile.getName()),
                String.format("Moved folder {%s} exists now in source destination {%s}.", clFolderWithFile.getName(), clFolderMoved.getName()));
        share.navigateToFolder(drone, clFolderWithFile.getName());
        Assert.assertTrue(share.isFileVisible(drone, clFileInFolder.getName()),
                String.format("File {%s} was also moved in the process and exists in Share", clFileInFolder.getName()));
    }

    /**
     * Prerequisites:
     * in client subscription: create one file (a)
     * check the file exist in share prior to tests
     * Test:
     * in client move the file out of subscription (i.e. downloadFolder)
     * wait for sync and check that this file is not found in Share after refresh
     * ALF-2610: Move out of subscription
     */
    @AlfrescoTest(testlink = "ALF-2610")
    @Test(groups = { "MacOnly"})
    public void inClientMoveFileOutsideSubscription()
    {
        // inClientMoveFileOutsideSubscription
        File clFileMoved = addDataInClient(getRandomFileIn(getLocalSiteLocationClean(), "clFile", "rtf"), notepad);
        runDataCreationInClient();

        share.navigateToDocuemntLibrary(drone, siteName);
        share.refreshSharePage(drone);
        
        Assert.assertTrue(share.isFileVisible(drone, clFileMoved.getName()),
                String.format("File {%s} exists in Share prior to move operation .", clFileMoved.getName()));

        explorer.openApplication();
        explorer.moveFile(clFileMoved, new File(downloadPath));
        syncWaitTime(CLIENTSYNCTIME);
        share.navigateToDocuemntLibrary(drone, siteName);
        Assert.assertFalse(share.isFileVisible(drone, clFileMoved.getName()),
                String.format("File {%s} removed from Share after it was moved from client.", clFileMoved.getName()));
    }
}
