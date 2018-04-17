
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.MiniBatchDictionaryLearning;
    /*
        batch_size : int,
        number of samples in each mini-batch


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_MiniBatchDictionaryLearning_batch_size extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1;
        }

        @Override
        protected double getMax() {
            return 1;
        }

        @Override
        protected int getSteps() {
            return -1;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
