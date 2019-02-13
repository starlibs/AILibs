package jaicore.ml.learningcurve.extrapolation.lc.client;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import jaicore.ml.learningcurve.extrapolation.lc.LinearCombinationConfiguration;

/**
 * This class describes the client that is responsible for the communication
 * with the McmcService. The client accepts x- and y-values of anchor points,
 * creates a request and sends this request to the McmcService. The
 * configuration which was computed by the McmcService is returned after the
 * response has been received.
 * 
 * @author Felix Weiland
 *
 */
public class McmcServiceClient {

	// We assume the service to be running locally
	private static final String SERVICE_URL = "http://localhost:8080/jaicore/web/api/v1/mcmc/modelparams";

	public LinearCombinationConfiguration getConfigForAnchorPoints(int[] xValuesArr, double[] yValuesArr) {
		// Create request
		McmcRequest request = new McmcRequest();
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

		// Create service client
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(SERVICE_URL);

		// Send request and wait for response
		Response response = target.request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(request, MediaType.APPLICATION_JSON));

		// Create configuration object from response
		LinearCombinationConfiguration configuration = response.readEntity(LinearCombinationConfiguration.class);

		return configuration;
	}

}
