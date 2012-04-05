package com.raphfrk.craftproxylib.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkUtils {

	private static AtomicInteger rotate = new AtomicInteger(0);
	
	public static InetAddress getNextLocalIP() {
		
		byte[] addr = new byte[4];
		
		int index = rotate.getAndIncrement() & 0x7FFFFFFF;
		addr[0] = (byte)127;
		addr[1] = (byte)(64 + (index & 0x7F));
		addr[2] = (byte)(64 + ((index >> 7) & 0x7F));
		addr[3] = (byte)1;
		
		try {
			return InetAddress.getByAddress(addr);
		} catch (UnknownHostException e) {
			return null;
		}
		
	}
	
	public static boolean isLocal(InetAddress a) {
		List<InetAddress> localIPs = getLocalIPs();
		if (localIPs == null) {
			return false;
		} else {
			return localIPs.contains(a);
		}
	}
	
	public static List<InetAddress> getLocalIPs() {

		Enumeration<NetworkInterface> interfaces;

		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return null;
		}

		List<InetAddress> ips = new ArrayList<InetAddress>();

		while(interfaces.hasMoreElements()) {
			NetworkInterface current = interfaces.nextElement();

			if(current != null) {
				Enumeration<InetAddress> addresses = current.getInetAddresses();

				while(addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if(addr != null) {
						ips.add(addr);
					}
				}
			}
		}

		return ips;

	}
	
}
