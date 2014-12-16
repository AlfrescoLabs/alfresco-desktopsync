package org.alfresco.sync;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.Assert;

import java.io.File;
import java.util.regex.Pattern;

import org.alfresco.application.windows.NotepadApplications;
import org.alfresco.explorer.WindowsExplorer;
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.site.document.ContentType;
import org.alfresco.po.share.util.FailedTestListener;
import org.alfresco.util.AbstractTest;
import org.alfresco.utilities.Application;
import org.alfresco.utilities.LdtpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.SkipException;
import org.testng.annotations.Listeners;


@Listeners(FailedTestListener.class)
public class NotepadSyncTest extends AbstractTest
{
    private static Log logger = LogFactory.getLog(NotepadSyncTest.class);
    NotepadApplications notepad = new NotepadApplications();
    ShareUtil share = new ShareUtil();
    WindowsExplorer explorer = new WindowsExplorer();
    LdtpUtil ldtpObject = new LdtpUtil();
    long SERVERSYNCTIME = 300000;
    //long CLIENTSYNCTIME = 120000;
    long CLIENTSYNCTIME = 60000;
    String[] userInfo = new String[2];
    String clientCreatedFolder = "";
    String shareCreatedFolder = "";
    String syncLocation = "";
    String shareFilePath = "";
    final String FILEEXT = ".txt";

    @BeforeClass
    public void initialSetupOfShare()
    {
        userInfo[0] = username;
        userInfo[1] = password;
        syncLocation = location + File.separator + siteName;
        shareFilePath = downloadPath.toLowerCase();

    }

    /**
     * This test will create a notePad file in client and validate whether it is visible in Share
     * Step1 - Create a file in Note pad save it without any content
     * Step2 - Close Notepad
     * Step3 - Wait for Sync time which is 2 mins for client
     * Step4 - Login in share
     * Step5 - Access sync site
     * Step6 - Check the new file created in client is present in share.
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
            notepad.openNotepadApplication();
            notepad.setNotepadWindow("Notepad");
            notepad.saveAsNotpad(syncLocation, fileName);
            notepad.closeNotepad(fileName);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));

        }
        catch (Throwable e)
        {
            throw new SkipException("create test case failed", e);
        }
        finally
        {
            share.logout(drone);
        }
    }

    /**
     * This test will create a Folder and then a file in Share and validate whether it is visible in client
     * Step1 - login in share
     * Step2 - Access sync site
     * Step3 - Create a Folder
     * Step4 - Open the folder created in Step3
     * Step5 - Upload File inside the folder
     * Step6 - Wait for Sync time which is 5 mins for share
     * Step6 - in the client access the sync folder
     * Step7 - Validate the folder with file created in share is synced correctly
     */

