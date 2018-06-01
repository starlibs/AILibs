package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.RobustScaler;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        with_centering : boolean, True by default
        If True, center the data before scaling.
        This will cause ``transform`` to raise an exception when attempted on
        sparse matrices, because centering them entails building a dense
        matrix which in common use cases is likely to be too large to fit in
        memory.


    */
    public class OptionsFor_RobustScaler_with_centering extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
