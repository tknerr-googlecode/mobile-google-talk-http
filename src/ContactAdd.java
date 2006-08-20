import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 * @author Vorobev
 *
 * Sends authorization request to JID
 * tb - TextField for JID
 */
public class ContactAdd implements CommandListener{

	private Display d;
	private Displayable ret;
	private NetworkThread nt;
	private Form form;
	private Command ok, close;
	private TextField tb;
	
	/**
	 * @param d Display instance
	 * @param ret parent class, who will get control after closing this form 
	 * @param nt Network thread processing jabber packets
	 */
	public ContactAdd(Display d, Displayable ret, NetworkThread nt) {
		super();
		this.d = d;
		this.ret = ret;
		this.nt = nt;
		ok = new Command("Add", Command.EXIT, 0);
		close = new Command("Close", Command.OK, 0);
		form = new Form("Add contact");
		form.addCommand(ok);
		form.addCommand(close);
		form.setCommandListener(this);
		tb = new TextField("Type user's JID here", "", 50, TextField.EMAILADDR);
		form.append(tb);
		d.setCurrent(form);
		// TODO Auto-generated constructor stub
	}

	public void commandAction(Command arg0, Displayable arg1) {
		// TODO Auto-generated method stub
		if(arg0.equals(close))
		{
			d.setCurrent(ret);
			return;
		}
		if(arg0.equals(ok))
		{
			if(tb.getString().equals(""))
				return;
			nt.writeToAir("<presence to=\""+tb.getString()+"\" type=\"subscribe\"/>");
			/*
			 * Send presence packet to server
			 * */
			d.setCurrent(ret);
			return;
		}
	}

}
