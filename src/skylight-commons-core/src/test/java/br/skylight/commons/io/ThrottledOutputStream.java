package br.skylight.commons.io;

//ThrottledOutputStream - output stream with throttling
//
// Copyright (C)1996,1998 by Jef Poskanzer <jef@mail.acme.com>. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// Visit the ACME Labs Java page for up-to-date versions of this and other
// fine Java utilities: http://www.acme.com/java/

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/// Output stream with throttling.
// <P>
// Restricts output to a specified rate.  Also includes a static utility
// routine for parsing a file of throttle settings.
// <P>
// <A HREF="/resources/classes/Acme/Serve/ThrottledOutputStream.java">Fetch the software.</A><BR>
// <A HREF="/resources/classes/Acme.tar.gz">Fetch the entire Acme package.</A>

public class ThrottledOutputStream extends FilterOutputStream {

	private long maxBps;
	private long bytes;
	private long start;

	// / Constructor.
	public ThrottledOutputStream(OutputStream out, long maxBps) {
		super(out);
		this.maxBps = maxBps;
		bytes = 0;
		start = System.currentTimeMillis();
	}

	private byte[] oneByte = new byte[1];

	// / Writes a byte. This method will block until the byte is actually
	// written.
	// @param b the byte to be written
	// @exception IOException if an I/O error has occurred
	public void write(int b) throws IOException {
		oneByte[0] = (byte) b;
		write(oneByte, 0, 1);
	}

	// / Writes a subarray of bytes.
	// @param b the data to be written
	// @param off the start offset in the data
	// @param len the number of bytes that are written
	// @exception IOException if an I/O error has occurred
	public void write(byte b[], int off, int len) throws IOException {
		// Check the throttle.
		bytes += len;
		long elapsed = System.currentTimeMillis() - start;
		long bps = bytes * 1000L / elapsed;
		if (bps > maxBps) {
			// Oops, sending too fast.
			long wakeElapsed = bytes * 1000L / maxBps;
			try {
				Thread.sleep(wakeElapsed - elapsed);
			} catch (InterruptedException ignore) {
			}
		}

		// Write the bytes.
		out.write(b, off, len);
	}

}
