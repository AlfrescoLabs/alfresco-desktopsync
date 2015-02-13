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

/**
 * This class will contain all the installer stuff for sync 
 */

package org.alfresco.os.win.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.alfresco.utilities.LdtpUtil;
import org.testng.Assert;
import org.testng.SkipException;

import com.cobra.ldtp.Ldtp;

public class Installer extends LdtpUtil
{

     /**
      * Start the set up from a particular location 
      */
    public void startInstaller(String path)
    {
        Ldtp ldtp;
        try
        {
            TimeUnit.SECONDS.sleep(5);
        }
        catch (InterruptedException e)
        {
        }
        try
        {
            Runtime.getRuntime().exec(path);
        }
        catch (IOException e1)
        {
            throw new SkipException("The sync exe cannot be started", e1);
        }

        try
        {
            TimeUnit.SECONDS.sleep(3);
        }
        catch (InterruptedException e)
        {
            throw new SkipException("TimeoutException ", e);
        }
        ldtp = new Ldtp("Alfresco Desktop Sync - InstallShield Wizard");
        ldtp.setWindowName("Alfresco Desktop Sync - InstallShield Wizard");
        ldtp.activateWindow("Alfresco Desktop Sync - InstallShield Wizard");
        ldtp.waitTillGuiExist("Next");
        ldtp.click("Next");
        ldtp.click("I accept the terms in the license agreement");
    //    Assert.assertEquals(ldtp.isTextStateEnabled("Next") , 1);
        ldtp.click("Install");
        ldtp.waitTillGuiExist("Finish");
        ldtp.click("Finish");
    }



}

