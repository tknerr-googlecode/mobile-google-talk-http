import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * @author Knerr
 *
 * class for transmitting stanzas over a httpconnection as definend in JEP 124
 */
public class HTTPBindNetworkThread extends NetworkThread {

	public static int DEFAULT_WAIT = 30;

	private String httpburl; // url of the HTTB Binding gateway

	private int wait = DEFAULT_WAIT; // max seconds to keep the connection
				// open, default value

	private HttpConnection[] conn = new HttpConnection[2]; //allow exactly 2 connections 
	
	private int defaultConn = 0;
	
	private long rid = -1; // request id

	private String sid = null; // session id

	private boolean terminating = false; //indicates that we want to gracefully terminate
	
	private Thread secondThread = null;
	/**
	 * @param l
	 */
	public HTTPBindNetworkThread(ConnectLog l) {
		super(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see AbstractNetworkThread#run()
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

		/*
		 * dissect http binding url
		 */
		httpburl = log.getProfile().getHttpbindurl();
		if (httpburl.trim().equals("") || httpburl.indexOf("://") < 0) {
			log.addMessage("Invalid HTTPBinding url:" + httpburl
					+ ". try https://www.butterfat.net:443/punjab/httpb/");
			reconnect();
			return;
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
						+ "\" id=\"15\" xmlns=\"jabber:client\"><usersetting xmlns=\"google:setting\"><autoacceptrequests value=\"false\"/>"
						+ "<mailnotifications value=\"true\"/></usersetting></iq>");
				// Sends mail notification request to GTalk server
				y = readStanza();
				if (y.getAttr("type").equals("error")) {
					// Sorry :(
					ended = true;
				}
			}
		}
		
		log.initRoster();
		log.getRoster().getRoster().setTitle(log.getProfile().getUser());
		
		if (!ended) {
			//Requests roster items (for all servers)
			writeToAir("<iq type=\"get\" id=\"roster\">"
					+ "<query xmlns=\"jabber:iq:roster\"/></iq>");
			readAndHandleMultipleStanza(0);
			
			if (!isGoogle()) {
				writeToAir("<presence><show>" + log.getProfile().getShowStr() + "</show><status>"
						+ log.getProfile().getStatus() + "</status></presence>");
				readAndHandleMultipleStanza(0);
			}
			
			// Send current status and status text to non Google Talk
			// servers
			if (isGoogle()) {
				writeToAir("<presence><show></show><status></status></presence>");
				readAndHandleMultipleStanza(0);
				writeToAir("<iq type=\"get\" id=\"23\"><query xmlns=\"google:mail:notify\" q=\"(!label:^s) (!label:^k) ((label:^u) (label:^i) (!label:^vm))\"/></iq>");					
				readAndHandleMultipleStanza(0);
				writeToAir("<iq type=\"get\" to=\"" + log.getProfile().getUser() + "\" id=\"21\"><query xmlns=\"google:shared-status\"/></iq>");
				readAndHandleMultipleStanza(0);
				/*
				 * 1. Sets empty status and status text for GTalk server - GTalk
				 * new mail count and mail info 3. Requests google shared status
				 * lists
				 */
			}
		}
		
		//switching the default connection causes all new calls to
		//writeToAir to be executed in a new thread with the 2nd httpconnection object
		defaultConn = 1;
		statusSet = false;
		
