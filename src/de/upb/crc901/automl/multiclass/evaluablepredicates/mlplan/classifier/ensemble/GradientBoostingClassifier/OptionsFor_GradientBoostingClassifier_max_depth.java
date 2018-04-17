
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.ensemble.GradientBoostingClassifier;
    /*
        max_depth : integer, optional (default=3)
        maximum depth of the individual regression estimators. The maximum
        depth limits the number of nodes in the tree. Tune this parameter
        for best performance; the best value depends on the interaction
        of the input variables.


    */

    import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

    public class OptionsFor_GradientBoostingClassifier_max_depth extends OptionsPredicate {
        
    	private static List<Object> validValues = Arrays.asList(new Object[]{1, 2, 4, 5, 6, 7, 10, 15, 20}); // default is 3

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
