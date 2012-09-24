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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class SenderAppender extends AppenderSkeleton {

	private Sender sender;
	
	public SenderAppender(Sender sender) {
		this.sender = sender;
	}

	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		//ignore DEBUG logs
		if (event.getLevel().toInt() > Priority.DEBUG_INT)
			sender.send(generateXMLmessage(event));
	}
	
	private String generateXMLmessage(LoggingEvent event) {
		StringBuilder sb = new StringBuilder();
		
		sb
			.append("<logging-event xmlns='eXmin'")
			.append(" level='").append(event.getLevel()).append("'")
			.append(" timestamp='").append(event.getTimeStamp()).append("'")
			.append(" >");
		
		sb.append("<level>").append(event.getLevel()).append("</level>");
		sb.append("<thread>").append(event.getThreadName()).append("</thread>");
				
		final LocationInfo loc = event.getLocationInformation();

		sb
			.append("<location ")
			.append(" class-name='").append(loc.getClassName()).append("'")
			.append(" method='").append(loc.getMethodName()).append("'")
			.append(" line-number='").append(loc.getLineNumber()).append("'")
			.append(" />");

		sb.append("<message>").append(event.getMessage()).append("</message>");

		ThrowableInformation th = event.getThrowableInformation();
		if (th != null) {
			sb.append("<throwable-information>");
			String[] strs = th.getThrowableStrRep();
			for (int i = 0; i < strs.length; i++) {
				sb.append(strs[i]);
			}
			sb.append("</throwable-information>");
		}

		sb.append("</logging-event>");
		
		return sb.toString();
	}

}
