package br.skylight.commons.services;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.SerializableState;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ServiceDefinition;

@ServiceDefinition
public abstract class StorageService extends Worker {

	private File baseDir;
	
	@Override
	public void onActivate() throws Exception {
		baseDir = getBaseDir();
		if(baseDir.exists()) {
			if(!baseDir.isDirectory()) {
				throw new IllegalArgumentException("'baseDir' must be a directory");
			}
		}
		baseDir = IOHelper.resolveDir(baseDir);
	}

	public abstract File getBaseDir();

	public void saveState(SerializableState state, String subject, String filename) throws IOException {
		File dir = baseDir;
		if(subject!=null && subject.trim().length()>0) {
			dir = IOHelper.resolveDir(baseDir, subject);
		}
		IOHelper.writeStateToFile(state, IOHelper.resolveFile(dir, filename));
	}
	
	public <T extends SerializableState> T loadState(String subject, String filename, Class<T> baseClass) {
		try {
			File dir = baseDir;
			if(subject!=null && subject.trim().length()>0) {
				dir = IOHelper.resolveDir(baseDir, subject);
			}
			File f = new File(dir, filename);
			if(f.exists()) {
				return IOHelper.readStateFromFile(f, baseClass);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteFile(String filename) {
		File f = new File(baseDir, filename);
		if(f.exists()) {
			f.delete();
		}
	}

	public File resolveDir(String dirName) {
		return resolveDir(baseDir, dirName);
	}
	
	public File resolveDir(File parentDir, String dirName) {
		return IOHelper.resolveDir(parentDir, dirName);
	}

	public File resolveFile(String fileName) throws IOException {
		File f = new File(baseDir, fileName);
		if(!f.exists()) {
			f.createNewFile();
		}
		return f;
	}

	public File getFile(String fileName) throws IOException {
		return new File(getBaseDir(), fileName);
	}
	public File getFile(String subject, String fileName) {
		return new File(new File(getBaseDir(), subject), fileName);
	}

	public File resolveFile(String dirName, String fileName) throws IOException {
		return resolveFile(null, dirName, fileName);
	}
	
	public File resolveFile(String parentDirName, String dirName, String fileName) throws IOException {
		File parentDir = baseDir;
		if(parentDirName!=null && parentDirName.trim().length()>0) {
			parentDir = resolveDir(parentDirName);
		}
		File f = new File(resolveDir(parentDir, dirName), fileName);
		if(!f.exists()) {
			f.createNewFile();
		}
		return f;
	}
	
	public File resolveTimestampFile(String dirName, String fileSufix) throws IOException {
//		subject = subject.replaceAll("\\s", "");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		String fn = getBaseDir().getName() + "-" + dirName + "-" + tf.format(new Date()) + fileSufix;
		return resolveFile(dirName, df.format(new Date()), fn);
	}

}
