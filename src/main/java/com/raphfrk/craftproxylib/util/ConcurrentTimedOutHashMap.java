package com.raphfrk.craftproxylib.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ConcurrentTimedOutHashMap<K, V> extends ConcurrentHashMap<K, V> {

	private static final long serialVersionUID = 1L;

	private final AtomicLong lastPurge = new AtomicLong(0);
	private final long timeout;
	
	public ConcurrentTimedOutHashMap(int timeout) {
		this.timeout = timeout;
	}
	
	public V put(K key, V value) {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastPurge.get() > timeout) {
			lastPurge.set(currentTime);
			clear();
		}
		return super.put(key,  value);
	}
	
}
