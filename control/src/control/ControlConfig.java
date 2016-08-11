package control;

import java.util.ArrayList;
import java.util.HashMap;
import processing.core.PApplet;

/**
 *         Config for the control window and osc ports;
 *         Allows for Text, Toggle, Slider, and Dropdown menu.
 *         
 *         USER CONFIGURATION
 *         manually add the number of ports to the constructor.
 *         manually add the controls along with parameter specifications
 *         such as 
 *         - name (should be the name recognized by the application you 
 *         wish to control)
 *         - control type
 *         - label to describe control in the window
 *         - which osc address to send control updates to
 *         
 *         If you want more types of control, add them to 
 *         the enum ControlType below, and add treatment for them in main program.
 */
public class ControlConfig extends PApplet {
	static int STATUS = -1;
	static int TEXT = 0;
	static int TOGGLE = 1;
	static int SLIDER = 2;
	static int DROPDOWN = 3;
	enum ControlType {TOGGLE, SLIDER, DROPDOWN, TEXT, STATUS};
	ArrayList<String> ctrlNames = new ArrayList<String>();
	HashMap<String,ControlType> ctrlTypes = new HashMap<String,ControlType>();
	HashMap<String,String> ctrlLabels = new HashMap<String,String>();
	HashMap<String,Integer> ctrlAddresses = new HashMap<String,Integer>();
	// Dropdowns require more information. Structures to manage this
	ArrayList<String> dropdownNames = new ArrayList<String>();
	HashMap<String,Boolean> haveDropdownList = new HashMap<String,Boolean>();
	HashMap<String,Integer> dropdownAddresses = new HashMap<String,Integer>();

	//osc
	int listenOnPort;
	String listenPathPrefix;
	String configPathPrefix;
	ArrayList<String> oscIps = new ArrayList<String>();
	ArrayList<Integer> oscPorts = new ArrayList<Integer>();
	ArrayList<String> oscPathPrefixes = new ArrayList<String>();

	//formatting 
	int xSize = 300;
	int ySize = 600;
	int xStart = 20;
	int yStart = 10;
	int xStep = 180;
	int yGap = 20;
	int yHeight = 20;
	int yHeightDropdown = 100;
	int textSize = 14;
	int bgColor = color(0);
	int statusColor = color(200);
	
	public ControlConfig() {
		// define OSC addresses
		//IN
		this.listenOnPort = 57130; //is rhizome port on altsndtrk
		// make sure these prefixes are not a substring of the other
		this.listenPathPrefix = "/panel"; //prefix that altsndtrk sends to broadcast current values for controls
		this.configPathPrefix = "/conf"; //prefix that altsndtrk sends for configuration
		//OUT
		int TOaltsndtrk = 0;
		int TOsupercollider = 1;
		this.oscPorts.add(TOaltsndtrk,57140);	//is listenOn port on altsndtrk
		this.oscPorts.add(TOsupercollider,57120); //is supercollider port on altsndtrk
		this.oscIps.add(TOaltsndtrk,"127.0.0.1");
		this.oscIps.add(TOsupercollider,"127.0.0.1");
		// make sure these prefixes are not a substring of the other
		this.oscPathPrefixes.add(TOaltsndtrk,"/p5");
		this.oscPathPrefixes.add(TOsupercollider,"/sc");
				
		////////////////////////////////////////////////////////////
		// CONTROL ELEMENTS
		//order listed is placement of control element from top down
		this.initElement("status_ast", ControlType.STATUS, "Altsndtrk - ",
				TOaltsndtrk);
		// Video source
		this.initElement("video_source", ControlType.TEXT, "Video Source",
				TOaltsndtrk);
		this.initElement("webcam", ControlType.TOGGLE, "Use Webcam",
				TOaltsndtrk);
		this.initElement("video_time", ControlType.SLIDER, "Video Time",
				TOaltsndtrk);
		this.initElement("movies", ControlType.DROPDOWN, "Select Video",
				TOaltsndtrk);
	
		// Analyses
		this.initElement("analysis_heading", ControlType.TEXT, "Analyses",
				TOaltsndtrk);
		this.initElement("a_hist", ControlType.TOGGLE, "Histogram",
				TOaltsndtrk);
		this.initElement("a_of", ControlType.TOGGLE, "Optical Flow",
				TOaltsndtrk);
		this.initElement("a_seq", ControlType.TOGGLE, "Sequencer",
				TOaltsndtrk);
		this.initElement("a_blob", ControlType.TOGGLE, "Blob",
				TOaltsndtrk);
		// Tweak
		this.initElement("tweaks_heading", ControlType.TEXT, "Tweaks",
		TOaltsndtrk);
		this.initElement("bgsub", ControlType.TOGGLE, "Background Subtraction",
				TOaltsndtrk);
		this.initElement("of_regression", ControlType.SLIDER, "Optical Flow regression",
		TOaltsndtrk);
		this.initElement("of_smoothness", ControlType.SLIDER, "Optical Flow Smooth",
		TOaltsndtrk);
//		// Mask
//		this.initElement("displayEnabled", ControlType.TOGGLE, "Display");	
		this.initElement("status_sc", ControlType.STATUS, "Supercollider - ",
				TOsupercollider);
	}
	
	private void initElement(String name, ControlType ctrlType, String label, 
			Integer address) {
		this.ctrlNames.add(name);
		this.ctrlTypes.put(name, ctrlType);
		this.ctrlLabels.put(name, label);
		this.ctrlAddresses.put(name, address);
		if (ctrlType == ControlType.DROPDOWN) {
			this.dropdownNames.add(name);
			this.haveDropdownList.put(name, false);
			this.dropdownAddresses.put(name, address);
		}
	}
}
