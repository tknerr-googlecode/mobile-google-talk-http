import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import custom.RichText;

/**
 * @author Vorobev
 *
 * Class ChangeStatus - Changes user status
 * 2 Commands - Ok, Cancel
 * 1 TextField - for custom status
 * 1 big ChoiceGroup - for selecting avialable statuses
 * online, dnd, away, xa - positions for "custom" group items 
 */
public class ChangeStatus implements CommandListener {

	private Form form;
	private Command ok, exit;
	private ChoiceGroup status;
	private TextField tf;
	private RosterList rl;
	private RichText statusText;
	private Display d;
	int online = -1, dnd = -1, away = -1, xa = -1;
	
	/**
	 * @param rl - RosterList instance
	 * @param d - Display instance
	 */
	public ChangeStatus(RosterList rl, Display d){
		super();
		this.rl = rl;
		this.d = d;
		form = new Form("Current status:");
		statusText = new RichText(d);
		statusText.setCanvasWidth(rl.getRoster().getWidth());
		statusText.addImage(RosterItem.getImageByStatus(rl.getProfile().getStatusID()));
		statusText.addContent(rl.getProfile().getStatus().equals("")?"<No status text>":rl.getProfile().getStatus());
		statusText.finish();
		form.append(statusText);
//		form.append(new ImageItem(rl.getProfile().getStatus(), RosterItem.getImageByStatus(rl.getProfile().getStatusID()), ImageItem.LAYOUT_EXPAND, rl.getProfile().getStatusStr().equals("")?
//				"online" : rl.getProfile().getStatusStr()));
		tf = new TextField("Input status: ", rl.getProfile().getStatus(), 255, TextField.ANY);
		form.append(tf);
		status = new ChoiceGroup("Select status: ", ChoiceGroup.EXCLUSIVE);

		online = 0;
		status.append("Set custom", RosterItem.online);
		if(rl.getProfile().getStatusID()==0 && (rl.getProfile().getStatus().equals("") || !rl.getThread().isGoogle()))
			status.setSelectedIndex(online, true);
/*		
 * If status text is empty or server not is GTalk and status is "Online" - Check this item
*/		
		for(int i=0; i<rl.getOnlines().size(); i++)
		{
			status.append(rl.getOnlines().elementAt(i).toString(), RosterItem.online);
			if(rl.getProfile().getStatus().equals(rl.getOnlines().elementAt(i).toString()) && 
					rl.getProfile().getStatusID()==0)
				status.setSelectedIndex(status.size()-1, true);
/*
 * If status text equals current group item and status is "Online" - Select this item			
*/		
		}
		
		if(!rl.getThread().isGoogle()) {//google only supports auto-away
			status.append("Set custom", RosterItem.away);
			away = status.size()-1;
			if(rl.getProfile().getStatusID()==1 && (rl.getProfile().getStatus().equals("") || !rl.getThread().isGoogle()))
			status.setSelectedIndex(away, true);
			/*		
			 * If status text is empty or server not is GTalk and status is "Away" - Check this item
			*/		
			for(int i=0; i<rl.getAways().size(); i++)
			{
				status.append(rl.getAways().elementAt(i).toString(), RosterItem.away);
				if(rl.getProfile().getStatus().equals(rl.getAways().elementAt(i).toString()) && 
						rl.getProfile().getStatusID()==1)
					status.setSelectedIndex(status.size()-1, true);
				/*
				 * If status text equals current group item and status is "Away" - Select this item			
				*/		
			}
		}

		if(!rl.getThread().isGoogle()) {
			status.append("Set custom", RosterItem.xa);
			/*		
			 * Support for XA status only for non GTalk servers
			*/		
			
			xa = status.size()-1;
			
			if(rl.getProfile().getStatusID()==2 && (rl.getProfile().getStatus().equals("") || !rl.getThread().isGoogle()))
				status.setSelectedIndex(xa, true);
		}
		/*		
		 * Support for XA status only for non GTalk servers
		*/		
		status.append("Set custom", RosterItem.dnd);
		dnd = status.size()-1;
		if(rl.getProfile().getStatusID()==3 && (rl.getProfile().getStatus().equals("") || !rl.getThread().isGoogle()))
			status.setSelectedIndex(dnd, true);
		/*		
		 * If status text is empty or server not is GTalk and status is "DND" - Check this item
		*/		
		for(int i=0; i<rl.getBusies().size(); i++)
		{
			status.append(rl.getBusies().elementAt(i).toString(), RosterItem.dnd);
			if(rl.getProfile().getStatus().equals(rl.getBusies().elementAt(i).toString()) && 
					rl.getProfile().getStatusID()==3)
				status.setSelectedIndex(status.size()-1, true);
			/*		
			 * If status text is empty or server not is GTalk and status is "Busy" - Check this item
			*/		
		}


		form.append(status);
		ok = new Command("Ok", Command.BACK, 0);
		exit = new Command("Cancel", Command.BACK, 0);
		form.addCommand(ok);
		form.addCommand(exit);
		form.setCommandListener(this);
		d.setCurrent(form);
		/*
		 * Start form working
		 * */
	}

	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	
	public void commandAction(Command arg0, Displayable arg1) {
		// TODO Auto-generated method stub
		if(arg0.equals(ok))
		{
			/*
			 * Ok Command selected
			 */
			if(status.getSelectedIndex()==online || status.getSelectedIndex()==dnd 
					|| status.getSelectedIndex()==away || status.getSelectedIndex()==xa)
			{
				rl.getProfile().setStatus(tf.getString());
				/*
				 * Custom status selected. Simply set TextField as status text
				 */
			}
			else
			{
				rl.getProfile().setStatus(status.getString(status.getSelectedIndex()));
				/*
				 * Extract group item text and set it as status text
				 */
			}
			
			/*
			 * Next, check selected status type and set status ID in profile
			 * */
			if(status.getSelectedIndex()>=dnd)
				rl.getProfile().setStatusID(3);
			else
			{
				if(status.getSelectedIndex()>=xa && !rl.getThread().isGoogle())
					rl.getProfile().setStatusID(2);
				else
					if(status.getSelectedIndex()>=away && !rl.getThread().isGoogle())
						rl.getProfile().setStatusID(1);
					else
						rl.getProfile().setStatusID(0);
			}
			/*
			 * Generate jabber packet and send it to server
			 * */
			rl.getThread().generatePresense();
			d.setCurrent(rl.getRoster());
			return;
		}
		if(arg0.equals(exit))
		{
			/*
			 * Simply close this form
			 * */
			d.setCurrent(rl.getRoster());
			return;
		}
	}
}
