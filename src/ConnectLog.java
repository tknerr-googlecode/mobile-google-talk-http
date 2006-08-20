import java.util.Calendar;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

/**
 * 
 */

/**
 * @author Vorobev
 *
 */

public class ConnectLog implements CommandListener{

	/**
	 * 
	 */
	private Form form;
	private Command close;
	private Command back;
	private NetworkThread thread;
	private RosterList roster;
	private Display d;
	private JabberProfile jp;
	private static ConnectLog instance = null;
	
	public static ConnectLog getInstance()
	{
		if(instance==null)
			instance = new ConnectLog();
		return instance;
	}
	
	private ConnectLog() {
		super();
		close = new Command("Close session", Command.OK, 0);
		form = new Form("Network log");
		form.setCommandListener(this);
		form.addCommand(close);
		if (MGTalk.DEBUG) {
			back = new Command("Back", Command.BACK, 0);
			form.addCommand(back);
		}
	}
	
	public void setCurrent()
	{
		if (d!=null)
			d.setCurrent(form);
	}
	
	public void newSession(Display d, JabberProfile jp)
	{
		this.d = d;
		this.jp = jp;
		d.setCurrent(form);
		if (jp.getHttpbind()==0)
			thread = new TCPNetworkThread(this);
		else
			thread = new HTTPBindNetworkThread(this);
	}
	
	public void initRoster()
	{
		roster = new RosterList(d, jp);
		roster.setThread(thread);
		roster.init();
	}
	
	public void addMessage(String m)
	{
		//
		try {
			if(thread.isEnded())
				return;
//			d.setCurrent(form);
//			RichText t = new RichText(d);
//			t.setCanvasWidth(form.getWidth());
			Calendar cl = Calendar.getInstance();
			int h = 0;
			h = cl.get(Calendar.HOUR_OF_DAY);
			String tim = h+":"+(cl.get(Calendar.MINUTE)<10? "0"+cl.get(Calendar.MINUTE) : 
				""+cl.get(Calendar.MINUTE))+":"+(cl.get(Calendar.SECOND)<10?"0":"")+cl.get(Calendar.SECOND)+" ";
//			t.addContent(tim, t.getDefaultColor(), true);
//			t.addContent(m);
//			t.finish();
			if(form.size()>10)
				form.delete(form.size()-1);
//			form.insert(0, t);
			StringItem t2 = new StringItem(null, tim+m);
			t2.setLayout(Item.LAYOUT_NEWLINE_AFTER);
			form.insert(0, t2);
//			d.setCurrentItem(t2);
		} catch (Exception e) {
		}
	}

	public Display getDisplay()
	{
		return d;
	}
	
	public JabberProfile getProfile()
	{
		return jp;
	}
	
	/**
	 * @return Returns the thread.
	 */
	public NetworkThread getThread() {
		return thread;
	}

	/**
	 * @param thread The thread to set.
	 */
	public void setThread(NetworkThread thread) {
		this.thread = thread;
		roster.setThread(thread);
	}

	/**
	 * @return Returns the roster.
	 */
	public RosterList getRoster() {
		return roster;
	}

	/**
	 * @param roster The roster to set.
	 */
	public void setRoster(RosterList roster) {
		this.roster = roster;
	}

	public void commandAction(Command arg0, Displayable arg1) {
		if (MGTalk.DEBUG && arg0.equals(back)) {
			if (thread != null && thread.isAlive() && roster != null && roster.getRoster() != null) 
				d.setCurrent(roster.getRoster());
			else if (roster != null && roster.getRoster() != null)
				new ProfileList(d);
		}
		if(arg0.equals(close))
		{
			getProfile().setAutoReconnect(0);
			thread.terminate(false);
			new ProfileList(d);
			return;
		}
		
	}

}