    @Test
    public void testToCreateAFolderAndFileInShare()
    {
        logger.info("test to create a folder with file in share started");
        try
        {
            String name = share.getFileName(share.getTestName()).toLowerCase();
            String fileName = (name + FILEEXT).toLowerCase();
            String folderName = name;
            File file = ShareUtil.newFile(fileName, fileName);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderName, "sync", "sync");
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            share.uploadFile(drone, file);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFolderPresent(syncLocation + File.separator + folderName));
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + folderName + File.separator + fileName));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);

        }

    }

    /**
     * Test to create folder and sub Folder in client and validate whether folders is sync correctly in share.
     * Step1 - In client open the explorer , access the sync folder
     * Step2 - From the explorer menu create a new folder
     * Step3 - Open the created folder and using the context folder create another folder
     * Step4 - Wait for Sync time which is 2 mins for client
     * Step6 - Login in share
     * Step7 - open sync site document library
     * Step8 - Validate the folder is synced correctly
     * Step9 - Navigate to folder
     * Step10 - Validate whether the subFolder created is synced correctly
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
            subFolderName = (folderName + "clientsub").toLowerCase();
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.createandOpenFolder(folderName);
            explorer.rightClickCreate(folderName, subFolderName, Application.FOLDER);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, folderName));
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            Assert.assertTrue(share.isFileVisible(drone, subFolderName));

        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            ldtpObject.setOnWindow(folderName);
            explorer.closeExplorer();
            clientCreatedFolder = folderName + File.separator + subFolderName;
        }
    }

    /**
     * Test to add file inside the sub folder in the client
     * This test case is dependents on the previous method testToCreateFolderInClient
     * Step1 - open Windows Explorer
     * Step2 - open the sub folder
     * Step3 - Create a note pad inside the sub folder
     * Step5 - Open the note pad and add a line of text called desktop sync
     * Step6 - Wait for Sync time which is 2 mins for client
     * Step7 - login in share
     * Step8 - open the sync site dashboard
     * Step9 - Navigate to the sub Folder
     * step10 - Validate the file created in client is synced correctly
     */
   @Test(dependsOnMethods = "testToCreateFolderInClient")
    public void testToAddFileInsideTheFolderCreatedInClient()
    {
        logger.info("test to create a FILE inside the sub folder created in previous testcase ");
        String fileName = share.getFileName(share.getTestName()).toLowerCase();
        String syncPath = (syncLocation + File.separator + clientCreatedFolder + fileName +FILEEXT).toLowerCase();
        String sharePath = shareFilePath + File.separator + fileName + FILEEXT;
        String[] folders = clientCreatedFolder.split(Pattern.quote(File.separator));
        String currentFolder = folders[(folders.length) - 1];
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation + File.separator + clientCreatedFolder);
            explorer.rightClickCreate(currentFolder, fileName, Application.TEXTFILE);
            explorer.oepnFileInCurrentFolder(fileName);
            notepad.editNotepad("desktop sync", fileName);
            notepad.ctrlSSave();
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + clientCreatedFolder);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT)); 
            share.shareDownloadFileFromDocLib(drone, fileName + FILEEXT, sharePath);
            Assert.assertTrue(compareTwoFiles(syncPath, sharePath));
            notepad.closeNotepad(fileName);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            ldtpObject.setOnWindow(currentFolder);
            explorer.closeExplorer();
        }
    }

    /**
     * Test cases to create empty sub folder in share without any files is sycned to the client correctly
     * Step1 - login in share
     * Step2 - Open the sync site
     * Step3 - Create a Folder
     * Step4 - open the created Folder
     * Step5 - Create a sub Folder
     * Step6 - Wait for Sync time which is 5 mins for share
     * Step7 - In client validate both the folder and sub folder is present
     */
    @Test
    public void testToCreateFolderInShare()
    {

        String shareCreatedFolder = share.getFileName(share.getTestName()).toLowerCase();
        String subFolderName = "sub_" + shareCreatedFolder;
        try
        {
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, shareCreatedFolder, shareCreatedFolder, shareCreatedFolder);
            ShareUtil.selectContent(drone, shareCreatedFolder);
            share.createFolder(drone, subFolderName, subFolderName, subFolderName);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFolderPresent(syncLocation + File.separator + shareCreatedFolder));
            Assert.assertTrue(explorer.isFolderPresent(syncLocation + File.separator + shareCreatedFolder + File.separator + subFolderName));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
        }
    }

    /**
     * Test to edit a file in client which is already synced
     */
    @Test
    public void updateFileInClient()
    {
        String fileName = share.getFileName(share.getTestName()).toLowerCase();
        String clientLocation = syncLocation +File.separator + fileName + FILEEXT;
        String shareLocation = shareFilePath + File.separator + fileName + FILEEXT;
        try
        {

            logger.info("opening a blank note pad application and saving the same");
            notepad.openNotepadApplication();
            notepad.setNotepadWindow("Notepad");
            notepad.saveAsNotpad(syncLocation, fileName);
            notepad.editNotepad("first create in client", fileName);
            notepad.ctrlSSave();
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + ".txt"));
            notepad.appendTextToNotepad("adding another line of text", fileName);
            notepad.ctrlSSave();
            syncWaitTime(CLIENTSYNCTIME);
            share.shareDownloadFileFromDocLib(drone, fileName + FILEEXT,shareLocation);
            Assert.assertTrue(compareTwoFiles(clientLocation, shareLocation));
            notepad.closeNotepad(fileName);
            syncWaitTime(CLIENTSYNCTIME);
            share.shareDownloadFileFromDocLib(drone, fileName + FILEEXT, shareLocation);
            Assert.assertTrue(compareTwoFiles(clientLocation, shareLocation));
        }
        catch (Throwable e)
        {
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
        }

    }

    /**
     * Upload a new version in share
     */
    @Test
    public void testUpdateFileInShare()
    {
        String fileName = share.getFileName(share.getTestName() +FILEEXT).toLowerCase();
        ContentDetails content = new ContentDetails();
        content.setName(fileName);
        content.setDescription(fileName);
        content.setTitle(fileName);
        content.setContent("share created file");
        String clientLocation = syncLocation + File.separator +  fileName;
        String shareLocation = shareFilePath + File.separator + fileName;
        try
        {
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createContent(drone, content, ContentType.PLAINTEXT);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + fileName));
            share.uploadNewVersionOfDocument(drone, fileName, fileName, "test sync update");
            syncWaitTime(SERVERSYNCTIME);
      //      share.shareDownloadFileFromDocLib(drone, fileName +FILEEXT, shareLocation);
            Assert.assertTrue(compareTwoFiles(clientLocation, shareLocation));
        }
        catch (Throwable e)
        {
            throw new SkipException("test case failed " + share.getTestName(), e);
        }

    }

    /**
     * Test case to delete a file just created
     */
    @Test
    public void testToDeleteFileJustCreated()
    {
        String fileName = share.getFileName(share.getTestName()).toLowerCase();
        try
        {
            notepad.openNotepadApplication();
            notepad.saveAsNotpad(syncLocation, fileName);
            notepad.editNotepad("desktop Automated Testing", fileName);
            notepad.ctrlSSave();
            notepad.closeNotepad(fileName);
            syncWaitTime(CLIENTSYNCTIME);
            explorer.deleteFile(fileName, true);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
        }
    }

    /**
     * Test case to Delete a folder created in the previous test case.
     */
    @Test(dependsOnMethods = "testToCreateFolderInShare")
    public void testToDeleteFolder()
    {
        try
        {
            explorer.openFolder(syncLocation);
            explorer.deleteFolder(shareCreatedFolder, true);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, shareCreatedFolder));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
        }
    }

    /**
     * In share delete a folder with File
     */
    @Test
    public void testDeleteFolderWithFileInShare()
    {
        String folderName = share.getFileName(share.getTestName()).toLowerCase();
        String fileName = share.getFileName(share.getTestName() + FILEEXT).toLowerCase();
        try
        {
            File file = ShareUtil.newFile(fileName, fileName);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderName, folderName, folderName);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            share.uploadFile(drone, file);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + folderName + File.separator + fileName));
            share.deleteContentInDocLib(drone, folderName);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(explorer.isFilePresent(syncLocation + File.separator + folderName + File.separator + fileName));

        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
        }
    }

    /**
     * Delete file with folder in client
     */
    @Test
    public void testDeleteFolderWithFileInClient()
    {
        String folderName = share.getFileName(share.getTestName()).toLowerCase();
        String fileName = share.getFileName(share.getTestName()).toLowerCase();
        try
        {
            explorer.openWindowsExplorer();
            logger.info("open specific folder in explorer");
            explorer.openFolder(syncLocation);
            explorer.createandOpenFolder(folderName);
            explorer.rightClickCreate(folderName, fileName, Application.TEXTFILE);
            explorer.openFile(fileName, syncLocation + File.pathSeparator + folderName);
            notepad.editNotepad("sync client testing", fileName);
            notepad.ctrlSSave();
            notepad.closeNotepad(fileName);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
            ldtpObject.setOnWindow(folderName);
            explorer.deleteFolder(folderName, true);
            syncWaitTime(CLIENTSYNCTIME);
            Assert.assertFalse(share.isFileVisible(drone, fileName + FILEEXT));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            ldtpObject.setOnWindow(folderName);
            explorer.closeExplorer();
        }
    }

    /**
     * 
     */

}
