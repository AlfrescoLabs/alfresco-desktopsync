package org.alfresco.sync;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.alfresco.explorer.WindowsExplorer;
import org.alfresco.utilities.Application;
import org.alfresco.utilities.LdtpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cobra.ldtp.Ldtp;
import com.cobra.ldtp.LdtpExecutionError;

public class WindowExplorerUtil
{
    private static Log logger = LogFactory.getLog(WindowExplorerUtil.class);
    WindowsExplorer WindowsExplorer = new WindowsExplorer();
    LdtpUtil abstractUtil = new LdtpUtil();

    /**
     * open a explorer window and open folder
     * 
     * @param folderPath
     */

    public Ldtp openFolder(String folderPath)
    {
        Ldtp ldtp = null;
        try
        {
            ldtp = WindowsExplorer.openWindowsExplorer();
            WindowsExplorer.openFolder(folderPath);
        }
        catch (InterruptedException | LdtpExecutionError | IOException e)
        {

        }
        return ldtp;
    }

    /**
     * is folder present ..the folder path is the parent folder path
     * 
     * @param - path
     */

    public boolean isFolderPresent(String folderPath)
    {
        return WindowsExplorer.checkFolderExist(folderPath);
    }

    /**
     * is file present - The path should be with the file you to check
     * 
     * @param path
     */
    public boolean isfilePresent(String path)
    {
        return WindowsExplorer.checkFileExist(path);
    }

    /**
     * Util method for waiting
     * 
     * @return
     * @throws InterruptedException
     */
    public void syncWaitTime(long totalWaitTime) throws InterruptedException
    {
        int delaytime = 60000;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MMM-dd HH:mm (z)");
        long delay = delaytime;
        while (delay <= totalWaitTime)
        {
            logger.info("Sync wait time started to sleep - " + format.format(System.currentTimeMillis()));
            Thread.sleep(delay);
            delay = delay + delaytime;
        }
        logger.info("Sync wait time end  - " + format.format(System.currentTimeMillis()));
    }

    /**
     * Util method to create a folder
     * 
     * @return
     * @throws InterruptedException
     */
    public Ldtp createFolderAndOpen(String path, String folderName)
    {
        logger.info("create folder using LDTP");
        Ldtp ldtp = openFolder(path);
        abstractUtil.setLdtp(ldtp);
        WindowsExplorer.createandOpenFolder(folderName);
        logger.info("Folder created and opened");
        return ldtp;
    }

    /**
     * Right click and create folder once a new folder is created 
     */
    public void rightClickCreateFolder(String currentFolderName ,String folderName , Application applicationType)
    {
        logger.info("right click to create folder ");
        WindowsExplorer.rightClickCreate(currentFolderName, folderName, applicationType);
    }
    
    /**
     * Close explorer window ..
     * 
     */
    public void closeExplorer(String folderName)
    {
        Ldtp ldtp = new Ldtp(folderName);
        abstractUtil.setLdtp(ldtp);
        WindowsExplorer.closeExplorer();
    }
    
}
