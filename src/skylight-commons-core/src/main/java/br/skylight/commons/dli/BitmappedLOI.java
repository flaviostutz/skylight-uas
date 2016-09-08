package br.skylight.commons.dli;

import java.util.ArrayList;
import java.util.List;

import br.skylight.commons.LOI;

public class BitmappedLOI extends Bitmapped {

	private List<LOI> lois = new ArrayList<LOI>();
	
	public BitmappedLOI() {
	}

	public List<LOI> getLOIs() {
		lois.clear();
		for (LOI loi : LOI.values()) {
			if(isLOI(loi)) {
				lois.add(loi);
			}
		}
		return lois;
	}
	
	public BitmappedLOI(int ... lois) {
		for (int l : lois) {
			setLOI(l, true);
		}
	}
	
	public void setLOI(int loi, boolean value) {
		if(loi<2 || loi>5) throw new IllegalArgumentException("LOI number must be between 2 and 5. number=" + loi);
		setBit(loi-1, value);
	}
	
	@Override
	public void setBit(int pos, boolean value) {
		lois.clear();
		super.setBit(pos, value);
	}
	
	public boolean isLOI(int loi) {
		if(loi<2 || loi>5) throw new IllegalArgumentException("LOI number must be between 2 and 5. number=" + loi);
		return isBit(loi-1);
	}

	public boolean matchAny(BitmappedLOI otherLOIs) {
		for (int i=2; i<=5; i++) {
			if(isLOI(i) && otherLOIs.isLOI(i)) {
				return true;
			}
		}
		return false;
	}

	public void setLOIs(int ... lois) {
		for (int l : lois) {
			setLOI(l, true);
		}
	}

	public static BitmappedLOI valueOf(LOI loi) {
		BitmappedLOI b = new BitmappedLOI();
		b.setLOIs(loi.getNumber());
		return b;
	}

	public boolean isLOI(LOI loi) {
		return isLOI(loi.getNumber());
	}
	
}
