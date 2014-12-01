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

import java.util.Properties;

import org.alfresco.test.utilities.AbstractTestUitl;
import org.alfresco.webdrone.WebDrone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeSuite;

public class AbstractTest extends ShareAbstract
{
    Properties officeAppProperty = new Properties();
    ShareAbstract share = new ShareAbstract();
    private static Log logger = LogFactory.getLog(AbstractTestUitl.class);
    public WebDrone drone;

   
    @BeforeSuite(alwaysRun = true)
    public void initialSetup()
    {
        try
        {
            share.setupContext("share-po-test-context.xml");
            drone = share.getWebDrone();
            System.out.println("username" + username);
        }
        catch (Exception e)
        {
            logger.error("Failed to load office App properties in the AbstractUtil Class :" + this.getClass(), e);
        }

    }

}