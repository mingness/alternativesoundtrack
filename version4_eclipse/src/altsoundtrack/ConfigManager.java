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
	private Config cfg;

	/**
	 * @param path
	 *            Absolute path to the json file
	 * @param cfg
	 *            Object containing the configuration (a bunch of public
	 *            properties)
	 */
	public ConfigManager(String path, Config cfg) {
		this.path = path;
		this.cfg = cfg;

		if (configExists()) {
			load();
		}
		// Save it, in case the Config class has new properties which are
		// not yet in the json file. This will add the missing properties
		// to the file.
		save();
	}

	/**
	 * Saves the properties found in the Config object to the json file
	 */
	public void save() {
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
	public void load() {
		try {
			FileInputStream fileIn = new FileInputStream(path);
			JsonReader reader = new JsonReader(fileIn);
			cfg = (Config) reader.readObject();
			reader.close();
			fileIn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return true if the json file is found on the hard drive
	 */
	public boolean configExists() {
		File f = new File(path);
		return f.isFile();
	}
}
