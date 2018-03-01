package jaicore.logging;

public class LoggerFactory {

  /**
   * Forbid to create an object of ListHelper as there are only static methods allowed here.
   */
  private LoggerFactory() {

  }

  /**
   * Returns a logger object to perform the logging.
   *
   * @param clazz
   * @return
   */
  public static Logger getLogger(final Class<?> clazz) {
    String loggerName = clazz.getName();
    return new SimpleLogger(loggerName, getLogLevelForLogger(loggerName));
  }

  /**
   * Searches the config file for the given loggerName and performs a longest match for matching
   * configurations.
   *
   * @param loggerName
   *          Name of the logger
   * @return Returns the log level. Default is ELogLevel.WARN.
   */
  private static ELogLevel getLogLevelForLogger(final String loggerName) {
    return ELogLevel.TRACE;
  }

}
