package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.pca.IncrementalPCA;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        copy : bool, (default=True)
        If False, X will be overwritten. ``copy=False`` can be used to
        save memory but is unsafe for general use.


    */
    public class OptionsFor_IncrementalPCA_copy extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
