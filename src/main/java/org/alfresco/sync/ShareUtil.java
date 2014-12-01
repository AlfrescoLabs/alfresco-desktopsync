/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.alfresco.po.share.DashBoardPage;
import org.alfresco.po.share.LoginPage;
import org.alfresco.po.share.RepositoryPage;
import org.alfresco.po.share.SharePage;
import org.alfresco.po.share.exception.ShareException;
import org.alfresco.po.share.site.CreateSitePage;
import org.alfresco.po.share.site.NewFolderPage;
import org.alfresco.po.share.site.SiteDashboardPage;
import org.alfresco.po.share.site.SitePage;
import org.alfresco.po.share.site.UploadFilePage;
import org.alfresco.po.share.site.document.ContentDetails;
import org.alfresco.po.share.site.document.ContentType;
import org.alfresco.po.share.site.document.CreatePlainTextContentPage;
import org.alfresco.po.share.site.document.DocumentDetailsPage;
import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.po.share.site.document.FileDirectoryInfo;
import org.alfresco.webdrone.HtmlPage;
import org.alfresco.webdrone.WebDrone;
import org.alfresco.webdrone.exception.PageException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.NoSuchElementException;
import org.testng.SkipException;

/**
 * Util methods for all the share activities
 * 
 * @author sprasanna
 */

public class ShareUtil
{
    private static Log logger = LogFactory.getLog(ShareUtil.class);
    public static long refreshDuration = 25000;
    final static String SITE_VISIBILITY_PUBLIC = "public";
    protected static final String SITE_VISIBILITY_PRIVATE = "private";
    protected static final String SITE_VISIBILITY_MODERATED = "moderated";
    public final static String DOCLIB = "DocumentLibrary";
    protected static final String UNIQUE_TESTDATA_STRING = "sync";
    private static final String SITE_DASH_LOCATION_SUFFIX = "/page/site/";

    /**
     * User Log-in followed by deletion of session cookies Assumes User is *NOT* logged in.
     *
     * @param drone WebDrone Instance
     * @param userInfo String username, password
     * @return boolean true: if log in succeeds
     */
    public synchronized SharePage loginToShare(WebDrone drone, String[] userInfo, String shareUrl)
    {
        LoginPage loginPage;
        SharePage sharePage;
        try
        {
            if ((userInfo.length < 2))
            {
                throw new Exception("Invalid login details");
            }

            checkIfdroneNull(drone);

            drone.navigateTo(shareUrl);

            sharePage = getSharePage(drone);
            // Logout if already logged in
            try
            {
                loginPage = sharePage.render();
            }
            catch (ClassCastException e)
            {
                loginPage = logout(drone).render();
            }

            logger.info("Start: Login: " + userInfo[0] + " Password: " + userInfo[1]);

            loginPage.loginAs(userInfo[0], userInfo[1]);
            sharePage = drone.getCurrentPage().render();

            if (!sharePage.isLoggedIn())
            {
                throw new ShareException("Method isLoggedIn return false");
            }
        }
        catch (Exception e)
        {
            String errorMessage = "Failed: Login: " + userInfo[0] + " Password: " + userInfo[1] + " Error: " + e;
            logger.info(errorMessage);
            throw new SkipException(errorMessage);
        }

        return sharePage;
    }

    /**
     * Checks if drone is null, throws UnsupportedOperationException if so.
     *
     * @param drone WebDrone Instance
     * @throws UnsupportedOperationException if drone is null
     */
    protected static void checkIfdroneNull(WebDrone drone)
    {
        if (drone == null)
        {
            throw new UnsupportedOperationException("WebDrone is required");
        }
    }

    /**
     * Checks if the current page is share page, throws PageException if not.
     *
     * @param drone WebDrone Instance
     * @return SharePage
     * @throws PageException if the current page is not a share page
     */
    public static SharePage getSharePage(WebDrone drone)
    {
        checkIfdroneNull(drone);
        try
        {
            HtmlPage generalPage = drone.getCurrentPage().render(refreshDuration);
            return (SharePage) generalPage;
        }
        catch (PageException pe)
        {
            throw new PageException("Can not cast to SharePage: Current URL: " + drone.getCurrentUrl());
        }
    }

