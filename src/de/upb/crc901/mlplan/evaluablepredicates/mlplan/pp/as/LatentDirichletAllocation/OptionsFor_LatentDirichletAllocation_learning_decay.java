
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.LatentDirichletAllocation;
    /*
        learning_decay : float, optional (default=0.7)
        It is a parameter that control learning rate in the online learning
        method. The value should be set between (0.5, 1.0] to guarantee
        asymptotic convergence. When the value is 0.0 and batch_size is
        ``n_samples``, the update method is same as batch learning. In the
        literature, this is called kappa.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_LatentDirichletAllocation_learning_decay extends NumericRangeOptionPredicate {
        
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
    
