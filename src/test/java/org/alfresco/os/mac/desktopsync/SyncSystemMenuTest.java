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

package org.alfresco.os.mac.desktopsync;

import junit.framework.Assert;

import org.alfresco.utilities.LdtpUtils;
import org.testng.annotations.Test;

/**
 * @author Paul Brodner
 */
public class SyncSystemMenuTest
{
    @Test(groups = { "MacOnly" })
    public void synchNow()
    {
        SyncSystemMenu app = new SyncSystemMenu();
        app.synchNow();
    }

    @Test(groups = { "MacOnly" })
    public void testQuit()
    {
        SyncSystemMenu app = new SyncSystemMenu();
        app.quit();
        LdtpUtils.waitToLoopTime(5);
        Assert.assertFalse("Desktop Sync app is still running", LdtpUtils.isProcessRunning(app.getApplicationName()));
    }
}