		while (!ended) {
			// Main cycle
			try {
				if (!terminated && !busy) {
					
					/*
					 * this thread keeps looping until we terminate the session. it writes an
					 * empty request (<body/>) and blocks until the server sends a stanza. the server
					 * should respond with an empty response (<body/>) after <wait> seconds if there
					 * is nothing to send , or earlier if there is an incoming stanza.
					 * We restart the loop once we have handled the stanza
					 */
					//log("sending empty body [" + new Date().toString() + "]");
					writeToAir("", 0);
					readAndHandleMultipleStanza(0);
//					XmlNode x = readStanza(0);
//					handleStanza(x);
					//log("got response from server [" + new Date().toString() + "]: " + x);
					
				} 
			} catch (Exception e) {
				ended = true;
				// Sorry :)
			}
		}
		terminate(true);
	}

	
	
	/**
	 * used for the inner loop, where one response can contains multiple stanza
	 * @param connIdx
	 */
	private synchronized void readAndHandleMultipleStanza(int connIdx) {
		XmlNode n = readResponse(connIdx);
		int s = n.getChilds().size();
		for (int i=0; i<s; i++) {
			handleStanza((XmlNode) n.getChilds().elementAt(i));
		}
	}
		
	
	
	/**
	 * reads the next stanza from the given connection
	 * @param httpconn
	 * @return
	 */
	private XmlNode readStanza(int connIdx) {
		XmlNode n = readResponse(connIdx);
		int s = n.getChilds().size();
		if (s > 1) {
			log("### Can only read one stanza - discarding "+(s-1)+" others!");
			return (XmlNode) n.getChilds().firstElement();
		} else if (s < 1) {
			//discard empty stanza
			return new XmlNode();
		}
		return n;
	}
	
	/**
	 * reads the next non-empty stanza (blocking)
	 * 
	 * @return
	 */
	protected XmlNode readStanza() {
		//use default connection
		return readStanza(defaultConn);
	}

	/**
	 * reads the returned response incl. the enclosing body element
	 * 
	 * @return
	 */
	private XmlNode readResponse(int connIdx) {
		busy = true;
		XmlNode x = new XmlNode();
		if (ended) {
			terminate(true);
			return x;
		}
		do {
			if (!ended) {
				InputStream is = null;
				try {
					HttpConnection httpconn = conn[connIdx];
					int rc = ((HttpConnection)httpconn).getResponseCode();
					if (rc != 200)
						throw new Exception("Unexpected response code: " + rc);
					is = httpconn.openInputStream();
					x.init("", is);
				} catch (Exception e) {
					// e.printStackTrace();
					log("Exception reading resp: " + e.getMessage());
					ended = true;
				} finally {
					try {
						if (is != null)
							is.close();
					} catch (IOException e) {
						// e.printStackTrace();
					}
				}
				busy = false;
			}
		} while (x.getName().equals("") && !ended);
		log("Read Stanza ["+connIdx+"]: " + x.toString());

		return x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see AbstractNetworkThread#startSession(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	protected void startSession(String addr, String domain, String user,
			String pass, String resource, String Status) throws Exception {
		try {
			// throw new Exception("Cannt connect");
			log.addMessage("Opening first stream");
			log("Initiate stream");
			generateInitialRequestId(); // sets rid
			
			writeToAirPlain("<body to=\""
					+ domain
					+ "\" hold=\"1\" wait=\""
					+ wait
					+ "\" rid=\""
					+ rid
					+ "\" "
					+ "xml:lang=\"en\" "
					+ (log.getProfile().getSsl() == 1 ? "secure=\"true\" " : "")
					+ ""
					+ (addr.substring(0, addr.indexOf(":")).equals(domain) ? ""
							: "route=\"xmpp:" + addr + "\" ")
					+ "xmlns=\"http://jabber.org/protocol/httpbind\" " +
					// "newkey=\"ca393b51b682f61f98e7877d61146407f3d0a770\" " +
					"/>", 0);
			
			XmlNode x = readResponse(0);
			// log("Session creation response received: "+x.toString());
			if (!x.getName().equals("body"))
				throw new Exception(
						"Error opening stream - no body element returned");
			sid = x.getAttr("sid");
			if (sid.length() == 0)
				throw new Exception("Session ID not given!");
			if (x.getAttr("requests").equals("1"))
				throw new Exception("Server supports only polling behaviour!");
			// adjust wait attribute
			int tmpWait = Integer.parseInt(x.getAttr("wait"));
			if (tmpWait < wait)
				wait = tmpWait;
			// if (x.getAttr("inactivity").length()>0) {
			// int inact=Integer.parseInt(x.getAttr("inactivity"));
			// if (inact < wait)
			// wait = inact;
			// }

			log.addMessage("Authenticating");
			// log("features: "+x.child("features"));
			x = x.child("features");
			doAuthentication(x, user, pass, domain);

			log("Binding resource");
			// Resource binding and session establishing
			writeToAir("<iq type=\"set\" id=\"bind_1\">"
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
			log("Exception found: " + e.getMessage());
			throw new Exception(e.getMessage());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see AbstractNetworkThread#terminate(boolean)
	 */
	public void terminate(boolean reconnect) {
		if (terminating) 
			return;
		terminating = true;
		
		//terminate gracefully if we are already connected
		if (defaultConn == 1) {
			writeToAir("<presence type=\"unavailable\" xmlns=\"jabber:client\"/>");
			try {
				//wait for request to be sent
				sleep(4000);
			} catch (InterruptedException e3) {
				e3.printStackTrace();
			}
		}
		
		if (terminated)
			return;
		try {
			log.addMessage("Disconnected");
		} catch (Exception e2) {
		}
		
		wait = DEFAULT_WAIT;
		rid = -1;
		sid = null;
		ended = true;
		terminated = true;
		defaultConn = 0;
		
		//close open conections
		for (int i = 0; i < conn.length; i++) {
			try {
				if (conn[i] != null)
					conn[i].close();
			} catch (Exception e) {
			}	
		}
		
		try {
			log.setCurrent();
		} catch (Exception e1) {
			// e1.printStackTrace();
			log("Exception while terminating: " + e1.getMessage());
		}
		System.out.println("Terminated!");
		try {
			if (reconnect)
				reconnect();
		} catch (Exception e) {
			// e.printStackTrace();
			log("Exception while terminating: "+e.getMessage());
		}
	}
	
	/**
	 * writes the message to the outputsream of the given connection
	 * @param mess
	 * @param conn
	 */
	private void writeToAir(String mess, int connIdx)  {
		writeToAirPlain("<body rid=\"" + (++rid) + "\" sid=\"" + sid + "\" "
				+ "xmlns=\"http://jabber.org/protocol/httpbind\">" + mess
				+ "</body>", connIdx);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see AbstractNetworkThread#writeToAir(java.lang.String)
	 */
	protected void writeToAir(final String mess) {
		//default connection to use when called from outside
		if (defaultConn == 1) {
			//allow max 2 threads / httpconns
			while (secondThread != null && secondThread.isAlive()) {
				try {
					sleep(200);
				} catch (InterruptedException e) {
				}
				//System.out.println("############### WAITED FOR THREAD TO FINISH");
			}
			secondThread = new Thread() {
				public void run() {
					writeToAir(mess, defaultConn);
					readAndHandleMultipleStanza(defaultConn);
				}
			};
			secondThread.start();
		} else {
			writeToAir(mess, defaultConn);
		}
	}

	/**
	 * sends the message, but does not autonmatically include the enclosing body
	 * element.
	 * 
	 * @param mess
	 */
	private void writeToAirPlain(String mess, int connIdx) {
		if (ended) {
			terminate(true);
			return;
		}
		OutputStream out = null;
		try {
			byte[] bout = ToUTF(mess).getBytes();
			HttpConnection httpconn = (HttpConnection) Connector.open(httpburl);
			conn[connIdx] = httpconn;
			// for MIDP1.0 device which do not support HTTP 1.1 by default
			// ((HttpConnection)conn).setRequestProperty("Connection",
			// "keep-alive");
			if (!httpburl.startsWith("https://")) {
				// O2 WAP Flat stuff, only needed for unencrypted http
				httpconn.setRequestProperty("User-Agent",
						"Profile/MIDP-2.0 Configuration/CLDC-1.1");
				httpconn.setRequestProperty("X-WAP-Profile",
						"bla");
			}
			httpconn.setRequestMethod("POST");
			httpconn.setRequestProperty("Content-Length", ""
					+ bout.length);
			out = httpconn.openOutputStream();
			if (out != null) {
				out.write(bout);
				// os.flush(); //don't flush, because punjab can't handle
				// transfer-encoding: chunked
			}
			log("writtenToAir ["+connIdx+"]: " + mess);
		} catch (Exception e) {
			log("Exception found: " + e.getMessage());
			ended = true;
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
	}

	/**
	 * generates a random rid with max. 10 digits. note: rid must not exceed
	 * 9007199254740991 during the session
	 * 
	 * @return
	 */
	private long generateInitialRequestId() {
		String strRid = "";
		Random r = new Random();
		for (int i = 0; i < 10; i++)
			strRid += "" + r.nextInt(10);
		rid = Long.parseLong(strRid);
		log("initial rid: " + strRid);
		return rid;
	}
}
