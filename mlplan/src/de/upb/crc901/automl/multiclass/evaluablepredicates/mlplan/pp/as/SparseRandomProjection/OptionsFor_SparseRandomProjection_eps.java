
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.SparseRandomProjection;
    /*
        eps : strictly positive float, optional, (default=0.1)
        Parameter to control the quality of the embedding according to
        the Johnson-Lindenstrauss lemma when n_components is set to
        'auto'.

        Smaller values lead to better embedding and higher number of
        dimensions (n_components) in the target projection space.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_SparseRandomProjection_eps extends NumericRangeOptionPredicate {
        
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
            return false;
        }
    }
    
