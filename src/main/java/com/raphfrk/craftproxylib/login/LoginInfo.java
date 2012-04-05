package com.raphfrk.craftproxylib.login;

import com.raphfrk.craftproxylib.MCSocket;

public class LoginInfo {
	
	private final String username;
	private final String hostname;
	private final int port;
	private final int entityId;
	private final String levelType;
	private final int serverMode;
	private final byte unused;
	private final byte maxPlayers;
	private final byte difficulty;
	private final String hash;
	private final int dimension;
	private final MCSocket server;
	private final MCSocket client;
	
	private final String error;
	
	public LoginInfo(String error) {
		this.error = error;
		this.username = null;
		this.port = -1;
		this.hostname = null;
		this.levelType = null;
		this.serverMode = 0;
		this.entityId = 0;
		this.unused = 0;
		this.difficulty = 0;
		this.maxPlayers = 0;
		this.hash = null;
		this.dimension = 0;
		this.server = null;
		this.client = null;
	}
	
	public LoginInfo(String username, String hostname, int port) {
		this(username, hostname, port, null, 0, 0, 0, null, (byte)0, (byte)0, (byte)0, 0, null, null);
	}
	
	public LoginInfo(LoginInfo info, int protocolVersion) {
		this(
				info.getUsername(),
				info.getHostname(),
				info.getPort(),
				info.getLevelType(),
				info.getServerMode(),
				info.getEntityId(),
				protocolVersion,
				info.getHash(),
				info.getDifficulty(),
				info.getUnused(),
				info.getMaxPlayers(),
				info.getDimension(),
				info.getServerSocket(),
				info.getClientSocket());
	}
	
	public LoginInfo(LoginInfo info, String hash) {
		this(
				info.getUsername(),
				info.getHostname(),
				info.getPort(),
				info.getLevelType(),
				info.getServerMode(),
				info.getEntityId(),
				info.getProtocolVersion(),
				hash,
				info.getDifficulty(),
				info.getUnused(),
				info.getMaxPlayers(),
				info.getDimension(),
				info.getServerSocket(),
				info.getClientSocket());
	}
	
	public LoginInfo(LoginInfo info, int entityId, String levelType, int serverMode, int dimension, byte difficulty, byte unused, byte maxPlayers) {
		this(
				info.getUsername(),
				info.getHostname(),
				info.getPort(),
				levelType,
				serverMode,
				entityId,
				info.getProtocolVersion(),
				info.getHash(),
				difficulty,
				unused,
				maxPlayers,
				dimension,
				info.getServerSocket(),
				info.getClientSocket());
	}
	
	public LoginInfo(LoginInfo info, MCSocket server, MCSocket client) {
		this(
				info.getUsername(),
				info.getHostname(),
				info.getPort(),
				info.getLevelType(),
				info.getServerMode(),
				info.getEntityId(),
				info.getProtocolVersion(),
				info.getHash(),
				info.getDifficulty(),
				info.getUnused(),
				info.getMaxPlayers(),
				info.getDimension(),
				server,
				client);
	}
	
	public LoginInfo(String username, String hostname, int port, String levelType, int serverMode, int entityId, int protocolVersion, String hash, byte difficulty, byte unused, byte maxPlayers, int dimension, MCSocket server, MCSocket client) {
		this.username = username;
		this.hostname = hostname;
		this.port = port;
		this.error = null;
		this.levelType = levelType;
		this.serverMode = serverMode;
		this.entityId = entityId;
		this.difficulty = difficulty;
		this.unused = unused;
		this.maxPlayers = maxPlayers;
		this.hash = hash;
		this.dimension = dimension;
		this.server = server;
		this.client = client;
	}

	
	public String getError() {
		return error;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getLevelType() {
		return levelType;
	}
	
	public int getServerMode() {
		return serverMode;
	}
	
	public int getEntityId() {
		return entityId;
	}
	
	public int getProtocolVersion() {
		return entityId;
	}
	
	public byte getUnused() {
		return unused;
	}
	
	public byte getMaxPlayers() {
		return maxPlayers;
	}
	
	public byte getDifficulty() {
		return difficulty;
	}
	
	public String getHash() {
		return hash;
	}
	
	public int getDimension() {
		return dimension;
	}
	
	public MCSocket getServerSocket() {
		return server;
	}
	
	public MCSocket getClientSocket() {
		return client;
	}

	public String toString() {
		if (this.error != null) {
			return error;
		} else {
			return "{username=" + username + 
					", hostname=" + hostname + 
					", port=" + port + 
					", entityId =" + entityId + 
					", levelType=" + levelType + 
					", serverMode=" + serverMode +
					", unused=" + unused +
					", maxPlayers=" + maxPlayers +
					", difficulty=" + difficulty +
					", hash=" + hash + "}";
		}
	}
	
}
