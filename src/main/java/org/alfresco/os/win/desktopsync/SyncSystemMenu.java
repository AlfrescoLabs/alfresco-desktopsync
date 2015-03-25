package org.alfresco.os.win.desktopsync;

import org.alfresco.os.win.Application;
import org.apache.log4j.Logger;

/**
 *
 * Handles operations related to Desktop Sync Notification icon in System Tray (bottom right corner)
 * Created by rdorobantu on 2/25/2015.
 * @author - Sprasanna
 */
public class SyncSystemMenu extends Application
{
    private static Logger logger = Logger.getLogger(SyncSystemMenu.class);
    String notificationWindowName = "Context";
    String alfrescoNotificationItemName = "*ync*";
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
    public void activateCurrentDialog()
    {
        openContext();
        focus();
     //   getLdtp().click("1076");
    } 
    
    
    /**
     * Method to check conflict status is correct for the file name paseed 
     * @param conflictType - String 
     * @return - Boolean 
     */
    public boolean isConflictStatusCorrect(String conflictType, String fileName) throws Exception
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
      private void openConflictDialog() throws Exception
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
    public void resolveConflictingFiles(String fileName, String typeOfResolve) throws Exception
    {
        openConflictDialog();
        logger.info("resolving conflict for the file " + fileName + " type of resolution is " + typeOfResolve);
        getLdtp().click(fileName);
        getLdtp().generateKeyEvent("<space>");
        getLdtp().click(typeOfResolve);
        getLdtp().waitTime(3);
        getLdtp().click("OK");
    }
    
    /**
     * Click on manage Account in the system tray 
     * @param - String - Action like Remove Account , manage Folder 
     */
    public void manageAccount(String action) throws Exception
    {
        openContext();
        getLdtp().setWindowName(notificationWindowName);
        getLdtp().selectMenuItem("Manage Account");
        logger.info("Click on Manage Account and get all the actions");
        getLdtp().setWindowName("Manage Account");
        getLdtp().click(action);
    }
    
    /**
     * Remove account confirmation 
     */
    public String removeAccount()
    {
        getLdtp().setWindowName("Delete Multiple Items");
        return getLdtp().getObjectProperty("Are*","label");
    }
    
    /**
     * delete multiple items cancel
     */
    public void removeAccountConfirmation(boolean confirmYesOrNo)
    {
      logger.info("remove confirmation  "+  confirmYesOrNo);
        if (confirmYesOrNo)
        {
            getLdtp().click("Yes");
        }
        else
        {
            getLdtp().click("No");
        }
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
    
    /**
     * Hit sync now button in the context menu
     *
     */
    
    public void syncNow()
    {
        logger.info("Force Sync process from Sys Tray!");
        openContext();
        getLdtp().setWindowName(notificationWindowName);
        getLdtp().selectMenuItem("Sync Now");      
        getLdtp().waitTime(3);
    }
}
