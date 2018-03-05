package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.SGDClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        penalty : str, 'none', 'l2', 'l1', or 'elasticnet'
        The penalty (aka regularization term) to be used. Defaults to 'l2'
        which is the standard regularizer for linear SVM models. 'l1' and
        'elasticnet' might bring sparsity to the model (feature selection)
        not achievable with 'l2'.


    */
    public class OptionsFor_SGDClassifier_penalty extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"none", "l1", "elasticnet"}); // l2 is default

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
