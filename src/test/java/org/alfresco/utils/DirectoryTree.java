package org.alfresco.utils;

import java.io.File;
import org.apache.commons.logging.Log;

/**
 * Print Directory Tree structure
 */
public class DirectoryTree
{

    private File root;
    private StringBuilder infoTree;

    /**
     * This will be the root folder from where we will display the tree
     * 
     * @param onFolder
     */
    public DirectoryTree(File onFolder)
    {
        setRoot(onFolder);
    }

    public StringBuilder showTree()
    {
        return parseRootFolder(getRoot(), 0, getInfoTree());
    }

    public void showTree(Log logger, String info)
    {
        logger.info(info + "\n" + showTree());
    }

    private StringBuilder parseRootFolder(File folder, int level, StringBuilder info)
    {
        if (!folder.exists())
        {
            return info.append("Folder: " + folder.getPath() + " does not exists.");
        }
        logInfo(info, level).append("[D] ").append(folder.getName()).append("\n");

        File[] objects = folder.listFiles();

        for (int i = 0; i < objects.length; i++)
        {
            if (objects[i].isDirectory())
            {
                parseRootFolder(objects[i], level + 1, info);
            }
            else
            {
                logInfo(info, level).append("[F] -- ").append(objects[i].getName()).append("\n");
            }
        }

        return info;
    }

    private static StringBuilder logInfo(StringBuilder info, int level)
    {
        for (int i = 1; i < level; i++)
        {
            info.append("  |  ");
        }

        return info;
    }

    public File getRoot()
    {
        return root;
    }

    public void setRoot(File root)
    {
        this.root = root;
        setInfoTree(new StringBuilder());
    }

    private StringBuilder getInfoTree()
    {
        return infoTree;
    }

    private void setInfoTree(StringBuilder infoTree)
    {
        this.infoTree = infoTree;
    }
}
