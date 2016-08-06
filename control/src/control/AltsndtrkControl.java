package control;

import control.ControlConfig;
import controlP5.CallbackEvent;
import controlP5.CallbackListener;
import controlP5.ControlEvent;
import controlP5.ControlFont;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.Controller;
import controlP5.ScrollableList;
import controlP5.Textfield;
import controlP5.Textlabel;
import controlP5.Toggle;
import controlP5.Slider;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import java.util.ArrayList;
import java.util.HashMap;
import processing.core.PApplet;
import processing.core.PFont;


/**
 * Altsndtrk Control
 *
 * @author mingness
 *
 * All user configuration is in ConsoleConfig, including osc ports,
 * and the controls to include in the console. 
 * controls that are supported are Toggle, Slider, and ScrollableList (which 
 * is called a dropdown in this code as well). 
 *
 * state of values are not maintained here, but are broadcasted by the
 * application you wish to control. oscEvent parses these broadcasts.
 * When a control changes via this program, the change is sent to the 
 * main application.
 * 
 * The running status of the remote program is also tracked via regular
 * osc messages from the remote program with the address 
 * cc.configPathPrefix + cc.oscPathPrefixes[i]
 * where i is the index associated with the remote program in ControlConfig.
 * 
 * ScrollableLists require in addition to the control value also a list of 
 * possible values that the user choses from. When the remote application 
 * is turned on this list is (re)initialized.
 */
public class AltsndtrkControl extends PApplet {

	private ControlP5 cp5;
	private ControlConfig cc;
	CallbackListener cb;
	
	// OSC
	private OscP5 osc;
	private ArrayList<NetAddress> oscTo;
	//if remote program is live
	private Boolean[] oscLive;
	private Integer[] oscLastMillis; 
	private String[] oscStatusControlName; 
	private int idleThreshold = 3000;
	private HashMap<Integer,ArrayList<String>> addressToDropdownListNames 
		= new HashMap<Integer,ArrayList<String>>(); 
	private HashMap<String,Boolean> isPress = new HashMap<String,Boolean>(); 
 
		
	// In Processing 3 you specify size() inside settings()
	@Override
	public void settings() {
		size(300, 400);
	}

