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


import org.alfresco.os.win.Application;

import com.cobra.ldtp.Ldtp;

public class ManageFolders extends Application
{
    /**
     * Enum for all the application that can be used in windows
     * @author sprasanna
     *
     */
    public enum syncOptions
    {
        MYFILES("My Files"),
        FOLDERS("Favorite Folders"),
        SITES("Favorite Sites"),
        REPO("Repository");
        
        private String options;
 
        private syncOptions(String type)
        {
            options = type;
        }
 
        public String getsyncOptions()
        {
            return options;
        }
    }
    
    public ManageFolders()
    {
        setWaitWindow("My Account");
        setLdtp(initializeLdtp());
    }
    /**
     * Select the tab to get the list
     * 
     * @param - String - Tab name we want to select
     */
    public void selectTabs(syncOptions tabName)
    {
        Ldtp ldtp = getLdtp();
        ldtp.mouseMove(tabName.getsyncOptions());
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
                if (eachObject.contains(eachSite))
                {
                    if (eachObject.contains("lbl"))
                    {
                        ldtp.doubleClick(eachObject);
                        break;
                    }
                }
            }
        }
    }
    /**
     * Select Sync now button and look for the select Folder Dialog
     */
    public void selectSync()
    {
        getLdtp().click("Sync");
        getLdtp().waitTime(3);
        getLdtp().setWindowName("Sync Success !");
        getLdtp().click("OK");
    }

    /**
     * Select My files and shared Files 
     */
    public void selectMyFiles()
    {
        getLdtp().doubleClick("My Files");
        getLdtp().doubleClick("Shared Files");
    }
    
    /**
     * Validating to see whether sync is succesful 
     */
    public boolean isSyncSuccessful()
    {
        boolean syncSucessful = false;
        try
        {
           String getText =  getLdtp().getObjectProperty("Desktop sync*", "label");
           if (getText.contains("Desktop sync is now installed and running."))
               syncSucessful = true;
        }
        catch(Exception e)
        {
        }
        return syncSucessful;
    }
    /**
     * Click on the sync sucess dialog
     */
    public void clickOkSyncSucessDialog()
    {
        getLdtp().click("OK");
    }
}
