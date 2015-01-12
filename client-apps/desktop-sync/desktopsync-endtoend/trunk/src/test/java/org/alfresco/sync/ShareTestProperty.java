/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.sync;


/**
 * Properties used for test cases.
 * 
 * @author Subashni Prasanna
 * @since 1.1
 */
public class ShareTestProperty
{
    private final String shareUrl;
    private final String username;
    private final String password;
    private final String googleUserName;
    private final String googlePassword;
    private final String location;
    private final String siteName;
    private final String officeVersion;
    private final String officePath2010;
    private final String officePath2013;
    private final String fileAppend;
    protected long popupRendertime;
    private  String filedirectoryPath;
    
    public ShareTestProperty (final String shareUrl,
                              final String username,
                              final String password,
                              final String googleUserName,
                              final String googlePassword,
                              final String location,
                              final String sitename,
                              final String officeVersion,
                              final String office2010,   
                              final String office2013,
                              final String filedirectoryPath,
                              final String fileappend
                             ) 
    {
        this.shareUrl = shareUrl;
        this.username = username;
        this.password = password;
        this.googleUserName = googleUserName;
        this.googlePassword = googlePassword;
        this.location = location;
        this.siteName = sitename;
        this.officeVersion = officeVersion;
        this.officePath2010 = office2010;
        this.officePath2013 = office2013;
        this.filedirectoryPath = filedirectoryPath;
        this.fileAppend = fileappend;
        
    }


    public String getFileAppend()
    {
        return fileAppend;
    }


    public String getShareUrl()
    {
        return shareUrl;
    }
    
    
    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getGoogleUserName()
    {
        return googleUserName;
    }
    
    public String getGooglePassword()
    {
        return googlePassword;
    }

    public String getLocation()
    {
        return location;
    }
    public String getSiteName()
    {
        return siteName;
    }
    public String getOfficeVersion()
    {
        return officeVersion;
    }
    
    public String getOfficePath()
    {
        String officePath = "";
        if(officeVersion.equals(2010))
        {
            officePath =  officePath2010;
         }
        else 
        {
            officePath = officePath2013;
        }
        
        return officePath;
    }
    
    public String getFiledirectoryPath()
    {
        return filedirectoryPath;
    }


    public void setFiledirectoryPath(String filedirectoryPath)
    {
        this.filedirectoryPath = filedirectoryPath;
    }

}
