package altsoundtrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

// Documentation for json-io: https://github.com/jdereg/json-io

/**
 * @author aBe
 *         The purpose of the ConfigManager is to load / save a json
 *         file containing a serialized Config object.
 *
 */
public class ConfigManager {
	private final String path;

	/**
	 * @param path
	 *            Absolute path to the json file
	 * @param cfg
	 *            Object containing the configuration (a bunch of public
	 *            properties)
	 */
	public ConfigManager(String path) {
		this.path = path;
	}

	/**
	 * Saves the properties found in the Config object to the json file
	 */
	public void save(Config cfg) {
		try {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put(JsonWriter.PRETTY_PRINT, true);
			String json = JsonWriter.objectToJson(cfg, args);
			PrintWriter out = new PrintWriter(path);
			out.println(json);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the json file into the Config object
	 */
	public Config load() {
		try {
			FileInputStream fileIn = new FileInputStream(path);
			JsonReader reader = new JsonReader(fileIn);
			Config cfg = (Config) reader.readObject();
			reader.close();
			fileIn.close();
			return cfg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return true if the json file is found on the hard drive
	 */
	public boolean configExists() {
		File f = new File(path);
		return f.isFile();
	}
}
