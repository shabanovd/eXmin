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

import java.net.URI;
import java.util.Enumeration;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.FixedCountSubscriptionRecoveryPolicy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.policy.RoundRobinDispatchPolicy;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.net.JMSAppender;
import org.exist.EXistException;
import org.exist.config.ConfigurationException;
import org.exist.config.Startable;
import org.exist.monitoring.jms.JMS;
import org.exist.monitoring.jms.Subscriber;
import org.exist.plugin.Jack;
import org.exist.plugin.PluginsManager;
import org.exist.storage.DBBroker;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Plugin implements Jack, Startable {
	
	protected BrokerService broker;
	protected PolicyMap policyMap = new PolicyMap();
	
	public Plugin(PluginsManager manager) throws ConfigurationException {
		System.out.println("run logger");
		
		System.out.println("running broker");
		try {
			runBroker();
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}
		
		Subscriber subscriber = new Subscriber();
		
		System.out.println("create appender");
		
		JMSAppender appender = new JMSAppender();
		
		appender.setName("JMX log appender");
		appender.setInitialContextFactoryName("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		//'failover' - mean reconnect if connection gets down
		appender.setProviderURL("failover:tcp://localhost:61616");
		appender.setTopicBindingName(JMS.topicName);
		appender.setTopicConnectionFactoryBindingName("ConnectionFactory");
		appender.activateOptions();
		
		System.out.println("adding appender");

		Logger logger = Logger.getRootLogger();
		logger.addAppender(appender);
		
		@SuppressWarnings({ "deprecation", "unchecked" })
		Enumeration<Category> cats = Logger.getCurrentCategories();
		while (cats.hasMoreElements()) {
			cats.nextElement().addAppender(appender);
		}
		System.out.println("done.");
	}

	private void runBroker() throws Exception {
		broker = BrokerFactory.createBroker(new URI("broker:tcp://localhost:61616"));
		
		broker.setBrokerName("eXmin");
//		broker.addConnector("tcp://localhost:61616");
		
//		policyMap.setDefaultEntry(getDefaultPolicy());
//		broker.setDestinationPolicy(policyMap);

		broker.start();
	}
	
	protected PolicyEntry getDefaultPolicy() {
		PolicyEntry policy = new PolicyEntry();
		policy.setDispatchPolicy(new RoundRobinDispatchPolicy());
		policy.setSubscriptionRecoveryPolicy(new FixedCountSubscriptionRecoveryPolicy());
		return policy;
	}
	 
	@Override
	public void startUp(DBBroker broker) throws EXistException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sync() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		try {
			broker.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
