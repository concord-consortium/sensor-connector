/*
 * Created by Daniel Marell 2011-08-28 16:12
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

public class libusb_request_recipient {
  /**
   * Device.
   */
  public static final int RECIPIENT_DEVICE = 0;

  /**
   * Interface.
   */
  public static final int RECIPIENT_INTERFACE = 1;

  /**
   * Endpoint.
   */
  public static final int RECIPIENT_ENDPOINT = 2;

  /**
   * Other.
   */
  public static final int RECIPIENT_OTHER = 3;
}
