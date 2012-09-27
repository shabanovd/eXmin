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

import org.exist.EXistException;
import org.exist.LifeCycle;
import org.exist.config.Configurable;
import org.exist.config.Configuration;
import org.exist.config.ConfigurationException;
import org.exist.config.annotation.ConfigurationClass;
import org.exist.monitoring.Logs;
import org.exist.storage.DBBroker;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
@ConfigurationClass("collector")
public class Collector implements Configurable, LifeCycle {

	protected TopicConnection connection = null;
	protected TopicSession session = null;

	protected TopicSubscriber subscriber = null;
	protected MessagersListener listener = null;

	private Configuration configuration = null;
	private Logs logs = null;

	public Collector(Logs logs, Configuration config) throws ConfigurationException {
		this.logs = logs;
		
		configuration = config;
	}

	public void shutdown() {
	}

	@Override
	public void startUp(DBBroker broker) throws EXistException {
		start(broker);
	}

	@Override
	public void start(DBBroker broker) throws EXistException {

		try {
			connection = logs.jms.connectionFactory.createTopicConnection();

			session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

			subscriber = session.createSubscriber(logs.jms.topic);
			listener = new MessagersListener();
			subscriber.setMessageListener(listener);

			connection.start();
			
			System.out.println("collector started.");

		} catch (JMSException e) {
			throw new ConfigurationException(e);
		}
	}

	@Override
	public void sync(DBBroker broker) throws EXistException {
	}

	@Override
	public void stop(DBBroker broker) throws EXistException {
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
			}
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