package org.alfresco.os.win.desktopsync;

import org.alfresco.os.win.Application;
import org.testng.Assert;

/**
 *
 * Handles operations related to Desktop Sync Notification icon in System Tray (bottom right corner)
 * Created by rdorobantu on 2/25/2015.
 * @author - Sprasanna
 */
public class SyncSystemMenu extends Application
{

    String notificationWindowName = "Context";
    String alfrescoNotificationItemName = "Latest*";
    String desktopWindowName = "pane0";

    public SyncSystemMenu()
    {
        setWaitWindow(desktopWindowName);
        setLdtp(initializeLdtp());
    }

    public void openContext()
    {
        logger.info("open the system tray menu of sync ");
        getLdtp().mouseRightClick(alfrescoNotificationItemName);
        getLdtp().waitTime(2);
    }
    
    /**
     * Method to check conflict status is correct for the file name paseed 
     * @param conflictType - String 
     * @return - Boolean 
     */
    public boolean isConflictStatusCorrect(String conflictType, String fileName)
    {
        try
        {
            openConflictDialog();
            logger.info("Checking the conflict status for the file " + fileName);
            String[] getAllObjectList = getLdtp().getObjectList();
            for (String eachObject : getAllObjectList)
            {
                if(eachObject.contains(fileName))
                {
                    getLdtp().waitTillGuiExist(conflictType);
                    return true;
                }
            }
        }
        catch (Exception e)
        {
        }
        return false;
    }
    
    /**
     * Method to open Conflict dialog  
     * 
     */
      private void openConflictDialog()
      {
          openContext();
          logger.info("open the conflict dialog");
          getLdtp().setWindowName(notificationWindowName);
          getLdtp().selectMenuItem("View Conflicting Files");
          getLdtp().waitTime(2);
          getLdtp().setWindowName("Conflicting Files");
      }
    /**
     * Resolving the conflict based on the file name
     * @param fileName
     * @param typeOfResolve
     */
    public void resolveConflictingFiles(String fileName, String typeOfResolve)
    {
        openConflictDialog();
        logger.info("resolving conflict for the file " + fileName + " type of resolution is " + typeOfResolve);
        getLdtp().click(fileName);
        getLdtp().generateKeyEvent("<space>");
        getLdtp().click(typeOfResolve);
        getLdtp().waitTime(3);
        getLdtp().click("OK");
    }
    
    /** Resolving the conflict based on the file name
     * without re-opening the conflict dialog page
     * @param fileName
     */

    public void resolveConflictingFilesWithoutOpeningWindow(String fileName, String typeOfResolve)
    {
        logger.info("resolving conflict for the file " + fileName + " type of resolution is " + typeOfResolve);
        getLdtp().click(fileName);
        getLdtp().generateKeyEvent("<space>");
        getLdtp().click(typeOfResolve);
        getLdtp().waitTime(3);
        getLdtp().click("OK");
    }
}
