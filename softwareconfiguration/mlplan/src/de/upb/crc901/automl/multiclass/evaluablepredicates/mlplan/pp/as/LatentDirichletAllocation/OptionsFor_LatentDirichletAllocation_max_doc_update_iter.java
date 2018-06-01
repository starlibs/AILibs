
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.LatentDirichletAllocation;
    /*
        max_doc_update_iter : int (default=100)
        Max number of iterations for updating document topic distribution in
        the E-step.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_LatentDirichletAllocation_max_doc_update_iter extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 30;
        }

        @Override
        protected double getMax() {
            return 150;
        }

        @Override
        protected int getSteps() {
            return 5;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