    /**
     * User Log out using logout URL Assumes User is logged in.
     *
     * @param drone WebDrone Instance
     */
    public synchronized HtmlPage logout(WebDrone drone)
    {
        HtmlPage currentPage = null;
        checkIfdroneNull(drone);
        try
        {
            SharePage page = drone.getCurrentPage().render();
            page.getNav().logout();
        }
        catch (Exception e)
        {
            // Already logged out.
            logger.info("already logged out" + e.getMessage());
        }
        return currentPage;
    }

    /**
     * Create site
     */
    public static boolean createSite(WebDrone drone, final String siteName, String desc, String siteVisibility)
    {
        if (siteName == null || siteName.isEmpty())
        {
            throw new IllegalArgumentException("site name is required");
        }
        boolean siteCreated = false;
        DashBoardPage dashBoard;
        SiteDashboardPage site = null;
        try
        {
            SharePage page = drone.getCurrentPage().render();
            dashBoard = page.getNav().selectMyDashBoard().render();
            CreateSitePage createSite = dashBoard.getNav().selectCreateSite().render();
            if (siteVisibility == null)
            {
                siteVisibility = SITE_VISIBILITY_PUBLIC;
            }
            if (siteVisibility.equalsIgnoreCase(SITE_VISIBILITY_MODERATED))
            {
                site = createSite.createModerateSite(siteName, desc).render();
            }
            else if (siteVisibility.equalsIgnoreCase(SITE_VISIBILITY_PRIVATE))
            {
                site = createSite.createPrivateSite(siteName, desc).render();
            }
            // Will create public site
            else
            {
                site = createSite.createNewSite(siteName, desc).render();
            }

            site.render();

            if (siteName.equalsIgnoreCase(site.getPageTitle()))
            {
                siteCreated = true;
            }
            return siteCreated;
        }
        catch (UnsupportedOperationException une)
        {
            String msg = String.format("Failed to create a new site %n Site Name: %s", siteName);
            throw new RuntimeException(msg, une);
        }
        catch (NoSuchElementException nse)
        {
            return false;
        }
    }

    /**
     * Creates a new folder at the Path specified, Starting from the Document Library Page.
     * Assumes User is logged in and a specific Site is open.
     * 
     * @param drone WebDrone Instance
     * @param folderName String Name of the folder to be created
     * @param folderTitle String Title of the folder to be created
     * @param folderDesc String Description of the folder to be created
     * @return DocumentLibraryPage
     */
    public  DocumentLibraryPage createFolder(WebDrone drone, String folderName, String folderTitle, String folderDesc)
    {
        DocumentLibraryPage docPage = null;

        // Open Document Library
        SharePage thisPage =getSharePage(drone);

        if (!(thisPage instanceof RepositoryPage) && (!(thisPage instanceof DocumentLibraryPage)))
        {
            docPage = openDocumentLibrary(drone);
        }
        else
        {
            docPage = (DocumentLibraryPage) thisPage;
        }

        NewFolderPage newFolderPage = docPage.getNavigation().selectCreateNewFolder().render();
        docPage = newFolderPage.createNewFolder(folderName, folderTitle, folderDesc).render();

        logger.info("Folder Created" + folderName);
        return docPage;
    }

    /**
     * Open document Library: Top Level Assumes User is logged in and a Specific
     * Site is open.
     *
     * @param drone WebDrone Instance
     * @return DocumentLibraryPage
     */
    public static DocumentLibraryPage openDocumentLibrary(WebDrone drone)
    {
        // Assumes User is logged in
        /*
         * SharePage page = getSharePage(drone); if (page instanceof
         * DocumentLibraryPage) { return (DocumentLibraryPage) page; }
         */

        // Open DocumentLibrary Page from Site Page
        SitePage site = (SitePage) getSharePage(drone);

        DocumentLibraryPage docPage = site.getSiteNav().selectSiteDocumentLibrary().render();
        logger.info("Opened Document Library");
        return docPage;
    }
    
