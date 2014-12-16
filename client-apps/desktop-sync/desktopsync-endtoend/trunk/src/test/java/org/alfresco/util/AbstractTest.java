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

package org.alfresco.util;

/**
 * This method contains all the required LDTP until
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Properties;

import org.alfresco.test.utilities.AbstractTestUitl;
import org.alfresco.webdrone.WebDrone;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeSuite;

public class AbstractTest extends ShareAbstract
{
    Properties officeAppProperty = new Properties();
    private static Log logger = LogFactory.getLog(AbstractTestUitl.class);
    public WebDrone drone;

   
    @BeforeSuite(alwaysRun = true)
    public void initialSetup()
    {
        try
        {
            setupContext("share-po-test-context.xml");
            drone = getWebDrone();
        }
        catch (Exception e)
        {
            logger.error("Failed to load office App properties in the AbstractUtil Class :" + this.getClass(), e);
        }

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
     * Compare the file bytes
     * 
     * @return boolean
     * @throws Exception
     */
    public boolean compareTwoFiles(String syncFilePath, String shareFilePath) throws Exception
    {
        boolean isFileSame = false;
            byte[] syncFileByte = read(syncFilePath);
            byte[] shareFileByte = read(shareFilePath);
            if (Arrays.equals(syncFileByte, shareFileByte))
            {
                isFileSame = true;
            }
        return isFileSame;
    }

    /**
     * Util method to get byte stream 
     * @param path
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private byte[] read(String path) throws FileNotFoundException, IOException
    {
        String encoding = "UTF-8";
        InputStream in = new FileInputStream(path);
        return IOUtils.toByteArray(new InputStreamReader(in), encoding);
    }


}