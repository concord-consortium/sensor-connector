/*
 * Created by Daniel Marell 2011-08-28 15:19
 * http://marellbox.marell.se/bundles/libusb10j
 * http://blog.cag.se/?author=2
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011 Daniel Marell 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.concord.usb.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/**
 * A structure representing the standard USB device descriptor.
 * <p/>
 * This descriptor is documented in section 9.6.1 of the USB 2.0 specification.
 * All multiple-byte fields are represented in host-endian format.
 */
public class libusb_device_descriptor extends Structure {
  /**
   * Size of this descriptor (in bytes).
   */
  public byte bLength;

  /**
   * Descriptor type.
   * Will have value libusb_descriptor_type::LIBUSB_DT_DEVICE LIBUSB_DT_DEVICE in this context.
   */
  public byte bDescriptorType;

  /**
   * USB specification release number in binary-coded decimal.
   * A value of 0x0200 indicates USB 2.0, 0x0110 indicates USB 1.1, etc.
   */
  public short bcdUSB;

  /**
   * USB-IF class code for the device.
   */
  public byte bDeviceClass;

  /**
   * USB-IF subclass code for the device, qualified by the bDeviceClass value.
   */
  public byte bDeviceSubClass;

  /**
   * USB-IF protocol code for the device, qualified by the bDeviceClass and bDeviceSubClass values.
   */
  public byte bDeviceProtocol;

  /**
   * Maximum packet size for endpoint 0.
   */
  public byte bMaxPacketSize0;

  /**
   * USB-IF vendor ID.
   */
  public short idVendor;

  /**
   * USB-IF product ID.
   */
  public short idProduct;

  /**
   * Device release number in binary-coded decimal.
   */
  public short bcdDevice;

  /**
   * Index of string descriptor describing manufacturer.
   */
  public byte iManufacturer;

  /**
   * Index of string descriptor describing product.
   */
  public byte iProduct;

  /**
   * Index of string descriptor containing device serial number.
   */
  public byte iSerialNumber;

  /**
   * Number of possible configurations.
   */
  public byte bNumConfigurations;

  @Override
  public String toString() {
    return "libusb_device_descriptor {" +
            " bLength=" + bLength +
            " bDescriptorType=" + bDescriptorType +
            " bcdUSB=" + bcdUSB +
            " bDeviceClass=" + bDeviceClass +
            " bDeviceSubClass=" + bDeviceSubClass +
            " bDeviceProtocol=" + bDeviceProtocol +
            " bMaxPacketSize0=" + bMaxPacketSize0 +
            " idVendor=" + idVendor +
            " idProduct=" + idProduct +
            " bcdDevice=" + bcdDevice +
            " iManufacturer=" + iManufacturer +
            " iProduct=" + iProduct +
            " iSerialNumber=" + iSerialNumber +
            " bNumConfigurations=" + bNumConfigurations +
            "}";
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected List getFieldOrder() {
	return Arrays.asList(new String[] { 
			"bLength", 
			"bDescriptorType",
			"bcdUSB",
			"bDeviceClass",
			"bDeviceSubClass",
			"bDeviceProtocol",
			"bMaxPacketSize0",
			"idVendor",
			"idProduct",
			"bcdDevice",
			"iManufacturer",
			"iProduct",
			"iSerialNumber",
			"bNumConfigurations"});
  }
}
