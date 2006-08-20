import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;

import custom.RichText;

/**
 * @author Vorobev
 * This routine holds info about roster item
 *
 */
public class RosterItem {
	public static String[] smiles = {":))", ":-))", "=))", ":)", ":-)", "=)", 
		";)", ";-)", ":D", ":-D", "=D",":P",":-P","=P",":-p",":p","=p",
		";P",";-P",";p",";-p",":lol:","<lol>","lol ","LOL ",":*",":-*",":{}",":-{}",
		":(",":-(","=(",":<",":-<","=<",":cry:","B)","B-)"};
	public static String[] smilep = {"3",   "3",    "3",   "1",  "1",   "1",  
		"2",  "2",   "3",  "3",   "3", "4", "4",  "4", "4",  "4", "4",
		"5", "5",  "5", "5"  ,"6",    "6",    "6",   "6",   "7", "7",  "7",  "7",
		"8", "8",  "8", "9", "9",  "9", "9",    "10","10"};

	
	private String name = "";
	private String statusStr = "";
	private String jid = "";
	private String fullJid = "";//jid with resource
	private Vector mess = new Vector();//History messages
	private Vector times = new Vector();//History times
	boolean isNew = false;//History has new unread message
	private String session = "0";//Chat session
	
	private Vector statuses = new Vector();
	
	private MessageForm form = null;//Separate MessageForm for each contact
	private Display d;
	private RosterList rl;
	
	private int status = 0;
	private int index = -1;
	/*
	 * Static fields for pictures
	 * */
	public static Image online = getImage("/imgs/status_avail.png");
	public static Image offline = getImage("/imgs/status_offline.png");
	public static Image away = getImage("/imgs/status_idle.png");
	public static Image xa = getImage("/imgs/status_xa.png");
	public static Image dnd = getImage("/imgs/status_busy.png");
	//0 - Offline, 1 - Online, 2 - Away, 3 - XA, 4 - DND

	
	/**
	 * @return String containing history for a contact
	 */
	/**
	 * Creates or returns MessageForm 
	 * @param d
	 * @param rl
	 */
	public void startForm()
	{
		isNew = false;
		setStatusInfoForMessage();
		form.startForm();
	}
	
	/**
	 * Adds new message into history
	 * @param m
	 * @param from
	 */
	public void addMessage(String m, boolean from)
	{
		mess.addElement(m);
		Calendar cl = Calendar.getInstance();
		int h = 0;
		if(cl.getTimeZone().useDaylightTime())
			h = cl.get(Calendar.HOUR_OF_DAY)+1;
		else
			h = cl.get(Calendar.HOUR_OF_DAY);
		String t = h+":"+(cl.get(Calendar.MINUTE)<10? "0"+cl.get(Calendar.MINUTE) : ""+cl.get(Calendar.MINUTE));
		RichText message = new RichText(d);
		
		message.setCanvasWidth(getMessageForm().getForm().getWidth());
		
		if(from)
		{
			times.addElement(t+" << ");
			message.addContent(t+" << ", rl.getProfile().COLOR_FROM, true);
			message.setDefaultCommand(getMessageForm().getQuote());
			message.setItemCommandListener(getMessageForm());
		}
		else
		{
			times.addElement(t+" >> ");
			message.addContent(t+" >> ", rl.getProfile().COLOR_TO, true);
		}
		if(rl.getProfile().getSmiles()>0)
		{
			//Smile processor here
			Vector strs = new Vector();
			strs.addElement(m);
			Vector smils = new Vector();
			for(int i=0; i<smiles.length; i++)
			{
				for(int j=0; j<strs.size(); j++)
				{
					int ind = -1;
					String s =strs.elementAt(j).toString(); 
					if((ind = s.indexOf(smiles[i]))!=-1)
					{
						String s1 = s.substring(0, ind);
						String s2 = s.substring(ind+smiles[i].length());
						strs.insertElementAt(s2, j+1);
						strs.removeElementAt(j);
						strs.insertElementAt(s1, j);
						smils.insertElementAt(smilep[i], j);
					}
				}
			}
			for(int i=0; i<strs.size(); i++)
			{
				message.addContent(strs.elementAt(i).toString());
				if(i<smils.size())
				{
					message.addImage(getImage("/smiles/"+smils.elementAt(i)+".png"));
				}
			}
		}
		else
			message.addContent(m);
		message.finish();
		getMessageForm().getForm().insert(2, message);
		isNew = true;
		if(d.getCurrent()==getMessageForm().getForm())
		{
			isNew = false;
		}
		if(mess.size()>rl.getProfile().getHistLength())
		{
			mess.removeElementAt(0);
			times.removeElementAt(0);
			getMessageForm().getForm().delete(getMessageForm().getForm().size()-1);
		}
	}
	/**
	 * If contact has unread messages - * adding
	 * @return
	 */
	public String getFullName()
	{
		if(isNew)
			return "* "+getName();
		else
			return getName();
	}
	
