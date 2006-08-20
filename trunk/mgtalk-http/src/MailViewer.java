import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.StringItem;

import custom.RichText;

/**
 * 
 */

/**
 * @author Vorobev
 *
 * List of new mails here. For adding new mail addMail method used
 */

public class MailViewer implements CommandListener{

	/**
	 * mailCount automatically increments in addMail method and resets to 0 after closing this form
	 */
	private Form f;
	private Command exit;
	private Display d;
	private RosterList rl;
	private int mailCount = 0;
	private Image mailImg = RosterItem.getImage("/imgs/mail_icon.png");
	
	public MailViewer(Display d, RosterList rl) {
		super();
		this.d = d;
		this.rl = rl;
		f = new Form("No new mail");
		exit = new Command("Close", Command.OK, 0);
		f.addCommand(exit);
		f.setCommandListener(this);
		
		/*
		 * Nothing special. Initialization routine
		 * 
		 * */
	}

	public void addMail(String from, String subj, String body)
	{
		mailCount++;
		RichText rt = new RichText(d);
		rt.setCanvasWidth(rl.getRoster().getWidth());
		rt.addImage(mailImg);
		rt.addContent(" From: ", rt.getDefaultColor(), true);
		rt.addContent(from+"\n");
		rt.addContent("Subject: ", rt.getDefaultColor(), true);
		rt.addContent(subj+"\n");
		rt.addContent(body);
		rt.finish();
		f.insert(0, rt);
		f.setTitle("New messages: "+mailCount);
	}
	
	public void startMe()
	{
		d.setCurrent(f);
		/*
		 * Shows this form
		 * */
	}
	
	public void commandAction(Command arg0, Displayable arg1) {
		// TODO Auto-generated method stub
		
		if(arg0.equals(exit))
		{
			mailCount = 0;
			d.setCurrent(rl.getRoster());
			/*
			 * Resets counter, shows roster
			 * */
		}
	}

}
