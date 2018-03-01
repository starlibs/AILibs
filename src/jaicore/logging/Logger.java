package jaicore.logging;

import java.io.PrintStream;

/**
 * Interface for the logger functionality that needs to be implemented by each concrete logger.
 *
 * @author wever
 *
 */
public interface Logger {

  /**
   * Log an exception together with a distinct message
   *
   * @param msg
   *          Message to log
   * @param e
   *          Throwable object of which the stacktrace shall be printed.
   */
  public void exception(String msg, Throwable e);

  /**
   * Log an error message.
   *
   * @param msg
   *          Message to log.
   */
  public void error(String msg);

  /**
   * Log a warn message.
   *
   * @param msg
   *          Message to log.
   */
  public void warn(String msg);

  /**
   * Log an info message.
   *
   * @param msg
   *          Message to log.
   */
  public void info(String msg);

  /**
   * Log a debug message.
   *
   * @param msg
   *          Message to log.
   */
  public void debug(String msg);

  /**
   * Log a trace message.
   *
   * @param msg
   *          Message to log.
   */
  public void trace(String msg);

  /**
   * Configure the log level to be output to the respective log destination.
   *
   * @param level
   */
  public void setLogLevel(PrintStream ps, ELogLevel level);

  /**
   * @return Get the currently specified log level.
   */
  public ELogLevel getLogLevel();

}
