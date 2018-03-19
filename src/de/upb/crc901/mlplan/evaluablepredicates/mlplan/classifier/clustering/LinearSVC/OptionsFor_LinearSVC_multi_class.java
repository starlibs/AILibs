package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.LinearSVC;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        multi_class : string, 'ovr' or 'crammer_singer' (default='ovr')
        Determines the multi-class strategy if `y` contains more than
        two classes.
        ``"ovr"`` trains n_classes one-vs-rest classifiers, while
        ``"crammer_singer"`` optimizes a joint objective over all classes.
        While `crammer_singer` is interesting from a theoretical perspective
        as it is consistent, it is seldom used in practice as it rarely leads
        to better accuracy and is more expensive to compute.
        If ``"crammer_singer"`` is chosen, the options loss, penalty and dual
        will be ignored.


    */
    public class OptionsFor_LinearSVC_multi_class extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
