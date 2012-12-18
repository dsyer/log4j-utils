package org.springframework.util.log4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;
import org.junit.Test;

public class DebugPatternLayoutTests {

	private static Logger logger = Logger.getLogger(DebugPatternLayoutTests.class);

	@Test
	public void testLogError() {
		logger.error("No!!!!", new RuntimeException("Planned"));
	}

	@Test
	public void testLogDebug() {
		logger.debug("Yes!!!!", new RuntimeException("Planned"));
	}

	@Test
	public void testFormatWithDebugAndThreeLines() throws Exception {
		String format = getFormat(2, Level.DEBUG);
		assertEquals(3, format.split("(?m)\n").length);
	}

	@Test
	public void testFormatWithDebugAndTwoLinesConverted() throws Exception {
		String format = getFormat(1, Level.DEBUG);
		assertEquals(1, format.split("(?m)\n").length);
	}

	@Test
	public void testFormatWithInfo() throws Exception {
		String format = getFormat(1, Level.INFO);
		assertTrue("Wrong format: " + format, format.split("(?m)\n").length > 3);
	}

	private String getFormat(int trace, Level level) throws Exception {
		DebugPatternLayout layout = new DebugPatternLayout();
		layout.setConversionPattern("%p: %c %m%n");
		layout.setDebugConversionPattern("%p: %c %m%n%throwable{" + trace + "}");
		layout.activateOptions();
		String format = layout.format(new LoggingEvent(DebugPatternLayoutTests.class.getName(), new RootLogger(
				Level.DEBUG), level, "Hello", new RuntimeException("Planned")));
		return format;
	}

}
