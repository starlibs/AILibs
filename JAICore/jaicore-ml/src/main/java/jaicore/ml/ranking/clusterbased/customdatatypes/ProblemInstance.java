package jaicore.ml.ranking.clusterbased.customdatatypes;

public class ProblemInstance<I> {
	/**
	 * @author Helen Beierling
	 *
	 * @param <I> stands for the observed instance
	 */
		private I instance;
		
		public ProblemInstance() {}
		public ProblemInstance(I inst) {
			this.instance = inst;
		}
		
		public I getInstance() {
			return instance;
		}
		public void setInstance(I newinstance) {
			this.instance = newinstance;
		}
		public boolean isEmpty() {
			return instance != null;
		}

}

