import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.media.Control;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

/**
 * This class handles main screen with roster
 * @author Vorobev
 *
 */
public class RosterList implements CommandListener{

	private JabberProfile jp;//current profile
	private Display d;
	private List roster;//Visible list
	private NetworkThread thread;//Network thread for processing 
	private RosterFactory factory;//Roster factory instanse
	private Command quit, delete, add, status, 
	mail, rename, volume, showlog;//Various Commands
	private String fullJid = "";//Session full JID
	private LogViewer log;//Not used
	
	private String lastTime = "";//Last new mail receive time
	private String tid = "";//Last new mail receive ID
	private MailViewer mails;
	
	private Vector onlines = new Vector();//Online statuses holder
	private Vector busies = new Vector();//Busy statuses holder
	private Vector aways = new Vector();//Away statuses holder
	
	Player p = null;//Player for playing sound
	
	/**
	 * This routine plays message
	 */
	public void playMessage()
	{
		try {
			if(p==null)
				p = Manager.createPlayer(getClass().getResourceAsStream("/imgs/ring.mid"), "audio/midi");
			if(p.getState()!=Player.STARTED)
			{
				p.realize();
				 Control cs[];
				 cs = p.getControls();
				 for (int i = 0; i < cs.length; i++) {
				     if (cs[i] instanceof VolumeControl)
				     {
				        try {
				        	if(((VolumeControl)cs[i]).getLevel()!=getProfile().getVolume())
				        		((VolumeControl)cs[i]).setLevel(getProfile().getVolume());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				     }
				 }				
				p.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public RosterList(Display d, JabberProfile jp) {
		this.d = d;
		this.jp = jp;
		setFullJid(jp.getUser());
		roster = new List("", List.IMPLICIT);
	}
	
	public void init()
	{
		d.setCurrent(roster);
		
		if (MGTalk.DEBUG) {
			showlog = new Command("Show log", Command.ITEM, 7);
			roster.addCommand(showlog);
		}
		
		quit = new Command("Close session", Command.BACK, 6);
		roster.addCommand(quit);

		delete = new Command("Delete", Command.ITEM, 4);
		roster.addCommand(delete);

		add = new Command("Add", Command.ITEM, 2);
		roster.addCommand(add);

		status = new Command("Status", Command.ITEM, 0);
		roster.addCommand(status);

		mail = new Command("Mailbox", Command.ITEM, 1);
		roster.addCommand(mail);

		rename = new Command("Rename", Command.ITEM, 3);
		roster.addCommand(rename);

		volume = new Command("Volume", Command.ITEM, 5);
		roster.addCommand(volume);

//		logC = new Command("Log", Command.BACK, 0);
//		roster.addCommand(logC);

		log = new LogViewer(d, this);
		mails = new MailViewer(d, this);
		roster.setCommandListener(this);
		factory = new RosterFactory(this);
	}
	
	public RosterFactory getRosterFactory()
	{
		return factory;
	}
	
	public List getRoster()
	{
		return roster;
	}
		
	public JabberProfile getProfile()
	{
		return jp;
	}
	
	public Display getDisplay()
	{
		return d;
	}

	public void commandAction(Command arg0, Displayable arg1) {
		
		if(arg0.equals(quit))
		{
			thread.terminate(false);
			new ProfileList(d);
			return;
		}
		if(arg0.equals(delete)&&roster.size()>0)
		{
			//Deletes contact from roster
			thread.writeToAir("<presence to=\""+factory.getItem(roster.getSelectedIndex()).getJid()+"\" type=\"unsubscribe\"/>");
			factory.deleteItem(factory.getItem(roster.getSelectedIndex()).getJid());
			return;
		}

		if(arg0.equals(rename)&&roster.size()>0)
		{
			//Starts renaming contact
			new ContactRename(d, this, thread, factory.getItem(roster.getSelectedIndex()));
			return;
		}
		
		if(arg0.equals(add))
		{
			//Starts adding contact
			new ContactAdd(d, d.getCurrent(), thread);
			return;
		}
		if(arg0.equals(status))
		{
			//Starts changing status
			new ChangeStatus(this, d);
			return;
		}
		if(arg0.equals(mail))
		{
			//Starts showing mail
			mails.startMe();
			return;
		}
		if(arg0.equals(volume))
		{
			//Starts showing mail
			new VolumeSetup(d, this);
			return;
		}
		if (MGTalk.DEBUG && arg0.equals(showlog)) {
			ConnectLog.getInstance().setCurrent();
			return;
		}
		if(roster.size()>0)
		{
			//Shows contacts history
			factory.getItem(roster.getSelectedIndex()).startForm();
			factory.refreshItem(factory.getItem(roster.getSelectedIndex()).getJid());
		}
	}

	/**
	 * @return Returns the thread.
	 */
	public NetworkThread getThread() {
		return thread;
	}

	/**
	 * @return Returns the fullJid.
	 */
	public String getFullJid() {
		return fullJid;
	}

	/**
	 * @param fullJid The fullJid to set.
	 */
	public void setFullJid(String fullJid) {
		this.fullJid = fullJid;
	}

	/**
	 * @return Returns the log.
	 */
	public LogViewer getLog() {
		return log;
	}

	/**
	 * @return Returns the lastTime.
	 */
	public String getLastTime() {
		return lastTime;
	}

	/**
	 * @param lastTime The lastTime to set.
	 */
	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}

	/**
	 * @return Returns the mails.
	 */
	public MailViewer getMails() {
		return mails;
	}

	/**
	 * @return Returns the tid.
	 */
	public String getTid() {
		return tid;
	}

	/**
	 * @param tid The tid to set.
	 */
	public void setTid(String tid) {
		this.tid = tid;
	}

	/**
	 * @return Returns the busies.
	 */
	public Vector getBusies() {
		return busies;
	}

	/**
	 * @param busies The busies to set.
	 */
	public void setBusies(Vector busies) {
		this.busies = busies;
	}

	/**
	 * @return Returns the onlines.
	 */
	public Vector getOnlines() {
		return onlines;
	}

	/**
	 * @param onlines The onlines to set.
	 */
	public void setOnlines(Vector onlines) {
		this.onlines = onlines;
	}

	/**
	 * @return Returns the aways.
	 */
	public Vector getAways() {
		return aways;
	}

	/**
	 * @param thread The thread to set.
	 */
	public void setThread(NetworkThread thread) {
		this.thread = thread;
	}
}
