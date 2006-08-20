import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * 
 */

/**
 * @author Kostya
 *
 * JabberProfile Bean stores profile info, converts info from and to byteArray
 */
public class JabberProfile {
	/*
	 * Default values for data fields
	 * */
	public int COLOR_TO = 0x00dd00;
	public int COLOR_FROM = 0xff0000;
	
	
	private String name = "GTalk", 
	host = "talk.google.com",
	httpbindurl = "https://www.butterfat.net:443/punjab/httpb/",
	user = "@gmail.com",
	pass = "", 
	status="Im here!", showStr = "";
	private int ssl = 0, httpbind = 1, auto = 0, port = 5222, sortBy = 0, 
	statusID  = 0, offline = 1, id = 0, isGoogle = 1, 
	lockStatus = 0, lockStatusStr = 1, 
	autoReconnect = 1, histLength = 10, volume = 100, smiles = 1;
	
	/**
	 * 
	 */
	public JabberProfile() {
		super();
	}

	/**
	 * @return converts data fields to byteArray
	 */
	public byte[] toByteArray()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(baos);
		try {
			os.writeUTF(name);
			os.writeUTF(host);
			os.writeUTF(httpbindurl);
			os.writeUTF(user);
			os.writeUTF(pass);
			os.writeUTF(status);
			os.writeInt(ssl);
			os.writeInt(httpbind);
			os.writeInt(auto);
			os.writeInt(port);
			os.writeInt(sortBy);
			os.writeInt(statusID);
			os.writeInt(offline);
			os.writeInt(isGoogle);
			os.writeInt(lockStatus);
			os.writeInt(lockStatusStr);
			os.writeInt(autoReconnect);
			os.writeInt(histLength);
			os.writeInt(volume);
			os.writeInt(smiles);
			os.flush();
			System.out.println("baos.size = "+baos.size());
			for(int i=baos.size(); i<255; i++)
			{
				os.writeInt(0);
			}
			/*
			 * Adding extra bytes for future use
			 * */
			os.close();
			return baos.toByteArray();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new byte[0];
		}
	}
	
	/**
	 * converts byteArray to data fileds
	 * @param data byteArray from RecordStore
	 * @return
	 */
	public boolean fromByteArray(byte data[])
	{
		try {
			DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
			name = is.readUTF();
			host = is.readUTF();
			httpbindurl = is.readUTF();
			user = is.readUTF();
			pass = is.readUTF();
			status = is.readUTF();
			ssl = is.readInt();
			httpbind = is.readInt();
			auto = is.readInt();
			port = is.readInt();
			sortBy = is.readInt();
			statusID = is.readInt();
			offline = is.readInt();
			isGoogle = is.readInt();
			lockStatus = is.readInt();
			lockStatusStr = is.readInt();
			autoReconnect = is.readInt();
			histLength = is.readInt();
			setVolume(is.readInt());
			setSmiles(is.readInt());
			is.close();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * @return Returns the host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host The host to set.
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the pass.
	 */
	public String getPass() {
		return pass;
	}

	/**
	 * @param pass The pass to set.
	 */
	public void setPass(String pass) {
		this.pass = pass;
	}

	/**
	 * @return Returns the ssl.
	 */
	public int getSsl() {
		return ssl;
	}

	/**
	 * @param ssl The ssl to set.
	 */
	public void setSsl(int ssl) {
		this.ssl = ssl;
	}

	/**
	 * @return Returns the status.
	 */
	public String getStatus() {
		return status==null? "" : status;
	}

	/**
	 * @param status The status to set.
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return Returns the user.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user The user to set.
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return Returns the auto.
	 */
	public int getAuto() {
		return auto;
	}

	/**
	 * @param auto The auto to set.
	 */
	public void setAuto(int auto) {
		this.auto = auto;
	}

	/**
	 * @return Returns the port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port The port to set.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	public String toString()
	{
		/*
		 * For debug purposes
		 * */
		return "name = "+name+", host = "+host+", user = "+user+", status = "+status+", id = "+id;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return Returns the offline.
	 */
	public int getOffline() {
		return offline;
	}

	/**
	 * @param offline The offline to set.
	 */
	public void setOffline(int offline) {
		this.offline = offline;
	}

	/**
	 * @return Returns the sortBy.
	 */
	public int getSortBy() {
		return sortBy;
	}

	/**
	 * @param sortBy The sortBy to set.
	 */
	public void setSortBy(int sortBy) {
		this.sortBy = sortBy;
	}

	/**
	 * @return Returns the statusID.
	 */
	public int getStatusID() {
		return statusID;
	}

	/**
	 * @param statusID The statusID to set.
	 */
	public void setStatusID(int statusID) {
		this.statusID = statusID;
	}

	/**
	 * @return Returns the isGoogle.
	 */
	public int getIsGoogle() {
		return isGoogle;
	}

	/**
	 * @param isGoogle The isGoogle to set.
	 */
	public void setIsGoogle(int isGoogle) {
		this.isGoogle = isGoogle;
	}

	/**
	 * @return Returns the statusStr.
	 */
	public String getShowStr() {
		switch(getStatusID())
		{
		case 0:
			return "";
		case 1:
			return "away";
		case 2:
			return "xa";
		case 3:
			return "dnd";
		}
		return "";
	}

	/**
	 * @param showStr The statusStr to set.
	 */
	public void setShowStr(String showStr) {
		this.showStr = showStr;
	}

	public int getLockStatus() {
		return lockStatus;
	}

	public void setLockStatus(int lockStatus) {
		this.lockStatus = lockStatus;
	}

	public int getLockStatusStr() {
		return lockStatusStr;
	}

	public void setLockStatusStr(int lockStatusStr) {
		this.lockStatusStr = lockStatusStr;
	}

	/**
	 * @return Returns the autoReconnect.
	 */
	public int getAutoReconnect() {
		return autoReconnect;
	}

	/**
	 * @param autoReconnect The autoReconnect to set.
	 */
	public void setAutoReconnect(int autoReconnect) {
		this.autoReconnect = autoReconnect;
	}

	/**
	 * @return Returns the histLength.
	 */
	public int getHistLength() {
		return histLength<=0?10:histLength;
	}

	/**
	 * @param histLength The histLength to set.
	 */
	public void setHistLength(int histLength) {
		this.histLength = histLength;
	}

	/**
	 * @return Returns the volume.
	 */
	public int getVolume() {
		return volume;
	}

	/**
	 * @param volume The volume to set.
	 */
	public void setVolume(int volume) {
		if(volume==0)
			volume = 100;
		this.volume = volume;
	}

	/**
	 * @return Returns the smiles.
	 */
	public int getSmiles() {
		return smiles;
	}

	/**
	 * @param smiles The smiles to set.
	 */
	public void setSmiles(int smiles) {
		this.smiles = smiles;
	}

	/**
	 * @return the httpbind
	 */
	public int getHttpbind() {
		return httpbind;
	}

	/**
	 * @param httpbind the httpbind to set
	 */
	public void setHttpbind(int httpbind) {
		this.httpbind = httpbind;
	}

	/**
	 * @return the httpbindurl
	 */
	public String getHttpbindurl() {
		return httpbindurl;
	}

	/**
	 * @param httpbindurl the httpbindurl to set
	 */
	public void setHttpbindurl(String httpbindurl) {
		this.httpbindurl = httpbindurl;
	}

}
