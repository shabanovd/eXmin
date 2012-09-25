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
package org.exist.monitoring.jms;

import java.util.Properties;

import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.exist.EXistException;
import org.exist.config.Configurable;
import org.exist.config.Configuration;
import org.exist.config.ConfigurationException;
import org.exist.config.Configurator;
import org.exist.config.Startable;
import org.exist.config.annotation.ConfigurationClass;
import org.exist.config.annotation.ConfigurationFieldAsAttribute;
import org.exist.config.annotation.ConfigurationFieldAsElement;
import org.exist.monitoring.MonitoringManager;
import org.exist.storage.DBBroker;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
@ConfigurationClass("JMS")
public class JMS implements Configurable, Startable {

	public static String initialContextFactoryName = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
//	public static final String queryName = "dynamicQueue/eXmin-logging";

	@ConfigurationFieldAsAttribute("id")
	public String id;

	@ConfigurationFieldAsElement("topic")
	public String topicName = "dynamicTopics/eXmin-logging";
	
	@ConfigurationFieldAsElement("provider-url")
	public String providerURL = "tcp://localhost:61616";
	
    private Context jndiContext = null;
    
    public TopicConnectionFactory  connectionFactory = null;
    public Topic topic = null;
    
	private Configuration configuration = null;

	public JMS(MonitoringManager manager, Configuration config) throws ConfigurationException {
//		this.manager = manager;
		
        configuration = Configurator.configure(this, config);
	}
    
	@Override
	public void startUp(DBBroker broker) throws EXistException {
        try {
        	Properties env = new Properties();
        	env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactoryName);
        	env.put(Context.PROVIDER_URL, providerURL);
        	  
            jndiContext = new InitialContext(env);
        } catch (NamingException e) {
        	throw new ConfigurationException(e);
        }
        
        try {
            connectionFactory = (TopicConnectionFactory) jndiContext.lookup("ConnectionFactory");
            topic = (Topic) jndiContext.lookup(topicName);
        } catch (NamingException e) {
        	throw new ConfigurationException(e);
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
