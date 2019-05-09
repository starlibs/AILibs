package de.upb.crc901.mlplan.core;

import jaicore.ml.weka.dataset.splitter.ArbitrarySplitter;
import jaicore.ml.weka.dataset.splitter.IDatasetSplitter;

public class MLPlanMekaBuilder extends MLPlanBuilder {

	private static final IDatasetSplitter DEFAULT_SELECTION_HOLDOUT_SPLITTER = new ArbitrarySplitter();
	private static final IDatasetSplitter DEFAULT_SEARCH_PHASE_SPLITTER = new ArbitrarySplitter();
	private static final IDatasetSplitter DEFAULT_SELECTION_PHASE_SPLITTER = new ArbitrarySplitter();

	public MLPlanMekaBuilder() {
		super();

	}

}
