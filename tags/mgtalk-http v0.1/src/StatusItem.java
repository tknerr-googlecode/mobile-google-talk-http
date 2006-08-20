/**
 * 
 */

/**
 * @author Vorobev
 *
 */
public class StatusItem {

	/**
	 * 
	 */
	private String jid = "", statusText = "";
	private int status = 0;
	
	public StatusItem()
	{
		super();
	}
	
	public StatusItem(String jid, String statusText, int status) {
		super();
		setJid(jid);
		setStatus(status);
		setStatusText(statusText);
		// TODO Auto-generated constructor stub
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
		this.jid = jid.toLowerCase();
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
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return Returns the statusText.
	 */
	public String getStatusText() {
		return statusText;
	}

	/**
	 * @param statusText The statusText to set.
	 */
	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}
	
	public String toString()
	{
		return jid;
	}
}
