package com.raphfrk.craftproxylib;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.raphfrk.craftproxylib.handler.PacketHandler;
import com.raphfrk.craftproxylib.handler.PacketHandlerRegistry;
import com.raphfrk.craftproxylib.packet.Packet;

/**
 * Represents a bridge for two MCSockets
 */
public class MCBridge {
	
	private final MCPassthroughThread serverToClientThread;
	private final MCPassthroughThread clientToServerThread;
	
	private static final PacketHandlerRegistry nullRegistry = new PacketHandlerRegistry();
	
	public MCBridge(Socket server, Socket client) throws IOException {
		this(new MCSocket(server, true), new MCSocket(client, false));
	}
	
	public MCBridge(Socket server, Socket client, PacketHandlerRegistry registry) throws IOException {
		this(new MCSocket(server, true), new MCSocket(client, false), registry);
	}
	
	public MCBridge(MCSocket server, MCSocket client) {
		this(server, client, nullRegistry);
	}
	
	public MCBridge(MCSocket server, MCSocket client, PacketHandlerRegistry registry) {
		AtomicBoolean closeSync = new AtomicBoolean(false);
		
		this.serverToClientThread = new MCPassthroughThread(server, client, closeSync, registry.getDownstreamHandlers());
		this.clientToServerThread = new MCPassthroughThread(client, server, closeSync, registry.getUpstreamHandlers());
		
		this.serverToClientThread.setReturnThread(clientToServerThread);
		this.clientToServerThread.setReturnThread(serverToClientThread);
	}
	
	/**
	 * Starts the threads for the bridge
	 */
	public void start() {
		serverToClientThread.start();
		clientToServerThread.start();
	}
	
	/**
	 * Joins against the threads for the bridge.  Both threads are killed if either socket closes.
	 * 
	 * @throws InterruptedException 
	 */
	public void join() throws InterruptedException {
		join(0);
	}
	
	/**
	 * Interrupts the bridge
	 */
	public void interrupt() {
		serverToClientThread.interrupt();
		clientToServerThread.interrupt();
	}
	
	/**
	 * Joins against the threads for the bridge.  Both threads are killed if either socket closes.
	 * 
	 * @param millis the time to wait in milliseconds, or 0 for infinite time.
	 * @throws InterruptedException 
	 */
	public void join(long millis) throws InterruptedException {
		long startTime = System.currentTimeMillis();
		serverToClientThread.join(millis);
		if (millis != 0) {
			millis -= System.currentTimeMillis() - startTime;
			if (millis == 0) {
				return;
			}
		}
		clientToServerThread.join(millis);
	}

	private static class MCPassthroughThread extends Thread {
		
		private final AtomicReference<PacketHandler>[] handlers;
		private final AtomicBoolean closeSync;
		
		private MCPassthroughThread counterFlowThread = null;
		private final MCSocket from;
		private final MCSocket to;
		private volatile boolean running = true;
		private volatile boolean readError = false;
		private volatile boolean writeError = false;
		private volatile boolean safeDeath = false;

		
		public MCPassthroughThread(MCSocket from, MCSocket to, AtomicBoolean closeSync, AtomicReference<PacketHandler>[] handlers) {
			this.from = from;
			this.to = to;
			this.handlers = handlers;
			this.closeSync = closeSync;
		}
		
		public void setReturnThread(MCPassthroughThread t) {
			this.counterFlowThread = t;
		}
		
		public boolean isReadError() {
			if (isAlive()) {
				throw new UnsupportedOperationException("This method should not be called while the thread is running");
			}
			return !safeDeath || readError;
		}
		
		public boolean isWriteError() {
			if (isAlive()) {
				throw new UnsupportedOperationException("This method should not be called while the thread is running");
			}
			return !safeDeath || writeError;
		}
		
		public void interrupt() {
			running = false;
			super.interrupt();
		}
		
		public void run() {
			Packet p = null;
			try {
				while (running) {
					try {
						p = from.readPacket(p);
					} catch (IOException ioe) {
						running = false;
						readError = true;
						ioe.printStackTrace();
						continue;
					}
					int id = p.getId();
					if (id == -1) {
						readError = true;
						running = false;
						throw new IllegalStateException("Incomplete packet read");
					}
					PacketHandler handler = handlers[id].get();
					if (handler != null) {
						try {
							p = handler.handle(p, to.getOutputStream(), from.getOutputStream());
						} catch (IOException ioe) {
							readError = true;
							writeError = true;
							running = false;
							throw new RuntimeException("Handler " + handler.getClass().getSimpleName() + " threw an IOException", ioe);
						} catch (Exception e) {
							readError = true;
							writeError = true;
							running = false;
							throw new RuntimeException("Handler " + handler.getClass().getSimpleName() + " threw an exception", e);
						}
					}
					if (p == PacketHandler.INTERRUPT) {
						running = false;
					} else if (p != null) {
						try {
							to.writePacket(p);
						} catch (IOException ioe) {
							ioe.printStackTrace();
							running = false;
							writeError = true;
						}
					}

				}
				safeDeath = true;
			} finally {
				if (counterFlowThread == null) {
					if (isReadError()) {
						try {
							from.close();
						} catch (IOException ioe) {
						}
					}
					if (isWriteError()) {
						try {
							to.close();
						} catch (IOException ioe) {
						}
					}
				} else {
					counterFlowThread.interrupt();
					boolean counterFlowThreadDead = !closeSync.compareAndSet(false, true);

					if (counterFlowThreadDead) {
						try {
							counterFlowThread.join();
						} catch (InterruptedException e) {
						}
						if (readError || counterFlowThread.isWriteError()) {
							try {
								from.close();
							} catch (IOException ioe) {
							}
						}
						if (writeError || counterFlowThread.isReadError()) {
							try {
								to.close();
							} catch (IOException ioe) {
							}
						}
					}
				}
			}
		}
	}
}
