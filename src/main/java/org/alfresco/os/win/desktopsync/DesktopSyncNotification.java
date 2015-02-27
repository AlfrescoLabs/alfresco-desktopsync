package org.alfresco.os.win.desktopsync;

import org.alfresco.os.win.Application;
import org.testng.Assert;

/**
 *
 * Handles operations related to Desktop Sync Notification icon in System Tray (bottom right corner)
 * Created by rdorobantu on 2/25/2015.
 */
public class DesktopSyncNotification extends Application
{

    String notificationWindowName = "Context";
    String alfrescoNotificationItemName = "Latest*";
    String desktopWindowName = "pane0";

    public DesktopSyncNotification()
    {
        setWaitWindow(desktopWindowName);
        setLdtp(initializeLdtp());
    }

    public void openContext()
    {
        getLdtp().mouseRightClick(alfrescoNotificationItemName);
        getLdtp().waitTime(2);
    }

    public boolean isConflictStatusCorrect(String conflictType)
    {
        try
        {
            openContext();
            getLdtp().setWindowName(notificationWindowName);
            getLdtp().selectMenuItem("View Conflicting Files");
            getLdtp().setWindowName("Conflicting Files");
            getLdtp().waitTillGuiExist(conflictType);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    public void resolveConflictingFiles(String fileName, String typeOfResolve)
    {
        openContext();
        getLdtp().setWindowName(notificationWindowName);
        getLdtp().selectMenuItem("View Conflicting Files");
        getLdtp().waitTime(2);
        getLdtp().setWindowName("Conflicting Files");
        getLdtp().click(fileName);
        getLdtp().generateKeyEvent("<space>");
        getLdtp().click(typeOfResolve);
        getLdtp().waitTime(3);
        getLdtp().click("OK");
    }
}
