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
import org.alfresco.utilities.LdtpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to update of file in both client (Windows machine) and share
 * 
 * @author Sprasanna
 */
public class UpdateContentSyncTest extends AbstractTest
{
    private static Log logger = LogFactory.getLog(UpdateContentSyncTest.class);
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
    @Test
    public void updateFileInClient()
    {
        logger.info("Test to update a file in client which is already synced");
        String fileName = "clientcreateandupdate" + fileAppend;
        String clientLocation = syncLocation + File.separator + fileName + FILEEXT;
        String shareLocation = shareFilePath + File.separator + fileName + FILEEXT;
        try
        {

            notepad.openNotepadApplication();
            notepad.setNotepadWindow("Notepad");
            notepad.saveAsNotpad(syncLocation, fileName);
            notepad.editNotepad("first create in client", fileName);
            notepad.ctrlSSave();
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertEquals(share.getDocLibVersionInfo(drone, fileName + FILEEXT), "1.1");
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
            notepad.appendTextToNotepad("adding another line of text", fileName);
            notepad.ctrlSSave();
            syncWaitTime(CLIENTSYNCTIME);
            share.navigateToDocuemntLibrary(drone, siteName);
            Assert.assertEquals(share.getDocLibVersionInfo(drone, fileName + FILEEXT), "1.2");
            share.shareDownloadFileFromDocLib(drone, fileName + FILEEXT, shareLocation);
            Assert.assertTrue(compareTwoFiles(clientLocation, shareLocation));
            notepad.closeNotepad(fileName);
            syncWaitTime(CLIENTSYNCTIME);
            Assert.assertEquals(share.getDocLibVersionInfo(drone, fileName + FILEEXT), "1.2");
            share.shareDownloadFileFromDocLib(drone, fileName + FILEEXT, shareLocation);
            Assert.assertTrue(compareTwoFiles(clientLocation, shareLocation));
        }
        catch (Throwable e)
        {
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
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
    @Test
    public void updateFileInShare()
    {
        logger.info("Test to check whether the share new version are getting synced correctly");
        String fileName = "sharecreateandupdate" + fileAppend + FILEEXT;
        ContentDetails content = new ContentDetails();
        content.setName(fileName);
        content.setDescription(fileName);
        content.setTitle(fileName);
        content.setContent("share created file");
        String clientLocation = syncLocation + File.separator + fileName;
        String shareLocation = shareFilePath + File.separator + fileName;
        try
        {
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createContent(drone, content, ContentType.PLAINTEXT);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + fileName));
            share.uploadNewVersionOfDocument(drone, fileName, fileName, "test sync update");
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(compareTwoFiles(clientLocation, shareLocation));
        }
        catch (Throwable e)
        {
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
        }
    }

}
