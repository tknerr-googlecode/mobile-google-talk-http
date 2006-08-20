import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 * @author Vorobev
 *
 * This class uses for edit profiles
 * It's very simple
 */
public class ProfileEdit implements CommandListener{

	private Form f;
	private JabberProfile j;
	private Command c1, c2, c3;
	private RecordStore rs;
	private Display d;
	private TextField name, host, httpbindurl, user, pass, status, port, histLength;
	private ChoiceGroup ssl, httpbind, auto, sortBy, statusID, offline, isGoogle, 
		lockStatus, lockStatusStr, autoReconnect, smiles;
	private Gauge volume;
	public ProfileEdit(Display d, JabberProfile jp, RecordStore r) {
		super();
		j = jp;
		rs = r;
		this.d = d;
		f = new Form("Jabber profile");
		c1 = new Command("Save", Command.CANCEL, 1);
		c2 = new Command("Delete", Command.OK, 0);
		c3 = new Command("Cancel", Command.OK, 0);
		f.addCommand(c2);
		f.addCommand(c3);
		f.addCommand(c1);
		f.setCommandListener(this);
//		f.append(new StringItem(null, "Profile name:"));
		name = new TextField("Profile name:", j.getName(), 20, 0);
		
		host = new TextField("Host name:", j.getHost(), 20, 0);
		
		httpbindurl = new TextField("HTTP Binding url (https url for encryption):", j.getHttpbindurl(), 60, 0);
		
		port = new TextField("Port:", ""+j.getPort(), 5, 0);
		port.setConstraints(TextField.NUMERIC);
		
		histLength = new TextField("Chat history length:", ""+j.getHistLength(), 3, 0);
		histLength.setConstraints(TextField.NUMERIC);
		
		user = new TextField("User name:", j.getUser(), 40, 0);

		pass = new TextField("Password:", j.getPass(), 40, 0);
		pass.setConstraints(TextField.PASSWORD);

		status = new TextField("Status text:", j.getStatus(), 40, 0);
		String[] s = {"Yes"};
		ssl = new ChoiceGroup("SSL?", ChoiceGroup.MULTIPLE, s, null);
		ssl.setSelectedIndex(0, j.getSsl()!=0);
		httpbind = new ChoiceGroup("Use HTTB Binding?", ChoiceGroup.MULTIPLE, s, null);
		httpbind.setSelectedIndex(0, j.getHttpbind()!=0);
		
		auto = new ChoiceGroup("Auto connect", ChoiceGroup.MULTIPLE, s, null);
		auto.setSelectedIndex(0, j.getAuto()!=0);

		String[] empty = {};
		sortBy = new ChoiceGroup("Sort by list", ChoiceGroup.EXCLUSIVE, empty, null);
		sortBy.append("Status, Name", null);
		sortBy.append("Name", null);
		sortBy.setSelectedIndex(0, j.getSortBy()==0);
		sortBy.setSelectedIndex(1, j.getSortBy()!=0);
		
		statusID = new ChoiceGroup("Initial status", ChoiceGroup.EXCLUSIVE, empty, null);
		
		statusID.append("Online", null);
		statusID.append("Away", null);
		statusID.append("XA", null);
		statusID.append("Busy", null);
		
		statusID.setSelectedIndex(0, j.getStatusID()==0);
		statusID.setSelectedIndex(1, j.getStatusID()==1);
		statusID.setSelectedIndex(2, j.getStatusID()==2);
		statusID.setSelectedIndex(3, j.getStatusID()==3);

		offline = new ChoiceGroup("Show offline users?", ChoiceGroup.MULTIPLE, s, null);
		offline.setSelectedIndex(0, j.getOffline()!=0);

		isGoogle = new ChoiceGroup("GTalk features?", ChoiceGroup.MULTIPLE, s, null);
		isGoogle.setSelectedIndex(0, j.getIsGoogle()!=0);

		lockStatus = new ChoiceGroup("GTalk  - disable status change by other clients?", ChoiceGroup.MULTIPLE, s, null);
		lockStatus.setSelectedIndex(0, j.getLockStatus()!=0);

		lockStatusStr = new ChoiceGroup("GTalk  - set status text after login?", ChoiceGroup.MULTIPLE, s, null);
		lockStatusStr.setSelectedIndex(0, j.getLockStatusStr()!=0);

		autoReconnect = new ChoiceGroup("Enable auto reconnect?", ChoiceGroup.MULTIPLE, s, null);
		autoReconnect.setSelectedIndex(0, j.getAutoReconnect()!=0);

		smiles = new ChoiceGroup("Show smiles?", ChoiceGroup.MULTIPLE, s, null);
		smiles.setSelectedIndex(0, j.getSmiles()!=0);

		volume = new Gauge("Set initial volume", true, 10, j.getVolume()/10);
		
		f.append(name);
		f.append(user);
		f.append(pass);
		f.append(host);
		f.append(ssl);
		f.append(port);
		f.append(httpbind);
		f.append(httpbindurl);
		f.append(auto);
		f.append(autoReconnect);
		f.append(isGoogle);
		f.append(statusID);
		f.append(lockStatus);
		f.append(status);
		f.append(lockStatusStr);
		f.append(sortBy);
		f.append(offline);
		f.append(histLength);
		f.append(smiles);
		f.append(volume);
		
		d.setCurrent(f);
		// TODO Auto-generated constructor stub
	}
	public void commandAction(Command arg0, Displayable arg1) {
		
		if(arg0.equals(c2))
		{//Delete button
			System.out.println("ID = "+j.getId());
			if(j.getId()!=0)
			{
				try {
					System.out.println("Delete start");
					rs.deleteRecord(j.getId());
					rs.closeRecordStore();
					System.out.println("Delete end");
				} catch (RecordStoreNotOpenException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidRecordIDException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RecordStoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(arg0.equals(c1))
		{//Save button
			j.setName(name.getString());
			j.setUser(user.getString());
			j.setPass(pass.getString());
			j.setHost(host.getString());
			j.setHttpbindurl(httpbindurl.getString());
			j.setStatus(status.getString());
			j.setPort(Integer.parseInt(port.getString()));
			j.setHistLength(Integer.parseInt(histLength.getString()));
			j.setSsl(ssl.isSelected(0)? 1 : 0);
			j.setHttpbind(httpbind.isSelected(0)? 1 : 0);
			j.setAuto(auto.isSelected(0)? 1 : 0);
			j.setSortBy(sortBy.getSelectedIndex());
			j.setStatusID(statusID.getSelectedIndex());
			j.setOffline(offline.isSelected(0)? 1 : 0);
			j.setIsGoogle(isGoogle.isSelected(0)? 1 : 0);
			j.setLockStatus(lockStatus.isSelected(0)? 1 : 0);
			j.setLockStatusStr(lockStatusStr.isSelected(0)? 1 : 0);
			j.setAutoReconnect(autoReconnect.isSelected(0)? 1 : 0);
			j.setSmiles(smiles.isSelected(0)? 1 : 0);
			j.setVolume(volume.getValue()==0?1:volume.getValue()*10);
			try {
				System.out.println("J = "+j);
				if(j.getId()!=0)
				{
					System.out.println("Saving old");
					rs.setRecord(j.getId(), j.toByteArray(), 0, j.toByteArray().length);
				}
				else
				{
					System.out.println("Adding new "+j.toByteArray().length);
					rs.addRecord(j.toByteArray(), 0, j.toByteArray().length);
				}
				if(j.getAuto()>0)
				{
					for(RecordEnumeration e = rs.enumerateRecords(null, null, false); e.hasNextElement();)
					{
						//
						int id = e.nextRecordId();
						JabberProfile tmp = new JabberProfile();
						tmp.fromByteArray(rs.getRecord(id));
						if(id!=j.getId() && tmp.getAuto()>0)
						{
							tmp.setAuto(0);
							rs.setRecord(id, tmp.toByteArray(), 0, tmp.toByteArray().length);
						}
					}
				}
				rs.closeRecordStore();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ProfileList l = new ProfileList(d);
		System.out.println("Save finish");			
	}
}
