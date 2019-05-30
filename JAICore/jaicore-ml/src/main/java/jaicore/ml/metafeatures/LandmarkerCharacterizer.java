package jaicore.ml.metafeatures;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Characterizer that applies several characterizers to a data set, but does
 * not use any probing.
 * 
 * @author Helena Graf, Mirko
 *
 */
public class LandmarkerCharacterizer extends GlobalCharacterizer {

	private Logger logger = LoggerFactory.getLogger(LandmarkerCharacterizer.class);

	/**
	 * Constructs a new LandmarkerCharacterizer. Construction is the same as for the
	 * {@link ranker.core.metafeatures.GlobalCharacterizer}, except that only
	 * Characterizers that do not use probing are initialized.
	 * 
	 * @throws DatasetCharacterizerInitializationFailedException
	 *             if the characterizer cannot be initialized properly
	 */
	public LandmarkerCharacterizer() throws DatasetCharacterizerInitializationFailedException {
		super();
		logger.trace("Initialize");
	}

	@Override
	protected void initializeCharacterizers() throws Exception {
		super.characterizers = new ArrayList<>();
		super.addLandmarkerCharacterizers(characterizers);
	}

}