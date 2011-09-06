package com.group_finity.mascot.mac.jna;

import com.sun.jna.Structure;
import com.sun.jna.Pointer;

public class AuthorizationItem extends Structure {
	public String name;
	public int valueLength;
	public Pointer value;
	public int flags;

	public AuthorizationItem(String name, int valueLength, Pointer value, int flags) { 
		super();
		this.name = name;
		this.valueLength = valueLength;
		this.value = value;
		this.flags = flags;
	}
}
