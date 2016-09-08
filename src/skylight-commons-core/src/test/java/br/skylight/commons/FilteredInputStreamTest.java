package br.skylight.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.io.FilteredInputStream;
import br.skylight.commons.io.StreamFilter;

public class FilteredInputStreamTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		FileInputStream is = new FileInputStream(new File("D:\\bedroom.mpg"));
		FilteredInputStream<StreamFilter> fis = new FilteredInputStream<StreamFilter>(is, new StreamFilter() {
			public void doFiltering(byte byteIn, OutputStream os, FilteredInputStream<? extends StreamFilter> is) throws IOException {
				super.doFiltering(byteIn, os, is);
//				System.out.print(".");
			}
		});
		fis.setHighThoughputMode(true);
//		fis.startFilteredMode();
		int c = 0;
		TimedBoolean t = new TimedBoolean(1000);
		while(true) {
//			if(fis.available()>0) {
				int e = fis.read();
				if(e!=-1) {
					c ++;
				}
				if(t.checkTrue()) {
					System.out.println(c + " Kb/s");
					c = 0;
				}
//			} else {
//				Thread.sleep(5);
//			}
		}
	}
	
}
