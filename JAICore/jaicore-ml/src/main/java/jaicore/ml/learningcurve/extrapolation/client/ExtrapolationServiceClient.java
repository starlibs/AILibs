package jaicore.ml.learningcurve.extrapolation.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import jaicore.ml.learningcurve.extrapolation.InvalidAnchorPointsException;

/**
 * This class describes the client that is responsible for the communication
 * with an Extrapolation Service. The client accepts x- and y-values of anchor
 * points, creates a request and sends this request to an Extrapolation Service.
 * The configuration which was computed by the Extrapolation Service is returned
 * after the response has been received.
 *
 * @author Felix Weiland
 * @author Lukas Brandt
 *
 */
public class ExtrapolationServiceClient<C> {

	private String serviceUrl;
	private Class<C> configClass;

	public ExtrapolationServiceClient(final String serviceUrl, final Class<C> configClass) {
		this.serviceUrl = serviceUrl;
		this.configClass = configClass;
	}

	public C getConfigForAnchorPoints(final int[] xValuesArr, final double[] yValuesArr) throws InvalidAnchorPointsException, InterruptedException, ExecutionException {

		/* Create request */
		ExtrapolationRequest request = new ExtrapolationRequest();
		List<Integer> xValues = new ArrayList<>();
		for (int x : xValuesArr) {
			xValues.add(x);
		}
		List<Double> yValues = new ArrayList<>();
		for (double y : yValuesArr) {
			yValues.add(y);
		}
		request.setxValues(xValues);
		request.setyValues(yValues);

		/* Create service client */
		Client client = ClientBuilder.newClient();
		WebTarget target = null;
		try {
			target = client.target(new URI(this.serviceUrl));
		} catch (Exception e) {
			throw new IllegalStateException("No WebTarget!", e);
		}

		/* Send request and wait for response */
		Future<Response> future = target.request(MediaType.APPLICATION_JSON).async().post(Entity.entity(request, MediaType.APPLICATION_JSON));
		Response response;
		response = future.get();

		if (response.getStatus() == 500 && response.readEntity(String.class).equals("Invalid anchorpoints")) {
			throw new InvalidAnchorPointsException();
		}

		return response.readEntity(this.configClass);
	}

}
