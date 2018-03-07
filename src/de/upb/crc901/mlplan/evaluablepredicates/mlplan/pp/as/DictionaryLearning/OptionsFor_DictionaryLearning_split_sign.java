package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.DictionaryLearning;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        split_sign : bool, False by default
        Whether to split the sparse feature vector into the concatenation of
        its negative part and its positive part. This can improve the
        performance of downstream classifiers.


    */
    public class OptionsFor_DictionaryLearning_split_sign extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
