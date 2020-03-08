package citiesDistance;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Represents the program's configuration. Reads the config file in the
 * predetermined directory and returns the required parameters.
 * 
 * @author Rolandas
 */

public class Config {
	private final String configPath = "config/config.cfg";
	InputStream in = null;
	Properties configFile;

	public Config() {
		configFile = new java.util.Properties();
		try {
			in = new FileInputStream(configPath);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		try {
			configFile.load(in);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public String getKey() {
		return getProperty("key");
	}

	public int getSearchRadius() {
		return Integer.parseInt(getProperty("searchRadius"));
	}

	public int getLimit() {
		return Integer.parseInt(getProperty("limit"));
	}

	public String getProperty(String key) {
		String value = this.configFile.getProperty(key);
		return value;
	}
}
