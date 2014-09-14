package org.concord.usb.jna;

import com.sun.jna.Pointer;

public class LibUsbListDevices {
	public static void main(String[] args) {
	    LibUsb usb;

	    try {
	      usb = LibUsb.libUsb;
	    } catch (UnsatisfiedLinkError e) {
	      e.printStackTrace();
	      return;
	    }
	    int rc;

	    rc = usb.libusb_init(null);

	    usb.libusb_set_debug(null, 3);

	    Pointer[] pa = new Pointer[1];
	    rc = usb.libusb_get_device_list(null, pa);
	    if (rc <= 0) {
	    	switch (rc) {
	    	case libusb_error.ERROR_NO_MEM:
	    		throw new OutOfMemoryError("ERROR_NO_MEM when calling libusb_get_device_list");
	    	default:
	    		throw new RuntimeException("Usb: return code: " + rc);
	    	}
	    }

	    Pointer device_list = pa[0];
	    Pointer[] parr = device_list.getPointerArray(0);
	    System.out.println("Found " + parr.length + " devices");

	    for (Pointer usb_device : parr) {
	        libusb_device_descriptor[] arr = new libusb_device_descriptor[1];
	        rc = usb.libusb_get_device_descriptor(usb_device, arr);
	        if (rc < 0) {
	          switch (rc) {
	            case libusb_error.ERROR_ACCESS:
	              throw new RuntimeException("Usb: Permission Error");
	            case libusb_error.ERROR_NO_DEVICE:
	              throw new RuntimeException("Usb: No Device Error");
	            default:
	              throw new RuntimeException("Usb: return code: " + rc);
	          }
	        }
	        libusb_device_descriptor usbDevice = arr[0];
			System.out.format("0x%04x 0x%04x\n", usbDevice.idVendor, usbDevice.idProduct);
	    }

	    // Free the device list itself and unref all the devices in the list
	    usb.libusb_free_device_list(null, device_list, 1);
	    
	    usb.libusb_exit(null);

	}

}
