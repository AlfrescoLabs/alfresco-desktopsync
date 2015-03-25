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

package org.alfresco.os.mac.desktopsync;

import org.alfresco.os.mac.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cobra.ldtp.LdtpExecutionError;

/**
 * Handles AlfrescoDesktopSync tool on MAC system
 * 
 * @author <a href="mailto:paulbrodner@gmail.com">Paul Brodner</a>
 */
public class SyncSystemMenu extends Application
{
    private static Log logger = LogFactory.getLog(SyncSystemMenu.class);
    public SyncSystemMenu()
    {
        setApplicationName("AlfrescoDesktopSyncClient");
        // set the root path of the Finder Window to the current user Documents folder
        setApplicationPath("/Applications/AlfrescoDesktopSyncClient.app");
        // each finder has the window name set to the current folder name
        setWaitWindow("appAlfrescoDesktopSyncClient");
    }

    /**
     * Try to Sync the folder based on the
     */
    public void synchNow()
    {
        logger.info("Force Sync process from Sys Tray!");
        openTrayMenu();
        getLdtp().click("mnuSyncNow!");
        getLdtp().waitTime(5);
    }

    /**
     * Quit application, using the context menu
     */
    public void quit()
    {
        openTrayMenu();
        getLdtp().click("mnuQuit");
    }

    private void openTrayMenu()
    {
        try
        {
            getLdtp().click("mnu0");
        }
        catch (LdtpExecutionError e)
        {
            if (e.getMessage().contains(getWaitWindow()))
            {
                logger.error("Could not open Sys Tray of Desktop Sync. Please restart application!");
            }
            else
            {
                logger.error(e);
            }
        }
    }

    @Override
    public void exitApplication()
    {
        killProcess();
    }
}
