Log4j DispatcherAppender
========================

A [Log4j](http://logging.apache.org/log4j/1.2) `Appender` that
dispatches to a different `Appender` instance depending on the value
of the [Nested Diagnostic
Context](http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/NDC.html).
The delegate appender is copied and a single property is overriden
(e.g. a file location) based on a layout pattern.  A typical use case
would be directing logs to a different file based on the business data
that are being processed.

Note that adding an appender to an existing one cannot be done using
the Log4j `PropertiesConfigurator` so you have to use XML or Java to
configure a `DispatcherAppender`.

License: Apache 2.0

Usage
-----

Sample `log4j.xml`:

    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">

    <log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/" debug="false">

      <appender name="LOGGER" class="org.springframework.util.log4j.DispatcherAppender">
        <param name="propertyName" value="file" />
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="target/logs/%x.log" />
        </layout>
        <appender-ref ref="FILE" />
      </appender>

      <appender name="FILE" class="org.apache.log4j.FileAppender">
        <param name="file" value="target/logs/default.log" />
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%5p: %m%n" />
        </layout>
      </appender>

      <root>
        <priority value="info" />
        <appender-ref ref="LOGGER" />
      </root>

    </log4j:configuration>

Application code:

    logger.info("foo");
    NDC.push("alt");
    try {
      logger.info("foo");
    } finally {
      NDC.clear();
    }

Result:

    $ ls target/logs
    default.log    alt.log
