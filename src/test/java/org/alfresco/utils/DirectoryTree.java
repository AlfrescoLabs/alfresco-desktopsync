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

package org.alfresco.utils;

import java.io.File;
import org.apache.commons.logging.Log;

/**
 * Print Directory Tree structure
 * 
 * @author Paul Brodner
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
