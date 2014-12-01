/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.sync;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.alfresco.application.windows.NotepadApplications;
import org.alfresco.explorer.WindowsExplorer;
import org.alfresco.utilities.Application;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cobra.ldtp.Ldtp;
import com.cobra.ldtp.LdtpExecutionError;

public class NotepadUtil
{
    private static Log logger = LogFactory.getLog(NotepadUtil.class);
    NotepadApplications notepad = new NotepadApplications();
    WindowsExplorer explorer = new WindowsExplorer();
    
    /**
     * Create a note pad file and save in a particular location
     * 
     * @throws Exception
     */

    public Ldtp createandSaveNotepad(String fileName, String filePath) throws Exception
    {
        Ldtp ldtp = null;
        try
        {
            ldtp = notepad.openNotepadApplication();
            notepad.saveAsNotpad(filePath, fileName);
        }
        catch (LdtpExecutionError | IOException e)
        {
            throw new Exception("Failed to create  a note pad and save it in particular location" + e.getStackTrace());
        }
        return ldtp;
    }

    /**
     * CloseNote pad application
     * 
     * @throws Exception
     */

    public void closeNotepad(String fileName) throws Exception
    {
        try
        {
            notepad.closeNotepad(fileName);
        }
        catch (LdtpExecutionError e)
        {
            throw new Exception("Failed to close the note pad" + e.getStackTrace());
        }
    }

    /**
     * open a note pad from a particular folder of the sync set
     * Add a new line
     * Save the file
     * 
     * @throws Exception
     */

    public void openAndEditNotepad(String fileName, String filePath, String textToAdd) throws Exception
    {
        try
        {
            Ldtp ldtp = explorer.openFile(fileName, filePath);
            notepad.editNotepad(textToAdd, fileName);
            notepad.ctrlSSave();

        }
        catch (LdtpExecutionError e)
        {
            throw new Exception("Failed to close the note pad" + e.getStackTrace());
        }
    }

    /**
     * Right click create a note pad file
     * open the file for editing
     * Save the note pad
     * 
     * @throws Exception
     */
    public void createNotePadInExplorer(Ldtp ldtp, String folderToCreateFile, String fileName, String contentToAdd) throws Exception
    {
        try
        {
            logger.info("The folder is opened for creating a file");
            explorer.rightClickCreate(folderToCreateFile, fileName, Application.TEXTFILE);
            logger.info("File is created");
            explorer.oepnFileInCurrentFolder(fileName);
            logger.info("File is opening for editing");
            notepad.editNotepad(contentToAdd, fileName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new Exception("Failed to create a note pad file to edit " + e.getStackTrace());
        }
    }

    /**
     * Compare the file bytes
     * 
     * @return boolean
     * @throws Exception
     */
    public boolean compareTwoFiles(String syncFilePath, String shareFilePath) throws Exception
    {
        boolean isFileSame = false;
        try
        {
            byte[] syncFileByte = read(syncFilePath);
            byte[] shareFileByte = read(shareFilePath);
            if (Arrays.equals(syncFileByte, shareFileByte))
            {
                isFileSame = true;
            }
        }
        catch (Exception e)
        {
            throw new Exception("unable to compare two files in the given path " + e.getStackTrace());
        }
        return isFileSame;
    }

    private byte[] read(String path) throws FileNotFoundException, IOException
    {
        String encoding = "UTF-8";
        InputStream in = new FileInputStream(path);
        return IOUtils.toByteArray(new InputStreamReader(in), encoding);
    }

}
