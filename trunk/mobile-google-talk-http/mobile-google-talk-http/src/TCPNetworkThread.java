import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.StreamConnection;

/**
 * @author Knerr
 * 
 * class for transmitting stanzas over tcp connection
 */
public class TCPNetworkThread extends NetworkThread {
	

	private InputStream is = null;
	private OutputStream os = null;
	private StreamConnection conn = null;
	
	/**
	 * Constructs thread and starts it
	 * 
	 * @param l
	 */
	public TCPNetworkThread(ConnectLog l) {
		super(l);
	}


	/**
	 * Terminates current NetworkThread softly Closes connection, informs user
	 * about it, and sets ended flag
	 * 
	 * @param reconnect
	 */
	public synchronized void terminate(boolean reconnect) {
		if (terminated)
			return;
		try {
			log.addMessage("Disconnected");
		} catch (Exception e2) {
		}
		ended = true;
		terminated = true;
		try { if(is!=null) is.close(); } catch (Exception e) {}
		try { if(os!=null) os.close(); } catch (Exception e) {}
		try { if(conn!=null) conn.close(); } catch (Exception e) {}
		
		try {
			log.setCurrent();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
			System.out.println("Ex2");
		}
		notify();
		System.out.println("Terminated!");
		try {
			Thread.sleep(1500);
			if (reconnect)
				reconnect();
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Ex3");
		}
	}

	/**
	 * 
	 * Main method that processes jabber packets
	 */
	public void run() {
		log.addMessage("Connecting...");
		/*
		 * Username must consists of 3 parts: <user>@<domain> Split username
		 * and extract user and domain values
		 */
		String user = log.getProfile().getUser();
		if (user.indexOf("@") == -1) {
			log.addMessage("Invalid username " + user);
			reconnect();
			return;
		}
		String domain = user.substring(user.indexOf("@") + 1, user.length());
		user = user.substring(0, user.indexOf("@"));
		/*
		 * Constructs Jabber server address
		 */
		String addr = log.getProfile().getHost() + ":"
				+ log.getProfile().getPort();
		if (log.getProfile().getSsl() == 1) {
			addr = "ssl://" + addr;
		} else {
			addr = "socket://" + addr;
		}
		/*
		 * If user wants to work with Google server - generate it
		 */
		if (log.getProfile().getIsGoogle() > 0) {
			token = getGoogleToken(log.getProfile().getUser(), log.getProfile()
					.getPass());
			log("Receveived X-Google-Token: " + token);
		}
		if (ended) {
			reconnect();
			return;
		}
		try {
			/*
			 * Starts session with jabber server
			 */
			startSession(addr, domain, user, log.getProfile().getPass(),
					"Mobile", log.getProfile().getStatus());
		} catch (Exception e) {
			// If any exception throws - terminate connection
			log.addMessage(e.getMessage());
			reconnect();
			return;
		}
		if (ended) {
			reconnect();
			return;
		}
		log.addMessage("Successfully connected with "
				+ log.getProfile().getUser());
		
		// Calculates string representation of initial user status
		if (isGoogle()) {
			// Sets important Google settings
			if (ended) {
				reconnect();
				return;
			}
			writeToAir("<iq type=\"get\" id=\"6\"><query xmlns=\"google:relay\"/></iq>");
			// Informs Google Talk that we want to use GTalk features
			XmlNode y = readStanza();
			if (y.getAttr("type").equals("error")) {
				// Sorry :(
				ended = true;
			} else {
				writeToAir("<iq type=\"set\" to=\""
						+ log.getProfile().getUser()
						+ "\" id=\"15\"><usersetting xmlns=\"google:setting\"><autoacceptrequests value=\"false\"/>"
						+ "<mailnotifications value=\"true\"/></usersetting></iq>");
				// Sends mail notification request to GTalk server
				y = readStanza();
				if (y.getAttr("type").equals("error")) {
					// Sorry :(
					ended = true;
				}

			}
		}
		if (!ended) {
			if (!isGoogle())
				writeToAir("<presence><show>" + log.getProfile().getShowStr() + "</show><status>"
						+ log.getProfile().getStatus() + "</status></presence>");
			// Send current status and status text to non Google Talk
			// servers
			writeToAir("<iq type=\"get\" id=\"roster\">"
					+ "<query xmlns=\"jabber:iq:roster\"/></iq>");
			// Requests roster items (for all servers)
			if (isGoogle()) {
				writeToAir("<presence><show></show><status></status></presence>");
				writeToAir("<iq type=\"get\" id=\"23\"><query xmlns=\"google:mail:notify\" q=\"(!label:^s) (!label:^k) ((label:^u) (label:^i) (!label:^vm))\"/></iq>"
						+ "<iq type=\"get\" to=\""
						+ log.getProfile().getUser()
						+ "\" id=\"21\"><query xmlns=\"google:shared-status\"/></iq>");
				/*
				 * 1. Sets empty status and status text for GTalk server - GTalk
				 * will return last/current status and status text 2. Requests
				 * new mail count and mail info 3. Requests google shared status
				 * lists
				 */
			}
		}
		long nowTime = new Date().getTime();
		log.initRoster();
		log.getRoster().getRoster().setTitle(log.getProfile().getUser());

		statusSet = false;
		while (!ended) {
			// Main cycle
			try {
				if ((is.available() > 0) && (!busy)) {
					// if nobody reads packet and data is ready - get packet
					XmlNode x = readOneStanza();
					handleStanza(x);
					
				} else {
					//re-send presence every 3 mins of inactivity- why??
					//guess this works like a ping for the server to inform that we are still there
					//some auto-away code would go here
					if (!busy) {
						if (new Date().getTime() - nowTime > 600000) {
							nowTime = new Date().getTime();
							writeToAir("<presence"
									+ " from=\"" + log.getRoster().getFullJid() + "\"" 
									+ "><show>" + log.getProfile().getShowStr() + "</show><status>"
									+ log.getProfile().getStatus() 
									+ "</status></presence>");
						}
					}
					Thread.sleep(1000);
					// Wait for next packet
				}
			} catch (Exception e) {
				ended = true;
				// Sorry :)
			}
		}
		terminate(true);
	}

