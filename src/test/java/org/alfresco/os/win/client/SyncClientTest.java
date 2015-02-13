package org.alfresco.os.win.client;

import org.alfresco.os.win.client.InitialSync;
import org.alfresco.sync.AbstractTest;
import org.testng.annotations.Test;

public class SyncClientTest extends AbstractTest
{
    InitialSync  client = new InitialSync();
    
    @Test
    public void start()
    {
       // client.startInstaller("C:\\Users\\GuestTest\\Downloads\\setup_x86_29Jan2015.exe");
      // String[] sites = {"movesite7"};
        client.startSyncClient();
      client.login("syncuser1", "password123", "http://172.30.40.174:8080/alfresco");
      // client.selectTabs("Favourate Sites");
      // client.getSystemTray();
     //  client.selectSites(sites);
    }

}
