package org.alfresco.sync;

import java.io.File;
import java.util.regex.Pattern;

import org.alfresco.po.share.util.FailedTestListener;
import org.alfresco.util.AbstractTest;
import org.alfresco.utilities.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.cobra.ldtp.Ldtp;

@Listeners(FailedTestListener.class)
public class NotepadSyncTest extends AbstractTest
{
    private static Log logger = LogFactory.getLog(NotepadSyncTest.class);
    NotepadUtil notepad = new NotepadUtil();
    ShareUtil share = new ShareUtil();
    WindowExplorerUtil explorer = new WindowExplorerUtil();
    long SERVERSYNCTIME = 360000;
    long CLIENTSYNCTIME = 120000;
    String[] userInfo = new String[2];
    String testName;
    String clientCreatedFolder = "";

    @BeforeClass
    public void initialSetupOfShare()
    {
        userInfo[0] = username;
        userInfo[1] = password;
        testName = this.getClass().getSimpleName();

    }

    /**
     * This test will create a notePad file in client and validate whether it is visible in Share
     * 
     * @throws Exception
     */

    @Test
    public void testToCreateAFileInClient()
    {
        logger.info("test to create file in client started");
        try
        {
            String fileName = share.getFileName(share.getTestName());
            Ldtp ldtp = notepad.createandSaveNotepad(fileName, location + "\\" + siteName);
            notepad.closeNotepad(fileName);
            explorer.syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + ".txt"));

        }
        catch (Throwable e)
        {
            throw new SkipException("create test case failed" + e.getStackTrace());
        }
        finally
        {
            share.logout(drone);
        }
    }

    /**
     * This test will create a Folder and then a file in Share and validate whether it is visible in client
     * Steps
     */

    @Test
    public void testToCreateAFolderAndFileInShare()
    {
        logger.info("test to create a folder with file in share started");
        try
        {
            String fileName = share.getFileName(share.getTestName());
            String folderName = fileName;
            File file = share.newFile(fileName, fileName);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderName, "sync", "sync");
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            share.uploadFile(drone, file);
            explorer.syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFolderPresent(location + File.separator + siteName + File.separator + folderName));
            Assert.assertTrue(explorer.isfilePresent(location + File.separator + siteName + File.separator + folderName + File.separator + fileName));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + testName + " " + e.getStackTrace());
        }
        finally
        {
            share.logout(drone);
            
        }

    }

    /**
     * Test to create folder in client without any content and validate whether folder is sync correctly in share.
     */
    @Test
    public void testToCreateFolderInClient()
    {
        logger.info("test to create a folder and then sub folder in client");
        String folderName = "";
        String subFolderName = "";
        try
        {
            folderName = share.getFileName(share.getTestName()).toLowerCase();
            subFolderName =(folderName+"clientsub").toLowerCase();
            Ldtp ldtp = explorer.createFolderAndOpen(location + File.separator + siteName, folderName);
            explorer.rightClickCreateFolder(folderName,subFolderName, Application.FOLDER);
            explorer.syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, folderName));
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator +folderName);
            Assert.assertTrue(share.isFileVisible(drone, subFolderName));
            
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + testName + " " + e.getStackTrace());
        }
        finally
        {
            share.logout(drone);
            explorer.closeExplorer(folderName);
            clientCreatedFolder = folderName + File.separator + subFolderName;
        }
    }
    
    /**
     * Test to add file inside the sub folder in the client 
     */
    @Test 
    public void testToAddFileInsideTheFolderCreatedInClient()
    {
        clientCreatedFolder = "filesync-testtocreatefolderinclient"  + File.separator + "filesync-testtocreatefolderinclientclientsub";
        logger.info("test to create a FILE inside the sub folder created in previous testcase ");
        String fileName = ("testFile").toLowerCase();
        String syncPath = (location + File.separator + siteName +File.separator + clientCreatedFolder + fileName).toLowerCase();
        String shareFilePath = downloadPath.toLowerCase();
        String[] folders = clientCreatedFolder.split(Pattern.quote(File.separator)) ;
        String currentFolder = folders[(folders.length) - 1];
        try
        {
        
         //   fileName = share.getFileName(share.getTestName()).toLowerCase();
            Ldtp ldtp = explorer.openFolder(location + File.separator + siteName +File.separator + clientCreatedFolder);
            notepad.createNotePadInExplorer(ldtp , currentFolder, fileName , "hello world");
            explorer.syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator +clientCreatedFolder);
            Assert.assertTrue(share.isFileVisible(drone, fileName));
            share.shareDownloadFile(drone, fileName);
            Assert.assertTrue(notepad.compareTwoFiles(syncPath, shareFilePath));
         }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + testName + " " + e.getStackTrace());
        }
    }
}
