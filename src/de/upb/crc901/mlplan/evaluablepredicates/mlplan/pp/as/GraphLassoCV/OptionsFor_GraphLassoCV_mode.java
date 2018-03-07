package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.GraphLassoCV;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        mode : {'cd', 'lars'}
        The Lasso solver to use: coordinate descent or LARS. Use LARS for
        very sparse underlying graphs, where number of features is greater
        than number of samples. Elsewhere prefer cd which is more numerically
        stable.


    */
    public class OptionsFor_GraphLassoCV_mode extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"cd", "lars"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