	public static Image getImage(String s)
	{
		try {
			return Image.createImage(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
	
	public RosterItem(RosterList rl, Display d) {
		super();
		this.rl = rl;
		this.d = d;
		
		// TODO Auto-generated constructor stub
	}
	
	private MessageForm getMessageForm()
	{
		if(form==null)
			form = new MessageForm(d, rl, this);
		return form;
	}
	
	private int min(int i1, int i2)
	{
		if(i1>i2)
			return i2;
		else
			return i1;
	}
	
	/**
	 * This routine compares two Roster items based on sorting rules from profile
	 * @param r
	 * @param jp
	 * @return
	 */
	public boolean compare(RosterItem r, JabberProfile jp)
	{
		if(isNew() && !r.isNew())
			return false;
		if(!isNew() && r.isNew())
			return true;
		if(getStatus()!=0 && r.getStatus()==0)
			return false;
		if(getStatus()==0 && r.getStatus()!=0)
			return true;
		if(getStatus()==r.getStatus() || jp.getSortBy()==1)
		{
			int len1 = getName().length();
			int len2 = r.getName().length();
			String str1 = getName().toLowerCase();
			String str2 = r.getName().toLowerCase();
			for(int i=0; i<min(len1, len2); i++)
			{
				if(str1.charAt(i)>str2.charAt(i))
				{
					return true;
				}
				if(str1.charAt(i)<str2.charAt(i))
				{
					return false;
				}
			}
			return len1<len2;
		}
		else
		{
			if(r.getStatus()==0)
				return false;
			
			switch (getStatus())
			{
			case 0:
				return true;
			case 1:
				return false;
			default:
				if(getStatus()>r.getStatus())
					return true;
				else
					return false;
			}
		}
	}
	/**
	 * @return Returns the name.
	 */
	public Image getImg()
	{
		switch(getStatus())
		{
		case 0:
			return offline;
		case 1:
			return online;
		case 3:
			return xa;
		case 4:
			return dnd;
		}
		return away;
	}
	
	public static Image getImageByStatus(int st)
	{
		switch(st)
		{
		case 0:
			return online;
		case 1:
			return away;
		case 2:
			return xa;
		case 3:
			return dnd;
		}
		return away;		
	}
	
	public String getName() {
		if(name!="")
			return name;
		return jid;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
		if(form!=null)
			getMessageForm().getForm().setTitle(name);
	}
	/**
	 * @return Returns the status.
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * @param status The status to set.
	 */
	public void setStatus(int status, String statusStr, String fullJid) {
		fullJid = fullJid.toLowerCase();
		StatusItem st = null;
		int stIndex = -1;
		for(int i = 0; i<statuses.size(); i++)
		{
			if(statuses.elementAt(i).toString().equals(fullJid))
			{
				stIndex = i;
				st = (StatusItem) statuses.elementAt(i);
			}
		}
		if(stIndex==-1)
		{
			//Need to insert new contact
			if(status==0)
				return;
			st = new StatusItem(fullJid, statusStr, status);
			statuses.addElement(st);
		}
		else
		{
			//Updating existing
			st.setStatus(status);
			st.setStatusText(statusStr);
			statuses.removeElementAt(stIndex);
			statuses.insertElementAt(st, 0);
		}
		//Deletes all offline statuses
		for(int i=statuses.size()-1; i>=0; i--)
		{
			if(((StatusItem)statuses.elementAt(i)).getStatus()==0)
			{
				statuses.removeElementAt(i);
			}
		}
		if(statuses.size()==0)
		{
			this.status = 0;
			this.statusStr = "";
		}
		int newStatus = 0;
		String newStatusStr = "";
		for(int i = 0; i<statuses.size(); i++)
		{
			st = (StatusItem) statuses.elementAt(i);
			if(st.getStatus()==1 && newStatus!=1)
			{
				newStatus = 1;
				newStatusStr = st.getStatusText();
			}
			if(st.getStatus()==2 && (newStatus==3 || newStatus==0))
			{
				newStatus = 2;
				newStatusStr = st.getStatusText();
			}
			if(st.getStatus()==3 && newStatus==0)
			{
				newStatus = 3;
				newStatusStr = st.getStatusText();
			}			

			if(st.getStatus()==4 && newStatus!=1)
			{
				newStatus = 4;
				newStatusStr = st.getStatusText();
			}			
		}
		this.status = newStatus;
		this.statusStr = newStatusStr;
		if(form!=null)
			setStatusInfoForMessage();
		//System.out.println(getStatusStr()+" = "+getStatus());
	}
	/**
	 * @return Returns the statusStr.
	 */
	public void setStatusInfoForMessage()
	{
		RichText rt = new RichText(rl.getDisplay());
		rt.setCanvasWidth(rl.getRoster().getWidth());
		rt.addImage(getImg());
		rt.addContent(" "+(getStatusStr().equals("")?"<No status text>":getStatusStr()));
		rt.finish();
		getMessageForm().getForm().setTitle(name);
		getMessageForm().getForm().delete(0);
		getMessageForm().getForm().insert(0, rt);
//		getMessageForm().setStatusHolder(rt);
	}
	
	public String getStatusStr() {
		return statusStr;
	}
	/**
	 * @return Returns the index.
	 */
	public int getIndex() {
		return index;
	}
	/**
	 * @param index The index to set.
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	/**
	 * @return Returns the jid.
	 */
	public String getJid() {
		return jid;
	}
	/**
	 * @param jid The jid to set.
	 */
	public void setJid(String jid) {
		this.jid = jid;
	}

	/**
	 * @return Returns the session.
	 */
	public String getSession() {
		return session;
	}

	/**
	 * @param session The session to set.
	 */
	public void setSession(String session) {
		this.session = session;
	}

	/**
	 * @return Returns the isNew.
	 */
	public boolean isNew() {
		return isNew;
	}

	/**
	 * @return Returns the mess.
	 */
	public Vector getMess() {
		return mess;
	}

}
