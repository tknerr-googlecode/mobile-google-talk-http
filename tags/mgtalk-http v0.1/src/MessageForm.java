import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.TextField;

import custom.RichText;

/**
 * 
 */

/**
 * @author Vorobev
 * 
 * Form for contacts history viewing and new messages typing and sending
 *
 */
public class MessageForm implements CommandListener, ItemCommandListener{

	/**
	 * 
	 */
	private RosterList roster;
	private RosterItem ri;
	private Form form;
	private Display d;
	private TextField tb;//Type new message here

	private RichText statusHolder;
	Command send, quote;//Send Command
	Command toList;//Close roster Command
	
	/**
	 * @param d Display instance
	 * @param l RosterList instance
	 * @param r Current RosterItem selecting from roster
	 */
	public MessageForm(Display d, RosterList l, RosterItem r) {
		super();
		roster = l;
		ri = r;
		this.d = d;
		form = new Form(r.getName());
		statusHolder = new RichText(d);
		statusHolder.setCanvasWidth(roster.getRoster().getWidth());
		System.out.println("MessageForm "+roster.getRoster().getWidth());
		statusHolder.addImage(RosterItem.getImageByStatus(0));
		statusHolder.finish();
		tb = new TextField(null, "", 2000, TextField.ANY);
		form.append(statusHolder);
		form.append(tb);
		send = new Command("Send", Command.EXIT, 0);
		form.addCommand(send);
		toList = new Command("Roster", Command.OK, 0);
		quote = new Command("Quote", Command.ITEM, 0);
		form.addCommand(toList);
		form.setCommandListener(this);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return Returns the form.
	 */
	public Form getForm() {
		return form;
	}
	/**
	 * @param s Input string
	 * @return returns modified for history viewing String
	 */
	public String enlargeStr(String s)
	{
		return s+"\n";
	}
	
	/**
	 * Starts form
	 */
	public void startForm()
	{
//		tb.setLabel(ri.getStatusStr());
		d.setCurrent(form);
		d.setCurrentItem(tb);
		System.out.println("Current set");
	}

	public void commandAction(Command arg0, Displayable arg1) {
		// TODO Auto-generated method stub
		if(arg0.equals(send)&&!tb.getString().trim().equals(""))
		{
			/*
			 * Send Command pushed and TextField is not empty.
			 * Sends typed message by sending apropriate jabber packet
			 * */
			roster.getThread().writeToAir("<message to=\""+ri.getJid()+"\" from=\""+roster.getFullJid()+"\" type=\"chat\" id=\""+ri.getSession()+"\" xmlns=\"jabber:client\">"+(roster.getThread().isGoogle()?"<nos:x value=\"disabled\" xmlns:nos=\"google:nosave\"/>":"")+"<body>"+tb.getString()+"</body></message>");
			ri.addMessage(tb.getString(), false); //Add sended message to history
			tb.setString(""); //Clear TextField for new message typing
			return;
		}
		if(arg0.equals(toList))
		{
			d.setCurrent(roster.getRoster());
			return;
			//Close form
		}
	}

	/**
	 * @return Returns the statusHolder.
	 */
	public RichText getStatusHolder() {
		return statusHolder;
	}

	private int findPosition(Item it)
	{
		for(int i=2; i<form.size(); i++)
		{
			if(form.get(i).equals(it))
				return i-2;
		}
		return -1;
	}
	public static String replace(String src, String what, String to)
	{
		int f = -to.length();
		while((f = src.indexOf(what, f+to.length()))!=-1)
		{
			System.out.println(src.substring(0, f)+"::"+src.substring(f+what.length(), src.length()));
			src = src.substring(0, f)+to+src.substring(f+what.length(), src.length());
		}
		return src;
	}
	
	public void commandAction(Command arg0, Item arg1) {
		// TODO Auto-generated method stub
		int pos = findPosition(arg1);
		if(pos==-1)
			return;
		pos = ri.getMess().size()-1-pos;
		System.out.println("Message was: "+ri.getMess().elementAt(pos).toString());
		String res = MessageForm.replace(">"+ri.getMess().elementAt(pos).toString(), "\n","\n>");
		tb.setString(res+"\n");
		d.setCurrentItem(tb);
//		ri.getMess().elementAt(pos).toString()
	}

	/**
	 * @return Returns the quote.
	 */
	public Command getQuote() {
		return quote;
	}

	/**
	 * @param statusHolder The statusHolder to set.
	 */
	public void setStatusHolder(RichText statusHolder) {
		this.statusHolder = statusHolder;
	}

	/**
	 * @return Returns the si.
	 */
	
}