    /**
     * Assumes a specific Site is open Opens the Document Library Page and navigates to the Path specified.
     * 
     * @param drone WebDrone Instance
     * @param folderPath: String folder path relative to DocumentLibrary e.g. DOCLIB + file.seperator + folderName1
     * @throws SkipException if error in this API
     */
    public DocumentLibraryPage navigateToFolder(WebDrone drone, String folderPath) throws Exception
    {
        DocumentLibraryPage docPage;

        try
        {
            if (folderPath == null)
            {
                throw new UnsupportedOperationException("Incorrect FolderPath: Null");
            }

           
                docPage = openDocumentLibrary(drone);

            // Resolve folderPath, considering diff treatment for non-windows OS
            logger.info(folderPath);
            String[] path = folderPath.split(Pattern.quote(File.separator));

            // Navigate to the parent Folder where the file needs to be uploaded
            for (int i = 0; i < path.length; i++)
            {
                if (path[i].isEmpty())
                {
                    // Ignore, Continue to the next;
                    logger.debug("Empty Folder Path specified: " + path.toString());
                }
                else
                {
                    if ((i == 0) && (path[i].equalsIgnoreCase(ShareUtil.DOCLIB)))
                    {
                        // Repo or Doclib is already open
                        logger.info("Base Folder: " + path[i]);
                    }
                    else
                    {
                        logger.info("Navigating to Folder: " + path[i]);
                        docPage = selectContent(drone, path[i]).render();
                    }
                }
            }
            logger.info("Selected Folder:" + folderPath);
        }
        catch (Exception e)
        {
            throw new SkipException("Skip test. Error in navigateToFolder: " + e.getMessage());
        }

        return docPage;
    }

    /**
     * Util traverses through all the pages of the doclib to find the content within the folder and clicks on the contentTile
     * @param drone
     * @param contentName
     * @return
     */
    public static HtmlPage selectContent(WebDrone drone, String contentName)
    {
        return getFileDirectoryInfo(drone, contentName).clickOnTitle().render();
    }
    
    /**
     * Util traverses through all the pages of the doclib to find the content within the folder
     * @param drone
     * @param contentName
     * @return
     */
    public static FileDirectoryInfo getFileDirectoryInfo(WebDrone drone, String contentName)
    {
        Boolean moreResultPages = true;
        FileDirectoryInfo contentRow = null;
        DocumentLibraryPage docLibPage = getSharePage(drone).render();

        // Start from first page
        while (docLibPage.hasPreviousPage())
        {
            docLibPage = docLibPage.selectPreviousPage().render();
        }

        while (moreResultPages)
        {
            // Get Search Results
            try
            {
                contentRow = docLibPage.getFileDirectoryInfo(contentName);
                break;
            }
            catch (PageException pe)
            {
                // Check next Page if available
                moreResultPages = docLibPage.hasNextPage();

                if (moreResultPages)
                {
                    docLibPage = docLibPage.selectNextPage().render();
                }
            }
        }
        
        // Now return the content found else throw PageException
        if (contentRow == null)
        {
            throw new PageException(String.format("File directory info with title %s was not found in the selected folder", contentName));
        }
        
        return contentRow;
    }
    /**
     * Creates a new folder at the Path specified, Starting from the Document
     * Library Page. Assumes User is logged in and a specific Site is open.
     *
     * @param drone           WebDrone Instance
     * @param folderName       String Name of the folder to be created
     * @param folderDesc       String Description of the folder to be created
     * @param parentFolderPath String Path for the folder to be created, under
     *                         DocumentLibrary : such as constDoclib + file.seperator +
     *                         parentFolderName1 + file.seperator + parentFolderName2
     * @throws Exception
     */
    public  DocumentLibraryPage createFolderInFolder(WebDrone drone, String folderName, String folderDesc, String folderTitle ,String parentFolderPath) throws Exception
    {
        try
        {
            // Data setup Options: Use UI, Use API, Copy, Data preloaded?

            // Using Share UI
            // Navigate to the parent Folder where the file needs to be uploaded
            navigateToFolder(drone, parentFolderPath);

            // Create Folder
            return createFolder(drone, folderName,folderTitle, folderDesc);
        }
        catch (Exception ex)
        {
            throw new SkipException("Skip test. Error in Create Folder: " + ex.getMessage());
        }
    }

