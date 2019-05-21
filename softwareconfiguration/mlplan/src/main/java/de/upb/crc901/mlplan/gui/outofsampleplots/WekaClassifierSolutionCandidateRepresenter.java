package de.upb.crc901.mlplan.gui.outofsampleplots;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionCandidateRepresenter;
import weka.classifiers.Classifier;
import weka.core.SerializationHelper;

public class WekaClassifierSolutionCandidateRepresenter implements SolutionCandidateRepresenter {

	private static final Logger LOGGER = LoggerFactory.getLogger(WekaClassifierSolutionCandidateRepresenter.class);

	@Override
	public String getStringRepresentationOfSolutionCandidate(Object solutionCandidate) {
		if (solutionCandidate instanceof Classifier) {

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try {
				SerializationHelper.write(outputStream, solutionCandidate);
				final byte[] byteArray = outputStream.toByteArray();
				return Base64.getEncoder().encodeToString(byteArray);
			} catch (Exception e) {
				LOGGER.error("Cannot write solution candidate: {} due to error {}", solutionCandidate, e);
				return null;
			}

		}
		return solutionCandidate.toString();
	}

}
