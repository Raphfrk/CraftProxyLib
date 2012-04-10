package com.raphfrk.craftproxylib.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ConcurrentTimedOutHashMap<K, V> {

	private ConcurrentHashMap<K, List<V>> map = new ConcurrentHashMap<K, List<V>>();
	
	private static final long serialVersionUID = 1L;

	private final AtomicLong lastPurge = new AtomicLong(0);
	private final long timeout;
	private final long length;
	
	public ConcurrentTimedOutHashMap(int timeout, int length) {
		this.timeout = timeout;
		this.length = length;
	}
	
	public V put(K key, V value) {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastPurge.get() > timeout) {
			lastPurge.set(currentTime);
			map.clear();
		}
		
		boolean success = false;
		
		V removed = null;
		
		while (!success) {
			List<V> oldList = map.get(key);
			List<V> list;
			if (oldList == null) {
				list = new LinkedList<V>();
			} else {
				list = new LinkedList<V>(oldList);
			}

			list.add(value);
			if (list.size() > length) {
				removed = list.remove(0);
			}

			if (oldList == null) {
				List<V> old = map.putIfAbsent(key, list);
				success = old == null;
			} else {
				success = map.replace(key, oldList, list);
			}
		}
		return removed;
	}
	
	public V get(K key) {
		
		List<V> list = map.get(key);
		if (list == null) {
			return null;
		} else if (list.size() != length) {
			return null;
		} else {
			return list.get(list.size() - 1);
		}
		
	}
	
}
