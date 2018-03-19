package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.Birch;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        copy : bool, default True
        Whether or not to make a copy of the given data. If set to False,
        the initial data will be overwritten.

    Attributes
    
    */
    public class OptionsFor_Birch_copy extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
