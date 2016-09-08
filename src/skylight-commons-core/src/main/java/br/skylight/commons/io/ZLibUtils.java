package br.skylight.commons.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import br.skylight.commons.infra.IOHelper;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZStream;
import com.jcraft.jzlib.ZStreamException;

public class ZLibUtils {

	public enum CompressionLevel {
		NO_COMPRESSION(JZlib.Z_NO_COMPRESSION), DEFAULT_COMPRESSION(JZlib.Z_DEFAULT_COMPRESSION), BEST_SPEED(JZlib.Z_BEST_COMPRESSION), BEST_COMPRESSION(JZlib.Z_BEST_SPEED);
		int level;

		private CompressionLevel(int level) {
			this.level = level;
		}

		public int getLevel() {
			return level;
		}
	}

	public static int compress(byte[] sourceData, int sourceLen, byte[] targetData, CompressionLevel compressionLevel) throws ZStreamException {
		ZStream c_stream = new ZStream();

		int err = c_stream.deflateInit(compressionLevel.getLevel());
		checkError(c_stream, err, "deflateInit");

		c_stream.next_in = sourceData;
		c_stream.next_in_index = 0;
		c_stream.avail_in = sourceLen;

		c_stream.next_out = targetData;
		c_stream.next_out_index = 0;
		c_stream.avail_out = targetData.length;

		err = c_stream.deflate(JZlib.Z_NO_FLUSH);
		checkError(c_stream, err, "deflate");

		// while (c_stream.total_in != sourceData.length && c_stream.total_out <
		// targetData.length) {
		// c_stream.avail_in = c_stream.avail_out = 1; // force small buffers
		// err = c_stream.deflate(JZlib.Z_NO_FLUSH);
		// checkError(c_stream, err, "deflate");
		// }

		while (true) {
			c_stream.avail_out = 1;
			err = c_stream.deflate(JZlib.Z_FINISH);
			if (err == JZlib.Z_STREAM_END)
				break;
			checkError(c_stream, err, "deflate");
		}

		err = c_stream.deflateEnd();
		checkError(c_stream, err, "deflateEnd");

		return (int) c_stream.total_out;
	}

	public static int uncompress(byte[] sourceData, int sourceLen, byte[] targetData) throws ZStreamException {
		ZStream d_stream = new ZStream();

		d_stream.next_in = sourceData;
		d_stream.next_in_index = 0;
		d_stream.next_out = targetData;
		d_stream.next_out_index = 0;

		int err = d_stream.inflateInit();
		checkError(d_stream, err, "inflateInit");

		// err = d_stream.inflate(JZlib.Z_NO_FLUSH);
		// checkError(d_stream, err, "inflate");

		while (d_stream.total_out < targetData.length && d_stream.total_in < sourceData.length) {
			d_stream.avail_in = d_stream.avail_out = 1; /* force small buffers */
			err = d_stream.inflate(JZlib.Z_NO_FLUSH);
			if (err == JZlib.Z_STREAM_END)
				break;
			checkError(d_stream, err, "inflate");
		}

		err = d_stream.inflateEnd();
		checkError(d_stream, err, "inflateEnd");

		return (int) d_stream.total_out;
	}

	private static void checkError(ZStream z, int err, String msg) throws ZStreamException {
		if (err != JZlib.Z_OK) {
			if (z.msg != null) {
				throw new ZStreamException("Error on " + msg + ". code=" + err + "; msg=" + z.msg);
			} else {
				throw new ZStreamException("Error on " + msg + ". code=" + err);
			}
		}
	}

	public static void main(String[] args) throws IOException, TimeoutException {
		// generate random data
		FileInputStream fis = new FileInputStream(new File("d:\\sample.in"));
		byte[] uncompressed = new byte[99999];
		IOHelper.readFully(fis, uncompressed, 1000, false);

		// compress
		byte[] compressed = new byte[999999];
		int compressedLength = compress(uncompressed, uncompressed.length, compressed, CompressionLevel.BEST_SPEED);

		System.out.println("Compress ratio: " + 100F * ((float) compressedLength / (float) uncompressed.length) + "%");

//		compressed[430]++;

		// uncompress
		byte[] result = new byte[999999];
		int rs = uncompress(compressed, compressedLength, result);
		for (int i = 0; i < uncompressed.length; i++) {
			if (uncompressed[i] != result[i]) {
				System.out.println("Uncompressed data doesn't match original");
				return;
			}
		}
		System.out.println("Compression OK");
	}

}
