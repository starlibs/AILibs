package jaicore.basic;

public class ValueUtil {

  /**
   * Forbid to create an object of ListHelper as there are only static methods allowed here.
   */
  private ValueUtil() {
    // intentionally do nothing
  }

  public static String valueToString(final double value, final int decimals) {
    StringBuilder sb = new StringBuilder();
    sb.append(round(value, decimals));
    while (sb.toString().length() < decimals + 2) {
      sb.append("0");
    }
    return sb.toString();
  }

  public static double round(final double valueToRound, final int decimals) {
    int multiplier = (int) Math.pow(10, decimals);
    double raisedValue = Math.round(valueToRound * multiplier);
    return raisedValue / multiplier;
  }

}
