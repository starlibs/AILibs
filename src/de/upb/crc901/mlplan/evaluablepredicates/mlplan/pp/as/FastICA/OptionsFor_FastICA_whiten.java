package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.FastICA;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        whiten : boolean, optional
        If whiten is false, the data is already considered to be
        whitened, and no whitening is performed.


    */
    public class OptionsFor_FastICA_whiten extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
