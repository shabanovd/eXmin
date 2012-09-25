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
import org.exist.monitoring.jms.JMSSender;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Logging {
	
	public Logging(MonitoringManager manager) {
		
		JMSSender sender = new JMSSender();
		
		TopicConnectionFactory cf = manager.jms.connectionFactory;
		sender.setConnectionFactory(cf);
		sender.setTopic(manager.jms.topic);
		
		SenderAppender appender = new SenderAppender(sender);
		appender.setName("JMX log appender");
		
		System.out.println("adding appender");

		Logger logger = Logger.getRootLogger();
		logger.addAppender(appender);
		
		@SuppressWarnings({ "deprecation", "unchecked" })
		Enumeration<Category> cats = Logger.getCurrentCategories();
		while (cats.hasMoreElements()) {
			cats.nextElement().addAppender(appender);
		}
	}

}
