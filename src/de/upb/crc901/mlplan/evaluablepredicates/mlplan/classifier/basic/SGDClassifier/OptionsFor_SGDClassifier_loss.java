package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.SGDClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        loss : str, default: 'hinge'
        The loss function to be used. Defaults to 'hinge', which gives a
        linear SVM.

        The possible options are 'hinge', 'log', 'modified_huber',
        'squared_hinge', 'perceptron', or a regression loss: 'squared_loss',
        'huber', 'epsilon_insensitive', or 'squared_epsilon_insensitive'.

        The 'log' loss gives logistic regression, a probabilistic classifier.
        'modified_huber' is another smooth loss that brings tolerance to
        outliers as well as probability estimates.
        'squared_hinge' is like hinge but is quadratically penalized.
        'perceptron' is the linear loss used by the perceptron algorithm.
        The other losses are designed for regression but can be useful in
        classification as well; see SGDRegressor for a description.


    */
    public class OptionsFor_SGDClassifier_loss extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"log", "modified_huber", "squared_hinge", "perceptron", "squared_loss", "huber", "epsilon_insensitive', 'squared_epsilon_insensitive"}); // hinge is default 

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
