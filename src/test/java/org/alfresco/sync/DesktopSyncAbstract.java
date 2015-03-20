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
import java.util.ArrayList;
import java.util.List;

import org.alfresco.os.win.desktopsync.ManageAccount;
import org.alfresco.os.win.desktopsync.SyncSystemMenu;
import org.alfresco.utilities.LdtpUtils;
import org.alfresco.webdrone.WebDrone;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;

/**
 * Handle context Beans from alfresco projects
 * 
 * @author Paul Brodner
 */
public abstract class DesktopSyncAbstract
{
    private static Log logger = LogFactory.getLog(DesktopSyncAbstract.class);
    private static ApplicationContext ctx;
    protected static String shareUrl;
    protected static String password;
    protected static String username;
    protected static String googleusername;
    protected static String googlepassword;
    protected static String location;
    protected static String siteName;
    protected static String fileAppend;
    protected static String installerPath;
    public static WebDrone drone;
    
    SyncSystemMenu contextMenu = new SyncSystemMenu();
    ManageAccount account = new ManageAccount();

   // protected static long SERVERSYNCTIME = 300000;
    protected static long SERVERSYNCTIME = 60000;
    protected static long CLIENTSYNCTIME = 60000;
    protected static String downloadPath;
    public static long maxWaitTime_CloudSync = 50000;

    public void setupContext() throws Exception
    {
        List<String> contextXMLList = new ArrayList<String>();
        contextXMLList.add("desktopsync-test-context.xml");
        ctx = new ClassPathXmlApplicationContext(contextXMLList.toArray(new String[contextXMLList.size()]));
        DesktopSyncProperties t = (DesktopSyncProperties) ctx.getBean("desktopSyncProperties");
        shareUrl = t.getShareUrl();
        username = t.getUsername();
        password = t.getPassword();
        googleusername = t.getGoogleUserName();
        googlepassword = t.getGooglePassword();
        location = t.getLocation();
        siteName = t.getSiteName();
        downloadPath = t.getFiledirectoryPath();
        installerPath = t.getInstallerpath();

        displayProperties("Client Sync Location", location);
        displayProperties("Share URL", shareUrl);
        displayProperties("SiteName", siteName);
        displayProperties("downloadPath", downloadPath);
    }

    private void displayProperties(String property, String value)
    {
        logger.info(String.format("Using DesktopSync property '%s'={%s}", property, value));
    }

    public WebDrone getWebDrone() throws Exception
    {
        WebDrone drone;
        drone = (WebDrone) ctx.getBean("webDrone");
        drone.maximize();
        return drone;
    }

    @AfterSuite(alwaysRun = true)
    public void closeWebDrone()
    {
//        // Delete the site before close
        if (SystemUtils.OS_NAME.contains("Windows"))
        {
            removeAccount();
        }
        if (logger.isTraceEnabled())
        {
            logger.trace("Closing web drone");
        }
        // Close the browser
        if (drone != null)
        {
          drone.quit();
          drone = null;
        }
    }

    /**
     * Delete Account from the client
     */
    public void removeAccount()
    {
        try
        {
            contextMenu.manageAccount("Remove Account");
            Assert.assertEquals(contextMenu.removeAccount(), "Are you sure you want to permanently delete all of these items?");
            contextMenu.removeAccountConfirmation(true);
            Assert.assertTrue(account.isLoginSuccessful(), "control is transferred to login screen");
            Thread.sleep(1000);
            Assert.assertFalse(LdtpUtils.isDirectoryPresent(location + File.separator + siteName));
            account.clickCancelButton();
        }
        catch (Throwable e)
        {
            logger.error(e);
        }
    }
}
