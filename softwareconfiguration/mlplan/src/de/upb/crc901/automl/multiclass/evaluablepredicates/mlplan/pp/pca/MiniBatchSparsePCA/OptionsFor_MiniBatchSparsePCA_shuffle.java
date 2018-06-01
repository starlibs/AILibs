package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.pca.MiniBatchSparsePCA;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        shuffle : boolean,
        whether to shuffle the data before splitting it in batches


    */
    public class OptionsFor_MiniBatchSparsePCA_shuffle extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
