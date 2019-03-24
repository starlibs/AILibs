package jaicore.web.mcmc;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class WebServerCustomization implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

	public static final String PORT_CONFIG_ENV_VARIABLE = "WEBSERVER_PORT";

	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {
		String portStr = System.getenv(PORT_CONFIG_ENV_VARIABLE);
		if (portStr != null) {
			int port;
			try {
				port = Integer.parseInt(portStr);
			} catch (Exception e) {
				throw new RuntimeException(String.format("Cannot parse port", portStr), e);
			}
			factory.setPort(port);
		}

	}

}
