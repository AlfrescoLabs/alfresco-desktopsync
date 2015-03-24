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

package org.alfresco.os.mac.app;

import java.io.File;

import org.alfresco.os.mac.DesktopSyncMacTest;
import org.alfresco.os.win.app.Notepad;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.TestException;
import org.testng.annotations.Test;

/**
 * This class will test the screenshot failures and errors logged.
 * You can add @Listeners(org.alfresco.test.FailedTestListener.class) at class level here
 * or in suite-xml file. Please see {@link resources/mac-os-smoke.xml} suite.
 * 
 * @author Paul Brodner
 */
public class ScreenShotTest extends DesktopSyncMacTest
{
    private static final Logger logger = Logger.getLogger(ScreenShotTest.class);

    @Test(groups = { "Development" })
    public void test1eql2()
    {
        try
        {
            AssertJUnit.assertEquals(1, 2);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new TestException(e.getMessage(), e.getCause());
        }
    }

    @Test(groups = { "Development" })
    public void ldtp()
    {
        try
        {
            if (SystemUtils.IS_OS_MAC)
            {
                TextEdit notepad = new TextEdit();
                notepad.openApplication();
                notepad.close("error");
            }
            else if (SystemUtils.IS_OS_WINDOWS)
            {
                Notepad notepad = new Notepad();
                notepad.openApplication();
                notepad.close(new File("error"));
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new TestException(e.getMessage(), e.getCause());
        }
    }
}
