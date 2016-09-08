package br.skylight.uav;

import java.io.IOException;

import br.skylight.commons.infra.IOHelper;

public class Test {

	public static void main(String[] args) throws IOException {
//		Process p = Runtime.getRuntime().exec(args[0]);
//		InputStream is = p.getInputStream();
//		while(true) {
//			System.out.print(byteToHex((byte)is.read()) + " ");
//		}
		System.out.println(IOHelper.byteToHex((byte)((int)(byte)0x94)));
		System.out.println(((byte)0x94));
		System.out.println(-108);
	}
	
	public static String byteToHex(byte b) {
		// Returns hex String representation of byte b
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}
	
}
