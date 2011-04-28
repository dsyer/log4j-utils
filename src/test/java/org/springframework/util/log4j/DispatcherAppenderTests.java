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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.log4j.DispatcherAppender;

/**
 * @author Dave Syer
 * 
 */
public class DispatcherAppenderTests {

	private static final Logger logger = LogManager.getLogger(DispatcherAppenderTests.class);

	@Before
	public void init() throws Exception {
		System.setProperty("KEY", "key");
		FileUtils.deleteDirectory(new File("target/logs"));
		LogManager.resetConfiguration();
		CountingAppender.clear();
		CountingAppender.counter = 0;
	}

	@Test
	public void testVanillaAppender() throws Exception {

		DispatcherAppender appender = new DispatcherAppender();
		appender.addAppender(new CountingAppender("init:one"));
		appender.setLayout(new PatternLayout("key:%x"));
		appender.setPropertyName("key");
		appender.activateOptions();
		BasicConfigurator.configure(appender);

		logger.info("foo");
		NDC.push("two");
		logger.info("foo");
		NDC.clear();

		assertEquals("[key:two]", CountingAppender.keys.toString());
		assertEquals(2, CountingAppender.counter);

	}

	@Test
	public void testWithReplacements() throws Exception {
		
		DispatcherAppender appender = new DispatcherAppender();
		appender.addAppender(new CountingAppender("init:one"));
		appender.setLayout(new PatternLayout("${KEY}:%x"));
		appender.setPropertyName("key");
		appender.activateOptions();
		BasicConfigurator.configure(appender);

		logger.info("foo");
		NDC.push("two");
		logger.info("foo");
		NDC.clear();

		assertEquals("[key:two]", CountingAppender.keys.toString());
		assertEquals(2, CountingAppender.counter);

	}

	@Test
	public void testWithXml() throws Exception {
		
		Log4jConfigurer.initLogging("classpath:counter-dispatcher.xml");

		logger.info("foo");
		NDC.push("two");
		logger.info("foo");
		NDC.clear();

		assertEquals("[key:two]", CountingAppender.keys.toString());
		assertEquals(2, CountingAppender.counter);

	}

	@Test
	public void testFileAppenderWithXml() throws Exception {

		Log4jConfigurer.initLogging("classpath:file-dispatcher.xml");

		logger.info("foo");
		NDC.push("foo");
		logger.info("foo");
		NDC.clear();

		assertEquals(2, FileUtils.listFiles(new File("target/logs"), new String[] { "log" }, true).size());

	}

	@Test
	public void testNoAppenderWithXml() throws Exception {

		Log4jConfigurer.initLogging("classpath:no-appender.xml");

		logger.info("foo");
		NDC.push("foo");
		logger.info("foo");
		NDC.clear();

		// The logging fails (and reports an exception but doesn't throw it)
		assertEquals(0, CountingAppender.counter);

	}

	@Test
	public void testNoPropertyWithXml() throws Exception {

		Log4jConfigurer.initLogging("classpath:no-property.xml");

		logger.info("foo");
		NDC.push("foo");
		logger.info("foo");
		NDC.clear();

		// The logging fails (and reports an exception but doesn't throw it)
		assertEquals(0, CountingAppender.counter);

	}

	@Test
	public void testNoLayoutWithXml() throws Exception {

		Log4jConfigurer.initLogging("classpath:no-layout.xml");

		logger.info("foo");
		NDC.push("foo");
		logger.info("foo");
		NDC.clear();

		// The logging fails (and reports an exception but doesn't throw it)
		assertEquals(0, CountingAppender.counter);

	}

	@Test
	public void testBadProperty() throws Exception {

		DispatcherAppender appender = new DispatcherAppender();
		appender.addAppender(new CountingAppender());
		appender.setLayout(new PatternLayout("key:%x"));
		appender.setPropertyName("nonExistent");
		appender.activateOptions();
		BasicConfigurator.configure(appender);

		logger.info("foo");
		NDC.push("foo");
		logger.info("foo");
		NDC.clear();

		// All logging just goes to the default logger
		assertEquals(2, CountingAppender.counter);
		assertEquals(0, CountingAppender.keys.size());

	}

	public static class CountingAppender extends AppenderSkeleton {

		private static int counter = 0;
		
		private static Collection<String> keys = new ArrayList<String>();

		private String key;
		
		public CountingAppender() {
		}
		
		public CountingAppender(String key) {
			this.key = key;
		}
		
		public static void clear() {
			keys.clear();
		}
		
		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
			CountingAppender.keys.add(key);
		}

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
