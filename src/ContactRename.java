import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 * @author Vorobev
 * 
 * Form for renaming items in a roster
 */
public class ContactRename implements CommandListener{

	private Display d;
	private RosterList ret;
	private RosterItem ri;
	private NetworkThread nt;
	private Form form;
	private Command ok, close;
	private TextField tb;
	
	/**
	 * @param d Display instance
	 * @param ret RosterList instance
	 * @param nt Network thread processing jabber packets
	 * @param ri Selected RosterItem in a roster
	 */
	public ContactRename(Display d, RosterList ret, NetworkThread nt, RosterItem ri) {
		super();
		this.d = d;
		this.ri = ri;
		this.ret = ret;
		this.nt = nt;
		ok = new Command("Rename", Command.EXIT, 0);
		close = new Command("Close", Command.OK, 0);
		form = new Form("Rename contact");
		form.addCommand(ok);
		form.addCommand(close);
		form.setCommandListener(this);
		String jid = ri.getJid();
		if(jid.indexOf("/")!=-1)
		{
			jid = jid.substring(0, jid.indexOf("/"));
		}
		/*
		 * If jid contains resource information - cut it
		 * */
		tb = new TextField("Set new name for contact "+jid, ri.getName(), 50, TextField.EMAILADDR);
		form.append(tb);
		d.setCurrent(form);
	}

	public void commandAction(Command arg0, Displayable arg1) {
		if(arg0.equals(close))
		{
			d.setCurrent(ret.getRoster());
			return;
		}
		if(arg0.equals(ok))
		{
			String jid = ri.getJid();
			if(jid.indexOf("/")!=-1)
			{
				jid = jid.substring(0, jid.indexOf("/"));
			}
			/*
			 * Write appropriate packet
			 * */
			nt.writeToAir("<iq from=\""+ret.getFullJid()+"\" type=\"set\">" +
					"<query xmlns=\"jabber:iq:roster\">" +
					"<item jid=\""+jid+"\" " +
							"name=\""+tb.getString()+"\" subscription=\"both\">" +
									"</item></query></iq>");
			d.setCurrent(ret.getRoster());
			/*
			 * Close form
			 * */
			return;
		}
	}

}
