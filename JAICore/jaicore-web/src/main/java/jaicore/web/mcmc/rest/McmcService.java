package jaicore.web.mcmc.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jaicore.web.mcmc.rest.message.ErrorResponse;
import jaicore.web.mcmc.rest.message.McmcRequest;
import jaicore.web.mcmc.rest.message.McmcResponse;

@RestController
@RequestMapping("/mcmc")
public class McmcService {

	@PostMapping("/modelparams")
	public ResponseEntity<McmcResponse> computeModelParams(McmcRequest request) {
		McmcResponse dummyResponse = getDummyResponse();
		return ResponseEntity.ok().body(dummyResponse);
	}

	private McmcResponse getDummyResponse() {
		McmcResponse response = new McmcResponse();
		Map<String, Double> weights = new HashMap<>();
		weights.put("fun1", 0.1);
		weights.put("fun2", 0.9);
		response.setWeights(weights);

		Map<String, Double> fun1Params = new HashMap<>();
		fun1Params.put("a", 2.0);
		fun1Params.put("b", 1.2);

		Map<String, Double> fun2Params = new HashMap<>();
		fun2Params.put("alpha", 0.1);

		Map<String, Map<String, Double>> params = new HashMap<>();
		params.put("fun1", fun1Params);
		params.put("fun2", fun2Params);

		response.setParameters(params);

		return response;
	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		ErrorResponse er = new ErrorResponse(e.getLocalizedMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(er);
	}

}
