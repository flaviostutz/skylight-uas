package br.skylight.commons;

import br.skylight.commons.infra.CoordinatesHelper;

public class CoordinatesHelperTest {

	public static void main(String[] args) {
		if(CoordinatesHelper.getUComponent((float)Math.PI/3F, 1)!=(float)Math.cos(Math.PI/3F)) {
			System.out.println("ERROR! " + CoordinatesHelper.getUComponent((float)Math.PI/3F, 1)+"!="+(float)Math.cos(Math.PI/3F));
		}
		if(CoordinatesHelper.getUComponent((float)Math.PI*2/5, 1)!=(float)Math.cos(Math.PI*2/5F)) {
			System.out.println("ERROR! " + CoordinatesHelper.getUComponent((float)Math.PI/3F, 1)+"!="+(float)Math.cos(Math.PI/3F));
		}
	}
	
}
