package br.skylight.uav.infra;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import br.skylight.commons.SimpleObjectFIFO;
import br.skylight.commons.infra.ThreadWorker;

/**
 * Keeps an open connection to a file, and writes to it asynchronously.
 * 
 * @author Edu
 * 
 */
public class AsyncFileWriter extends ThreadWorker {

	private File file;
	private OutputStream fileOut;
	private SimpleObjectFIFO fila;

	// Note: queue doesn't grow - if it's full it's gonna block all calls

	public AsyncFileWriter(File file) throws IOException {
		super(0, 600, 5000);
		if (!file.exists()) {
			file.createNewFile();
		}
		this.file = file;
	}

	public void step() {
		if (fila.getSize() > 0) {
			byte[] conteudo = (byte[]) fila.remove();
			try {
				fileOut.write(conteudo);
				if (fila.getSize() == 0)
					fileOut.flush();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
	}

	public void write(byte[] bytes) {
		fila.add(bytes);
	}

	public void onActivate() {
		fila = new SimpleObjectFIFO(100);
		try {
			fileOut = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void onDeactivate() {
		try {
			fileOut.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
