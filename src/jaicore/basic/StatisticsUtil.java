package jaicore.basic;

import java.util.Collection;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TTest;

public class StatisticsUtil {

  /**
   * Forbid to create an object of ListHelper as there are only static methods allowed here.
   */
  private StatisticsUtil() {
    // intentionally do nothing
  }

  public static double max(final Collection<Double> values) {
    return values.stream().mapToDouble(x -> x).max().getAsDouble();
  }

  public static double min(final Collection<Double> values) {
    return values.stream().mapToDouble(x -> x).min().getAsDouble();
  }

  public static double mean(final Collection<Double> values) {
    return values.stream().mapToDouble(x -> x).average().getAsDouble();
  }

  public static double sum(final Collection<Double> values) {
    return values.stream().mapToDouble(x -> x).sum();
  }

  public static double variance(final Collection<Double> values) {
    final double mean = mean(values);
    return values.stream().mapToDouble(x -> x).map(x -> Math.pow(x - mean, 2) / values.size()).sum();
  }

  public static double standardDeviation(final Collection<Double> values) {
    return Math.sqrt(variance(values));
  }

  public static double mannWhitneyTwoSidedSignificanceP(final Collection<Double> valuesA, final Collection<Double> valuesB) {
    double[] valuesAArray = valuesA.stream().mapToDouble(x -> x).toArray();
    double[] valuesBArray = valuesB.stream().mapToDouble(x -> x).toArray();
    MannWhitneyUTest test = new MannWhitneyUTest();
    return test.mannWhitneyUTest(valuesAArray, valuesBArray);
  }

  public static boolean mannWhitneyTwoSidedSignificance(final Collection<Double> valuesA, final Collection<Double> valuesB) {
    double[] valuesAArray = valuesA.stream().mapToDouble(x -> x).toArray();
    double[] valuesBArray = valuesB.stream().mapToDouble(x -> x).toArray();
    MannWhitneyUTest test = new MannWhitneyUTest();
    return test.mannWhitneyUTest(valuesAArray, valuesBArray) < 0.05;
  }

  public static boolean mannWhitneyOneSidedSignificance(final Collection<Double> valuesA, final Collection<Double> valuesB) {
    double[] valuesAArray = valuesA.stream().mapToDouble(x -> x).toArray();
    double[] valuesBArray = valuesB.stream().mapToDouble(x -> x).toArray();
    MannWhitneyUTest test = new MannWhitneyUTest();
    return test.mannWhitneyUTest(valuesAArray, valuesBArray) < 0.01;
  }

  public static boolean twoSampleTTestSignificance(final Collection<Double> valuesA, final Collection<Double> valuesB) {
    double[] valuesAArray = valuesA.stream().mapToDouble(x -> x).toArray();
    double[] valuesBArray = valuesB.stream().mapToDouble(x -> x).toArray();
    TTest test = new TTest();
    return test.tTest(valuesAArray, valuesBArray, 0.05);
  }

  public static boolean twoSampleTTestSignificance(final double mean1, final double variance1, final double n1, final double mean2, final double variance2, final double n2) {
    double meanDifference = mean1 - mean2;
    double sP = Math.sqrt(((n1 - 1) * variance1 + (n2 - 1) * variance2) / (n1 + n2 - 2));
    double t = meanDifference / (sP * Math.sqrt(1 / n1 + 1 / n2));
    return t < 0.05;
  }
}
