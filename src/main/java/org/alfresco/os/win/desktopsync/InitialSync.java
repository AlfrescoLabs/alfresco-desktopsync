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

/**
 * Class contain all the methods required to start up the sync client do initial sync
 */
package org.alfresco.os.win.desktopsync;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.alfresco.os.win.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.SkipException;

import com.cobra.ldtp.Ldtp;

public class InitialSync extends Application
{

    private static Log logger = LogFactory.getLog(InitialSync.class);

    /**
     * Method to start up the windows native client
     */
    public void startSyncClient()
    {
        Ldtp ldtp;
        try
        {
            TimeUnit.SECONDS.sleep(5);
        }
        catch (InterruptedException e)
        {
        }
        try
        {
            Runtime.getRuntime().exec("C:\\Program Files (x86)\\Alfresco\\My Product Name\\AlfrescoDesktopSync.exe");
        }
        catch (IOException e1)
        {
            throw new SkipException("The sync exe cannot be started", e1);
        }

        try
        {
            TimeUnit.SECONDS.sleep(3);
        }
        catch (InterruptedException e)
        {
            throw new SkipException("TimeoutException ", e);
        }
        ldtp = new Ldtp("My Account");
        ldtp.setWindowName("My Account");
        ldtp.activateWindow("My Account");
        setLdtp(ldtp);
    }

    /**
     * Enter user name
     * 
     * @param - userName
     */
    public void enterUserName(String userName)
    {
        logger.info("enter user name");
        Ldtp ldtp = getLdtp();
        ldtp.enterString("1043", userName);
    }

    /**
     * Enter password
     * 
     * @param - password
     */
    public void enterPassword(String password)
    {
        logger.info("enter password");
        Ldtp ldtp = getLdtp();
        ldtp.enterString("1044", password);
    }

    /**
     * Enter URL
     * 
     * @param - password
     */
    public void enterUrl(String url)
    {
        logger.info("enter URL");
        Ldtp ldtp = getLdtp();
        ldtp.enterString("1042", url);
    }

    /**
     * Click on Ok button
     */
    public void clickOkButton()
    {
        logger.info("Click on Ok button");
        Ldtp ldtp = getLdtp();
        ldtp.click("OK");
    }

    /**
     * Click on Cancel button
     */
    public void clickCancelButton()
    {
        logger.info("Click on cancel button");
        Ldtp ldtp = getLdtp();
        ldtp.click("Cancel");
    }

    /**
     * login in sync client
     * 
     * @param - String - username
     * @param - String - password
     * @param - String - Url
     */
    public void login(String username, String password, String url)
    {
        Ldtp ldtp = getLdtp();
        enterUserName(username);
        ldtp.keyPress("<tab>");
        enterPassword(password);
        ldtp.keyPress("<tab>");
        enterUrl(url);
        clickOkButton();
    }

    /**
     * Select the tab to get the list
     * 
     * @param - String - Tab name we want to select
     */
    public void selectTabs(String tabName)
    {
        Ldtp ldtp = getLdtp();
        ldtp.mouseMove(tabName);
    }

    /**
     * Select the site
     * 
     * @parm - String[] - list of sites you want to select
     */
    public void selectSites(String[] sites)
    {
        Ldtp ldtp = getLdtp();
        String[] allSites = ldtp.getObjectList();
        for (String eachSite : sites)
        {
            for (String eachObject : allSites)
            {
                System.out.println("each object under a tab " + eachObject);
                if (eachObject.contains(eachSite))
                {
                    if (eachObject.contains("lbl"))
                    {
                        ldtp.doubleClick(eachObject);
                        ldtp.comboSelect(eachObject, eachSite);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Access system tray icon menu
     */
    public void getSystemTray()
    {
        // ldtp.click("pane2");
        String[] allWindows = getLdtp().getAppList();
        for (String objects : allWindows)
        {
            System.out.println("objects " + objects);
        }
        String[] allWindows1 = getLdtp().getAllStates("AlfrescoDesktopSync");
        for (String objects : allWindows1)
        {
            System.out.println("objects " + objects);
        }

    }

}
