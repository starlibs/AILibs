package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.GraphLasso;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        verbose : boolean, default False
        If verbose is True, the objective function and dual gap are
        plotted at each iteration.


    */
    public class OptionsFor_GraphLasso_verbose extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
