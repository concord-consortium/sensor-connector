/*
 * Created by Daniel Marell 2011-08-28 10:18
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

public class libusb_error {
  /**
   * Success (no error)
   */
  public static final int SUCCESS = 0;

  /**
   * Input/output error
   */
  public static final int ERROR_IO = -1;

  /**
   * Invalid parameter
   */
  public static final int ERROR_INVALID_PARAM = -2;

  /**
   * Access denied (insufficient permissions)
   */
  public static final int ERROR_ACCESS = -3;

  /**
   * No such device (it may have been disconnected)
   */
  public static final int ERROR_NO_DEVICE = -4;

  /**
   * Entity not found
   */
  public static final int ERROR_NOT_FOUND = -5;

  /**
   * Resource busy
   */
  public static final int ERROR_BUSY = -6;

  /**
   * Operation timed out
   */
  public static final int ERROR_TIMEOUT = -7;

  /**
   * Overflow
   */
  public static final int ERROR_OVERFLOW = -8;

  /**
   * Pipe error
   */
  public static final int ERROR_PIPE = -9;

  /**
   * System call interrupted (perhaps due to signal)
   */
  public static final int ERROR_INTERRUPTED = -10;

  /**
   * Insufficient memory
   */
  public static final int ERROR_NO_MEM = -11;

  /**
   * Operation not supported or unimplemented on this platform
   */
  public static final int ERROR_NOT_SUPPORTED = -12;

  /**
   * Other error
   */
  public static final int ERROR_OTHER = -99;

  /**
   * Get text for error code.
   * @param c Error code
   * @return Text string
   */
  public static String getText(int c) {
    switch (c) {
      case SUCCESS:
        return "SUCCESS";
      case ERROR_IO:
        return "ERROR_IO";
      case ERROR_INVALID_PARAM:
        return "ERROR_INVALID_PARAM";
      case ERROR_ACCESS:
        return "ERROR_ACCESS";
      case ERROR_NO_DEVICE:
        return "ERROR_NO_DEVICE";
      case ERROR_NOT_FOUND:
        return "ERROR_NOT_FOUND";
      case ERROR_BUSY:
        return "ERROR_BUSY";
      case ERROR_TIMEOUT:
        return "ERROR_TIMEOUT";
      case ERROR_OVERFLOW:
        return "ERROR_OVERFLOW";
      case ERROR_PIPE:
        return "ERROR_PIPE";
      case ERROR_INTERRUPTED:
        return "ERROR_INTERRUPTED";
      case ERROR_NO_MEM:
        return "ERROR_NO_MEM";
      case ERROR_NOT_SUPPORTED:
        return "ERROR_NOT_SUPPORTED";
      case ERROR_OTHER:
        return "ERROR_OTHER";
      default:
        return "?(" + c + ")";
    }
  }
}