	/**
	 * This routine reads next packet from stream (blocking). if we receive
	 * empty packet, discard it and wait for next stanza.
	 * 
	 * @return
	 */
	protected XmlNode readStanza() {
		busy = true;
		XmlNode x = new XmlNode();
		if (ended) {
			terminate(true);
			return x;
		}
		do {
			if (!ended) {
				try {
					x.init("", is);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("e: " + e.getMessage());
					ended = true;
				}
				busy = false;
			}
		} while (x.getName().equals("") && !ended);
		log("Read Stanza: " + x);
		return x;
	}

	/**
	 * This routine siply reads next packet from stream. Blocks until next
	 * stanza is received.
	 * 
	 * @return
	 */
	private XmlNode readOneStanza() {
		busy = true;
		XmlNode x = new XmlNode();
		if (ended) {
			terminate(true);
			return x;
		}
		if (!ended) {
			try {
				x.init("", is);
			} catch (Exception e) {
				ended = true;
			}
			busy = false;
		}
		log("Read One Stanza:" + x);
		return x;
	}

	/**
	 * This routine writes packet to stream If Exception thrown, terminate is
	 * called
	 * 
	 * @param mess
	 */
	protected void writeToAir(String mess) {
		if (ended) {
			terminate(true);
			return;
		}
		try {
			if (os != null) {
				os.write(ToUTF(mess).getBytes());
				os.flush();
				log("written to air: "+mess);
			}
		} catch (Exception e) {
			ended = true;
		}
	}

	/**
	 * Establishes connection with Jabber server
	 * 
	 * @param addr
	 * @throws Exception
	 */
	private void initConnection(String addr) throws Exception {
		try {
			log.addMessage("Init connection to " + addr);
			conn = (SocketConnection) Connector.open(addr);
			// conn.setSocketOption(SocketConnection.LINGER, 0);
			// conn.setSocketOption(SocketConnection.SNDBUF, 30);
			// conn.setSocketOption(SocketConnection.RCVBUF, 30);
			((SocketConnection) conn).setSocketOption(
					SocketConnection.KEEPALIVE, 1);
			is = conn.openInputStream();
			os = conn.openOutputStream();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
	}

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
	protected void startSession(String addr, String domain, String user,
			String pass, String resource, String Status) throws Exception {
		try {

			initConnection(addr);
			// throw new Exception("Cannt connect");
			log.addMessage("Opening first stream");
			log("Initiate stream");
			writeToAir("<?xml version=\"1.0\"?><stream:stream to=\""
					+ domain
					+ "\" xmlns=\"jabber:client\" xmlns:stream=\"http://etherx.jabber.org/streams\" version=\"1.0\">");
			XmlNode x = readStanza();
			if (x.getName().equals("stream:error"))
				throw new Exception("Error opening stream");
			log(x.toString());
			log.addMessage("Authenticating");
			doAuthentication(x, user, pass, domain);
			
			writeToAir("<?xml version=\"1.0\"?><stream:stream xmlns:stream=\"http://etherx.jabber.org/streams\" xmlns=\"jabber:client\" to=\""
					+ domain + "\" version=\"1.0\">");
			log.addMessage("Opening next stream");
			x = readStanza();
			if (x.getValue().equals("stream:error"))
				throw new Exception("Error opening second stream");
			log("Binding resource");
			// Resource binding and session establishing
			writeToAir("<iq type=\"set\" id=\"bind\">"
					+ "<bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\">"
					+ "<resource>" + resource + "</resource></bind></iq>");
			log.addMessage("Binding resource");
			x = readStanza();
			if (x.getAttr("type").equals("error"))
				throw new Exception("Error binding resource");
			writeToAir("<iq to=\""
					+ domain
					+ "\" type=\"set\" id=\"sess_1\">"
					+ "<session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/></iq>");
			log.addMessage("Opening session");
			x = readStanza();
			if (x.getAttr("type").equals("error"))
				throw new Exception("Error opening session");
			log.addMessage("Session Open!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log("Exception found: " + e.getMessage());
			throw new Exception(e.getMessage());
		}
	}
}
