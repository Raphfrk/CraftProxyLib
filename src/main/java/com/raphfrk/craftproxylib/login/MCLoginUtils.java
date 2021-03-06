package com.raphfrk.craftproxylib.login;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;

import com.raphfrk.craftproxylib.MCSocket;
import com.raphfrk.craftproxylib.packet.Packet;
import com.raphfrk.craftproxylib.packet.standard.HandshakePacket;
import com.raphfrk.craftproxylib.packet.standard.LoginPacket;
import com.raphfrk.craftproxylib.packet.standard.RespawnPacket;
import com.raphfrk.craftproxylib.util.NetworkUtils;

public class MCLoginUtils {
	
	/**
	 * Handles a client login.  If there is a connect map match, the proxy will connect to that server.<br>
	 * <br>
	 * The order of matching for the map is as follows:<br>
	 *  "playername;hostname:port"
	 *  "hostname:port"
	 *  "playername"
	 *  
	 * @param client the client Socket
	 * @param playerConnectMap a map which maps to server addresses
	 * @param defaultServer the default server to connect to
	 * @param rotateIP activates ip address rotation when connecting to local network IPs
	 * @return
	 * @throws IOException
	 */
	public static LoginInfo handleLogin(Socket client, Map<String, InetSocketAddress> connectMap, InetSocketAddress defaultServer, boolean rotateIP) throws IOException {
		
		MCSocket mClient = new MCSocket(client, false);
		
		LoginInfo info = MCLoginUtils.acceptClientHandshake(mClient, null);
		
		if (info.getError() != null || info.isServerPing()) {
			return info;
		}
		
		InetSocketAddress serverAddr;
		
		if (connectMap != null) {
			serverAddr = connectMap.get(info.getUsername() + ";" + info.getHostname() + ":" + info.getPort());
			if (serverAddr == null) {
				serverAddr = connectMap.get(info.getHostname() + ":" + info.getPort());
				if (serverAddr == null) {
					serverAddr = connectMap.get(info.getHostname() + ":" + info.getPort());
					if (serverAddr == null) {
						serverAddr = defaultServer;
					}
				}
			}
		} else {
			serverAddr = defaultServer;
		}
		
		Socket server;
		
		InetAddress serverIP = serverAddr.getAddress();
		int serverPort = serverAddr.getPort();
		
		server = getServerSocket(serverIP, serverPort, rotateIP);
		
		MCSocket mServer = new MCSocket(server, true);
		
		MCLoginUtils.sendClientHandshake(mServer, info);
		
		info = new LoginInfo(info, mServer, mClient);
		
		info = MCLoginUtils.acceptServerHandshake(mServer, mClient, info);
		
		if (info.getError() != null) {
			return info;
		}
		
		info = MCLoginUtils.acceptClientLogin(mClient, mServer, info);
		
		if (info.getError() != null) {
			return info;
		}
		
		info = MCLoginUtils.acceptServerLogin(mServer, mClient, info);
		
		return info;
		
	}
	
	/**
	 * Logs into a server using the given login info.  A respawn packet is sent to the client to simulate login.
	 * 
	 * @param mClient the client socket to send the respawn packet to
	 * @param info the login info to use when logging in
	 * @param target the server hostname and port to connect to
	 * @param rotateIP
	 * @return
	 * @throws IOException
	 */
	public static LoginInfo handleLogin(MCSocket mClient, LoginInfo info, String target, boolean rotateIP) throws IOException {
		String serverHostname = MCSocket.parseHostname(target);
		int serverPort = MCSocket.parsePort(target);
		return handleLogin(mClient, info, serverHostname, serverPort, rotateIP);
	}
	
	/**
	 * Logs into a server using the given login info.  A respawn packet is sent to the client to simulate login.
	 * 
	 * @param mClient the client socket to send the respawn packet to
	 * @param info the login info to use when logging in
	 * @param hostname the server hostname
	 * @param port the server port
	 * @param rotateIP
	 * @return
	 * @throws IOException
	 */
	public static LoginInfo handleLogin(MCSocket mClient, LoginInfo info, String hostname, int port, boolean rotateIP) throws IOException {
		Socket server = getServerSocket(hostname, port, rotateIP);
		
		MCSocket mServer = new MCSocket(server, true);

		MCLoginUtils.sendClientHandshake(mServer, info);

		info = new LoginInfo(info, mServer, mClient);
		
		info = MCLoginUtils.acceptServerHandshake(mServer, null, info);

		if (info.getError() != null) {
			return info;
		}
		
		LoginPacket login = new LoginPacket(info);
		
		mServer.writePacket(login);

		info = MCLoginUtils.acceptServerLogin(mServer, null, info);

		if (info.getError() != null) {
			return info;
		}

		RespawnPacket respawn1 = new RespawnPacket(info, true);
		RespawnPacket respawn2 = new RespawnPacket(info, false);
		
		mClient.writePacket(respawn1);
		mClient.writePacket(respawn2);
		
		return info;
	}

