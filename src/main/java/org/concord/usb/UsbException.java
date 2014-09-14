package org.concord.usb;

@SuppressWarnings("serial")
public class UsbException extends Exception {

	public UsbException(Throwable e) {
		super(e);
	}

	public UsbException(String message) {
		super(message);
	}
	
}