    /**
     * Assumes User is logged in and a specific Site's Doclib is open, Parent Folder is pre-selected.
     * 
     * @param file File Object for the file in reference
     * @return DocumentLibraryPage
     * @throws SkipException if error in this API
     */
    public  HtmlPage uploadFile(WebDrone drone, File file) 
    {
        DocumentLibraryPage docPage;
        try
        {
            checkIfdroneNull(drone);
            docPage = drone.getCurrentPage().render(refreshDuration);
            // Upload File
            UploadFilePage upLoadPage = docPage.getNavigation().selectFileUpload().render();
            docPage = upLoadPage.uploadFile(file.getCanonicalPath()).render();
            docPage.setContentName(file.getName());
            logger.info("File Uploaded:" + file.getCanonicalPath());
        }
        catch (Exception e)
        {
            throw new SkipException("Skip test. Error in UploadFile: " + e);
        }

        return docPage.render();
    }
    
    /**
     * This method is used to create content with name, title and description.
     * User should be logged in and present on site page.
     *
     * @param drone
     * @param contentDetails
     * @param contentType
     * @return {@link DocumentLibraryPage}
     * @throws Exception
     */
    public static DocumentLibraryPage createContent(WebDrone drone, ContentDetails contentDetails, ContentType contentType) throws Exception
    {
        // Open Document Library
        DocumentLibraryPage documentLibPage = openDocumentLibrary(drone);
        DocumentDetailsPage detailsPage = null;

        try
        {
                CreatePlainTextContentPage contentPage = documentLibPage.getNavigation().selectCreateContent(contentType).render();
                detailsPage = contentPage.create(contentDetails).render();
                documentLibPage = (DocumentLibraryPage) detailsPage.getSiteNav().selectSiteDocumentLibrary();
                documentLibPage.render();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
            throw new SkipException("Error in creating content." + e);
        }

        return documentLibPage;
    }