	/**
	 * Reads a client login handshake from a socket
	 * 
	 * @param socket
	 * @return the info provided by the client on login
	 * @throws IOException
	 */
	public static LoginInfo acceptClientHandshake(MCSocket socket) throws IOException {
		return acceptClientHandshake(socket, null);
	}
	
	/**
	 * Reads a client login handshake from a socket and forwards it to a server
	 * 
	 * @param client the client socket
	 * @param server the server socket to forward any packets to
	 * @return the info provided by the client on login
	 * @throws IOException
	 */
	public static LoginInfo acceptClientHandshake(MCSocket client, MCSocket server) throws IOException {

		Packet p = new Packet();
		
		do {
			p = client.readPacket(p);
			if (server != null) {
				server.writePacket(p);
			}
		} while (p.getId() == 0xFA);
		
		if (p.getId() == 0xFE) {
			return new LoginInfo(true, client, server);
		}
		
		if (p.getId() != 2) {
			return error("Client did not send handshake packet", client, server);
		}

		
		String usernameHostname = null;
		try {
			usernameHostname = (String)p.readField(0);
		} catch (ClassCastException cce) {
			return error("Unable to parse handshake packet", client, server);
		}
		
		if (usernameHostname == null) {
			return error("Handshake packet contains null username", client, server);
		}
		
		String[] split = usernameHostname.split(";");
		
		String username = null;
		String hostname = null;
		int port = -1;
		
		username = split[0];

		if (split.length > 1) {
			username = split[0];
			String[] split2 = split[1].split(":");
			hostname = split2[0];
			
			if (split2.length > 1) {
				try {
					port = Integer.parseInt(split2[1]);
				} catch (NumberFormatException nfe) {
				}
			}
		}
		
		if (port == -1) {
			port = 25565;
		}
		
		return new LoginInfo(username, hostname, port);
	}
		
	/**
	 * Sends a client login handshake to a socket
	 * 
	 * @param socket
	 * @param loginInfo the info for the handshake packet
	 * @throws IOException
	 */
	public static void sendClientHandshake(MCSocket socket, LoginInfo loginInfo) throws IOException {
		String handshakeMessage;
		if (loginInfo.getHostname() == null) {
			handshakeMessage = loginInfo.getUsername();
		} else {
			handshakeMessage = loginInfo.getUsername() + ";" + loginInfo.getHostname() + ":" + loginInfo.getPort();
		}
		
		Packet p = new HandshakePacket(handshakeMessage);
		 
		socket.writePacket(p); 
	}
	
	/**
	 * Reads a server handshake packet from a socket
	 * 
	 * @param socket
	 * @return the connection hash
	 * @throws IOException
	 */
	public static LoginInfo acceptServerHandshake(MCSocket socket, LoginInfo info) throws IOException {
		return acceptServerHandshake(socket, null, info);
	}
	
	/**
	 * Reads a server handshake packet from a socket and forwards it to a client
	 * 
	 * @param server the server socket
	 * @param client the client socket to forward any packets to
	 * @return the connection hash
	 * @throws IOException
	 */
	public static LoginInfo acceptServerHandshake(MCSocket server, MCSocket client, LoginInfo info) throws IOException {
		
		Packet p = new Packet();
		
		do {
			p = server.readPacket(p);
			if (client != null) {
				client.writePacket(p);
			}
		} while (p.getId() == 0xFA);
		
		if (p.getId() != 2) {
			return error("Server did not send handshake packet", server, client);
		}

		
		String hash = null;
		try {
			hash = (String)p.readField(0);
		} catch (ClassCastException cce) {
			return error("Unable to parse handshake packet", server, client);
		}
		
		if (hash == null) {
			return error("Server handshake packet contains null hash", server, client);
		}
		
		return new LoginInfo(info, hash);
	}
	
	/**
	 * Sends a server login handshake to a socket
	 * 
	 * @param socket
	 * @param hash the connection hash
	 * @throws IOException
	 */
	public static void sendServerHandshake(MCSocket socket, LoginInfo info) throws IOException {
		Packet p = new HandshakePacket(info.getHash());
		 
		socket.writePacket(p); 
	}
	
