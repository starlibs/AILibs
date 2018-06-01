package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.GraphLassoCV;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        verbose : boolean, optional
        If verbose is True, the objective function and duality gap are
        printed at each iteration.


    */
    public class OptionsFor_GraphLassoCV_verbose extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
