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
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to move of files in client (MAC OS machine)
 * 
 * @author Paul Brodner
 */
public class MoveContentSyncShareTest extends DesktopSyncMacTest
{
    private static final Logger logger = Logger.getLogger(MoveContentSyncShareTest.class);
    TextEdit notepad = new TextEdit();
    FinderExplorer explorer = new FinderExplorer();

    File _shSiteOutOfSubscription;
    File _shFolderOutOfSync;

    
    @BeforeClass
    public void start() throws Exception
    {
        _shSiteOutOfSubscription        = addDataInShare(new File("siteOut"), TEST_DATA.SITE);
        _shFolderOutOfSync              = addDataInShare(getRandomFolderIn(getLocalSiteLocationClean(), "shFolderOutOfSync"), TEST_DATA.FOLDER);
        runDataCreationInShare(false);

        shareLogin.loginToShare(drone, userInfo, shareUrl);
        // site out of subscription
        share.navigateToDocuemntLibrary(drone, _shSiteOutOfSubscription.getName());
        share.createFolder(drone, _shFolderOutOfSync.getName(), _shFolderOutOfSync.getName(), _shFolderOutOfSync.getName());
        
        share.navigateToDocuemntLibrary(drone, siteName);
        syncWaitTime(SERVERSYNCTIME);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown()
    {
        shareLogin.logout(drone);
    }

    /**
     * Prerequisite:
     * In Share create folder (a) with file (b) and another empty folder (c)
     * Wait for the server sync and validate the existence of this directories/files in client
     * Test:
     * In Share move file (b) to folder (c)
     * Wait for server sync
     * In Client verify that file (b) exists in folder (c) and it doesn't also exists in folder (a)
     * ALF-2609 Move content in Share with some files
     * BUG: folder is not synched in client
     * 
     * @throws Exception
     */
    @Test(groups = { "MacOnly"})
    public void inShareMoveFileFromFolderInOtherEmptyFolder() throws Exception
    {
        File _shFolderWithFile1 = addDataInShare(getRandomFolderIn(getLocalSiteLocationClean(), "shFolder1"), TEST_DATA.FOLDER);
        File _shFileInFolder1 = addDataInShare(getRandomFileIn(_shFolderWithFile1, "file1", "rtf"), TEST_DATA.FILE);
        File _shEmptyFolderMove1 = addDataInShare(getRandomFolderIn(getLocalSiteLocationClean(), "shFolderM1"), TEST_DATA.FOLDER);
        runDataCreationInShare();
        //end data creation
        
        share.navigateToDocuemntLibrary(drone, siteName);
        Assert.assertTrue(share.isFileVisible(drone, _shFolderWithFile1.getName()),
                String.format("Folder source {%s} was found in share prior to test", _shFolderWithFile1.getName()));
        Assert.assertTrue(share.isFileVisible(drone, _shEmptyFolderMove1.getName()),
                String.format("Folder destination {%s} was found in share prior to test", _shEmptyFolderMove1.getName()));
        share.navigateToFolder(drone, _shFolderWithFile1.getName());
        Assert.assertTrue(share.isFileVisible(drone, _shFileInFolder1.getName()),
                String.format("File source {%s} was found in share folder {%s} prior to test", _shFileInFolder1.getName(), _shFolderWithFile1.getName()));

        share.copyOrMoveArtifact(drone, "All Sites", siteName, _shEmptyFolderMove1.getName(), _shFileInFolder1.getName(), "Move");
        syncWaitTime(SERVERSYNCTIME);

        File movedFile = new File(_shEmptyFolderMove1.getPath(), _shFileInFolder1.getName());
        Assert.assertTrue(movedFile.exists(), String.format("File moved in share was synched on Client {%s}", movedFile.getPath()));
    }

    /**
     * Prerequisites:
     * In share folder (a) with file (b) and empty folder (c) are created and synched to client
     * Test:
     * Move folder (a) to folder (c)
     * Wait for the server sync
     * Check in client that folder (c) contains folder (a) with file (b)
     * No TestLink ID
     * @throws Exception 
     */
    @Test(groups = { "MacOnly"})
    public void inShareMoveFolderWithFileInAnotherFolder() throws Exception
    {
        File _shEmptyFolderMove = addDataInShare(getRandomFolderIn(getLocalSiteLocationClean(), "shFolderM1"), TEST_DATA.FOLDER);
        File _shFolderWithFile2 = addDataInShare(getRandomFolderIn(getLocalSiteLocationClean(), "shFolder2"), TEST_DATA.FOLDER);
        File _shFileInFolder2   = addDataInShare(getRandomFileIn(_shFolderWithFile2, "file2", "rtf"), TEST_DATA.FILE);
        runDataCreationInShare();
        //end data creation
        
        share.navigateToDocuemntLibrary(drone, siteName);
        Assert.assertTrue(share.isFileVisible(drone, _shFolderWithFile2.getName()),
                String.format("Folder source {%s} was found in share prior to test", _shFolderWithFile2.getName()));
        Assert.assertTrue(share.isFileVisible(drone, _shEmptyFolderMove.getName()),
                String.format("Empty folder {%s} was found in share prior to test", _shEmptyFolderMove.getName()));
        logger.info(String.format("Moving folder with file {%s} into empty folder {%s}", _shEmptyFolderMove.getName(), _shFolderWithFile2.getName()));
        share.copyOrMoveArtifact(drone, "All Sites", siteName, _shEmptyFolderMove.getName(), _shFolderWithFile2.getName(), "Move");

        syncWaitTime(SERVERSYNCTIME);

        File movedFolder = new File(_shEmptyFolderMove.getPath(), _shFolderWithFile2.getName());
        File movedFileWFolder = new File(movedFolder.getPath(), _shFileInFolder2.getName());
        Assert.assertTrue(movedFolder.exists(), String.format("Folder moved in share was synched on Client {%s}", movedFolder.getPath()));
        Assert.assertTrue(movedFileWFolder.exists(), String.format("File moved with folder in share was synched on Client {%s}", movedFileWFolder.getPath()));
    }

    /**
     * Prerequisites:
     * ALF-3286 Move folder with file into Subscription in Share
     */
    @Test(groups = { "MacOnly"})
    public void inShareMoveFolderWithFileIntoSubscription()
    {
        share.navigateToDocuemntLibrary(drone, _shSiteOutOfSubscription.getName());
        logger.info(String.format("Moving folder with file {%s} into root document library", _shFolderOutOfSync.getName()));
        share.copyOrMoveArtifact(drone, "All Sites", siteName, null, _shFolderOutOfSync.getName(), "Move");
        syncWaitTime(SERVERSYNCTIME);

        File movedFolder = new File(getLocalSiteLocationClean(), _shFolderOutOfSync.getName());
        Assert.assertTrue(movedFolder.exists(),
                String.format("File moved from external Site into subscription was synched in client {%s}", movedFolder.getPath()));
    }

    /**
     * Prerequisites:
     * In Share create folder (a) with file (b) and another empty folder (c)
     * Wait for the server sync and validate the existence of this directories/files in client
     * Test:
     * In Share move file (b) to folder (c)
     * Wait for server sync
     * In Client verify that file (b) exists in folder (c) and it doesn't also exists in folder (a)
     * ALF-2611: Move out of subscription in Share
     * bug: first folder _shFileInSubscription4 is now synched in client prior to test
     * @throws Exception 
     */
    @Test(groups = { "MacOnly"})
    public void inShareMoveFileOutsideSubscription() throws Exception
    {
        File _shFileInSubscription = addDataInShare(getRandomFileIn(getLocalSiteLocationClean(), "fileInShare", "rtf"), TEST_DATA.FILE);
        runDataCreationInShare();
        //end data creation
        
        share.navigateToDocuemntLibrary(drone, siteName);
        Assert.assertTrue(share.isFileVisible(drone, _shFileInSubscription.getName()),
                String.format("File exists in share {%s} prior to test", _shFileInSubscription.getName()));
        Assert.assertTrue(_shFileInSubscription.exists(), String.format("File is synched in Client {%s} prior to test", _shFileInSubscription.getName()));

        logger.info(String.format("Moving file {%s} outside subscription, in site: {%s}", _shFileInSubscription.getName(), _shSiteOutOfSubscription.getName()));
        
        share.copyOrMoveArtifact(drone, "All Sites", _shSiteOutOfSubscription.getName(), null, _shFileInSubscription.getName(), "move");
        share.refreshSharePage(drone);
        Assert.assertFalse(share.isFileVisible(drone, _shFileInSubscription.getName()),
                String.format("File was removed from share {%s}", _shFileInSubscription.getName()));
        syncWaitTime(SERVERSYNCTIME);
        Assert.assertFalse(_shFileInSubscription.exists(),
                String.format("File was removed in Client {%s} after move operation", _shFileInSubscription.getName()));
    }

    /**
     * Prerequisites:
     * AONE-3285 Move folder with file outside Subscription in Share
     * @throws Exception 
     */
    @Test(groups = { "MacOnly"})
    public void inShareMoveFolderWithFileOutsideSubscription() throws Exception
    {
        File _shFolderInSubscription = addDataInShare(getRandomFolderIn(getLocalSiteLocationClean(), "shFoldeInSync"), TEST_DATA.FOLDER);
        addDataInShare(getRandomFileIn(_shFolderInSubscription, "fileInSub", "rtf"), TEST_DATA.FILE);
        runDataCreationInShare();
        //end data creation
        
        share.navigateToDocuemntLibrary(drone, siteName);
        Assert.assertTrue(share.isFileVisible(drone, _shFolderInSubscription.getName()),
                String.format("Folder exists in Share {%s} prior to test", _shFolderInSubscription.getName()));
        Assert.assertTrue(_shFolderInSubscription.exists(), String.format("Folder exists in Client {%s} prior to test", _shFolderInSubscription.getName()));
        logger.info(String.format("Moving folder with file {%s} outside subscription in site: {%s}", _shFolderInSubscription.getName(),
                _shSiteOutOfSubscription.getName()));
        share.copyOrMoveArtifact(drone, "All Sites", _shSiteOutOfSubscription.getName(), null, _shFolderInSubscription.getName(), "move");
        syncWaitTime(SERVERSYNCTIME);
        Assert.assertTrue(_shFolderInSubscription.exists(),
                String.format("Folder is removed in Client {%s} after is moved in share out of subscription", _shFolderInSubscription.getName()));
    }
}
