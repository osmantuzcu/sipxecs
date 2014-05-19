/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 */
package org.sipfoundry.openfire.config.parser;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private List<LdapConfig> m_ldapList = new ArrayList<LdapConfig>();

    public void addLdap(LdapConfig ldapConfig) {
        m_ldapList.add(ldapConfig);
    }

    public List<LdapConfig> getLdapList() {
        return m_ldapList;
    }
}
