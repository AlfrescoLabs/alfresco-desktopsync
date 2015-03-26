package org.alfresco.sync;

import java.io.File;

import org.alfresco.utils.DirectoryTree;
import org.apache.log4j.Logger;

/**
 * Utility class with helper methods
 * 
 * @author Paul Brodner
 */
public class DesktopSyncUtils
{
    private static Logger logger = Logger.getLogger(DesktopSyncUtils.class);

    public static void showTreeInfo(File location)
    {
        new DirectoryTree(location).showTree(logger, "Folder content:");
    }
}
