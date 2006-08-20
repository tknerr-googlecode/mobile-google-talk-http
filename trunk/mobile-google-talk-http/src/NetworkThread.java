import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import com.twmacinta.util.MD5;

/**
 * @author Knerr
 * 
 * abstract class for transmitting stanzas over some 
 * network connection to a jabber server.
 * 
 */
public abstract class NetworkThread extends Thread {

	protected boolean ended = false; // Network error flag

	protected boolean busy = false; // Indicates if someone reads packet

	protected boolean google = false; // Google Talk server flag

	protected boolean terminated = false; // Indicates if someone closed

	// connection

	protected String token = ""; // Google token holder

	protected ConnectLog log;

//	protected boolean logging = true;

	protected boolean statusSet = false;
	
	/**
	 * Constructs thread and starts it
	 * 
	 * @param l
	 */
	public NetworkThread(ConnectLog l) {
		log = l;
		start();
	}

	/**
	 * thread routine to keep the connection to the server open and handle
	 * stanzas during the session
	 */
	public abstract void run();


	/**
	 * Initializes session with Jabber server
	 * 
	 * @param addr
	 * @param domain
	 * @param user
	 * @param pass
	 * @param resource
	 * @param Status
	 * @throws Exception
	 */
	protected abstract void startSession(String addr, String domain,
			String user, String pass, String resource, String Status)
			throws Exception;

	/**
	 * Terminates current NetworkThread softly Closes connection, informs user
	 * about it, and sets ended flag
	 * 
	 * @param reconnect tries to reconnect if set to true and autoreconnect is enabled
	 */
	public abstract void terminate(boolean reconnect);

	/**
	 * This routine writes packet to the output stream. If Exception thrown, it
	 * tries to reconnect (terminate(true) is called)
	 * 
	 * @param mess
	 */
	protected abstract void writeToAir(String mess);
	
	/**
	 * reads the content of the next stanza which is not empty.
	 * Blocks until next stanza is available;
	 * @return
	 */
	protected abstract XmlNode readStanza();
	
	/**
	 * Converts String to UTF-8 String. Code from Colibry IM messenger used
	 * 
	 * @param s
	 *            String to convert
	 * @return converted String
	 */
	protected static String ToUTF(String s) {
		int i = 0;
		StringBuffer stringbuffer = new StringBuffer();

		for (int j = s.length(); i < j; i++) {
			int c = (int) s.charAt(i);
			if ((c >= 1) && (c <= 0x7f)) {
				stringbuffer.append((char) c);
			}
			if (((c >= 0x80) && (c <= 0x7ff)) || (c == 0)) {
				stringbuffer.append((char) (0xc0 | (0x1f & (c >> 6))));
				stringbuffer.append((char) (0x80 | (0x3f & c)));
			}
			if ((c >= 0x800) && (c <= 0xffff)) {
				stringbuffer.append(((char) (0xe0 | (0x0f & (c >> 12)))));
				stringbuffer.append((char) (0x80 | (0x3f & (c >> 6))));
				stringbuffer.append(((char) (0x80 | (0x3f & c))));
			}
		}

		return stringbuffer.toString();
	}

	/**
	 * Base16 encodes input string
	 * 
	 * @param s
	 *            input string
	 * @return output string
	 */
	protected String Base16Encode(String s) {
		String res = "";
		for (int i = 0; i < s.length(); i++) {
			res += Integer.toHexString(s.charAt(i));
		}
		return res;
	}

	/**
	 * This routine generates MD5-DIGEST response via SASL specification
	 * 
	 * @param user
	 * @param pass
	 * @param realm
	 * @param digest_uri
	 * @param nonce
	 * @param cnonce
	 * @return
	 */
	protected String generateAuthResponse(String user, String pass,
			String realm, String digest_uri, String nonce, String cnonce) {
		String val1 = user + ":" + realm + ":" + pass;
		byte bb[] = new byte[17];
		bb = md5It(val1);
		int sl = new String(":" + nonce + ":" + cnonce).length();
		byte cc[] = new String(":" + nonce + ":" + cnonce).getBytes();
		byte bc[] = new byte[99];
		for (int i = 0; i < 16; i++) {
			bc[i] = bb[i];
		}
		for (int i = 16; i < sl + 16; i++) {
			bc[i] = cc[i - 16];
		}
		String val2 = new String(MD5.toHex(md5It(bc, sl + 16)));
		String val3 = "AUTHENTICATE:" + digest_uri;
		val3 = MD5.toHex(md5It(val3));
		String val4 = val2 + ":" + nonce + ":00000001:" + cnonce + ":auth:"
				+ val3;
		// System.out.println("Before auth = "+val4+", val1 = "+val1);
		val4 = MD5.toHex(md5It(val4));
		// System.out.println("Val4 = "+val4);

		String enc = "charset=utf-8,username=\"" + user + "\",realm=\"" + realm
				+ "\"," + "nonce=\"" + nonce + "\",cnonce=\"" + cnonce + "\","
				+ "nc=00000001,qop=auth,digest-uri=\"" + digest_uri + "\","
				+ "response=" + val4;
		String resp = MD5.toBase64(enc.getBytes());
		return resp;
	}

