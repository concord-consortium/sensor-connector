package org.concord.usb;

public class UsbDevice {
	public short vendorId;
	public short productId;
	public UsbDevice(short vendorId, short productId) {
		this.vendorId = vendorId;
		this.productId = productId;
	}
}
