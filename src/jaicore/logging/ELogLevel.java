package jaicore.logging;

/**
 * Different options to define the log level of each logger.
 *
 * @author wever
 *
 */
public enum ELogLevel {
  /** Turn off logging at all */
  OFF,
  /** Log only exceptions */
  EXCEPTION,
  /** Log at least errors */
  ERROR,
  /** Log at least warns (default) */
  WARN,
  /** Log at least infos */
  INFO,
  /** Log at least debugs */
  DEBUG,
  /** Log at least traces */
  TRACE;
}
