/*
 * Copyright 2006-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.util.log4j;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Dave Syer
 * 
 */
public class GenericLog4jAppenderTests {

	private static final Logger logger = LogManager.getLogger(GenericLog4jAppenderTests.class);

	@Before
	public void init() {
		LogManager.resetConfiguration();
		CountingAppender.counter = 0;
		OtherAppender.counter = 0;
	}

	@Test
	public void testBasicLogging() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("log4j.rootCategory", "INFO, basic");
		properties.setProperty("log4j.appender.basic", ConsoleAppender.class.getName());
		properties.setProperty("log4j.appender.basic.layout", PatternLayout.class.getName());
		properties.setProperty("log4j.appender.basic.layout.ConversionPattern", "basic:%p:%m%n");
		PropertyConfigurator.configure(properties);
		logger.info("foo");
	}

	@Test
	public void testCustomAppender() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("log4j.rootCategory", "INFO, counter");
		properties.setProperty("log4j.appender.counter", CountingAppender.class.getName());
		PropertyConfigurator.configure(properties);
		logger.info("foo");
		assertEquals(1, CountingAppender.counter);
	}

	@Test
	public void testCustomAppenderWithNonPrimitiveProperty() throws Exception {
		Log4jConfigurer.initLogging("classpath:wrapper.xml");
		logger.info("foo");
		assertEquals(1, CountingAppender.counter);
	}

	@Test
	public void testCustomAppenderNotUsed() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("log4j.rootCategory", "INFO,other");
		properties.setProperty("log4j.appender.counter", CountingAppender.class.getName());
		properties.setProperty("log4j.appender.other", OtherAppender.class.getName());
		// Force the counter appender to be instantiated
		properties.setProperty("log4j.category.test", "TRACE,counter");
		PropertyConfigurator.configure(properties);
		logger.info("foo");
		assertEquals(1, OtherAppender.counter);
		assertEquals(0, CountingAppender.counter);
	}

	@Test
	public void testCustomAppenderBasicConfigurator() throws Exception {
		BasicConfigurator.configure(new CountingAppender());
		logger.info("foo");
		assertEquals(1, CountingAppender.counter);
	}

	public static class WrapperAppender extends AppenderSkeleton implements AppenderAttachable {

		private Appender delegate;

		public void addAppender(Appender newAppender) {
			delegate = newAppender;
		}

		@SuppressWarnings("rawtypes")
		public Enumeration getAllAppenders() {
			List<Appender> list = delegate == null ? Collections.<Appender> emptyList() : Arrays.asList(delegate);
			return new Vector<Appender>(list).elements();
		}

		public Appender getAppender(String name) {
			return delegate;
		}

		public boolean isAttached(Appender appender) {
			return appender == delegate;
		}

		public void removeAllAppenders() {
			delegate = null;
		}

		public void removeAppender(Appender appender) {
			if (appender == delegate) {
				delegate = null;
			}
		}

		public void removeAppender(String name) {
			if (name.equals(delegate.getName())) {
				delegate = null;
			}
		}

		@Override
		protected void append(LoggingEvent event) {
			delegate.doAppend(event);
		}

		public void close() {
			delegate.close();
		}

		public boolean requiresLayout() {
			return false;
		}

	}

	public static class CountingAppender extends AppenderSkeleton {

		private static int counter = 0;

		@Override
		protected void append(LoggingEvent event) {
			counter++;
		}

		@Override
		public void close() {
			counter = 0;
		}

		@Override
		public boolean requiresLayout() {
			return false;
		}

	}

	public static class OtherAppender extends AppenderSkeleton {

		private static int counter = 0;

		@Override
		protected void append(LoggingEvent event) {
			counter++;
		}

		@Override
		public void close() {
			counter = 0;
		}

		@Override
		public boolean requiresLayout() {
			return false;
		}

	}

}