    /**isFileVisible is to check whether file or folder visible..
     * @param drone
     * @param contentName
     * @return
     */
    public  boolean isFileVisible(WebDrone drone, String contentName)
    {
        try
        {
            getFileDirectoryInfo(drone, contentName);
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }
    /**
     * Helper to consistently get the filename.
     *
     * @param partFileName String Part Name of the file for uniquely identifying /
     *                     mapping test data with the test
     * @return String fileName
     */
    protected String getFileName(String partFileName)
    {
        String fileName = "";

        fileName = String.format("File%s-%s", UNIQUE_TESTDATA_STRING, partFileName);

        return fileName;
    }
    
    /**
     * Open Site and then Open Document Library Assumes User is logged in and a
     * Specific Site Dashboard is open.
     *
     * @param driver   WebDrone Instance
     * @param siteName String Name of the Site
     * @return DocumentLibraryPage
     */
    public  DocumentLibraryPage openSitesDocumentLibrary(WebDrone driver, String siteName)
    {
        // Assumes User is logged in

        // Checking for site doc lib to be open.
        HtmlPage page = getSharePage(driver).render();
        if (page instanceof DocumentLibraryPage)
        {
            if (((DocumentLibraryPage) page).isSite(siteName) && ((DocumentLibraryPage) page).isDocumentLibrary())
            {
                logger.info("Site doc lib page open ");
                return ((DocumentLibraryPage) page);
            }
        }

        // Open Site
        openSiteDashboard(driver, siteName);

        // Open DocumentLibrary Page from SiteDashBoard
        DocumentLibraryPage docPage = openDocumentLibrary(driver);

        // Return DocLib Page
        return docPage;
    }
    
    /**
     * From the User DashBoard, navigate to the Site DashBoard and waits for the
     * page render to complete. Assumes User is logged in.
     *
     * @param driver   WebDrone Instance
     * @param siteName String Name of the site to be opened
     * @return SiteDashboardPage
     * @throws PageException
     */
    public static SiteDashboardPage openSiteDashboard(WebDrone driver, String siteName) throws PageException
    {
        // Assumes User is logged in
        HtmlPage page = getSharePage(driver).render();

        // Check if site dashboard is already open. Return
        if (page instanceof SiteDashboardPage)
        {
            if (((SiteDashboardPage) page).isSite(siteName))
            {
                logger.info("Site dashboad page already open for site - " + siteName);
                return page.render();
            }
        }


        //Open User DashBoard: Using SiteURL
         SiteDashboardPage siteDashPage = openSiteURL(driver, getSiteShortname(siteName));

        // Open User DashBoard: Using SiteFinder
        // SiteDashboardPage siteDashPage = SiteUtil.openSiteFromSearch(driver, siteName);

        // logger.info("Opened Site Dashboard using SiteURL: " + siteName);

        return siteDashPage;
    }
    /**
     * Method to navigate to site dashboard url, based on siteshorturl, rather than sitename
     * This is to be used to navigate only as a util, not to test getting to the site dashboard
     * 
     * @param drone
     * @param siteShortURL
     * @return {@link SiteDashBoardPage}
     */
    public static SiteDashboardPage openSiteURL(WebDrone drone, String siteShortURL)
    {
        String url = drone.getCurrentUrl();
        String target = url.substring(0, url.indexOf("/page/")) + SITE_DASH_LOCATION_SUFFIX + getSiteShortname(siteShortURL) + "/dashboard";
        drone.navigateTo(target);
        SiteDashboardPage siteDashboardPage = getSharePage(drone).render();

        return siteDashboardPage.render();
    }
    
    /**
     * Helper to consistently get the Site Short Name.
     *
     * @param siteName String Name of the test for uniquely identifying / mapping
     *                 test data with the test
     * @return String site short name
     */
    public static String getSiteShortname(String siteName)
    {
        String siteShortname = "";
        String[] unallowedCharacters = { "_", "!" };

        for (String removeChar : unallowedCharacters)
        {
            siteShortname = siteName.replace(removeChar, "");
        }

        return siteShortname.toLowerCase();
    }

    /**
     * Helper to create a new file, empty or with specified contents if one does
     * not exist. Logs if File already exists
     *
     * @param filename String Complete path of the file to be created
     * @param contents String Contents for text file
     * @return File
     */
    public File newFile(String filename, String contents)
    {
        File file = new File(filename);

        try
        {
            if (!file.exists())
            {

                if (!contents.isEmpty())
                {
                    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8").newEncoder());
                    writer.write(contents);
                    writer.close();
                }
                else
                {
                    file.createNewFile();
                }
            }
            else
            {
                logger.debug("Filename already exists: " + filename);
            }
        }
        catch (IOException ex)
        {
            logger.error("Unable to create sample file", ex);
        }
        return file;
    }
    /**
     * Helper returns the test / methodname. This needs to be called as the 1st
     * step of the test. Common Test code can later be introduced here.
     *
     * @return String testcaseName
     */
    public  String getTestName()
    {
        String testID = Thread.currentThread().getStackTrace()[2].getMethodName();
        return testID;
    }
    
    /**
     * Util to download a file in a particular path
     */

    public void shareDownloadFile(WebDrone drone , String FileName )
    {
       FileDirectoryInfo fileInfo =  getFileDirectoryInfo(drone, FileName);
       fileInfo.selectDownload();
    }
}

   
