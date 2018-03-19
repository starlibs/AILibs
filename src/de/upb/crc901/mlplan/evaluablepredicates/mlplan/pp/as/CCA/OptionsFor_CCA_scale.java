package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.CCA;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        scale : boolean, (default True)
        whether to scale the data?


    */
    public class OptionsFor_CCA_scale extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
