package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.MLPClassifier;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

import java.util.Arrays;
import java.util.List;

/*
    activation : {'identity', 'logistic', 'tanh', 'relu'}, default 'relu'
    Activation function for the hidden layer.

    - 'identity', no-op activation, useful to implement linear bottleneck,
      returns f(x) = x

    - 'logistic', the logistic sigmoid function,
      returns f(x) = 1 / (1 + exp(-x)).

    - 'tanh', the hyperbolic tan function,
      returns f(x) = tanh(x).

    - 'relu', the rectified linear unit function,
      returns f(x) = max(0, x)


*/
public class OptionsFor_MLPClassifier_activation extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "identity", "logistic", "tanh" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
