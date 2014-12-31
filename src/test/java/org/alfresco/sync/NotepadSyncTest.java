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
    public void createAFileInClient()
    {
        logger.info("test to create file in client started");
        String fileName = "clientfile";
        try
        {
            // String fileName = share.getFileName(share.getTestName()).toLowerCase();
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
    public void createAFolderAndFileInShare()
    {
        logger.info("test to create a folder with file in share started");
        String name = "sharefolderandfile";
        try
        {
            // String name = share.getFileName(share.getTestName()).toLowerCase();
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
    public void createFolderInClient()
    {
        logger.info("test to create a folder and then sub folder in client");
        String folderName = "clientfolder";
        String subFolderName = "clientsubfolder";
        try
        {
            // folderName = share.getFileName(share.getTestName()).toLowerCase();
            // subFolderName = (folderName + "clientsub").toLowerCase();
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
    public void addFileInsideTheFolderCreatedInClient()
    {
        logger.info("test to create a FILE inside the sub folder created in previous testcase ");
        // String fileName = share.getFileName(share.getTestName()).toLowerCase();
        String fileName = "clientfileinsidefolder";
        String syncPath = (syncLocation + File.separator + clientCreatedFolder + fileName + FILEEXT).toLowerCase();
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
    public void createFolderInShare()
    {

        // String shareCreatedFolder = share.getFileName(share.getTestName()).toLowerCase();
        String shareCreatedFolder = "sharefolder";
        // String subFolderName = "sub_" + shareCreatedFolder;
        String subFolderName = "sharesubfolder";
        try
        {
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, shareCreatedFolder, shareCreatedFolder, shareCreatedFolder);
            share.selectContent(drone, shareCreatedFolder);
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
     * Step1 - Open notepad application
     * Step2 - Save the notepad to synclocation
     * Step3 - Add a new line of text "first create in client" to the notepad
     * Step4 - DO a ctrl S and save the file
     * Step5 - Wait for the sync time of 2 mins in case of client
     * Step6 - login to share
     * Step7 - Open site DocumentLibrary
     * Step8 - Validate whether the newly created File is Visible
     * Step9 - Check the version number is also set to 1.1
     * Step10 - Now access the client created notepad file and append the new line of text
     * "adding another line of text"
     * Step11 - Save the file to sync location using Ctrl S
     * Step12 - Wait for the sync time of 2 mins in case of client
     * Step13 - In share validate the version number is increased correctly
     * Step14 - In share Download the file
     * Step15 - Compare the two files between client and Share to see whether they are same
     * Step16 - Now Close the Notepad without any edit
     * Step17 - Wait for the sync time of 2 mins in case of client
     * Step18 - Validate whether the file is same and has the same version number
     */
    @Test
    public void updateFileInClient()
    {
        // String fileName = share.getFileName(share.getTestName()).toLowerCase();
        String fileName = "clientcreateandupdate";
        String clientLocation = syncLocation + File.separator + fileName + FILEEXT;
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
            Assert.assertEquals(share.getDocLibVersionInfo(drone, fileName + FILEEXT), "1.1");
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
            notepad.appendTextToNotepad("adding another line of text", fileName);
            notepad.ctrlSSave();
            syncWaitTime(CLIENTSYNCTIME);
            Assert.assertEquals(share.getDocLibVersionInfo(drone, fileName + FILEEXT), "1.2");
            share.shareDownloadFileFromDocLib(drone, fileName + FILEEXT, shareLocation);
            Assert.assertTrue(compareTwoFiles(clientLocation, shareLocation));
            notepad.closeNotepad(fileName);
            syncWaitTime(CLIENTSYNCTIME);
            Assert.assertEquals(share.getDocLibVersionInfo(drone, fileName + FILEEXT), "1.2");
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
     * Test to check whether the share new version are getting synced correctly
     * Step1 - login into Share
     * Step2 - Open site Document Library
     * Step3 - Create a content using create plain text menu
     * Step4 - Wait for the file to synced to client - which is 5 mins in case of Share
     * Step5 - Validate in client whether the file is visible
     * Step6 - Now in share upload a new version with the content as test sync update
     * Download a copy of the share file location c:\DownloadAlfresco
     * Step7 - Wait for the file to synced to client - which is 5 mins in case of Share
     * Step8 - Compare the two files to see whether the same
     */
    @Test
    public void updateFileInShare()
    {
        // String fileName = share.getFileName(share.getTestName() +FILEEXT).toLowerCase();
        String fileName = "sharecreateandupdate" + FILEEXT;
        ContentDetails content = new ContentDetails();
        content.setName(fileName);
        content.setDescription(fileName);
        content.setTitle(fileName);
        content.setContent("share created file");
        String clientLocation = syncLocation + File.separator + fileName;
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
     * Test case to delete a file just created
     * Step1 - Open Windows explorer and navigate to the synclocation
     * Step2 - open notepad application
     * Step3 - Add a new line and Save the file to the sync location
     * step4 - close notepad
     * step5 - Wait for the sync time - 2 mins as it is client sync
     * Step6 - in the explorer window delete the file
     * Step7 - Say yes in the delete confirmation dialog
     * step8 - Wait for the sync time - 2 mins as it is client sync
     * Step9 - login in share
     * step10 - validate the file is not visible in document library
     */
    @Test
    public void deleteFileJustCreated()
    {
        // String fileName = share.getFileName(share.getTestName() + "10").toLowerCase();
        String fileName = "clientdelete";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            notepad.openNotepadApplication();
            notepad.setNotepadWindow("Notepad");
            notepad.saveAsNotpad(syncLocation, fileName);
            notepad.editNotepad("desktop Automated Testing", fileName);
            notepad.ctrlSSave();
            notepad.closeNotepad(fileName);
            syncWaitTime(CLIENTSYNCTIME);
            explorer.activateApplicationWindow(siteName);
            explorer.deleteFile(fileName, true);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
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
            explorer.closeExplorer();
        }
    }

    /**
     * Test case to delete a folder which is already created by share in previous test case
     * testToCreateFolderInShare
     * Step1 - Open Windows explorer and navigate to the synclocation
     * Step2 - open notepad application
     * Step3 - delete the folder created in the previous test case
     * step4 - Wait for the sync time - 2 mins as it is client sync
     * Step5 - login in share
     * step6 - validate the folder is not visible in document library
     */
    @Test(dependsOnMethods = "createFolderInShare")
    public void deleteFolder()
    {
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.deleteFolder(shareCreatedFolder, true);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone, shareCreatedFolder));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(syncLocation);
            explorer.closeExplorer();
        }
    }

    /**
     * In share delete a folder with File which is synced
     * Step1 - create a new file
     * Step2 - login in to share
     * Step3 - open site document library
     * Step4 - create a folder
     * Step5 - Upload the created file inside the folder created in step4
     * Step6 - Wait for sync time - which is 5 mins in case of share
     * Step7 - Validate in the explorer that file is visible
     * Step8 - Now in share delete the folder
     * Step9 - Wait for sync time - which is 5 mins in case of share
     * Step10 - Validate that folder is not visible in sync location
     */
    @Test
    public void deleteFolderWithFileInShare()
    {
        // String folderName = share.getFileName(share.getTestName()).toLowerCase();
        // String fileName = share.getFileName(share.getTestName() + FILEEXT).toLowerCase();
        String folderName = "sharefolderdelete";
        String fileName = "sharefolderfiledelete" + FILEEXT;
        try
        {
            File file = ShareUtil.newFile(fileName, fileName);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderName, folderName, folderName);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            share.uploadFile(drone, file);
            share.navigateToDocuemntLibrary(drone, siteName);
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
     * Step1  - Open windows explorer and access the sync location
     * Step2  - Create a new folder and open the created the folder
     * Step3  - Inside the folder create a text file 
     * Step4  - Open the file and add a new line 
     * Step5  - Save the notepad 
     * Step6  - Wait for the sync - in this it is client sync time
     * Step7  - login in to share 
     * step9  - Open site dashboard and navigate to the folder created in client
     * step10 - Check whether the file is present
     * Step11 - Now in client navigate back to sync set 
     * Step12 - Right click on the folder and click on delete 
     * Step13 - Wait for the sync time - client sync time
     * Step14 - Check in share the folder is deleted and file is also deleted.
     */
    @Test
    public void deleteFolderWithFileInClient()
    {
        // String folderName = share.getFileName(share.getTestName()+ "3").toLowerCase();
        // String fileName = share.getFileName(share.getTestName()).toLowerCase();
        String folderName = "clientdeletefolder";
        String fileName = "clientdeletefile";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.createandOpenFolder(folderName);
            explorer.rightClickCreate(folderName, fileName, Application.TEXTFILE);
            explorer.oepnFileInCurrentFolder(fileName);
            notepad.editNotepad("sync client testing", fileName);
            notepad.ctrlSSave();
            notepad.closeNotepad(fileName);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
            explorer.activateApplicationWindow(folderName);
            explorer.backButtonInExplorer(siteName);
            explorer.activateApplicationWindow(siteName);
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
            explorer.activateApplicationWindow(siteName);
            explorer.closeExplorer();
        }
    }

    /**
     * Move files with subscription - This test case depends on the file created in the createAFileInclient test case
     * Step1  - Open windows explorer 
     * Step2  - Access the sync location
     * Step3  - Create a new folder 
     * Step4  - right click on  file created in the createAFileInClient(clientfile.txt) 
     * Step6  - select Cut 
     * step7  - open the folder and paste it 
     * Step8  - Now hit back button on the explorer and validate that file is not visible there 
     * Step9  - Wait for the sync time - Client time
     * Step10 - Login in share , access the site dashboard 
     * Step11 - Validate the file is not visible in the document library.
     * 
     */
    @Test(dependsOnMethods = "createAFileInClient")
    public void moveFolderWithInSubInClient()
    {
        String fileName = "clientfile" + FILEEXT;
        String folderName = "foldertomove";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.createNewFolderMenu(folderName);
            explorer.moveFileInCurrent(fileName, siteName, folderName);
            explorer.backButtonInExplorer(siteName);
            Assert.assertFalse(explorer.isFilePresent(fileName));
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone, fileName));
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            Assert.assertTrue(share.isFileVisible(drone, fileName));

        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(folderName);
            explorer.closeExplorer();
        }
    }

    /**
     * Move folder with File in Subscription - tested 
     * Step1  - open window explorer and open sync folder 
     * Step2  - Create two folders (one folder to move inside another)
     * Step3  - One the first folder and create a file 
     * Step4  - Add a line of text inside the file
     * Step5  - Close the notepad 
     * Step6  - Wait for it sync - Client wait time 
     * Step7  - Hit back button in the explorer 
     * Step8  - Move the folder with file inside the "movefolderClient"
     * Step9  - Validate whether the move is successful in share 
     */
    @Test
    public void moveFolderwithFileSubInClient()
    {
        String folderToMove = "movefolderclient";
        String fileName = "fileclient" ;
        String currentFolder = "folderclient1";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.createNewFolderMenu(folderToMove);
            explorer.rightClickCreate(siteName , currentFolder, Application.FOLDER);
            explorer.openFolderFromCurrent(currentFolder);
            explorer.rightClickCreate(currentFolder, fileName, Application.TEXTFILE);
            explorer.oepnFileInCurrentFolder(fileName);
            notepad.setNotepadWindow(fileName);
            notepad.editNotepad("desktop sync testing", fileName);
            notepad.ctrlSSave();
            notepad.closeNotepad(fileName);
            syncWaitTime(CLIENTSYNCTIME);
            explorer.activateApplicationWindow(currentFolder);
            explorer.backButtonInExplorer(siteName);
            explorer.moveFolderInCurrent(currentFolder, siteName, folderToMove);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderToMove);
            Assert.assertTrue(share.isFileVisible(drone, currentFolder));
            share.selectContent(drone, currentFolder);
            Assert.assertTrue(share.isFileVisible(drone, fileName));
        }
        catch (Throwable e)
        {
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(folderToMove);
            explorer.closeExplorer();
        }
    }

    /**
     * Move folder in share to a different site which is out of subscription
     * Step1  - Login in share 
     * Step2  - Create a new site (movesite)
     * Step3  - Open the sync site 
     * Step4  - Create a folder and a file inside the folder 
     * Step5  - Move the folder to moveSite
     * Step6  - Wait for the sync time of 5 mins 
     * Step7  - In client check whether the folder is not synced 
     * 
     */
    @Test
    public void moveFolderInShareOutOfSubscription()
    {
        String siteNameToMove = "moveSite";
        String folderName = "sharemovefolder";
        String fileName = "sharemovefile" + FILEEXT;
        ContentDetails content = new ContentDetails();
        content.setName(fileName);
        content.setDescription(fileName);
        content.setTitle(fileName);
        content.setContent("share created file");
        try
        {
            share.loginToShare(drone, userInfo, shareUrl);
         //   share.createSite(drone, siteNameToMove, "movesite", "public");
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderName, folderName, folderName);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            share.createContent(drone, content, ContentType.PLAINTEXT);
            share.navigateToDocuemntLibrary(drone, siteName);
            share.copyOrMoveArtifact(drone, "All Sites", siteNameToMove, null, folderName, "Move");
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(explorer.isFolderPresent(syncLocation + File.separator + folderName));
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
     * Move folder in share to a different site which is out of subscription - Tested
     */
    @Test
    public void moveFolderInShareWithInSubscription()
    {
        String folderName = "sharemovefolder";
        String fileName = "sharemovefile" + FILEEXT;
        String folderToMove = "sharefoldertomove";
        ContentDetails content = new ContentDetails();
        content.setName(fileName);
        content.setDescription(fileName);
        content.setTitle(fileName);
        content.setContent("share created file");
        try
        {
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderName, folderName, folderName);
            share.createFolder(drone, folderToMove, folderToMove, folderToMove);
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderName);
            share.createContent(drone, content, ContentType.XML);
            share.navigateToDocuemntLibrary(drone, siteName);
            syncWaitTime(SERVERSYNCTIME);
            share.copyOrMoveArtifact(drone, "All Sites", siteName, folderToMove, folderName, "Move");
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(explorer.isFolderPresent(syncLocation + File.separator + folderName));
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + folderToMove + File.separator +folderName + File.separator + fileName));
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
     * Move file inside a empty folder within the subscription in client - Tested
     */
    @Test
    public void moveFileInsideEmptyFolderInClient()
    {
        String fileName = "movefileemptyclient";
        String folderNameToMove = "moveemptyfolderclient";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.rightClickCreate(siteName, folderNameToMove, Application.FOLDER);
            explorer.rightClickCreate(siteName, fileName, Application.TEXTFILE);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
            explorer.activateApplicationWindow(siteName);
            explorer.moveFileInCurrent(fileName, siteName, folderNameToMove);
            syncWaitTime(CLIENTSYNCTIME);
            share.navigateToDocuemntLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone, fileName + FILEEXT));
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderNameToMove);
            Assert.assertTrue(share.isFileVisible(drone,  fileName + FILEEXT));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(folderNameToMove);
            explorer.closeExplorer();
        }
    }

    /**
     * Create and move immediately - tested
     */
    @Test
    public void moveFileInsideFolderInClient()
    {
        String fileName = "createmovefileclient";
        String folderNameToMove = "createmovefolderclient";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.createNewFolderMenu(folderNameToMove);
            explorer.rightClickCreate(siteName, fileName, Application.TEXTFILE);
            explorer.moveFileInCurrent(fileName, siteName, folderNameToMove);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone,fileName + FILEEXT));
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + folderNameToMove);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
        }
        catch (Throwable e)
        {
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(folderNameToMove);
            explorer.closeExplorer();
        }
    }
    
    /**
     * Move File out of the subscription - tested
     */
    @Test
    public void moveFileOutOfSubInClient()
    {
        String fileToMove = "filetomove2";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder("c:\\test");
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.rightClickCreate(siteName, fileToMove, Application.TEXTFILE);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone,fileToMove + FILEEXT));
            String windowName = ldtpObject.findWindowName(siteName);
            explorer.activateApplicationWindow(windowName);
            explorer.moveFileBetweenFolders(siteName, "test", fileToMove);
            syncWaitTime(CLIENTSYNCTIME);
            share.navigateToDocuemntLibrary(drone, siteName);
            Assert.assertFalse(share.isFileVisible(drone,  fileToMove + FILEEXT));
        }
        catch (Throwable e)
        {
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow(siteName);
            explorer.closeExplorer();
            explorer.activateApplicationWindow("test");
            explorer.closeExplorer();
        }
    }
    
    
    /**
     * Move a Folder with File out of Subscription  - tested
     */
    @Test
    public void moveFileIntoSubClient()
    {
      String fileName = "movefileintosub";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.openWindowsExplorer();
            explorer.openFolder("c:\\samplefile");
            String windowName = ldtpObject.findWindowName("samplefile");
            explorer.activateApplicationWindow(windowName);
            explorer.moveFileBetweenFolders("samplefile",siteName, fileName);
            Thread.sleep(3000);
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + fileName + FILEEXT));
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, fileName + FILEEXT));
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            throw new SkipException("test case failed " + share.getTestName(), e);
        }
        finally
        {
            share.logout(drone);
            explorer.activateApplicationWindow("samplefile");
            explorer.closeExplorer();
            explorer.activateApplicationWindow(siteName);
            explorer.closeExplorer();
        }
    }
    
    /**
     * Rename of file in client
     */
    @Test
    public void renameOfFileInClient()
    {
        String fileName = "renamefileclient";
        String newName = "fileclientrename";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.rightClickCreate(syncLocation, fileName, Application.TEXTFILE);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, ShareUtil.DOCLIB + File.separator + fileName + FILEEXT));
            explorer.renameFile(fileName + FILEEXT, newName);
            syncWaitTime(CLIENTSYNCTIME);
            Assert.assertTrue(share.isFileVisible(drone, ShareUtil.DOCLIB + File.separator + newName + FILEEXT));
        }
        catch(Throwable e)
        {
            throw new SkipException("test case failed " + share.getTestName(), e); 
        }
        finally
        {
            share.logout(drone);
            explorer.closeExplorer();
        }
    }
    
    /**
     * rename a folder in client 
     */
    @Test
    public void renameFolderInClient()
    {
        String folderName = "folderclientrename";
        String fileName = "fileclientrename";
        String rename = "renameclientfolder";
        try
        {
            explorer.openWindowsExplorer();
            explorer.openFolder(syncLocation);
            explorer.createNewFolderMenu(folderName);
            explorer.openFolderFromCurrent(folderName);
            explorer.rightClickCreate(folderName, fileName, Application.TEXTFILE);
            explorer.backButtonInExplorer(siteName);
            syncWaitTime(CLIENTSYNCTIME);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            Assert.assertTrue(share.isFileVisible(drone, ShareUtil.DOCLIB + File.separator + folderName));
            explorer.renameFolder(folderName, "rename");
            syncWaitTime(CLIENTSYNCTIME);
            Assert.assertTrue((share.isFileVisible(drone, ShareUtil.DOCLIB + File.separator + rename)));
            share.navigateToFolder(drone, ShareUtil.DOCLIB + File.separator + rename);
            Assert.assertTrue((share.isFileVisible(drone, ShareUtil.DOCLIB + File.separator + fileName)));
        }
        catch (Throwable e)
        {
            throw new SkipException("test case failed " + share.getTestName(), e); 
        }
        finally
        {
            share.logout(drone);
            explorer.closeExplorer();
        }
    }
    /**
     * Rename of file in share 
     */
    @Test
    public void renameFileInShare()
    {
      String fileName = "filesharerename" + FILEEXT;  
      String rename = "renamesharefile" + FILEEXT;
        try
        {
            File file = ShareUtil.newFile(fileName, fileName);
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.uploadFile(drone, file);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + fileName));
            share.editContentNameInline(drone, fileName, rename, true);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + rename));
        }
        catch(Throwable e)
        {
            throw new SkipException("test case failed " + share.getTestName(), e); 
        }
        finally
        {
            share.logout(drone);
            explorer.closeExplorer();
        }
    }
    /**
     * Rename of folder in share 
     */
    @Test
    public void renameFolderInShare()
    {
      String folderName = "foldersharerename" ; 
      String rename = "renamesharefolder" ;
        try
        {
            share.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.createFolder(drone, folderName, folderName, folderName);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + folderName));
            share.editContentNameInline(drone, folderName, rename, true);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(explorer.isFilePresent(syncLocation + File.separator + rename));
        }
        catch(Throwable e)
        {
            throw new SkipException("test case failed " + share.getTestName(), e); 
        }
        finally
        {
            share.logout(drone);
            explorer.closeExplorer();
        }
    }

}
