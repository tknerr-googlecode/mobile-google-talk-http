import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * @author Vorobev
 *
 */
public class MGTalk extends MIDlet {
	public static MGTalk instance;
	public static boolean DEBUG = false;
	public MGTalk()
	{		
		String dbg = getAppProperty("debug"); 
		if (dbg != null && dbg.equals("true"))
			DEBUG = true;
		ProfileList l = new ProfileList(Display.getDisplay(this), true);
		instance = this;
	}
	protected void startApp() throws MIDletStateChangeException {
		System.out.println("Start");
	}

	protected void pauseApp() {
		System.out.println("App paused");
		
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}
}
