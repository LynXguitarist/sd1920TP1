package sd1920.trab1.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * <p>
 * A class to perform service discovery, based on periodic service contact
 * endpoint announcements over multicast communication.
 * </p>
 * 
 * <p>
 * Servers announce their *name* and contact *uri* at regular intervals. The
 * server actively collects received announcements.
 * </p>
 * 
 * <p>
 * Service announcements have the following format:
 * </p>
 * 
 * <p>
 * &lt;service-name-string&gt;&lt;delimiter-char&gt;&lt;service-uri-string&gt;
 * </p>
 */
public class Discovery {
	private static Logger Log = Logger.getLogger(Discovery.class.getName());

	static {
		// addresses some multicast issues on some TCP/IP stacks
		System.setProperty("java.net.preferIPv4Stack", "true");
		// summarizes the logging format
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}

	// The pre-aggreed multicast endpoint assigned to perform discovery.
	static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
	static final int DISCOVERY_PERIOD = 1000;
	static final int DISCOVERY_TIMEOUT = 5000;
	final static int MAX_DATAGRAM_SIZE = 65536;

	// Used separate the two fields that make up a service announcement.
	private static final String DELIMITER = "\t";

	private InetSocketAddress addr;
	private String serviceName;
	private String serviceURI;

	protected static Map<String, String> urls = new HashMap<>();

	/**
	 * @param serviceName the name of the service to announce
	 * @param serviceURI  an uri string - representing the contact endpoint of the
	 *                    service being announced
	 */
	public Discovery(InetSocketAddress addr, String serviceName, String serviceURI) {
		this.addr = addr;
		this.serviceName = serviceName;
		this.serviceURI = serviceURI;
	}

	/**
	 * Starts sending service announcements at regular intervals...
	 */
	public void start() {
		Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s", addr, serviceName, serviceURI));

		byte[] announceBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();
		DatagramPacket announcePkt = new DatagramPacket(announceBytes, announceBytes.length, addr);

		try {
			@SuppressWarnings("resource")
			MulticastSocket ms = new MulticastSocket(addr.getPort());
			ms.joinGroup(addr.getAddress());

			// start thread to send periodic announcements
			new Thread(() -> {
				for (;;) {
					try {
						ms.send(announcePkt);
						Thread.sleep(DISCOVERY_PERIOD);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

			// start thread to collect announcements
			new Thread(() -> {
				DatagramPacket pkt = new DatagramPacket(new byte[1024], 1024);
				for (;;) {
					try {
						pkt.setLength(1024);
						ms.receive(pkt);
						String msg = new String(pkt.getData(), 0, pkt.getLength());
						String[] msgElems = msg.split(DELIMITER);
						if (msgElems.length == 2) { // periodic announcement

							String domain = pkt.getAddress().getCanonicalHostName();
							if (domain.indexOf('.') != -1)
								domain = domain.substring(0, domain.indexOf('.'));
							// adds in the map containing domain-uri
							urls.put(domain, msgElems[1]);
						}
					} catch (IOException e) {
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * return the uri for that domain
	 * 
	 * @param domain
	 * @return uri
	 */
	public static String getUri(String domain) {
		return urls.get(domain);
	}

}
