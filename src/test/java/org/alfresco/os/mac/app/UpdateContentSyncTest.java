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
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.site.document.ContentType;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utilities.LdtpUtils;
import org.testng.Assert;
import org.testng.TestException;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to update of file in both client (MAC machine) and share
 * Must be executed using the -Dwebdrone.browser=FireFoxDownloadToDir
 * 
 * @author Paul Brodner
 */
public class UpdateContentSyncTest extends DesktopSyncMacTest
{
    TextEdit notepad = new TextEdit();
    FinderExplorer explorer = new FinderExplorer();

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
    @Test(groups = { "MacOnly"})
    public void updateFileInClient()
    {
        File synchedFile = getRandomFileIn(getLocalSiteLocationClean(), "updateSyncFile", "rtf");
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
            Assert.assertTrue(share.isFileVisible(drone, synchedFile.getName()), "Notepad file " + synchedFile.getName() + " was synched to Share.");
            Assert.assertEquals(share.getDocLibVersionInfo(drone, synchedFile.getName()), "1.0", "Appropriate version found on synched file in share");

            notepad.focus(synchedFile.getName());
            notepad.edit("adding another line of text");
            notepad.save();

            syncWaitTime(CLIENTSYNCTIME);
            share.refreshSharePage(drone);
            Assert.assertEquals(share.getDocLibVersionInfo(drone, synchedFile.getName()), "1.1", "Appropriate version found on synched file in share");
            downloadFile.delete();
            share.shareDownloadFileFromDocLib(drone, synchedFile.getName(), downloadFile.getPath());
            Assert.assertTrue(compareTwoFiles(synchedFile.getPath(), downloadFile.getPath()), 
                    String.format("Synched file {%s} is the same as downloaded file {%s}.",synchedFile.getPath(), downloadFile.getPath()));
            notepad.close(synchedFile);
            syncWaitTime(CLIENTSYNCTIME);
            downloadFile.delete();
            
            Assert.assertEquals(share.getDocLibVersionInfo(drone, synchedFile.getName()), "1.1", "Appropriate version found on synched file in share");
            share.shareDownloadFileFromDocLib(drone, synchedFile.getName(), downloadFile.getPath());
            
            LdtpUtils.waitUntilFileExistsOnDisk(downloadFile);
            Assert.assertTrue(compareTwoFiles(synchedFile.getPath(), downloadFile.getPath()), 
                    String.format("Synched file {%s} is the same as downloaded file {%s}.",synchedFile.getPath(), downloadFile.getPath()));
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
    @Test(groups = { "MacOnly"})
    public void updateFileInShare()
    {
        File fileTestUpdate = getRandomFileIn(getLocalSiteLocationClean(), "updateFile", "rtf");
        File fileDownloaded = new File(downloadPath, fileTestUpdate.getName());
        ContentDetails content = new ContentDetails();
        content.setName(fileTestUpdate.getName());
        content.setDescription(fileTestUpdate.getName());
        content.setTitle(fileTestUpdate.getName());
        content.setContent("share created file");

        try
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createContent(drone, content, ContentType.PLAINTEXT);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(fileTestUpdate.exists(), String.format("Share new version file {%s} is synched on client.", fileTestUpdate.getPath()));

            share.uploadNewVersionOfDocument(drone, fileTestUpdate.getName(), fileTestUpdate.getName(), "test sync update");
            syncWaitTime(SERVERSYNCTIME);
            share.openSitesDocumentLibrary(drone, siteName);
            share.shareDownloadFileFromDocLib(drone, fileTestUpdate.getName(), fileTestUpdate.getPath());
            Assert.assertTrue(compareTwoFiles(fileTestUpdate.getPath(), fileDownloaded.getPath()), "Share and Client files are identical.");
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
