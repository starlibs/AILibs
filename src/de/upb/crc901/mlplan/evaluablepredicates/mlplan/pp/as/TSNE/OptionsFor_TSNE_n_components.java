
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.TSNE;
    /*
        n_components : int, optional (default: 2)
        Dimension of the embedded space.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_TSNE_n_components extends NumericRangeOptionPredicate {
        
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
    
