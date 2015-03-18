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

package org.alfresco.os.mac;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.alfresco.os.common.ApplicationBase;
import org.alfresco.os.mac.app.FinderExplorer;
import org.alfresco.os.mac.app.TextEdit;
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.site.document.ContentType;
import org.alfresco.sync.DesktopSyncTest;

/**
 * Helper class that should be extended by any MacOS tests
 * 
 * @author <a href="mailto:paulbrodner@gmail.com">Paul Brodner</a>
 */
public class DesktopSyncMacTest extends DesktopSyncTest
{
    public enum TEST_DATA
    {
        FILE, FOLDER, CONTENT, SITE
    };

    private LinkedHashMap<File, ApplicationBase> testClientData = new LinkedHashMap<File, ApplicationBase>();
    private LinkedHashMap<File, TEST_DATA> testServerData = new LinkedHashMap<File, TEST_DATA>();
    String info = "*** %s Data Creation Process in %s ***";

    /**
     * Just initialize a LinkedHashMap variable with all files or folders <fileFolder> that
     * you want to create using the <Application> class
     * All data within this array, will be processed by {@link #runDataCreationInClient()}
     * 
     * @param fileFolder
     * @param macApp
     * @return
     */
    protected File addDataInClient(File fileFolder, Application macApp)
    {
        testClientData.put(fileFolder, macApp);
        return fileFolder;
    }

    /**
     * Just initialize an ArrayList of Files or Folders <folderOrFile>
     * that are uploaded or created in the Share.
     * All data within this array, will be processed by {@link #runDataCreationInShare()}
     * 
     * @param folderOrFile
     * @return
     */
    protected File addDataInShare(File folderOrFile, TEST_DATA testData)
    {
        testServerData.put(folderOrFile, testData);
        return folderOrFile;
    }

    /**
     * This Method will create data in Client and also on Share
     * based on the files/folders/applications
     * 
     * @throws Exception
     */
    protected void runDataCreationProcess() throws Exception
    {
        runDataCreationInClient();
        runDataCreationInShare();
    }

    /**
     * Run and Create data in Client, added on {@link #addDataInClient(File, Application)}
     */
    protected void runDataCreationInClient()
    {
        logger.info(String.format(info, "Start", "Client"));
        // iterate over all client data and create the files/folders using the ApplicationBase app
        for (Entry<File, ApplicationBase> clientData : testClientData.entrySet())
        {
            File data = clientData.getKey();
            ApplicationBase appBase = clientData.getValue();

            logger.info(String.format("Preparing Client Data {%s} using {%s} Application.", data.getPath(), appBase.getApplicationName()));

            // creating data using Finder Explorer
            if (appBase instanceof FinderExplorer)
            {
                FinderExplorer app = ((FinderExplorer) appBase);
                app.openApplication();
                app.focus();
                app.createFolder(data);
                app.closeExplorer();
            }
            // creating data using TextEdit
            else if (appBase instanceof TextEdit)
            {
                TextEdit app = (TextEdit) appBase;
                app.openApplication();
                app.save(data);
                app.close(data);
            }
        }

        // wait process
        if (!testClientData.isEmpty() && testServerData.isEmpty())
        {
            // if we have already server data, then we will wait then
            // because SERVERSYNCTIME is greater than CLIENTSYNCTIME
            syncWaitTime(CLIENTSYNCTIME);
        }
        logger.info(String.format(info, "End", "Client"));
    }

    /**
     * With no parameters, this will create all data in Share and waits for the sync to happen
     * 
     * @throws Exception
     */
    protected void runDataCreationInShare() throws Exception
    {
        runDataCreationInShare(true);
    }

    /**
     * Run and create data in Share, found on data array created with {@link #addDataInClient(File, Application)}
     * 
     * @throws Exception
     */
    protected void runDataCreationInShare(boolean waitForSync) throws Exception
    {
        logger.info(String.format(info, "Start", "Share"));
        // login just once
        if (!testServerData.isEmpty())
        {
            shareLogin.loginToShare(drone, userInfo, shareUrl);
        }
        // create each Folder or File (within parent folder if needed) in the Share
        for (Entry<File, TEST_DATA> serverData : testServerData.entrySet())
        {
            File data = serverData.getKey();
            TEST_DATA dataType = serverData.getValue();

            // always start from Document Library
            share.navigateToDocuemntLibrary(drone, siteName);

            if (dataType.equals(TEST_DATA.CONTENT))
            {
                ContentDetails content = new ContentDetails();
                content.setName(data.getName());
                content.setDescription(data.getName());
                content.setTitle(data.getName());
                content.setContent("Share Created file from MacTest.runDataCreationInShare ");
                share.createContent(drone, content, ContentType.PLAINTEXT);
            }
            else if (dataType.equals(TEST_DATA.FOLDER))
            {
                logger.info("Creating Directory in Share:" + data.getName());
                share.createFolder(drone, data.getName(), data.getName(), "description");
            }
            else if (dataType.equals(TEST_DATA.FILE))
            {

                logger.info("Uploading File in Share:" + data.getName());
                File fileToUpload = share.newFile(data.getName(), data.getName());
                String parentFolder = data.getParentFile().getName();
                if (!parentFolder.equals(getLocalSiteLocationClean().getName()))
                {
                    // we need to navigate first to the parent folder of this file;
                    share.navigateToFolder(drone, parentFolder);
                }
                share.uploadFile(drone, fileToUpload);
                fileToUpload.delete(); // and deleting the temporary file
            }
            else if (dataType.equals(TEST_DATA.SITE))
            {
                share.createSite(drone, data.getName(), "Description", "public");
            }
        }

        // wait process
        if (!testServerData.isEmpty())
        {
            shareLogin.logout(drone);
            if (waitForSync)
                syncWaitTime(SERVERSYNCTIME);
        }
        logger.info(String.format(info, "End", "Share"));
    }
}
