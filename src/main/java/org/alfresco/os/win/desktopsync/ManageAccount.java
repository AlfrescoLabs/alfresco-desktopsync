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

import org.alfresco.os.common.ApplicationBase;
import org.alfresco.os.win.Application;

import com.cobra.ldtp.Ldtp;
import com.cobra.ldtp.LdtpExecutionError;

/**
 * Manage account class will contain all the util methods related to login dialog 
 * @author subaprasann
 *
 */
public class ManageAccount extends Application
{

    /**
     * Method to start up the windows native client
     */

    public ManageAccount() 
    {
        boolean is64bit = System.getProperty("sun.arch.data.model").contains("64");
        if (is64bit == true)
        {
            setApplicationPath("C:\\Program Files (x86)\\Alfresco\\Alfresco Desktop Sync\\AlfrescoDesktopSync.exe");
        }
        else
        {
            setApplicationPath("C:\\Program Files\\Alfresco\\Alfresco Desktop Sync\\AlfrescoDesktopSync.exe");
        }
        setApplicationName("AlfrescoDesktopSync.exe");
        setWaitWindow("My Account");
        getLdtp().setWindowName("My Account");
    }

    public ApplicationBase openApplication()
    {
        try
        {
            openApplication(new String[] { getApplicationPath() });
        }
        catch (Exception e)
        {
            logger.error("Could not open Application " + getApplicationName() + "Error: " + e);
        }
        return this;
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
    public void login(String[] userInfo, String url) throws Exception
    {
        Ldtp ldtp = getLdtp();
        enterUserName(userInfo[0]);
        ldtp.keyPress("<tab>");
        enterPassword(userInfo[1]);
        ldtp.keyPress("<tab>");
        enterUrl(url);
        clickOkButton();
    }

    /**
     * is login Successful -
     * 
     * @return - boolean
     */
    public boolean isLoginSuccessful()
    {
        try
        {
        //    getLdtp().waitTillGuiExist("My Account", 30);
            if (getLdtp().getWindowName().contains("My Account"))
                return true;
        }
        catch (Exception e)
        {
        }
        return false;
    }

    /**
     * Sync Error Dialog
     */
    public String getErrorText() throws LdtpExecutionError
    {
        getLdtp().setWindowName("dlgAlfresco Desktop Sync");
        getLdtp().activateWindow("dlgAlfresco Desktop Sync");
        String errorText = getLdtp().getObjectProperty("lblFailed*", "label");
        getLdtp().click("OK");
        getLdtp().setWindowName("My Account");
        getLdtp().activateWindow("My Account");
        return errorText;
    }
}