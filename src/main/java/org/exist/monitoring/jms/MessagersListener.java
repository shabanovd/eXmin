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

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class MessagersListener implements MessageListener {

	@Override
	public void onMessage(Message message) {
        try {
            if (message instanceof ActiveMQObjectMessage) {
            	ActiveMQObjectMessage msg = (ActiveMQObjectMessage) message;
//                System.out.println("Reading message: " + msg.getObject());
                
                if (msg.getObject() instanceof LoggingEvent) {
                	append((LoggingEvent)msg.getObject());
                }

            } else if (message instanceof TextMessage) {
            	TextMessage msg = (TextMessage) message;
                System.out.println("Reading message: " + msg.getText());
            
            } else {
                System.out.println("Message of wrong type: " + message.getClass().getName());
            }
        } catch (Throwable t) {
        	t.printStackTrace();
        }
	}
	
	protected void append(LoggingEvent log) {
		
		System.out.println("JMS - "+
//				log.fqnOfCategoryClass+" "+
				log.getLevel()+" "+
				log.getThreadName()+" "+
				log.getLocationInformation().getClassName()+" "+
				log.getLocationInformation().getLineNumber()+" "+
				log.getLocationInformation().getMethodName()+" "+
				log.getTimeStamp()+" "+
//				log.getLogger().getName()+" "+
				log.getMessage()+" ");

		if (log.getThrowableInformation() != null) {
			ThrowableInformation thI = log.getThrowableInformation();
			String[] strs = thI.getThrowableStrRep();
			System.out.println("*******");
//			System.out.println( thI.getThrowable().getMessage() );
			for (int i = 0; i < strs.length; i++) {
				System.out.println(strs[i]);
			}
			System.out.println("*******");
		}
	}
}
