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

import org.alfresco.os.win.desktopsync.SyncSystemMenu;
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.site.document.ContentType;
import org.alfresco.sync.DesktopSyncTest;
import org.alfresco.test.AlfrescoTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.TestException;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to update of file in both client (Windows machine) and share
 * 
 * @author Sprasanna
 * @author Paul Brodner
 */
public class UpdateContentSyncTest extends DesktopSyncTest
{
    Notepad notepad = new Notepad();
    WindowsExplorer explorer = new WindowsExplorer();
    SyncSystemMenu contenxtMenu = new SyncSystemMenu();

    /**
     * Test to update a file in client which is already synced
     * Step1 - Open notepad application
     * Step2 - Save the notepad to synclocation
     * Step3 - Add a new line of text "first create in client" to the notepad
     * Step4 - DO a ctrl S and save the file
     * Step5 - Wait for the sync time of 2 mins in case of client
     * Step6 - login to share
     * Step7 - Open site DocumentLibrary
     * Step8 - Validate whether the newly created File is Visible
     * Step9 - Check the version number is also set to 1.1
     * Step10 - Now access the client created notepad file and append the new line of text
     * "adding another line of text"
     * Step11 - Save the file to sync location using Ctrl S
     * Step12 - Wait for the sync time of 2 mins in case of client
     * Step13 - In share validate the version number is increased correctly
     * Step14 - In share Download the file
     * Step15 - Compare the two files between client and Share to see whether they are same
     * Step16 - Now Close the Notepad without any edit
     * Step17 - Wait for the sync time of 2 mins in case of client
     * Step18 - Validate whether the file is same and has the same version number
     */
    @AlfrescoTest(testlink = "ALF-2575")
    @Test
    public void updateFileInClient()
    {
        File synchedFile = getRandomFileIn(getLocalSiteLocation(), "updateSyncFile", "txt");
        File downloadFile = new File(downloadPath, synchedFile.getName());
        try
        {
            notepad.openApplication();
            notepad.saveAs(synchedFile);
            notepad.edit("first create in client");
            notepad.save();

            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertEquals(share.getDocLibVersionInfo(drone, synchedFile.getName()), "1.1", "Appropriate version found on synched file in share");
            notepad.appendData("adding another line of text");
            notepad.save();

            syncWaitTime(CLIENTSYNCTIME);
            share.navigateToDocuemntLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertEquals(share.getDocLibVersionInfo(drone, synchedFile.getName()), "1.2", "Appropriate version found on synched file in share");
            share.shareDownloadFileFromDocLib(drone, synchedFile.getName(), downloadFile.getPath());
            Assert.assertTrue(compareTwoFiles(synchedFile.getPath(), downloadFile.getPath()));
            notepad.close(synchedFile);
            syncWaitTime(CLIENTSYNCTIME);
            downloadFile.delete();
            Assert.assertEquals(share.getDocLibVersionInfo(drone, synchedFile.getName()), "1.2", "Appropriate version found on synched file in share");
            share.shareDownloadFileFromDocLib(drone, synchedFile.getName(), downloadFile.getPath());
            Assert.assertTrue(compareTwoFiles(synchedFile.getPath(), downloadFile.getPath()));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed-updateFileInClient ", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * Test to check whether the share new version are getting synced correctly
     * Step1 - login into Share
     * Step2 - Open site Document Library
     * Step3 - Create a content using create plain text menu
     * Step4 - Wait for the file to synced to client - which is 5 mins in case of Share
     * Step5 - Validate in client whether the file is visible
     * Step6 - Now in share upload a new version with the content as test sync update
     * Download a copy of the share file location c:\DownloadAlfresco
     * Step7 - Wait for the file to synced to client - which is 5 mins in case of Share
     * Step8 - Compare the two files to see whether the same
     */
    @AlfrescoTest(testlink = "ALF-2577")
    @Test
    public void updateFileInShare()
    {
        File fileTestUpdate = getRandomFileIn(getLocalSiteLocation(), "updateFile", "txt");
        File fileToUload = getRandomFileIn(getLocalSiteLocation(), "uploadFile", "txt");
        File downloadFile = new File(downloadPath, fileTestUpdate.getName());
        ContentDetails content = new ContentDetails();
        content.setName(fileTestUpdate.getName());
        content.setDescription(fileTestUpdate.getName());
        content.setTitle(fileTestUpdate.getName());
        content.setContent("share created file");

        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.createContent(drone, content, ContentType.PLAINTEXT);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(fileTestUpdate.exists(), "Share new version file is synched on client.");

            share.uploadNewVersionOfDocument(drone, fileTestUpdate.getName(), fileToUload.getName(), "test sync update");
            share.shareDownloadFileFromDocLib(drone, fileTestUpdate.getName(), downloadFile.getPath());
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(compareTwoFiles(fileTestUpdate.getPath(), downloadFile.getPath()));
        }
        catch (Throwable e)
        {
            throw new SkipException("test case failed -updateFileInShare ", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * Test case to edit a property and change the title and description of the file and validate
     * in the client the file is with the same name and content is same
     * Step 1 - login in share
     * Step 2 - open site Document library and navigate to folder
     * Step 3 - upload a new file
     * Step 4 - Wait for it sync - Server sync time
     * Step 5 - In the client validate whether the file is present
     * Step 6 - Edit the properties and change the description and title
     * Step 7 - Wait for sync time - server sync tim
     * Step 8 - Validate whether the file are same
     */

    public void setupEditProperties()
    {

    }

    public void editPropertiesInShare()
    {
        File editPropertyFile = getRandomFileIn(getLocalSiteLocation(), "shareFileToEdit", "txt");
        File downloadFile = new File(downloadPath, editPropertyFile.getName());
        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.newFile(editPropertyFile.getName(), "share created first the file");
            share.uploadFile(drone, editPropertyFile);
            // hit sync now to sync immediately
            contenxtMenu.syncNow();
            // syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(editPropertyFile.exists(), "Share first version of the file exist");
            share.editProperties(drone, editPropertyFile.getName(), null, "edit properties", "edit properties change");
            share.shareDownloadFileFromDocLib(drone, editPropertyFile.getName(), downloadFile.getPath());
            // syncWaitTime(SERVERSYNCTIME);
            // hit sync now to sync immediately
            contenxtMenu.syncNow();
            Assert.assertTrue(compareTwoFiles(editPropertyFile.getPath(), downloadFile.getPath()));
        }
        catch (Throwable e)
        {
            throw new TestException("test case failed -updateFileInShare ", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }

    }
}
