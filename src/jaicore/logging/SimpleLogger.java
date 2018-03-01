package jaicore.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A simple logger implementation providing simplistic logging features. In addition to the log
 * level and the message, it prints the name of the logger and the current timestamp.
 *
 * @author wever
 *
 */
public class SimpleLogger implements Logger {

  /**
   * Name of this logger.
   */
  private String loggerName;
  /**
   * Print stream
   */
  private Map<PrintStream, ELogLevel> destinationMap = new HashMap<>();

  SimpleLogger(final String loggerName, final ELogLevel logLevel) {
    this(loggerName);
    this.destinationMap.put(System.out, logLevel);
  }

  SimpleLogger(final String loggerName) {
    this.loggerName = loggerName;
  }

  @Override
  public void setLogLevel(final PrintStream ps, final ELogLevel level) {
    this.destinationMap.put(ps, level);
  }

  @Override
  public ELogLevel getLogLevel() {
    return this.getLogLevel();
  }

  @Override
  public void exception(final String msg, final Throwable e) {
    String stackTraceString = "";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      PrintStream ps = new PrintStream(baos, true, "utf-8");
      e.printStackTrace(ps);
      stackTraceString = new String(baos.toByteArray(), StandardCharsets.UTF_8);
    } catch (UnsupportedEncodingException e1) {
      this.error("Could not turn throwable stacktrace into string.");
    }
    this.log(ELogLevel.EXCEPTION, msg + "\n" + stackTraceString);
  }

  @Override
  public void error(final String msg) {
    this.log(ELogLevel.ERROR, msg);
  }

  @Override
  public void warn(final String msg) {
    this.log(ELogLevel.WARN, msg);
  }

  @Override
  public void info(final String msg) {
    this.log(ELogLevel.INFO, msg);
  }

  @Override
  public void debug(final String msg) {
    this.log(ELogLevel.DEBUG, msg);
  }

  @Override
  public void trace(final String msg) {
    this.log(ELogLevel.TRACE, msg);
  }

  /**
   * Helper method used by all the previously defined methods to threshold and format the output of
   * the logger in a consistent way. The log message is printed to the internal variable ps which is
   * of the type of a printstream.
   *
   * @param type
   *          Type of the log.
   * @param msg
   *          Message to log.
   */
  private void log(final ELogLevel type, final String msg) {
    for (Entry<PrintStream, ELogLevel> entry : this.destinationMap.entrySet()) {
      if (type.ordinal() <= entry.getValue().ordinal()) {
        StringBuffer sb = new StringBuffer();
        String[] values = { type.toString(), Thread.currentThread().getName(), this.loggerName, new Time(System.currentTimeMillis()) + "" };
        for (String value : values) {
          sb.append("[");
          sb.append(value);
          sb.append("]");
        }
        sb.append(" ");
        sb.append(msg);
        entry.getKey().println(sb.toString());
      }
    }
  }

}