	@Override
	public void setup() {
		cc = new ControlConfig();

		// Window settings
		surface.setLocation(50, 100);
		surface.setTitle("AltSndtrk - Control panel");
		surface.setResizable(true);
		surface.setSize(cc.xSize, cc.ySize);
		background(cc.bgColor);

		// OSC
		osc = new OscP5(this, cc.listenOnPort);
		oscTo = new ArrayList<NetAddress>();
		oscLive = new Boolean[cc.oscIps.size()];
		oscLastMillis = new Integer[cc.oscIps.size()]; 
		oscStatusControlName = new String[cc.oscIps.size()]; 
		for (int i=0; i<cc.oscIps.size(); i++) {
			oscTo.add(i,new NetAddress(cc.oscIps.get(i), cc.oscPorts.get(i)));
			oscLive[i]=false;
			oscLastMillis[i]=0;
			addressToDropdownListNames.put(i, new ArrayList<String>());
		}
		for (int i=0; i<cc.dropdownNames.size(); i++) {
			String thisName = cc.dropdownNames.get(i);
			int thisAddress = cc.dropdownAddresses.get(thisName);
			addressToDropdownListNames.get(thisAddress).add(thisName);
		}
		  
		// Console
		cp5 = new ControlP5(this);
		int xPos = cc.xStart;
		int yPos = cc.yStart;
		PFont pfont = createFont("Lucida Sans",(float) cc.textSize);
		ControlFont cFont = new ControlFont(pfont, cc.textSize);
		for (int i=0; i<cc.ctrlNames.size(); i++) {
			String thisName = cc.ctrlNames.get(i);
			switch (cc.ctrlTypes.get(thisName)) {
			case STATUS:
				// three controls: two textlabels, and one textfield
				cp5.addTextlabel(thisName+"0")
				.setPosition(xPos, yPos)
				.setFont(cFont)
				.setColor(cc.statusColor)
				.setValue(cc.ctrlLabels.get(thisName));
				cp5.addTextlabel(thisName)
				.setPosition(xPos+7*cc.ctrlLabels.get(thisName).length(), yPos)
				.setFont(cFont)
				.setColor(cc.statusColor)
				.setValue("OFF");

				oscStatusControlName[cc.ctrlAddresses.get(thisName)] = thisName;
				isPress.put(thisName, false);
				yPos+=cc.textSize+5;
				break;
			case TEXT:  
				cp5.addTextlabel(thisName)
				.setPosition(xPos, yPos)
				.setFont(cFont)
				.setValue(cc.ctrlLabels.get(thisName));

				isPress.put(thisName, false);
				yPos+=cc.textSize+5;
				break;
			case TOGGLE:  
				cp5.addToggle(thisName)
				.setLabel(cc.ctrlLabels.get(thisName))
				.setPosition(xPos, yPos)
				.setValue(false);
				
				isPress.put(thisName, false);
				yPos+=cc.yHeight+cc.yGap;
				break;
			case SLIDER:
				cp5.addSlider(thisName)
//				.setLabel(cc.ctrlLabels.get(thisName))
				.setLabel("")
				.setPosition(xPos, yPos).setSize(cc.xStep,cc.yHeight)
				.setRange(0, 1)
				.setValue(0.5f);
				yPos+=cc.yHeight+3;
				cp5.addTextlabel(thisName+"0")
				.setPosition(xPos, yPos)
				.setValue(cc.ctrlLabels.get(thisName).toUpperCase());

				isPress.put(thisName, false);
				yPos+=cc.yGap;
				break;
			case DROPDOWN:
				cp5.addScrollableList(thisName)
				.setLabel(cc.ctrlLabels.get(thisName))
				.setPosition(xPos, yPos).setSize(cc.xStep,cc.yHeightDropdown)
				.setBarHeight(cc.yHeight).setItemHeight(cc.yHeight)
				.setType(ScrollableList.LIST);

				isPress.put(thisName, false);
				yPos+=cc.yHeightDropdown+cc.yGap;
				break;
			// //////////////////////////////////////
			// add additional case treatment for additional control types here	
			// //////////////////////////////////////
			default:
				println("unrecognized control type");
				break;
			}
		}


		cp5.addCallback(new CallbackListener() {
			public void controlEvent(CallbackEvent e) {
				if (e.getAction() == ControlP5.ACTION_RELEASE) {
//				if (e.getAction() == ControlP5.EVENT) {
					Controller<?> c = e.getController();
					String name = c.getName();
					sendOsc(name, c.getValue());
					isPress.put(name, false);
//					println(name, isPress.get(name));
				} else if (e.getAction() == ControlP5.ACTION_PRESS) {
					Controller<?> c = e.getController();
					String name = c.getName();
					isPress.put(name, true);
//					println(name, isPress.get(name));
				}
			}
		});
	}


	@Override
	public void draw() {
			for (int i=0; i<oscLastMillis.length; i++) {
				boolean isLive = (millis()-oscLastMillis[i]) < idleThreshold;
				if (isLive ^ oscLive[i]) {
					// something changed
					oscLive[i] = isLive;
					if (!oscStatusControlName[i].isEmpty()) {
						String name = oscStatusControlName[i];
						stroke(cc.bgColor);
						fill(cc.bgColor);
						Textlabel thisControl = cp5.get(Textlabel.class, name);
						rect((int) thisControl.getPosition()[0], (int) thisControl.getPosition()[1], 
								thisControl.getWidth(), thisControl.getHeight());
						thisControl.setValue(getStatusText(isLive));
					}

					// if newly alive, reinitialize lists
					if (oscLive[i]) {
						for (int j=0; j<addressToDropdownListNames.get(i).size(); j++) {
							sendOscMessage(cc.configPathPrefix+"/"+addressToDropdownListNames.get(i).get(j),
								(float) 1, oscTo.get(i));
						}
					} else {
						for (int j=0; j<addressToDropdownListNames.get(i).size(); j++) {
							cc.haveDropdownList.put(addressToDropdownListNames.get(i).get(j), false);
						}
					}
				} //if
			} //for
	} //draw
	
