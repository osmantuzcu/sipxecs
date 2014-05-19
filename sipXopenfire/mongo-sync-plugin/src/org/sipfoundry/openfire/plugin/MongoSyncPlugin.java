/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipfoundry.openfire.plugin;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginClassLoader;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.util.JiveGlobals;
import org.sipfoundry.openfire.plugin.listener.ImdbOplogListener;
import org.sipfoundry.openfire.plugin.listener.OfdbOplogListener;
import org.sipfoundry.openfire.plugin.listener.ProfilesOplogListener;

public class MongoSyncPlugin implements Plugin {
    private static Logger logger = Logger.getLogger(MongoSyncPlugin.class);

    private static final String HAZELCAST_CONFIG_DIR = JiveGlobals.getProperty("hazelcast.config.xml.directory",
            JiveGlobals.getHomeDirectory() + "/conf");

    public MongoSyncPlugin() {
        PluginClassLoader classloader = (PluginClassLoader) this.getClass().getClassLoader();
        // this is meant to allow loading configuration files from outside the plugin JAR file
        File confFolder = new File(HAZELCAST_CONFIG_DIR);
        try {
            logger.debug("Adding conf folder " + confFolder);
            classloader.addURLFile(confFolder.toURI().toURL());
        } catch (MalformedURLException e) {
            logger.error(String.format("Error adding folder {} to classpath {}", HAZELCAST_CONFIG_DIR,
                    e.getMessage()));
        }
    }

    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        logger.info("Mongo sync plugin initializing");
        ThreadGroup group = new ThreadGroup("mongoSync");
        Thread imdbOpLogThread = new Thread(group, new ImdbOplogListener(), "imdbOpLogListener");
        Thread ofdbOpLogThread = new Thread(group, new OfdbOplogListener(), "ofdbOpLogListener");
        Thread profilesdbOpLogThread = new Thread(group, new ProfilesOplogListener(), "profilesOpLogListener");
        imdbOpLogThread.start();
        ofdbOpLogThread.start();
        profilesdbOpLogThread.start();
    }

    @Override
    public void destroyPlugin() {
        logger.info("Mongo sync plugin stopping");
        QueueManager.shutdown();
    }
}
