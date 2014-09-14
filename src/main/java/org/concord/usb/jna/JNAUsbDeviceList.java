package org.concord.usb.jna;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.concord.usb.UsbDevice;
import org.concord.usb.UsbDeviceList;
import org.concord.usb.UsbException;

import com.sun.jna.Pointer;

public class JNAUsbDeviceList {
	private static final Logger logger = Logger.getLogger(JNAUsbDeviceList.class.getName());

	public static ArrayList<UsbDevice> getAttachedDevices() throws UsbException{
		ArrayList<UsbDevice> devices = new ArrayList<UsbDevice>();
	    LibUsb usb;

	    try {
	      usb = LibUsb.libUsb;
	    } catch (UnsatisfiedLinkError e) {
	      throw new UsbException(e);
	    }
	    int rc;

	    rc = usb.libusb_init(null);

	    usb.libusb_set_debug(null, 3);

	    Pointer[] pa = new Pointer[1];
	    rc = usb.libusb_get_device_list(null, pa);
	    if (rc <= 0) {
	    	switch (rc) {
	    	case libusb_error.ERROR_NO_MEM:
	    		throw new UsbException("ERROR_NO_MEM when calling libusb_get_device_list");
	    	default:
	    		throw new UsbException("Usb: return code: " + rc);
	    	}
	    }

	    Pointer device_list = pa[0];
	    Pointer[] parr = device_list.getPointerArray(0);

	    for (Pointer usb_device : parr) {
	        libusb_device_descriptor[] arr = new libusb_device_descriptor[1];
	        rc = usb.libusb_get_device_descriptor(usb_device, arr);
	        if (rc < 0) {
	          switch (rc) {
	            case libusb_error.ERROR_ACCESS:
	              throw new UsbException("Usb: Permission Error");
	            case libusb_error.ERROR_NO_DEVICE:
	              throw new UsbException("Usb: No Device Error");
	            default:
	              throw new UsbException("Usb: return code: " + rc);
	          }
	        }
	        libusb_device_descriptor usbDevice = arr[0];
	        devices.add(new UsbDevice(usbDevice.idVendor, usbDevice.idProduct));
	    }

	    // Free the device list itself and unref all the devices in the list
	    usb.libusb_free_device_list(null, device_list, 1);
	    
	    usb.libusb_exit(null);
	    return devices;
	}
	
	public static void main(String[] args) throws UsbException {
		ArrayList<UsbDevice> attachedDevices = getAttachedDevices();
		System.out.println("VendID ProdID");
		for (UsbDevice usbDevice : attachedDevices) {
			System.out.format("0x%04x 0x%04x\n", usbDevice.vendorId, usbDevice.productId);
		}
	}
}
