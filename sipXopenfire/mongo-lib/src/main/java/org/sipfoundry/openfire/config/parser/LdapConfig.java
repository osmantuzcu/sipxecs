/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 */
package org.sipfoundry.openfire.config.parser;

public class LdapConfig {
    private String m_host;
    private int m_port;
    private boolean m_sslEnabled;
    private String m_baseDN;
    private String m_adminDN;
    private String m_adminPassword;
    private String m_usernameField;
    private String m_searchFilter;
    private String m_domain;
    private String m_timeout;

    public String getHost() {
        return m_host;
    }

    public void setHost(String host) {
        m_host = host;
    }

    public int getPort() {
        return m_port;
    }

    public void setPort(int port) {
        m_port = port;
    }

    public boolean isSslEnabled() {
        return m_sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        m_sslEnabled = sslEnabled;
    }

    public String getBaseDN() {
        return m_baseDN;
    }

    public void setBaseDN(String baseDN) {
        m_baseDN = baseDN;
    }

    public String getAdminDN() {
        return m_adminDN;
    }

    public void setAdminDN(String adminDN) {
        m_adminDN = adminDN;
    }

    public String getAdminPassword() {
        return m_adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        m_adminPassword = adminPassword;
    }

    public String getUsernameField() {
        return m_usernameField;
    }

    public void setUsernameField(String usernameField) {
        m_usernameField = usernameField;
    }

    public String getSearchFilter() {
        return m_searchFilter;
    }

    public void setSearchFilter(String searchFilter) {
        m_searchFilter = searchFilter;
    }

    public String getDomain() {
        return m_domain;
    }

    public void setDomain(String domain) {
        m_domain = domain;
    }

    public String getTimeout() {
        return m_timeout;
    }

    public void setTimeout(String timeout) {
        m_timeout = timeout;
    }
}
