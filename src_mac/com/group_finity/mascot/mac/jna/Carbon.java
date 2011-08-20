package com.group_finity.mascot.mac.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.Library;

import com.group_finity.mascot.mac.jna.ProcessSerialNumber;
import com.group_finity.mascot.mac.jna.AXUIElementRef;
import com.group_finity.mascot.mac.jna.CFStringRef;
import com.group_finity.mascot.mac.jna.CFTypeRef;


public interface Carbon extends Library {
	Carbon INSTANCE = (Carbon) Native.loadLibrary("Carbon", Carbon.class);

	long GetFrontProcess(ProcessSerialNumber psn);
	long GetProcessPID(final ProcessSerialNumber psn, LongByReference pidp);

	long AXUIElementCopyAttributeValue(
		AXUIElementRef element, CFStringRef attr, PointerByReference value);
	AXUIElementRef AXUIElementCreateApplication(long pid);
	boolean AXValueGetValue(AXValueRef value, long type, Pointer valuep);

	CFStringRef CFStringCreateWithCharacters(Pointer alloc, char[] source, long length);

	void CFRelease(CFTypeRef any);

	static final long kAXErrorSuccess = 0;
	static final long
	  kAXValueCGPointType = 1,
		kAXValueCGSizeType = 2;

}
