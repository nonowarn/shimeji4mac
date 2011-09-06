package com.group_finity.mascot.mac.jna;

import com.sun.jna.Structure;
import com.sun.jna.Pointer;

public class AuthorizationRights extends Structure {
	public int count;
	public Pointer items;

	public AuthorizationRights(int count, Pointer items) {
		super();
		this.count = count;
		this.items = items;
	}
}
