package de.upb.crc901.mlplan.test;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({ "file:conf/distsearch.properties" })
public interface IDistSearchConf extends Config {
	
	  public static final String PREFIX = "sc.distsearch.";
	  public static final String FOLDER_ASSERIALIZATION = PREFIX + "asserializationFolder";

	  // GET
	  @Key(FOLDER_ASSERIALIZATION)
	  public String getASSFolder();
}
