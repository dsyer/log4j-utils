package org.springframework.util.log4j;

import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.pattern.BridgePatternConverter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4j layout that logs DEBUG (and TRACE) with a different pattern than INFO (and above). The default pattern is set
 * as per the {@link EnhancedPatternLayout}, and the DEBUG pattern is set via the
 * {@link #setDebugConversionPattern(String)} property. E.g. to suppress stack traces in DEBUG:
 * 
 * <pre>
 * LOG_PATTERN=[%d{yyyy-MM-dd HH:mm:ss.SSS}] %t %5p %c{1}: %m%n
 * log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
 * log4j.appender.CONSOLE.layout=org.cloudfoundry.identity.uaa.log4j.DebugPatternLayout
 * log4j.appender.CONSOLE.layout.ConversionPattern=${LOG_PATTERN}
 * log4j.appender.CONSOLE.layout.DebugConversionPattern=${LOG_PATTERN}%throwable{0}
 * log4j.appender.CONSOLE.threshold=DEBUG
 * </pre>
 * 
 * @author Dave Syer
 * 
 */
public class DebugPatternLayout extends EnhancedPatternLayout {

	private PatternConverter debugFormatter;

	private String conversionPattern;

	@Override
	public boolean ignoresThrowable() {
		boolean result = super.ignoresThrowable();
		if (!result) {
			return false;
		}
		if (result && debugFormatter instanceof BridgePatternConverter) {
			result = ((BridgePatternConverter) debugFormatter).ignoresThrowable();
		}
		return result;
	}

	public void setDebugConversionPattern(String conversionPattern) {
		this.conversionPattern = OptionConverter.convertSpecialChars(conversionPattern);
		debugFormatter = createPatternParser(this.conversionPattern).parse();
	}

	@Override
	public void activateOptions() {
		super.activateOptions();
		if (debugFormatter == null) {
			setDebugConversionPattern(getConversionPattern());
		}
		if (!ignoresThrowable() && super.ignoresThrowable()) {
			setConversionPattern(getConversionPattern() + "%throwable{long}");
		}
	}

	@Override
	public String format(LoggingEvent event) {
		if (event.getLevel().equals(Level.DEBUG) || !event.getLevel().isGreaterOrEqual(Level.DEBUG)) {
			StringBuffer buf = new StringBuffer();
			debugFormatter.format(buf, event);
			convertTwoLinesToOneLine(buf, " [", "]");
			return buf.toString();
		}
		return super.format(event);
	}

	public static void convertTwoLinesToOneLine(StringBuffer input, String prefix, String suffix) {
		String ending = "\n";
		int index = input.indexOf(ending);
		int nextIndex = input.indexOf(ending, index + 1);
		if (index > 0 && nextIndex > 0 && input.indexOf(ending, nextIndex + 1) < 0) {
			input.replace(index, index+1, prefix);
			input.replace(input.length()-1, input.length(), suffix);
		}
	}

}
