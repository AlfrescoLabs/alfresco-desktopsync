package org.alfresco.os.win.desktopsync;

import org.testng.annotations.Test;

public class SyncSystemMenuTest {

  @Test(groups = { "development" })
  public void syncNow() {
    SyncSystemMenu app = new SyncSystemMenu();
    app.syncNow();
  }
}