	/**
	 * MD5 routines
	 * 
	 * @param s
	 * @return
	 */
	protected byte[] md5It(String s) {
		byte bb[] = new byte[16];
		try {
			MD5 md2 = new MD5(s.getBytes());
			return md2.doFinal();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bb;
	}

	/**
	 * MD5 routines
	 * 
	 * @param s
	 * @param l
	 * @return
	 */
	protected byte[] md5It(byte[] s, int l) {
		byte bb[] = new byte[16];
		try {
			byte tmp[] = new byte[l];
			for (int i = 0; i < l; i++) {
				tmp[i] = s[i];
			}
			MD5 md2 = new MD5(tmp);
			return md2.doFinal();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bb;
	}

	/**
	 * Service routine
	 * 
	 * @param dis
	 * @return
	 */
	private String readLine(DataInputStream dis) {
		String s = "";
		byte ch = 0;
		try {
			while ((ch = dis.readByte()) != -1) {
				// System.out.println("ch = "+ch);
				if (ch == '\n')
					return s;
				s += (char) ch;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
	
	/**
	 * URL-encodes the given string
	 * @param s
	 * @return
	 */
	private static String URLencode(String s)
	{
		if (s!=null) {
			StringBuffer tmp = new StringBuffer();
			int i=0;
			try {
				while (true) {
					int b = (int)s.charAt(i++);
					if ((b>=0x30 && b<=0x39) || (b>=0x41 && b<=0x5A) || (b>=0x61 && b<=0x7A)) {
						tmp.append((char)b);
					}
					else {
						tmp.append("%");
						if (b <= 0xf) tmp.append("0");
						tmp.append(Integer.toHexString(b));
					}
				}
			}
			catch (Exception e) {}
			return tmp.toString();
		}
		return null;
	}

	
	/**
	 * Generates X-GOOGLE-TOKEN response by communication with
	 * http://www.google.com
	 * 
	 * @param userName
	 * @param passwd
	 * @return
	 */
	protected String getGoogleToken(String userName, String passwd) {
		String first = "Email=" + URLencode(userName) + "&Passwd=" + URLencode(passwd)
				+ "&PersistentCookie=false&source=googletalk";
		log("getting token for: "+first);
		HttpConnection c = null;
		DataInputStream dis = null;
		OutputStream os = null;
		try {
			c = (HttpConnection) Connector
					.open("https://www.google.com:443/accounts/ClientAuth");
			c.setRequestMethod("POST");
			c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			c.setRequestProperty("Content-Length", ""+first.getBytes());
			os = c.openOutputStream();
			os.write(first.getBytes());
			os.close();
			log.addMessage("Connecting to www.google.com");
			dis = c.openDataInputStream();
			String str = readLine(dis);
			String SID = "";
			String LSID = "";
			if (str.startsWith("SID=") && !ended) {
				SID = str.substring(4, str.length());
				str = readLine(dis);
				LSID = str.substring(5, str.length());
				first = "SID=" + SID + "&LSID=" + LSID
						+ "&service=mail&Session=true";
				dis.close();
				c.close();
				c = (HttpConnection) Connector
						.open("https://www.google.com:443/accounts/IssueAuthToken");
				c.setRequestMethod("POST");
				c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				c.setRequestProperty("Content-Length", ""+first.getBytes());
				os = c.openOutputStream();
				os.write(first.getBytes());
				os.close();
				log.addMessage("Next www.google.com connection");
				dis = c.openDataInputStream();
				str = readLine(dis);
				String token = MD5.toBase64(new String("\0" + userName + "\0"
						+ str).getBytes());
				dis.close();
				c.close();
				return token;
			} else
				throw new Exception("Invalid response: "+str);
		} catch (Exception ex) {
			//ex.printStackTrace();
			log.addMessage("Exception while getting Google Token: "+ex.getMessage());
		} finally {
			try { if(dis!=null) dis.close(); } catch (Exception e) {}
			try { if(os!=null) os.close(); } catch (Exception e) {}
			try { if(c!=null) c.close(); } catch (Exception e) {}
		}
		return "";
	}
	
	/**
	 * authenticates the user with the most appropriate mechanism 
	 * @param x	features
	 * @param user
	 * @param pass
	 * @param domain
	 * @throws Exception
	 */
	protected void doAuthentication(XmlNode x, String user, String pass, String domain) throws Exception {
		if (x.child("mechanisms").hasValueOfChild("DIGEST-MD5")) {
			// DIGEST-MD5 authorization doing
			log("MD5 authorization doing");
			writeToAir("<auth xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" mechanism=\"DIGEST-MD5\"/>");
			log.addMessage("Sending authorization data");
			x = readStanza();
			if (x.getName().equals("failure"))
				throw new Exception("MD5 auth. error");
			String dec = new String(Base64.decode(x.getValue().getBytes()));
			int ind = dec.indexOf("nonce=\"") + 7;
			String nonce = dec.substring(ind, dec.indexOf("\"", ind + 1));
			String cnonce = "00deadbeef00";
			writeToAir("<response xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">"
					+ generateAuthResponse(user, pass, domain, "xmpp/"
							+ domain, nonce, cnonce) + "</response>");
			log.addMessage("Waiting for response");
			x = readStanza();
			// System.out.println(x);
			if (x.getName().equals("failure")) {
				throw new Exception("MD5 auth. error");
			}

			writeToAir("<response xmlns='urn:ietf:params:xml:ns:xmpp-sasl'/>");
			log.addMessage("Next authorization step");
			x = readStanza();
			if (x.getName().equals("failure"))
				throw new Exception("MD5 authorization error");
		} else {
			if (x.child("mechanisms").hasValueOfChild("X-GOOGLE-TOKEN")
					&& log.getProfile().getIsGoogle() > 0) {
				// X-GOOGLE-TOKEN authorization doing. User can disable
				// google features using by deselecting corresponding
				// checkbox in profile
				google = true;
				String resp = token;
				writeToAir("<auth xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" mechanism=\"X-GOOGLE-TOKEN\">"
						+ resp + "</auth>");
				log
						.addMessage("Starting google authorization with token length = "
								+ token.length());
				x = readStanza();
				if (x.getName().equals("failure"))
					throw new Exception(
							"GOOGLE authorization error with error message: "
									+ x.getChilds().elementAt(0).toString());
			} else if (x.child("mechanisms").hasValueOfChild("PLAIN")) {
				// PLAIN authorization supported by GTalk server in SSL
				// mode
				log("Using plain authorization");
				String resp = "\0" + user + "\0" + pass;
				writeToAir("<auth xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" mechanism=\"PLAIN\">"
						+ MD5.toBase64(resp.getBytes()) + "</auth>");
				log.addMessage("Starting PLAIN authorization");
				x = readStanza();
				if (x.getName().equals("failure"))
					throw new Exception("PLAIN authorization error");
			} else {
				throw new Exception("Unknown authorization mechanism: " + x);
			}
		}
	}
	
	/**
	 * handles an incoming stanza
	 * @param x
	 */
	protected synchronized void handleStanza(XmlNode x) {
		if (x.getName() == null || x.getName().equals("")) {
			// Data empty - continue sleep
			return;
		}
		if (x.getName().equals("iq")
				&& x.child("query").getAttr("xmlns").equals("jabber:iq:roster")) {
			// Data about contact received. Processes all
			// "query" childs and updates info abount
			// contact
			Vector v = x.child("query").getChilds();
			log.getRoster().setFullJid(x.getAttr("to"));
			for (int i = 0; i < v.size(); i++) {
				XmlNode y = (XmlNode) v.elementAt(i);
				// Show contacts only with "both" and "to"
				// subscriptions
				if (y.getAttr("subscription").equals("both"))
					log.getRoster().getRosterFactory().updateContact(
							y.getAttr("jid").toLowerCase(), y.getAttr("name"),
							null, null);
			}
		}
		if (x.getName().equals("presence")) {
			// We are here if someone changes own status
			//log("Need to change presence");
			if (x.getAttr("type").equals("subscribe")) {
				// User requests authorization - Ask for
				// decision from user
				log.getRoster().playMessage();
				new RequestAuth(log.getDisplay(), this, log.getDisplay()
						.getCurrent(), x.getAttr("from"), log.getProfile()
						.getUser());
			}
			// Updates contact info in a roster
			log.getRoster().getRosterFactory().updateContact(
					x.getAttr("from").toLowerCase(),
					null,
					x.getAttr("type").equals("") ? x.childValue("show") : x
							.getAttr("type"), x.childValue("status"));
		}
		if (x.getName().equals("message") && !x.getAttr("type").equals("error")) {
			// We received a Message, Lets RosterFactory
			// process it!
			log.getRoster().getRosterFactory().addMessage(
					x.getAttr("from").toLowerCase(), x.childValue("body"),
					x.getAttr("id"));
			// list.getDisplay().vibrate(1000);
		}
		if (x.getName().equals("iq")
				&& x.child("query").getAttr("xmlns")
						.equals("jabber:iq:version")) {
			// We received unsupported packet
			System.out.println("Proceed");
			writeToAir("<iq type=\"error\" to=\""
					+ x.getAttr("from")
					+ "\"><query xmlns=\"jabber:iq:version\"/><error code=\"501\" type=\"cancel\"><feature-not-implemented xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"/></error></iq>");
		}
		if (x.getName().equals("iq") && isGoogle()
				&& !x.child("mailbox").getAttr("result-time").equals("")) {
			// New received mail response. Processes all
			// mails and adds it to MailViewer
			Vector v = x.child("mailbox").getChilds();
			// Newer mails must be on top
			for (int i = v.size() - 1; i >= 0; i--) {
				XmlNode t = (XmlNode) v.elementAt(i);
				if (i == 0) {
					// Last mail. Remebers mail date and
					// mail ID
					log.getRoster().setLastTime(t.getAttr("date"));
					log.getRoster().setTid(t.getAttr("tid"));
				}
				log.getRoster().getMails().addMail(
						t.child("senders").child("sender").getAttr("name"),
						t.childValue("subject"), t.childValue("snippet"));
			}
			if (v.size() > 0) {
				// Start MailViewer and play sound
				log.getRoster().getMails().startMe();
				log.getRoster().playMessage();
			}
		}
		if (x.getName().equals("iq")
				&& isGoogle()
				&& x.child("new-mail").getAttr("xmlns").equals(
						"google:mail:notify")) {
			/*
			 * New mail notification. Requests new mails newer then remebered
			 * data and ID if this fields were stored before
			 */
			writeToAir("<iq type=\"get\" id=\"23\"><query xmlns=\"google:mail:notify\""
					+ (log.getRoster().getLastTime().equals("") ? ""
							: " newer-than-time=\""
									+ log.getRoster().getLastTime() + "\"")
					+ (log.getRoster().getTid().equals("") ? ""
							: " newer-than-tid=\"" + log.getRoster().getTid()
									+ "\"")
					+ " q=\"(!label:^s) (!label:^k) ((label:^u) (label:^i) (!label:^vm))\"/></iq>");
		}
		if (x.getName().equals("iq")
				&& isGoogle()
				&& x.child("query").getAttr("xmlns").equals(
						"google:shared-status")) {
			// We receive google:shared:list and current
			// status ans status text
			// If we dont want to set custom status - we
			// store received status text
			if (statusSet || log.getProfile().getLockStatusStr() == 0)
				log.getProfile().setStatus(
						x.child("query").childValue("status"));
			// Next, we calculate new status ID
			int newStatus = x.child("query").childValue("show").equals("") ? 0
					: (x.child("query").childValue("show").equals("away") ? 1
							: 3);
			// Clears all status holders
			log.getRoster().getOnlines().removeAllElements();
			log.getRoster().getBusies().removeAllElements();
			log.getRoster().getAways().removeAllElements();
			Vector v = x.child("query").getChilds();
			// This routine parses received packet and fills
			// status holders
			for (int i = 0; i < v.size(); i++) {
				XmlNode t = (XmlNode) v.elementAt(i);
				if (t.getName().equals("status-list")) {
					Vector v2 = t.getChilds();
					for (int j = 0; j < v2.size(); j++) {
						XmlNode t2 = (XmlNode) v2.elementAt(j);
						if (t.getAttr("show").equals("dnd"))
							log.getRoster().getBusies().addElement(
									t2.getValue());
//						google only supports auto-away
//						else if (t.getAttr("show").equals("away"))
//							log.getRoster().getAways()
//									.addElement(t2.getValue());
						else if (t.getAttr("show").equals("default"))
							log.getRoster().getOnlines().addElement(
									t2.getValue());
					}
				}
			}
			if (log.getProfile().getStatusID() == 2)
				log.getProfile().setStatusID(1);
			// Fix status ID for GTalk server (GTalk does
			// not support XA status) I think so :)
			// First if - we return our status back if
			// lockStatus selected in profile by sending
			// packet with old status
			// Otherwise we store new status in profile
			if (newStatus != log.getProfile().getStatusID()
					&& log.getProfile().getLockStatus() > 0)
				generatePresense();
			else
				log.getProfile().setStatusID(newStatus);
			// If we received status info first time and if
			// we want to set custom status text - it's time
			// to do this
			if (!statusSet && log.getProfile().getLockStatusStr() > 0) {
				statusSet = true;
				generatePresense();
			}
		}
	}
	
	/**
	 * This routine generates presense packet
	 */
	protected void generatePresense() {
		String outp = "";

		String show = log.getProfile().getShowStr();

		if (!isGoogle()) {
			// Very simple
			outp = "<presence><show>" + show + "</show><status>"
					+ log.getProfile().getStatus() + "</status></presence>";
		} else {
			// Very hard :(
			// First, insert into response current status and status text
			outp = "<iq type=\"set\" to=\"" + log.getProfile().getUser()
					+ "\">" + "<query xmlns=\"google:shared-status\"><status>"
					+ log.getProfile().getStatus() + "</status><show>" + show
					+ "</show>";
			// Next, collect XML stream by processing all status holders and
			// adding new status text if custom status was set
			String s = "";
			boolean found = false;
			for (int i = 0; i < log.getRoster().getOnlines().size(); i++) {
				s += "<status>"
						+ log.getRoster().getOnlines().elementAt(i).toString()
						+ "</status>";
				if (log.getRoster().getOnlines().elementAt(i).toString()
						.equals(log.getProfile().getStatus())
						&& log.getRoster().getProfile().getStatusID() == 0)
					found = true;

			}
			if (!found && log.getProfile().getStatusID() == 0
					&& !log.getProfile().getStatus().trim().equals(""))
				s = "<status>" + log.getProfile().getStatus() + "</status>" + s;
			outp += "<status-list show=\"default\">" + s + "</status-list>";

			s = "";
			found = false;
			for (int i = 0; i < log.getRoster().getBusies().size(); i++) {
				s += "<status>"
						+ log.getRoster().getBusies().elementAt(i).toString()
						+ "</status>";
				if (log.getRoster().getBusies().elementAt(i).toString().equals(
						log.getRoster().getProfile().getStatus())
						&& log.getProfile().getStatusID() == 3)
					found = true;

			}
			if (!found && log.getProfile().getStatusID() == 3
					&& !log.getProfile().getStatus().trim().equals(""))
				s = "<status>" + log.getProfile().getStatus() + "</status>" + s;
			outp += "<status-list show=\"dnd\">" + s + "</status-list>";

//			google only supports auto-away			
//			s = "";
//			found = false;
//			for (int i = 0; i < log.getRoster().getAways().size(); i++) {
//				s += "<status>"
//						+ log.getRoster().getAways().elementAt(i).toString()
//						+ "</status>";
//				if (log.getRoster().getAways().elementAt(i).toString().equals(
//						log.getProfile().getStatus())
//						&& log.getProfile().getStatusID() == 1)
//					found = true;
//
//			}
//			if (!found && log.getProfile().getStatusID() == 1
//					&& !log.getProfile().getStatus().trim().equals(""))
//				s = "<status>" + log.getProfile().getStatus() + "</status>" + s;
//			outp += "<status-list show=\"away\">" + s + "</status-list>";

			outp += "</query></iq>";
		}
		writeToAir(outp);
		// Writes response to stream
	}
	
	
	/**
	 * tries to reconnect if autorecoonect is true 
	 */
	protected void reconnect() {
		ended = true;
		if (log.getProfile().getAutoReconnect() > 0)
			log.newSession(log.getDisplay(), log.getProfile());
	}
	
	/**
	 * writes to the console if logging is enabled
	 * 
	 * @param s
	 */
	protected void log(String s) {
		if (MGTalk.DEBUG)
			//log.addMessage(s.substring(0, s.length()>80 ? 80 : s.length()));
			System.out.println(s);
	}

	/**
	 * @return Returns the ended.
	 */
	public boolean isEnded() {
		return ended;
	}

	/**
	 * @return true if we use google specific services.
	 */
	protected boolean isGoogle() {
		return google;
	}
}
