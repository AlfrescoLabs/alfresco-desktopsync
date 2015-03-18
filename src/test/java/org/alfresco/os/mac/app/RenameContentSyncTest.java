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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to Rename of file in both
 * client (MAC OS machine) and share
 * 
 * @author Paul Brodner
 */
public class RenameContentSyncTest extends DesktopSyncMacTest
{
    TextEdit notepad = new TextEdit();
    FinderExplorer explorer = new FinderExplorer();

    File _clFolder;
    File _clFolderFile;
    File _clFile;
    File _shFile;
    File _shFolderWFile;
    File _shFileInFolder;

    /**
     * Prepare the files and folders in Client and Share prior to the tests
     * We use this approach in order to gain more time and just sync once the files.
     */
    @BeforeClass
    public void prepareData() throws Exception
    {
        _clFolder       = addDataInClient(getRandomFolderIn(getLocalSiteLocationClean(), "clFolder"), explorer);
        _clFolderFile   = addDataInClient(getRandomFileIn(_clFolder, "clFolderFile", "rtf"), notepad);
        _clFile         = addDataInClient(getRandomFileIn(getLocalSiteLocationClean(), "file", "rtf"), notepad);
        _shFile         = addDataInShare(getRandomFileIn(getLocalSiteLocationClean(), "shFile", "rtf"), TEST_DATA.FILE);

        _shFolderWFile  = addDataInShare(getRandomFolderIn(getLocalSiteLocationClean(), "shFolderWFile"), TEST_DATA.FOLDER);
        _shFileInFolder = addDataInShare(getRandomFileIn(_shFolderWFile, "shFileInFolder", "rtf"), TEST_DATA.FILE);

        runDataCreationProcess();
    }

    /**
     * Rename of file in client
     * Step1 - create a file in client sync location
     * Step2 - Wait for the sync time -Client sync time
     * Step3 - Validate in share whether the file is available correctly
     * Step4 - In client rename the file using right click rename and enter the new name
     * Step5 - wait for the sync time - Client sync time
     * Step6 - Validate the file name is
     * changed in share
     */
    @Test
    public void renameFileInClient()
    {
        String fileRenamed = "fileRenamed.rtf";
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);

            Assert.assertTrue(share.isFileVisible(drone, _clFile.getName()),
                    String.format("File {%s} was synced successfuly to Share, before the rename operation", _clFile.getName()));

            explorer.openApplication();
            explorer.renameFile(_clFile, fileRenamed);
            explorer.closeExplorer();

            syncWaitTime(CLIENTSYNCTIME);
            share.refreshSharePage(drone);
            Assert.assertTrue(share.isFileVisible(drone, fileRenamed), String.format("File {%s} is synched and visible in Share.", fileRenamed));
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
    public void renameFolderWithFileInClient()
    {
        String folderRename = getRandomValue("clFolderRenamed");
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, _clFolder.getName()),
                    String.format("Client Folder {%s} was synched in Share, prior to rename operation.", _clFolder.getName()));

            explorer.openApplication();
            explorer.focus();
            explorer.renameFolder(_clFolder, folderRename);
            explorer.closeExplorer();

            syncWaitTime(CLIENTSYNCTIME);
            share.refreshSharePage(drone);
            Assert.assertTrue((share.isFileVisible(drone, folderRename)), String.format("Renamed folder {%s} was synched in Share", folderRename));
            share.navigateToFolder(drone, folderRename);

            Assert.assertTrue((share.isFileVisible(drone, _clFolderFile.getName())),
                    String.format("Original file {%s} exists in renamed folder in Share", _clFolderFile.getName()));
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
     * Rename of file in share
     * Step1 - Login into share
     * Step2 - Upload the file in site document library
     * Step3 - Wait for the sync time - Server sync time
     * Step4 - Check in the client sync location the file is visible.
     * Step6- In share rename the file
     * Step7 - Wait for the sync time - server sync time
     * Step8 - In client sync location the file is renamed correctly
     */
    @Test
    public void renameFileInShare()
    {
        File renamedFile = getRandomFileIn(getLocalSiteLocationClean(), "shFileRenamed", "rtf");
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);

            Assert.assertTrue(share.isFileVisible(drone, _shFile.getName()),
                    String.format("File {%s} exists in Share, prior to rename operation.", _shFile.getName()));
            share.editContentNameInline(drone, _shFile.getName(), renamedFile.getName(), true);

            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(renamedFile.exists(), "Renamed file in Share is now synched in Client");
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
     * Rename a folder with File in share
     */
    @Test
    public void renameFolderWithFileInShare()
    {
        File renamedFolderWFile = getRandomFolderIn(getLocalSiteLocationClean(), "rnmFolderWFile");
        File fileInClient = new File(renamedFolderWFile, _shFileInFolder.getName());
        try
        {
            Assert.assertTrue(_shFolderWFile.exists(),
                    String.format("Folder {%s} created in Share was synched to Client, prior to rename operation.", _shFolderWFile.getPath()));
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.editContentNameInline(drone, _shFolderWFile.getName(), renamedFolderWFile.getName(), true);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(_shFolderWFile.exists(),
                    String.format("Folder {%s} renamed in Share does not exists in Client after auto-sync.", _shFolderWFile.getPath()));
            Assert.assertTrue(renamedFolderWFile.exists(),
                    String.format("Folder {%s} renamed in Share exists in  Client after auto-sync.", renamedFolderWFile.getPath()));

            Assert.assertTrue(fileInClient.exists(),
                    String.format("File {%s} from renamed folder in Share, exists in  Client after auto-sync.", fileInClient.getPath()));
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
