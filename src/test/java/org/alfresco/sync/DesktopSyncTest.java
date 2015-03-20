/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import org.alfresco.po.share.steps.LoginActions;
import org.alfresco.po.share.steps.SiteActions;
import org.alfresco.utilities.LdtpUtils;
import org.alfresco.utils.DirectoryTree;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public class DesktopSyncTest extends DesktopSyncAbstract
{
    Properties officeAppProperty = new Properties();
    protected static Log logger = onThisClass();
    protected static String[] userInfo = null;

    // generic share variables used in tests
    protected static LoginActions shareLogin = new LoginActions();
    protected static SiteActions share = new SiteActions();
    protected boolean showClientFolderContent = true;

    @BeforeSuite(alwaysRun = true)
    public void initialSetup()
    {
        try
        {
            setupContext();
            drone = getWebDrone();
            userInfo = new String[] { username, password };

            // Site creation for windows
            if (SystemUtils.OS_NAME.contains("Windows"))
            {
                initialSiteSetUp();
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to load Bean context in :" + this.getClass(), e);
        }
    }

    /**
     * private method which will create a site and upload just one file so that as part of initial sync process
     * we can find out it was successful
     */
    private void initialSiteSetUp()
    {
        try
        {
            File initialShareFile = getRandomFile("initialShareFile", "txt");
            siteName = "desktopsyncsite" + RandomStringUtils.randomAlphanumeric(2);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.createSite(drone, siteName, siteName, "public");
            logger.info("site created - successful" + siteName);
            share.openSitesDocumentLibrary(drone, siteName);
            share.newFile(initialShareFile.getName(), "Initial file uploaded in share");
            share.uploadFile(drone, initialShareFile);
            shareLogin.logout(drone);
        }
        catch (Exception e)
        {
            logger.error("Failed to create file in share :" + this.getClass(), e);
        }
    }

    @BeforeClass(alwaysRun = true)
    public void initialSetupOfShare() throws Exception
    {
        logger.info("Initialize Setup of Class:" + getClass().getSimpleName());
    }

    /**
     * Util method for waiting
     *
     * @return
     * @throws InterruptedException
     */
    public void syncWaitTime(long totalWaitTime)
    {
        // based on the syncImmediate flag defined in desktopsync.properties file we can force or use the default wait method
        if (syncImmediately)
        {
            if (SystemUtils.IS_OS_MAC)
            {
                org.alfresco.os.mac.desktopsync.SyncSystemMenu app = new org.alfresco.os.mac.desktopsync.SyncSystemMenu();
                app.synchNow();
            }
            else if (SystemUtils.IS_OS_WINDOWS)
            {
                org.alfresco.os.win.desktopsync.SyncSystemMenu app = new org.alfresco.os.win.desktopsync.SyncSystemMenu();
                app.syncNow();
            }
            LdtpUtils.waitToLoopTime(6);
        }
        else
        {
            long delaytime = 60000;
            long delay = delaytime;
            logger.info("Sync Wait Time Started (waiting to pass: " + (totalWaitTime / delaytime) + " minute(s) )");
            while (delay <= totalWaitTime)
            {
                logger.info("Sleep - (for 1 minute)");
                try
                {
                    Thread.sleep(delaytime);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                delay = delay + delaytime;
            }
            logger.info("Sync Wait Time Ended (after: " + (totalWaitTime / delaytime) + " minute(s) )");
        }
    }

    /**
     * Compare the file bytes
     * 
     * @return boolean
     * @throws Exception
     */
    public boolean compareTwoFiles(String syncFilePath, String shareFilePath) throws Exception
    {
        boolean isFileSame = false;
        byte[] syncFileByte = read(syncFilePath);
        byte[] shareFileByte = read(shareFilePath);
        if (Arrays.equals(syncFileByte, shareFileByte))
        {
            isFileSame = true;
        }
        return isFileSame;
    }

    /**
     * Utility method to get byte stream
     * 
     * @param path
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private byte[] read(String path) throws FileNotFoundException, IOException
    {
        String encoding = "UTF-8";
        InputStream in = new FileInputStream(path);
        return IOUtils.toByteArray(new InputStreamReader(in), encoding);
    }

    /**
     * This will return the logger of the caller
     * 
     * @return logger of the class name
     */
    protected static Log onThisClass()
    {
        StackTraceElement thisCaller = Thread.currentThread().getStackTrace()[2];
        return LogFactory.getLog(thisCaller.getClassName());
    }

    /**
     * @return location of the site
     * @author Paul Brodner
     */
    public File getLocalSiteLocation()
    {
        File tmp = new File(getLocalSiteLocationClean(), getClass().getSimpleName());
        if (!tmp.exists())
        {
            tmp.mkdir();
        }
        return tmp;
    }

    /**
     * Return the location of the site in Client, without the name of the class.
     * 
     * @return
     */
    protected File getLocalSiteLocationClean()
    {
        return new File(location, siteName);
    }

    /**
     * Generate a new random filename using prefix and extension
     * 
     * @param prefix
     * @param extension
     * @return a new random File
     */
    protected File getRandomFile(String prefix, String extension)
    {
        return new File(String.format("%s-%s.%s", prefix, RandomStringUtils.randomAlphanumeric(5), extension));
    }

    /**
     * Generate a File object that points to a custom location
     * based on <prefix> and <extension>provided
     * Extension should not contain any period.
     * 
     * @param prefix
     * @param extension
     * @return
     */
    protected File getRandomFileIn(File location, String prefix, String extension)
    {
        File tmp = new File(location, getRandomFile(prefix, extension).getName());
        LdtpUtils.logInfo("Generated Random File: " + tmp.getPath());
        return tmp;
    }

    /**
     * Generate a File object that points to current client site
     * based on <prefix> and <extension>provided
     * Extension should not contain any period.
     * 
     * @param prefix
     * @param extensions
     * @return
     */
    protected File getRandomFolderIn(File location, String prefix)
    {
        File tmp = new File(location, getRandomValue(prefix));
        LdtpUtils.logInfo("Generated Random folder: " + tmp.getPath());
        return tmp;
    }

    /**
     * Returns a random values using the prefix provided
     * 
     * @param prefix
     * @return
     */
    protected String getRandomValue(String prefix)
    {
        return String.format("%s-%s", prefix, RandomStringUtils.randomAlphanumeric(5));
    }

    /**
     * Return if <fileOrFolder> is a Directory or a File
     * on MAC f.isDirectory() or f.isFile() doesn't work, so we will check the type object based
     * on the presence of the extension.
     * 
     * @param fileOrFolder
     * @return
     */
    protected boolean isFolder(File fileOrFolder)
    {
        return (FilenameUtils.getExtension(fileOrFolder.getName()).isEmpty());
    }

    @BeforeMethod
    public void showStartInfo(Method method)
    {
        logger.info("*** START TestNG Method: {" + method.getName() + "} ***");
        if (showClientFolderContent)
        {
            new DirectoryTree(getLocalSiteLocationClean()).showTree(logger, "CLIENT's local site content BEFORE test:");
        }
    }

    @AfterMethod
    public void showEndInfo(ITestResult method)
    {
        logger.info("*** END TestNG Method:   {" + method.getMethod().getMethodName() + "} ***");
        if (showClientFolderContent)
        {
            new DirectoryTree(getLocalSiteLocationClean()).showTree(logger, "CLIENT's local site content AFTER test:");
        }
    }
}