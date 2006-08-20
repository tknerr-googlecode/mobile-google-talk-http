import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

/**
 * 
 */

/**
 * @author Vorobev
 *
 */
public class LogViewer implements CommandListener{

	/**
	 * 
	 */
	private Form f;
	private Command exit;
	private Display d;
	private RosterList rl;
	
	public LogViewer(Display d, RosterList rl) {
		super();
		this.d = d;
		this.rl = rl;
		f = new Form("Log:");
		exit = new Command("Roster", Command.OK, 0);
		f.addCommand(exit);
		f.setCommandListener(this);
		// TODO Auto-generated constructor stub
	}

	public void addLog(String head, String body)
	{
//		f.append(new StringItem(head, body));
	}
	
	public void startMe()
	{
		d.setCurrent(f);		
	}
	
	public void commandAction(Command arg0, Displayable arg1) {
		// TODO Auto-generated method stub
		
		if(arg0.equals(exit))
		{
			d.setCurrent(rl.getRoster());
		}
	}

}