	private String getStatusText(Boolean isLive) {
		if (isLive) {
			return "LIVE";
		} else {
			return "OFF";
		}
	}
	
	private void sendOscMessage(String path, Float val, NetAddress to) {
		OscMessage msg = new OscMessage(path);
		msg.add(val);
		osc.send(msg, to);
	}

	private void sendOsc(String name, Float val) {
		//map name to path
		String path = cc.oscPathPrefixes.get(cc.ctrlAddresses.get(name))+"/"+name;
		// maps name to osc address to send to
		sendOscMessage(path, val, oscTo.get(cc.ctrlAddresses.get(name)));
	}
	
	public void oscEvent(OscMessage msg) {	
//		println(msg.toString());
		String name;
		if (msg.addrPattern().startsWith(cc.listenPathPrefix)) {
			name = msg.addrPattern().substring(cc.listenPathPrefix.length()+1);
			if (cc.ctrlNames.contains(name)) {
				if (!isPress.get(name)) {
					switch (cc.ctrlTypes.get(name)) {
					case TOGGLE:  
						boolean val;
						if (msg.checkTypetag("T")) {
							val = true;
						} else if (msg.checkTypetag("F")) {
							val = false;
						} else if (msg.checkTypetag("i")) {
							val = msg.get(0).intValue() == 1;
						} else if (msg.checkTypetag("f")) {
							val = msg.get(0).floatValue() == 1;
						} else if (msg.checkTypetag("d")) {
							val = msg.get(0).doubleValue() == 1;
						} else {
							println("unexpected case for typetag for Toggle.");
							return;
						}
						cp5.get(Toggle.class, name).setValue(val);
						break;
					case SLIDER:
						float valSlider;
						if (msg.checkTypetag("f")) {
							valSlider = msg.get(0).floatValue();
						} else if (msg.checkTypetag("d")) {
							valSlider = (float) msg.get(0).doubleValue();
						} else {
							println("unexpected case for typetag for Slider.");
							return;
						}
						cp5.get(Slider.class, name).setValue(valSlider);
						break;
					case DROPDOWN:
						int valDropdown;
						if (msg.checkTypetag("i")) {
							valDropdown = msg.get(0).intValue();
						} else if (msg.checkTypetag("f")) {
							valDropdown = round(msg.get(0).floatValue());
						} else if (msg.checkTypetag("d")) {
							valDropdown = round((float) msg.get(0).doubleValue());
						} else {
							println("unexpected case for typetag for ScrollableList.");
							return;
						}
						if (cc.haveDropdownList.get(name)) {
							cp5.get(ScrollableList.class, name).setValue(valDropdown);
						}
						break;
					default:
						println("unexpected case for control type for oscEvent.");
						break;
					} //switch
				} //if isPress
			} //if contains
		} else if (msg.addrPattern().startsWith(cc.configPathPrefix)) {
			name = msg.addrPattern().substring(cc.configPathPrefix.length()+1);
			if (cc.oscPathPrefixes.contains("/"+name)) {
//				println(name,oscLastMillis[cc.oscPathPrefixes.indexOf("/"+name)],millis());
				oscLastMillis[cc.oscPathPrefixes.indexOf("/"+name)] = millis();
			} else if (cc.dropdownNames.contains(name)) {		
				String[] ll = new String[msg.addrPattern().length()];
				for (int i=0; i<msg.addrPattern().length(); i++) {
					ll[i] = msg.get(i).stringValue();	
				}
				cp5.get(ScrollableList.class, name).setItems(ll);
				cc.haveDropdownList.put(name, true);
			} else {
				println("unexpected path for path prefix "+cc.configPathPrefix+".");
				return;
			}
		} else {
//			println("unexpected path:"+msg.addrPattern());
			return;
		}

	} //end oscEvent
	
	public static void main(String[] args) {
        ArrayList<String> options = new ArrayList<String>();
        options.add("--bgcolor=#000000 ");
        options.add("--hide-stop");
        options.add("control.AltsndtrkControl"); // com.x.Class
        String[] optionsArray = new String[options.size()];
        optionsArray = options.toArray(optionsArray);

        PApplet.main(optionsArray);
    }
	
} //end AltsndtrkControl
