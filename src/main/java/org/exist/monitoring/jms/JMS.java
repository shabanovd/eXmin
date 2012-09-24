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

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.exist.config.ConfigurationException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class JMS {

	public static final String topicName = "dynamicTopics/eXmin-logging";
	public static final String queryName = "dynamicQueue/eXmin-logging";
	
	public static String initialContextFactoryName = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
	public static String providerURL = "tcp://localhost:61616";
	
    protected Context jndiContext = null;
    public TopicConnectionFactory  topicConnectionFactory = null;

    public Topic topic = null;
    
    protected TopicConnection topicConnection = null;
    protected TopicSession topicSession = null;

    public JMS() throws ConfigurationException {
        try {
        	Properties env = new Properties();
        	env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactoryName);
        	env.put(Context.PROVIDER_URL, providerURL);
        	  
            jndiContext = new InitialContext(env);
        } catch (NamingException e) {
        	throw new ConfigurationException(e);
        }
        
        
        try {
            topicConnectionFactory = (TopicConnectionFactory) jndiContext.lookup("ConnectionFactory");
            topic = (Topic) jndiContext.lookup(topicName);
        } catch (NamingException e) {
        	throw new ConfigurationException(e);
        }
        
        try {
	        topicConnection = topicConnectionFactory.createTopicConnection();
	        topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
        	throw new ConfigurationException(e);
		}
	}
}
