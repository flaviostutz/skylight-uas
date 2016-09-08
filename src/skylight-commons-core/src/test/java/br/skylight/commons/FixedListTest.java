package br.skylight.commons;

import br.skylight.commons.infra.FixedList;

public class FixedListTest {

	public static void main(String[] args) {
		FixedList<Integer> l = new FixedList<Integer>(5);
		if(l.getSize()!=0) throw new AssertionError("OPS!");
		l.addItem(1);
		if(l.getSize()!=1) throw new AssertionError("OPS!");
		if(l.getItem(0)!=1) throw new AssertionError("OPS!");
		l.addItem(2);
		l.addItem(3);
		if(l.getSize()!=3) throw new AssertionError("OPS!");
		if(l.getItem(0)!=1) throw new AssertionError("OPS!");
		if(l.getItem(1)!=2) throw new AssertionError("OPS!");
		if(l.getItem(2)!=3) throw new AssertionError("OPS!");
		l.addItem(4);
		l.addItem(5);
		if(l.getSize()!=5) throw new AssertionError("OPS!");
		if(l.getItem(0)!=1) throw new AssertionError("OPS!");
		if(l.getItem(1)!=2) throw new AssertionError("OPS!");
		if(l.getItem(2)!=3) throw new AssertionError("OPS!");
		if(l.getItem(3)!=4) throw new AssertionError("OPS!");
		if(l.getItem(4)!=5) throw new AssertionError("OPS!");
		l.addItem(6);
		if(l.getItem(0)!=2) throw new AssertionError("OPS!");
		if(l.getItem(1)!=3) throw new AssertionError("OPS!");
		if(l.getItem(2)!=4) throw new AssertionError("OPS!");
		if(l.getItem(3)!=5) throw new AssertionError("OPS!");
		if(l.getItem(4)!=6) throw new AssertionError("OPS!");
		if(l.getSize()!=5) throw new AssertionError("OPS!");
		l.addItem(7);
		if(l.getItem(0)!=3) throw new AssertionError("OPS!");
		if(l.getItem(1)!=4) throw new AssertionError("OPS!");
		if(l.getItem(2)!=5) throw new AssertionError("OPS!");
		if(l.getItem(3)!=6) throw new AssertionError("OPS!");
		if(l.getItem(4)!=7) throw new AssertionError("OPS!");
		if(l.getSize()!=5) throw new AssertionError("OPS!");
	}
	
}
