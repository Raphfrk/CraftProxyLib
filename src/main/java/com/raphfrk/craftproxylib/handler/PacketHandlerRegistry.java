package com.raphfrk.craftproxylib.handler;

import java.util.concurrent.atomic.AtomicReference;

public class PacketHandlerRegistry {
	
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
	 * Gets the upstream handler array
	 * 
	 * @return an array of AtomicReferences
	 */
	public AtomicReference<PacketHandler>[] getUpstreamHandlers() {
		return this.upstreamHandlers;
	}
	
	/**
	 * Gets the downstream handler array
	 * 
	 * @return an array of AtomicReferences
	 */
	public AtomicReference<PacketHandler>[] getDownstreamHandlers() {
		return this.downstreamHandlers;
	}
	
}
