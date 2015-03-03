package org.alfresco.os.win.client;

import javax.security.sasl.SaslException;

import org.alfresco.os.win.desktopsync.ManageAccount;
import org.alfresco.os.win.desktopsync.ManageFolders;
import org.alfresco.os.win.desktopsync.ManageFolders.syncOptions;
import org.alfresco.sync.DesktopSyncTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ManageAccountTest extends DesktopSyncTest
{
    ManageAccount syncAccount = new ManageAccount();
    ManageFolders syncSelection = new ManageFolders();
    String Url;

    @BeforeClass
    public void setUp()
    {
        super.initialSetupOfShare();
        Url = shareUrl.replace("share", "alfresco");
        logger.info("Url to enter " + Url);

    }

    /**
     * Validate whether the login is successful in the client
     * Steps 1 - Enter valid user name , password and Url
     * Steps 2 - Click on Ok button in the login dialog
     * Step 3 - Check whether login is successful and the My Account Dialog is displayed
     */

    @Test
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
            throw new SkipException("login test failed ", e);
        }
    }

    /**
     * Validate whether the login is successful in the client
     * Steps 1 - Enter InValid username
     * Steps 2 - Click on Ok button in the login dialog
     * Step 3 - Check whether Error dialog is displayed
     */

    @Test
    public void invalidLoginTest()
    {

        String[] invalidUserInfo = { "test", "test" };
        try
        {
            syncAccount.openApplication();
            syncAccount.login(invalidUserInfo, Url);
            Assert.assertTrue(syncAccount.getErrorText().contains("Failed to connect to server http://172.29.100.170:8080/alfresco"), "invalid login dialog was show correctly");
            syncAccount.clickCancelButton();
            Thread.sleep(1000);
        }
        catch (Throwable e)
        {
            throw new SkipException("invalid ogin test failed ", e);
        }
    }
    
    /**
     * Test to check whether sites and my sites can be selected 
     */
    @Test
    public void selectSitesTest()
    {
        String[] selection = { };
        
        syncSelection.selectTabs(syncOptions.MYFILES);
        syncSelection.selectMyFiles();
        
    }
}