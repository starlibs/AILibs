
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.NuSVC;
    /*
        tol : float, optional (default=1e-3)
        Tolerance for stopping criterion.


    */

    import java.util.Arrays;
import java.util.List;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

    public class OptionsFor_NuSVC_tol extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{}); // deactivate this option (always use default)

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
