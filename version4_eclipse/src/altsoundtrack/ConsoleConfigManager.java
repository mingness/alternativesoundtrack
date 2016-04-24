package altsoundtrack;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.cedarsoftware.util.io.JsonWriter;

// Documentation for json-io: https://github.com/jdereg/json-io

/**
 * @author mingness
 *         The purpose of the ConsoleConfigManager is to save a json
 *         file containing a ConsoleConfig object. 
 *         adapted from ConfigManager by aBe.
 *
 */
public class ConsoleConfigManager {
	private final String path;

	/**
	 * @param path
	 *            path to the json file (relative to Main.java)
	 * @param cfg
	 *            Object containing the configuration (a bunch of public
	 *            properties)
	 */
	public ConsoleConfigManager(String path) {
		this.path = path;
	}

	/**
	 * Saves the properties found in the ConsoleConfig object to the json file
	 */
	public void save(ConsoleConfig cfg) {
		try {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put(JsonWriter.WRITE_LONGS_AS_STRINGS, true);
			String json = "cfgjson='"+JsonWriter.objectToJson(cfg, args)+"'";
			PrintWriter out = new PrintWriter(path);
			out.println(json);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
