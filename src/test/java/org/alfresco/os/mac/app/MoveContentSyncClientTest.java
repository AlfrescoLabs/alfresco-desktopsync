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

    File _clFolder1;
    File _clFile1;
    File _clFolderMove1;
    File _clFolder2;
    File _clFile2;
    File _clFile3;
    File _clFolderMove2;

    File _clFolder4;
    File _clFile4;
    File _clFolderMove4;

    File _clFile5;

    @BeforeClass
    public void setupData() throws Exception
    {
        // inClientMoveFileFromFolderInOtherEmptyFolder
        _clFolder1 = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clFolder"), explorer);
        _clFile1 = addDataInClient(getRandomFileIn(_clFolder1, "clFile", "rtf"), notepad);
        _clFolderMove1 = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clMoveFolder"), explorer);

        // inClientMoveFileFromFolderInOtherNonEmptyFolder
        _clFolder2 = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clFolder"), explorer);
        _clFile2 = addDataInClient(getRandomFileIn(_clFolder2, "clFile", "rtf"), notepad);
        _clFolderMove2 = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clMoveFolder"), explorer);
        _clFile3 = addDataInClient(getRandomFileIn(_clFolderMove2, "clFile", "rtf"), notepad);

        // inClientMoveFolderWithFileInOtherEmptyFolder
        _clFolder4 = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clFolder"), explorer);
        _clFile4 = addDataInClient(getRandomFileIn(_clFolder4, "clFile", "rtf"), notepad);
        _clFolderMove4 = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clMoveFolder"), explorer);

        // inClientMoveFileOutsideSubscription
        _clFile5 = addDataInClient(getRandomFileIn(getLocalSiteLocationClean(), "clFile", "rtf"), notepad);

        runDataCreationProcess();
        syncWaitTime(CLIENTSYNCTIME);
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
    @Test
    public void inClientMoveFileFromFolderInOtherEmptyFolder() throws Exception
    {
        explorer.openApplication();
        explorer.moveFile(_clFile1, _clFolderMove1);
        explorer.closeExplorer();
        syncWaitTime(CLIENTSYNCTIME);

        share.openSitesDocumentLibrary(drone, siteName);

        Assert.assertTrue(share.isFileVisible(drone, _clFolderMove1.getName()),
                String.format("Folder {%s} exists in Share, prior to tests", _clFolderMove1.getName()));
        share.navigateToFolder(drone, _clFolderMove1.getName());
        Assert.assertTrue(share.isFileVisible(drone, _clFile1.getName()), String.format("File{%s} from moved folder, was found in Share", _clFile1.getName()));

        share.navigateToDocuemntLibrary(drone, siteName);
        share.navigateToFolder(drone, _clFolder1.getName());
        Assert.assertFalse(share.isFileVisible(drone, _clFile1.getName()),
                String.format("File {%s} moved in client, does not exists in original folder {%s}", _clFile1.getName(), _clFolder1.getName()));
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
    @Test
    public void inClientMoveFileFromFolderInOtherNonEmptyFolder() throws Exception
    {
        explorer.openApplication();
        explorer.moveFolder(_clFile2, _clFolderMove2);
        explorer.closeExplorer();
        syncWaitTime(CLIENTSYNCTIME);
        share.openSitesDocumentLibrary(drone, siteName);

        Assert.assertTrue(share.isFileVisible(drone, _clFolderMove2.getName()),
                String.format("Folder {%s} moved exists in Share prior to move operation.", _clFolderMove2.getName()));

        share.navigateToFolder(drone, _clFolderMove2.getName());
        Assert.assertTrue(share.isFileVisible(drone, _clFile2.getName()),
                String.format("File {%s} moved in Client exists in new destination: {%s}.", _clFile2.getName(), _clFolderMove2.getName()));
        Assert.assertTrue(share.isFileVisible(drone, _clFile3.getName()),
                String.format("Existing file {%s} still exists in destination: {%s}.", _clFile3.getName(), _clFolderMove2.getName()));
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
    @Test
    public void inClientMoveFolderWithFileInOtherEmptyFolder() throws Exception
    {
        explorer.openApplication();
        explorer.moveFolder(_clFolder4, _clFolderMove4);
        explorer.closeExplorer();
        syncWaitTime(CLIENTSYNCTIME);

        share.openSitesDocumentLibrary(drone, siteName);
        
        Assert.assertTrue(share.isFileVisible(drone, _clFolderMove4.getName()),
                String.format("Destination folder {%s} exists in Share.", _clFolderMove4.getName()));
        Assert.assertFalse(share.isFileVisible(drone, _clFolder4.getName()),
                String.format("Folder {%s} moved from root, is not visible in share.", _clFolder4.getName()));

        share.navigateToFolder(drone, _clFolderMove4.getName());
        Assert.assertTrue(share.isFileVisible(drone, _clFolder4.getName()),
                String.format("Moved folder {%s} exists now in source destination {%s}.", _clFolder4.getName(), _clFolderMove4.getName()));
        share.navigateToFolder(drone, _clFolder4.getName());
        Assert.assertTrue(share.isFileVisible(drone, _clFile4.getName()),
                String.format("File {%s} was also moved in the process and exists in Share", _clFile4.getName()));
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
    @Test
    public void inClientMoveFileOutsideSubscription()
    {
        share.openSitesDocumentLibrary(drone, siteName);
        Assert.assertTrue(share.isFileVisible(drone, _clFile5.getName()),
                String.format("File {%s} exists in Share prior to move operation .", _clFile5.getName()));

        explorer.openApplication();
        explorer.moveFile(_clFile5, new File(downloadPath));
        syncWaitTime(CLIENTSYNCTIME);
        share.openSitesDocumentLibrary(drone, siteName);
        Assert.assertFalse(share.isFileVisible(drone, _clFile5.getName()),
                String.format("File {%s} removed from Share after it was moved from client.", _clFile5.getName()));
    }
}
