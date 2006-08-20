import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

/**
 * 
 */

/**
 * @author Vorobev
 * 
 * This simple class requests decision from user about accepting or rejecting authorization request
 */
public class RequestAuth implements CommandListener{

	/**
	 * 
	 */
	private Displayable ret;
	private Command ok, cancel;
	private Form form;
	private NetworkThread nt;
	private Display d;
	private String user, from;
	
	public RequestAuth(Display d, NetworkThread nt, Displayable disp, String user, String from) {
		super();
		form = new Form("Request");
		form.append(new StringItem("User "+user+" requests authorization. Add user to roster?", null));
		ok = new Command("Add", Command.EXIT, 0);
		cancel = new Command("Deny", Command.OK, 0);
		form.addCommand(ok);
		form.addCommand(cancel);
		form.setCommandListener(this);
		ret = disp;
		this.user = user;
		this.from = from;
		this.d = d;
		this.nt = nt;
		d.setCurrent(form);
		// TODO Auto-generated constructor stub
	}

	public void commandAction(Command arg0, Displayable arg1) {
		// TODO Auto-generated method stub
		if(arg0.equals(ok))
		{
			//Write appropriate packet if user accepts request
			nt.writeToAir("<presence to=\""+user+"\" type=\"subscribed\"/><presence from=\""+from+"\" to=\""+user+"\" type=\"subscribe\"/>");
			d.setCurrent(ret);
			return;
		}
		
		if(arg0.equals(cancel))
		{
			d.setCurrent(ret);
			return;			
		}
		
	}

}
