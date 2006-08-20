import java.util.Vector;

/**
 * @author Vorobev
 * 
 * This class handles roster
 *
 */
public class RosterFactory {

	private Vector contacts;//Vector for RostItem instances
	private RosterList list;//Visible list
	public RosterFactory(RosterList list) {
		super();
		this.list = list;
		contacts = new Vector();
	}
	/**
	 * Clears roster
	 */
	public void clear()
	{
		contacts.removeAllElements();
	}
	
	/**
	 * Usually returns RosterItem under cursor
	 * @param i
	 * @return
	 */
	public RosterItem getItem(int i)
	{
		return (RosterItem) contacts.elementAt(i);
	}
	
	/**
	 * This routine updates contact's info. If any parameter is null, then aproprite field will not update
	 * @param jid
	 * @param name
	 * @param status
	 * @param statusStr
	 */
	public void updateContact(String jid, String name, String status, String statusStr)
	{
		String fullJid = jid;
		if(jid.indexOf("/")!=-1)
		{
			jid = jid.substring(0, jid.indexOf("/"));
		}
		RosterItem ri = null;
		
		int selIndex = list.getRoster().getSelectedIndex();
			
		for(int i=0; i<contacts.size(); i++)
		{
//			System.out.println("JID = "+jid+"="+((RosterItem)contacts.elementAt(i)).getJid());
			if(((RosterItem)contacts.elementAt(i)).getJid().equals(jid))
			{//If we found item in roster, we delete this item from contacts and from list
				ri = (RosterItem)contacts.elementAt(i);
				contacts.removeElement(ri);
				//If we dont want to show offline users and user gone offline - we delete this contact from roster
				if(list.getProfile().getOffline()==1 || ri.getStatus()!=0)
					list.getRoster().delete(i);
				break;
			}
		}
		if(ri==null)
		{
			ri = new RosterItem(list, list.getDisplay());
			ri.setJid(jid);
		}
		//Updating RosterItem by new values
		if(name!=null)
			ri.setName(name);
		if(status!=null)
		{
			if(status.equals(""))
				status = "online";
			if(status.equals("unavailable"))
				ri.setStatus(0, statusStr, fullJid);
			if(status.equals("online"))
				ri.setStatus(1, statusStr, fullJid);
			if(status.equals("away"))
				ri.setStatus(2, statusStr, fullJid);
			if(status.equals("xa"))
				ri.setStatus(3, statusStr, fullJid);
			if(status.equals("dnd"))
				ri.setStatus(4, statusStr, fullJid);
		}
		//Next we insert contact into new position
		insertContact(ri);
		if(selIndex>-1 && list.getRoster().size()<selIndex)
			list.getRoster().setSelectedIndex(selIndex, true);
	}
	
	/**
	 * This routine inserts contact into roster based on compare method result
	 * @param r
	 */
	private void insertContact(RosterItem r)
	{
		int newInd = -1;
		for(int i=0;i<contacts.size(); i++)
		{
			if(((RosterItem)contacts.elementAt(i)).compare(r, list.getProfile()))
			{
				contacts.insertElementAt(r, i);
				if(list.getProfile().getOffline()==1 || r.getStatus()!=0)
					list.getRoster().insert(i, r.getFullName(), r.getImg());
				return;
			}
		}
		contacts.addElement(r);
		//Show offline users support
		if(list.getProfile().getOffline()==1 || r.getStatus()!=0)
			list.getRoster().append(r.getFullName(), r.getImg());
	}
	
	/**
	 * This routine simply deletes from and then adds contact into roster
	 * @param jid
	 */
	public void refreshItem(String jid)
	{
		if(jid.indexOf("/")!=-1)
		{
			jid = jid.substring(0, jid.indexOf("/"));
		}
		RosterItem ri = null;
		for(int i=0; i<contacts.size(); i++)
		{
//			System.out.println("JID = "+jid+"="+((RosterItem)contacts.elementAt(i)).getJid());
			if(((RosterItem)contacts.elementAt(i)).getJid().equals(jid))
			{
				ri = (RosterItem)contacts.elementAt(i);
				contacts.removeElement(ri);
				if(list.getProfile().getOffline()==1 || ri.getStatus()!=0)
					list.getRoster().delete(i);
				insertContact(ri);
				return;
			}
		}
		
	}
	
	/**
	 * This routine simply deletes contact from roster
	 * @param jid
	 */
	public void deleteItem(String jid)
	{
		if(jid.indexOf("/")!=-1)
		{
			jid = jid.substring(0, jid.indexOf("/"));
		}
		RosterItem ri = null;
		for(int i=0; i<contacts.size(); i++)
		{
			if(((RosterItem)contacts.elementAt(i)).getJid().equals(jid))
			{
				ri = (RosterItem)contacts.elementAt(i);
				contacts.removeElement(ri);
				if(list.getProfile().getOffline()==1 || ri.getStatus()!=0)
					list.getRoster().delete(i);
				return;
			}
		}
		
	}
	
	/**
	 * This routine searches contact in a roster, adds new message into contacts history and 
	 * plays sound and refreshes contact position
	 * @param jid
	 * @param mess
	 * @param id
	 */
	public void addMessage(String jid, String mess, String id)
	{
		//
		if(mess.trim().equals(""))
			return;
		if(jid.indexOf("/")!=-1)
		{
			jid = jid.substring(0, jid.indexOf("/"));
		}
		RosterItem ri = null;
		for(int i=0; i<contacts.size(); i++)
		{
//			System.out.println("JID = "+jid+"="+((RosterItem)contacts.elementAt(i)).getJid());
			if(((RosterItem)contacts.elementAt(i)).getJid().equals(jid))
			{
				list.playMessage();
				ri = (RosterItem)contacts.elementAt(i);
				ri.addMessage(mess.trim(), true);
				ri.setSession(id);
				contacts.removeElement(ri);
				if(list.getProfile().getOffline()==1 || ri.getStatus()!=0)
					list.getRoster().delete(i);
				insertContact(ri);
				return;
			}
		}
	}
	
}
