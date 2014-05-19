/**
 *
 *
 * Copyright (c) 2012 eZuce, Inc. All rights reserved.
 */
package org.sipfoundry.openfire.config.parser;

import java.io.IOException;

import org.apache.commons.digester.Digester;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ConfigurationParser {
    private static final String CONFIG = "multipleldap-openfire";
    private static final String CONFIG_LDAP = CONFIG + "/ldap";

    private static void addCallMethodInt(String elementName, String methodName, String tag, Digester digester) {
        digester.addCallMethod(String.format("%s/%s", tag, elementName), methodName, 0, new Class[] {
            Integer.class
        });
    }

    private static void addCallMethodString(String elementName, String methodName, String tag, Digester digester) {
        digester.addCallMethod(String.format("%s/%s", tag, elementName), methodName, 0, new Class[] {
            String.class
        });
    }

    private static void addCallMethodBoolean(String elementName, String methodName, String tag, Digester digester) {
        digester.addCallMethod(String.format("%s/%s", tag, elementName), methodName, 0, new Class[] {
            Boolean.class
        });
    }

    private static void addRules(Digester digester) {
        digester.setUseContextClassLoader(true);
        digester.addObjectCreate(CONFIG, Config.class.getName());
        digester.addObjectCreate(CONFIG_LDAP, LdapConfig.class.getName());
        digester.addSetNext(CONFIG_LDAP, "addLdap");
        addCallMethodString("host", "setHost", CONFIG_LDAP, digester);
        addCallMethodInt("port", "setPort", CONFIG_LDAP, digester);
        addCallMethodBoolean("sslEnabled", "setSslEnabled", CONFIG_LDAP, digester);
        addCallMethodString("baseDN", "setBaseDN", CONFIG_LDAP, digester);
        addCallMethodString("adminDN", "setAdminDN", CONFIG_LDAP, digester);
        addCallMethodString("adminPassword", "setAdminPassword", CONFIG_LDAP, digester);
        addCallMethodString("usernameField", "setUsernameField", CONFIG_LDAP, digester);
        addCallMethodString("searchFilter", "setSearchFilter", CONFIG_LDAP, digester);
        addCallMethodString("domain", "setDomain", CONFIG_LDAP, digester);
        addCallMethodString("timeout", "setTimeout", CONFIG_LDAP, digester);
    }

    public static Config parse(String fileName) throws SAXException, IOException {
        Digester digester = new Digester();
        addRules(digester);
        InputSource inputSource = new InputSource(fileName);
        digester.parse(inputSource);
        Config config = (Config) digester.getRoot();
        return config;
    }
}
