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

/**
 * DesktopSync properties
 * 
 * @author Subashni Prasanna
 * @author Paul Brodner
 * @since 1.0
 */
public class DesktopSyncProperties
{
    private final String shareUrl;
    private final String username;
    private final String password;
    private final String googleUserName;
    private final String googlePassword;
    private final String location;
    private final String siteName;
    protected long popupRendertime;
    private String filedirectoryPath;
    private final String installerpath;
    private final String grid;
    private final String browser;
    private final String version;
    private final String mimeTypes;
    private final boolean syncImmediately;
    
    public DesktopSyncProperties(
            final String shareUrl, 
            final String username, 
            final String password, 
            final String googleUserName, 
            final String googlePassword, 
            final String location, 
            final String sitename, 
            final String filedirectoryPath, 
            final String installerpath,
            final String grid,
            final String browser,
            final String version,
            final String mimeTypes,
            final boolean syncImmediately)
    {
        this.shareUrl = shareUrl;
        this.username = username;
        this.password = password;
        this.googleUserName = googleUserName;
        this.googlePassword = googlePassword;
        this.location = location;
        this.siteName = sitename;
        this.filedirectoryPath = filedirectoryPath;
        this.installerpath = installerpath;
        this.grid = grid;
        this.browser = browser;
        this.version = version;
        this.mimeTypes = mimeTypes;
        this.syncImmediately = syncImmediately;
    }

    public String getGrid()
    {
        return grid;
    }

    public String getBrowser()
    {
        return browser;
    }

    public String getVersion()
    {
        return version;
    }

    public String getInstallerpath()
    {
        return installerpath;
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

    public String getFiledirectoryPath()
    {
        return filedirectoryPath;
    }

    public String getMimeTypes()
    {
        return mimeTypes;
    }

    public boolean getSyncImmediately()
    {
        return syncImmediately;
    }
}
