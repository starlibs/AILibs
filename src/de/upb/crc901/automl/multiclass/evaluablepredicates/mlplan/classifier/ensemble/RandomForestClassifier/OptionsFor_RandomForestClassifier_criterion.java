package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.ensemble.RandomForestClassifier;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        criterion : string, optional (default="gini")
        The function to measure the quality of a split. Supported criteria are
        "gini" for the Gini impurity and "entropy" for the information gain.
        Note: this parameter is tree-specific.


    */
    public class OptionsFor_RandomForestClassifier_criterion extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"entropy"}); // gini is default

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
