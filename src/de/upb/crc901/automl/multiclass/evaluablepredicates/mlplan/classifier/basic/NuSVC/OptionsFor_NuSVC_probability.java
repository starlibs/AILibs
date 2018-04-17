package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.NuSVC;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        probability : boolean, optional (default=False)
        Whether to enable probability estimates. This must be enabled prior
        to calling `fit`, and will slow down that method.


    */
    public class OptionsFor_NuSVC_probability extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true"}); // false is in ignore anyway

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
