import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;

/**
 * 
 */

/**
 * @author Vorobev
 *
 */
public class VolumeSetup implements CommandListener{

	/**
	 * 
	 */
	private Display d;
	private RosterList list;
	private Form form;
	private Gauge volume;
	private Command ok, cancel;
	
	public VolumeSetup(Display d, RosterList list) {
		super();
		this.d = d;
		this.list = list;
		
		form = new Form("Volume control");
		volume = new Gauge("Set current volume", true, 10, list.getProfile().getVolume()/10);
		form.append(volume);
		ok = new Command("Set", Command.BACK, 0);
		cancel = new Command("Cancel", Command.BACK, 1);
		form.addCommand(ok);
		form.addCommand(cancel);
		form.setCommandListener(this);
		d.setCurrent(form);
		// TODO Auto-generated constructor stub
	}

	public void commandAction(Command arg0, Displayable arg1) {
		// TODO Auto-generated method stub
		if(arg0.equals(ok))
		{
			list.getProfile().setVolume(volume.getValue()==0?1:volume.getValue()*10);
			d.setCurrent(list.getRoster());
			return;
		}
		if(arg0.equals(cancel))
		{
			d.setCurrent(list.getRoster());
			return;			
		}
		
	}

}
