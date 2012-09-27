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

import java.util.Enumeration;

import javax.jms.TopicConnectionFactory;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.exist.EXistException;
import org.exist.LifeCycle;
import org.exist.config.Configurable;
import org.exist.config.Configuration;
import org.exist.config.Configurator;
import org.exist.config.annotation.ConfigurationClass;
import org.exist.config.annotation.ConfigurationFieldAsElement;
import org.exist.monitoring.jms.JMS;
import org.exist.monitoring.jms.JMSSender;
import org.exist.monitoring.jms.Collector;
import org.exist.storage.DBBroker;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
@ConfigurationClass("logs")
public class Logs implements Configurable, LifeCycle {
	
	@ConfigurationFieldAsElement("JMS")
	public JMS jms = null;
	
	@ConfigurationFieldAsElement("collector")
	private Collector collector = null;
	
	private SenderAppender appender;
	
	private MonitoringManager manager;
	private Configuration configuration = null;

	public Logs(MonitoringManager manager, Configuration config) {
		
		this.manager = manager;
		
        configuration = Configurator.configure(this, config);
	}

	@Override
	public void startUp(DBBroker broker) throws EXistException {
		start(broker);
	}

	@Override
	public void start(DBBroker broker) throws EXistException {
		
		if (jms == null)
			//XXX: log error!!!
			return;
		
		jms.startUp(broker);
		
		if (collector != null)
			collector.start(broker);

		JMSSender sender = new JMSSender();
		
		TopicConnectionFactory cf = jms.connectionFactory;
		sender.setConnectionFactory(cf);
		sender.setTopic(jms.topic);
		
		appender = new SenderAppender(manager, sender);
		appender.setName("JMX log appender");
		
		Logger logger = Logger.getRootLogger();
		logger.addAppender(appender);
		
		@SuppressWarnings({ "deprecation", "unchecked" })
		Enumeration<Category> cats = Logger.getCurrentCategories();
		while (cats.hasMoreElements()) {
			cats.nextElement().addAppender(appender);
		}
		System.out.println("appender done");
	}

	@Override
	public void sync(DBBroker broker) throws EXistException {
	}

	@Override
	public void stop(DBBroker broker) throws EXistException {
		Logger logger = Logger.getRootLogger();
		logger.removeAppender(appender);
		
		@SuppressWarnings({ "deprecation", "unchecked" })
		Enumeration<Category> cats = Logger.getCurrentCategories();
		while (cats.hasMoreElements()) {
			cats.nextElement().removeAppender(appender);
		}
		
		jms.stop(broker);

		if (collector != null)
			collector.shutdown();
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