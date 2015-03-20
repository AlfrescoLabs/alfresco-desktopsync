package org.alfresco.os.win.client;

import org.alfresco.os.win.desktopsync.ManageAccount;
import org.alfresco.os.win.desktopsync.ManageFolders;
import org.alfresco.os.win.desktopsync.ManageFolders.syncOptions;
import org.alfresco.os.win.desktopsync.SyncSystemMenu;
import org.alfresco.sync.DesktopSyncTest;
import org.alfresco.utilities.LdtpUtils;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ManageAccountTest extends DesktopSyncTest
{
    ManageAccount syncAccount = new ManageAccount();
    ManageFolders syncSelection = new ManageFolders();
    SyncSystemMenu contextMenu = new SyncSystemMenu();
    String Url;

    @BeforeClass
    public void setUp() throws Exception
    {
        Url = shareUrl.replace("share", "alfresco");
        logger.info("Url to enter " + Url);
    }

    /**
     * Validate whether the login is successful in the client
     * Steps 1 - Enter InValid username
     * Steps 2 - Click on Ok button in the login dialog
     * Step 3 - Check whether Error dialog is displayed
     */

    @Test(priority = 1)
    public void invalidLoginTest()
    {

        String[] invalidUserInfo = { "test", "test" };
        try
        {
            syncAccount.openApplication();
            syncAccount.login(invalidUserInfo, Url);
            Assert.assertTrue(syncAccount.getErrorText().contains("Failed to connect to server http://172.29.100.170:8080/alfresco"),
                    "invalid login dialog was show correctly");
            syncAccount.clickCancelButton();
            Thread.sleep(5000);
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("invalid ogin test failed ", e);
        }
    }

    /**
     * Validate whether the login is successful in the client
     * Steps 1 - Enter valid user name , password and Url
     * Steps 2 - Click on Ok button in the login dialog
     * Step 3 - Check whether login is successful and the My Account Dialog is displayed
     */

    @Test(priority = 2)
    public void syncClientLoginTest()
    {
        try
        {
            syncAccount.openApplication();
            syncAccount.login(userInfo, Url);
            Assert.assertTrue(syncAccount.isLoginSuccessful(), "login was successful");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new SkipException("login test failed ", e);
        }
    }

    /**
     * Test to check whether sites and my sites can be selected
     * 
     * @throws Exception
     */
    @Test(priority = 3)
    public void selectSites() throws Exception
    {
        String[] sitesToSelect = { siteName };
        try
        {
            // Selecting my files
            syncSelection.selectTabs(syncOptions.SITES);
            syncSelection.selectSites(sitesToSelect);
            syncSelection.selectSync();
            if (syncSelection.isSyncSuccessful())
            {
                syncSelection.clickOkSyncSucessDialog();
            }
            Thread.sleep(1000);
            System.out.println("getLocalSiteLocation().getParentFile().getName() " + getLocalSiteLocation().getParentFile().getName());
            Assert.assertTrue(getLocalSiteLocation().getParentFile().exists(), "site is synced successful");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new Exception("test of select my file and share files failed ", e);
        }
    }

}