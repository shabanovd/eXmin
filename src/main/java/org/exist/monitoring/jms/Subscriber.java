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

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.exist.config.ConfigurationException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Subscriber {
	
    protected TopicConnection connection = null;
    protected TopicSession session = null;

    protected TopicSubscriber subscriber = null;
    protected MessagersListener listener = null;

    public Subscriber(JMS jms) throws ConfigurationException {
		super();
                
        try {
	        connection = jms.connectionFactory.createTopicConnection();

	        session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            subscriber = session.createSubscriber(jms.topic);
            listener = new MessagersListener();
            subscriber.setMessageListener(listener);
            
            connection.start();
        
        } catch (JMSException e) {
        	throw new ConfigurationException(e);
        }
    }
	
	public void shutdown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {}
        }
	}
}