	/**
	 * Reads a client login packet from a socket
	 * 
	 * @param socket
	 * @param loginInfo the login info from the client handshake, the extra info is added
	 * @return the info provided by the client on login
	 * @throws IOException
	 */
	public static LoginInfo acceptClientLogin(MCSocket socket, LoginInfo loginInfo) throws IOException {
		return acceptClientLogin(socket, null, loginInfo);
	}
	
	/**
	 * Reads a client login packet from a socket
	 * 
	 * @param client the client socket
	 * @param server the server socket to forward any packets to
	 * @param loginInfo the login info from the client handshake, the extra info is added
	 * @return the info provided by the client on login
	 * @throws IOException
	 */
	public static LoginInfo acceptClientLogin(MCSocket client, MCSocket server, LoginInfo loginInfo) throws IOException {

		Packet p = new Packet();
		
		do {
			p = client.readPacket(p);
			if (server != null) {
				server.writePacket(p);
			}
		} while (p.getId() == 0xFA);
		
		if (p.getId() != 1) {
			return error("Client did not send login packet", client, server);
		}

		String username = null;
		int protocolVersion = 0;
		try {
			username = (String)p.readField(1);
			protocolVersion = (Integer)p.readField(0);
		} catch (ClassCastException cce) {
			return error("Unable to parse login packet", client, server);
		}
		
		if (username == null) {
			return error("Login packet contains null username", client, server);
		}
		
		if (!username.equals(loginInfo.getUsername())) {
			return error("Username given in handshake and login packet do not match", client, server);
		}

		return new LoginInfo(loginInfo, protocolVersion);
	}
	
	/**
	 * Sends a client login packet to a socket
	 * 
	 * @param socket
	 * @param hash the connection hash
	 * @throws IOException
	 */
	public static void sendClientLogin(MCSocket socket, LoginInfo info) throws IOException {
		Packet p = new LoginPacket(info.getProtocolVersion(), info.getUsername());
		 
		socket.writePacket(p); 
	}
	
	/**
	 * Reads a client login packet from a socket
	 * 
	 * @param server the server socket
	 * @param client the client socket to forward any packets to
	 * @param loginInfo the login info from the client handshake, the extra info is added
	 * @return the info provided by the client on login
	 * @throws IOException
	 */
	public static LoginInfo acceptServerLogin(MCSocket server, MCSocket client, LoginInfo loginInfo) throws IOException {

		Packet p = new Packet();
		
		do {
			p = server.readPacket(p);
			if (client != null) {
				client.writePacket(p);
			}
		} while (p.getId() == 0xFA);
		
		if (p.getId() != 1) {
			return error("Server did not send login packet", server, client);
		}

		int entityId = 0;
		String levelType = null;
		int serverMode = 0;
		int dimension = 0;
		byte difficulty = 0;
		byte unused = 0;
		byte maxPlayers = 0;
		try {
			entityId = (Integer)p.readField(0);
			levelType = (String)p.readField(2);
			serverMode = (Integer)p.readField(3);
			dimension = (Integer)p.readField(4);
			difficulty = (Byte)p.readField(5);
			unused = (Byte)p.readField(6);
			maxPlayers = (Byte)p.readField(7);
		} catch (ClassCastException cce) {
			return error("Unable to parse login packet", server, client);
		}
		
		if (levelType == null) {
			return error("Login packet contains null level type", server, client);
		}
	
		return new LoginInfo(loginInfo, entityId, levelType, serverMode, dimension, difficulty, unused, maxPlayers);
	}
	
	static SecureRandom hashGenerator = new SecureRandom();

	static String getHashString() {
		long hashLong;
		synchronized( hashGenerator ) {
			hashLong = hashGenerator.nextLong() & 0x7FFFFFFFFFFFFFFFL;
		}

		return Long.toHexString(hashLong);
	}
	
	private static LoginInfo error(String message, MCSocket socket, MCSocket socket2) throws IOException {
		IOException e = null;
		try {
			socket.close(message);
		} catch (IOException ioe) {
			e = ioe;
		}
		if (socket2 != null) {
			socket2.close(message);
		}
		if (e != null) {
			throw e;
		}
		return new LoginInfo(message);
	}
	
	private static Socket getServerSocket(String hostname, int port, boolean rotateIP) throws IOException {
		InetAddress addr = InetAddress.getByName(hostname);
		
		return getServerSocket(addr, port, rotateIP);
	}
		
	private static Socket getServerSocket(InetAddress addr, int port, boolean rotateIP) throws IOException {

		Socket server;
		
		if (rotateIP && NetworkUtils.isLocal(addr)) {
			InetAddress connectFromAddress = NetworkUtils.getNextLocalIP();
			server = new Socket(addr, port, connectFromAddress, 0);
		} else {
			server = new Socket(addr, port);
		}
		return server;
	}
	
}
