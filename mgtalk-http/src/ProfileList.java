import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

/**
 * @author Vorobev
 *
 * This form shows profile list and allows user add, modify or delete profiles
 */
public class ProfileList implements CommandListener {

	private List l;
	private RecordStore rs;
	Command c1;
	Command c2;
	Command c3;
	Display d;
	Vector jps;
	
	public ProfileList(Display d){
		this(d, false);
	}
	public ProfileList(Display d, boolean autoConnect){
		try {
			//RecordStore.deleteRecordStore("jabberRecords");
			rs = RecordStore.openRecordStore("jabberRecords", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.d = d;
		c2 = new Command("Edit", Command.ITEM, 0);
		c1 = new Command("Close client", Command.BACK, 0);
		l = new List("Profile list", List.IMPLICIT);
		l.addCommand(c1);
		l.addCommand(c2);
		if (MGTalk.DEBUG) {
			c3 = new Command("Show log", Command.ITEM, 0);
			l.addCommand(c3);
		}
		l.append("Add new...", RosterItem.offline);
		try {
			jps = new Vector();
			boolean isAutoFound = false;
			JabberProfile auto = null;
			
			for(RecordEnumeration e = rs.enumerateRecords(null, null, false); e.hasNextElement();)
			{
				try {
					JabberProfile jp = new JabberProfile();
					jp.setId(e.nextRecordId());
					jp.fromByteArray(rs.getRecord(jp.getId()));
					l.append(jp.getName(), RosterItem.online);
					jps.addElement(jp);
					if(jp.getAuto()>0 && autoConnect)
					{//If we found profile with autoConnect feture enabled - remeber this profile
						auto = jp;
						isAutoFound = true;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if(isAutoFound)
				{
					//Automatically start connecting with this profile
					
					ConnectLog.getInstance().newSession(d, auto);
//					RosterList rl = new RosterList(d,auto);
//					rl.init();
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		l.setCommandListener(this);
		setCurrent();
//		try {
//			Player p = Manager.createPlayer(getClass().getResourceAsStream("/imgs/ring.mid"), "audio/midi");
//			p.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	public void setCurrent() {
		d.setCurrent(l);
	}
	
	public void commandAction(Command arg0, Displayable arg1) {
		if(arg0.equals(c2))
		{//Edit button
			System.out.println("Edit selected");
			JabberProfile jp = new JabberProfile();
			if(l.getSelectedIndex()!=0)
			{//Start editing this profile
				try {
					jp = (JabberProfile) jps.elementAt(l.getSelectedIndex()-1);
				} catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			System.out.println("Start profileEdit");
			ProfileEdit pe = new ProfileEdit(d, jp, rs);
			System.out.println("End profileEdit");
			return;
		} 
		if(arg0.equals(c1))
		{//Close application
			try {
				MGTalk.instance.destroyApp(true);
				MGTalk.instance.notifyDestroyed();
			} catch (MIDletStateChangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		if (MGTalk.DEBUG && arg0.equals(c3)) {
			ConnectLog.getInstance().setCurrent();
			return;
		}
		if(l.getSelectedIndex()!=0)
		{//If we click on profile - start connecting, if not - start adding new
			try {
				ConnectLog cl = ConnectLog.getInstance();
				if(cl!=null)
					cl.newSession(d,(JabberProfile)jps.elementAt(l.getSelectedIndex()-1));
			} catch (Exception e) {
			}
		}
		else
			commandAction(c2, arg1);
	}

}
