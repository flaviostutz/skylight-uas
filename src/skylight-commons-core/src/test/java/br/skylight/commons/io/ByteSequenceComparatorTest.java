package br.skylight.commons.io;

import br.skylight.commons.infra.ByteSequenceComparator;

public class ByteSequenceComparatorTest {

	public static void main(String[] args) {
		ByteSequenceComparator sc = new ByteSequenceComparator();
		String test = "1234567890";
		for (byte b : test.getBytes()) {
			sc.addByte(b);
		}
		if(!sc.isLastBytesMatch("0".getBytes())) {
			System.out.println("ERROR!");
		}
		if(!sc.isLastBytesMatch("67890".getBytes())) {
			System.out.println("ERROR!");
		}
		if(!sc.isLastBytesMatch("1234567890".getBytes())) {
			System.out.println("ERROR!");
		}
		if(sc.isLastBytesMatch("1234".getBytes())) {
			System.out.println("ERROR!");
		}
		if(sc.isLastBytesMatch("9".getBytes())) {
			System.out.println("ERROR!");
		}
		if(sc.isLastBytesMatch("abc".getBytes())) {
			System.out.println("ERROR!");
		}
		if(sc.isLastBytesMatch("56789".getBytes())) {
			System.out.println("ERROR!");
		}
		
		if(!sc.isLastBytesPartial("890ABC".getBytes())) {
			System.out.println("ERROR!");
		}
		if(!sc.isLastBytesPartial("1234567890ABC".getBytes())) {
			System.out.println("ERROR!");
		}
		if(!sc.isLastBytesPartial("90".getBytes())) {
			System.out.println("ERROR!");
		}
		if(!sc.isLastBytesPartial("90AAA".getBytes())) {
			System.out.println("ERROR!");
		}
		if(sc.isLastBytesPartial("99AAA".getBytes())) {
			System.out.println("ERROR!");
		}
		if(sc.isLastBytesPartial("6890AAA".getBytes())) {
			System.out.println("ERROR!");
		}
	}
	
}
