package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.MiniBatchDictionaryLearning;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        shuffle : bool,
        whether to shuffle the samples before forming batches


    */
    public class OptionsFor_MiniBatchDictionaryLearning_shuffle extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
