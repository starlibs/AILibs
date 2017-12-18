package de.upb.crc901.mlplan.search.evaluators;

import java.io.File;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({ "file:conf/BasicMLEvaluator.properties" })
public interface IBasicMLEvaluatorConf extends Config {

	public static final String STATDIR = "statdir";

	@Key(STATDIR)
	public File getStatDir();
}
