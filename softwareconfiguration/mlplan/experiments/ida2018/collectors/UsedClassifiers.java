package ida2018.collectors;

import ida2018.IDA2018Util;

public class UsedClassifiers {

  public static void main(final String[] args) {

    for (String learner : IDA2018Util.getConsideredLearners()) {
      String displayedClassifierName = learner.substring(learner.lastIndexOf(".") + 1);
      String akk = "";
      switch (displayedClassifierName) {
        default:
          akk = displayedClassifierName.replaceAll("[a-z]", "");
          break;
      }
      System.out.println(displayedClassifierName + " (" + akk + "),");
    }

  }

}
