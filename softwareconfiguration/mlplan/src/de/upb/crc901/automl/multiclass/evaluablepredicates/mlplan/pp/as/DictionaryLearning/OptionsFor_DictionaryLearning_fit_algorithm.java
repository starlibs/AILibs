package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.DictionaryLearning;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        fit_algorithm : {'lars', 'cd'}
        lars: uses the least angle regression method to solve the lasso problem
        (linear_model.lars_path)
        cd: uses the coordinate descent method to compute the
        Lasso solution (linear_model.Lasso). Lars will be faster if
        the estimated components are sparse.

        .. versionadded:: 0.17
           *cd* coordinate descent method to improve speed.


    */
    public class OptionsFor_DictionaryLearning_fit_algorithm extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"lars", "cd"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
