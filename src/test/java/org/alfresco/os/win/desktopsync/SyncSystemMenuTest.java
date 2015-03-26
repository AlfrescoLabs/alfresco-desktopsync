package org.alfresco.os.win.desktopsync;

import org.apache.commons.lang.SystemUtils;
import org.testng.annotations.Test;

public class SyncSystemMenuTest
{
    @Test(groups = { "development" })
    public void syncNow()
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            SyncSystemMenu app = new SyncSystemMenu();
            app.syncNow();
        }
    }
}
