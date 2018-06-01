package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.GraphLasso;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        mode : {'cd', 'lars'}, default 'cd'
        The Lasso solver to use: coordinate descent or LARS. Use LARS for
        very sparse underlying graphs, where p > n. Elsewhere prefer cd
        which is more numerically stable.


    */
    public class OptionsFor_GraphLasso_mode extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"lars"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
