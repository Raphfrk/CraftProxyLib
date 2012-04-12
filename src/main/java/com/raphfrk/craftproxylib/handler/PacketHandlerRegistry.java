package com.raphfrk.craftproxylib.handler;

import java.util.concurrent.atomic.AtomicReference;

public class PacketHandlerRegistry {
	
	public static final PacketHandlerRegistry nullRegistry = new PacketHandlerRegistry();
	
	private final AtomicReference<PacketHandler>[] upstreamHandlers;
	private final AtomicReference<PacketHandler>[] downstreamHandlers;

	@SuppressWarnings("unchecked")
	public PacketHandlerRegistry() {
		upstreamHandlers = new AtomicReference[256];
		downstreamHandlers = new AtomicReference[256];

		for (int i = 0; i < 256; i++) {
			upstreamHandlers[i] = new AtomicReference<PacketHandler>();
			downstreamHandlers[i] = new AtomicReference<PacketHandler>();
		}
	}

	/**
	 * Registers a PacketHandler.
	 * 
	 * @param handler the handler to register, or null to deregister
	 * @param id the packet id
	 * @param upstream true to register for upstream (client to server) packets
	 * @return the old handler or null
	 */
	public PacketHandler registerHandler(PacketHandler handler, int id, boolean upstream) {
		if (id >= 256 || id < 0) {
			throw new IllegalArgumentException(id + " out of range");
		}
		AtomicReference<PacketHandler> ref;
		if (upstream) {
			ref = upstreamHandlers[id];
		} else {
			ref = downstreamHandlers[id];
		}
		
		return ref.getAndSet(handler);
	}
	
	/**
	 * Registers a PacketHandler for a range of ids
	 * 
	 * @param handler the handler to register or null to deregister
	 * @param idStart the first packet id to register
	 * @param idLength the number of ids to register
	 * @param upstream true to register for upstream (client to server) packets
	 */
	public void registerHandler(PacketHandler handler, int idStart, int idLength, boolean upstream) {
		for (int i = idStart; i < idStart + idLength; i++) {
			registerHandler(handler, i, upstream);
		}
	}
	
	/**
	 * Creates an array of handlers for handling upstream packets.<br>
	 * <br>
	 * Each MCBridge gets its own instances of all the handlers
	 * 
	 * @return an array of AtomicReferences
	 */
	public PacketHandler[] getUpstreamHandlers() {
		int length = upstreamHandlers.length;
		PacketHandler[] handlers = new PacketHandler[length];
		for (int i = 0; i < length; i++) {
			PacketHandler handler = upstreamHandlers[i].get();
			if (handler != null) {
				handlers[i] = handler.newInstance();
			}
		}
		return handlers;
	}
	
	/**
	 * Creates an array of handlers for handling downstream packets.<br>
	 * <br>
	 * Each MCBridge gets its own instances of all the handlers
	 * 
	 * @return an array of AtomicReferences
	 */
	public PacketHandler[] getDownstreamHandlers() {
		int length = downstreamHandlers.length;
		PacketHandler[] handlers = new PacketHandler[length];
		for (int i = 0; i < length; i++) {
			PacketHandler handler = downstreamHandlers[i].get();
			if (handler != null) {
				handlers[i] = handler.newInstance();
			}
		}
		return handlers;	
	}
	
}
