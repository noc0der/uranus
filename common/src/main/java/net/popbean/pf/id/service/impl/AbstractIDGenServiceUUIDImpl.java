package net.popbean.pf.id.service.impl;

import java.net.InetAddress;
import java.util.List;

import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.id.service.IDGenService;

import com.alibaba.fastjson.JSONObject;

public  abstract class AbstractIDGenServiceUUIDImpl implements IDGenService<String> {
	private static final int IP;
	private static short counter = (short) 0;
	private static final int JVM = (int) ( System.currentTimeMillis() >>> 8 );
	static {
		int ipadd;
		try {
			ipadd = toInt( InetAddress.getLocalHost().getAddress() );
		}
		catch (Exception e) {
			ipadd = 0;
		}
		IP = ipadd;
	}
	//
	/**
	 * Unique across JVMs on this machine (unless they load this class
	 * in the same quater second - very unlikely)
	 */
	protected int getJVM() {
		return JVM;
	}
	/**
	 * Unique in a millisecond for this JVM instance (unless there
	 * are $gt; Short.MAX_VALUE instances created in a millisecond)
	 */
	protected short getCount() {
		synchronized(AbstractIDGenServiceUUIDImpl.class) {
			if (counter<0) counter=0;
			return counter++;
		}
	}
	/**
	 * Unique in a local network
	 */
	protected int getIP() {
		return IP;
	}
	/**
	 * Unique down to millisecond
	 */
	protected short getHiTime() {
		return (short) ( System.currentTimeMillis() >>> 32 );
	}
	protected int getLoTime() {
		return (int) System.currentTimeMillis();
	}
	//
	/**
	 * 
	 * @param bytes
	 * @return
	 */
	public static int toInt( byte[] bytes ) {
		int result = 0;
		for (int i=0; i<4; i++) {
			result = ( result << 8 ) - Byte.MIN_VALUE + (int) bytes[i];
		}
		return result;
	}
}
