
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.NuSVC;
    /*
        max_iter : int, optional (default=-1)
        Hard limit on iterations within solver, or -1 for no limit.


    */

    import java.util.Arrays;
import java.util.List;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

    public class OptionsFor_NuSVC_max_iter extends OptionsPredicate {
        
        private static List<Integer> validValues = Arrays.asList(new Integer[]{1, 5}); // no large values make sense, because these are covered by -1

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
