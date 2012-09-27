/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2012 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id$
 */
package org.exist.monitoring;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.exist.Database;
import org.exist.EXistException;
import org.exist.collections.Collection;
import org.exist.config.*;
import org.exist.config.annotation.*;
import org.exist.monitoring.jms.*;
import org.exist.plugin.Plug;
import org.exist.plugin.PluginsManager;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.DBBroker;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.xmldb.XmldbURI;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
@ConfigurationClass("monitoring")
public class MonitoringManager implements Plug, Configurable, Startable {

	/* /db/system/monitoring */
	private final static XmldbURI COLLECTION_URI = XmldbURI.SYSTEM.append("monitoring");
	private final static XmldbURI CONFIG_FILE_URI = XmldbURI.create("config.xml");
	
    private final static Logger LOG = Logger.getLogger(MonitoringManager.class);
    
    @ConfigurationFieldAsAttribute("instance-id")
    private String instanceId = UUID.randomUUID().toString();
	
    @ConfigurationFieldAsElement("service-broker")
    @ConfigurationReferenceBy("id")
    @ConfigurationFieldClassMask("org.exist.monitoring.jms.Broker")
    private List<Broker> serviceBrokers = new ArrayList<Broker>();
    
	@ConfigurationFieldAsElement("logs")
	public Logs logs = null;

	protected Database db;
	private Collection collection = null;
	private Configuration configuration = null;
	
	public MonitoringManager(PluginsManager pm) {
		db = pm.getDatabase();
	}

	@Override
	public void startUp(DBBroker broker) throws EXistException {
        TransactionManager transaction = broker.getDatabase().getTransactionManager();
        Txn txn = null;
		
        Collection systemCollection = null;
		try {
			systemCollection = broker.getCollection(XmldbURI.SYSTEM);
	        if(systemCollection == null)
	        	throw new EXistException("/db/system collecton does not exist!");
		} catch (PermissionDeniedException e) {
			throw new EXistException(e);
		}

        try {
            collection = broker.getCollection(COLLECTION_URI);
            if (collection == null) {
                txn = transaction.beginTransaction();
                collection = broker.getOrCreateCollection(txn, COLLECTION_URI);
                if (collection == null){
                    return;
                }
                //if db corrupted it can lead to unrunnable issue
                //throw new ConfigurationException("Collection '/db/system/monitoring' can't be created.");

                collection.setPermissions(0770);
                broker.saveCollection(txn, collection);

                transaction.commit(txn);
            } 
        } catch (Exception e) {
            transaction.abort(txn);
            e.printStackTrace();
            LOG.debug("loading configuration failed: " + e.getMessage(), e);
        }

        Configuration _config_ = Configurator.parse(this, broker, collection, CONFIG_FILE_URI);
        configuration = Configurator.configure(this, _config_);
        
        for (Broker serviceBroker : serviceBrokers) {
//    		if (!serviceBroker.isStarted())
        	serviceBroker.start(broker);
        }
        
//		if (!logs.isStarted())
			logs.start(broker);
		
        System.out.println("monitoring started up");
	}
	
	public String getInstandeId() {
		return instanceId;
	}
	
	public void addServiceBroker(Configuration config) throws ConfigurationException {
		serviceBrokers.add( new Broker(this, config) );
	}

	@Override
	public void sync() {
        for (Broker serviceBroker : serviceBrokers)
        	serviceBroker.sync(null);
	}

	@Override
	public void stop() {
		if (logs != null)
			try {
				logs.stop(null);
			} catch (EXistException e) {
				e.printStackTrace();
			}
		
		for (Broker broker : serviceBrokers)
			try {
				broker.stop(null);
			} catch (EXistException e) {
				e.printStackTrace();
			}
	}

	@Override
	public boolean isConfigured() {
		return configuration != null;
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}
}
