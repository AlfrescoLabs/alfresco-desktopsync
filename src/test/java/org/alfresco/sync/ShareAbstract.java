package org.alfresco.sync;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.webdrone.WebDrone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterSuite;

public class ShareAbstract
{
        private static Log logger = LogFactory.getLog(ShareAbstract.class);
        private static ApplicationContext ctx;
        protected static String shareUrl;
        protected static String password;
        protected static String username;
        protected static String googleusername;
        protected static String googlepassword;
        protected static String location;
        protected static String siteName;
        protected static String officeVersion;
        protected static String officePath;
        protected static String fileAppend;
        protected WebDrone drone;
        protected static long SERVERSYNCTIME = 300000;
        protected static long CLIENTSYNCTIME = 60000;
        protected static String downloadPath;
        public static long maxWaitTime_CloudSync = 50000;


        public void setupContext(String contextFileName) throws Exception
        {
            if(logger.isTraceEnabled())
            {
                logger.trace("Starting test context");
            }

            List<String> contextXMLList = new ArrayList<String>();
            contextXMLList.add("share-po-test-context.xml");
            ctx = new ClassPathXmlApplicationContext(contextXMLList.toArray(new String[contextXMLList.size()]));

            ShareTestProperty t = (ShareTestProperty) ctx.getBean("shareTestProperties");
            shareUrl = t.getShareUrl();
            username = t.getUsername();
            password = t.getPassword();
            googleusername = t.getGoogleUserName();
            googlepassword = t.getGooglePassword();
            location = t.getLocation();
            siteName = t.getSiteName();
            officeVersion = t.getOfficeVersion();
            officePath = t.getOfficePath();
            downloadPath = t.getFiledirectoryPath();
            fileAppend = t.getFileAppend();
            
            if(logger.isTraceEnabled())
            {
                logger.trace("Alfresco shareUrl is" + shareUrl);
            }
        }
        
        public WebDrone getWebDrone() throws Exception
        {
            drone = (WebDrone) ctx.getBean("webDrone");
            drone.maximize();
            return drone;
        }

        @AfterSuite(alwaysRun = true)
        public void closeWebDrone()
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Closing web drone");
            }
            // Close the browser
            if (drone != null)
            {
                drone.quit();
                drone = null;
            }
        }

}
