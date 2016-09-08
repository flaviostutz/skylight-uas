package br.skylight.commons;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class VerificationResult {

	private List<String> infos = new ArrayList<String>();
	private List<String> warnings = new ArrayList<String>();
	private List<String> errors = new ArrayList<String>();
	
	public void addInfo(String message) {
		infos.add(message);
	}
	public void addWarning(String message) {
		warnings.add(message);
	}
	public void addError(String message) {
		errors.add(message);
	}
	
	public List<String> getInfos() {
		return infos;
	}
	public List<String> getErrors() {
		return errors;
	}
	public List<String> getWarnings() {
		return warnings;
	}

	public String getInfosStr() {
		return getString(infos);
	}
	public String getWarningsStr() {
		return getString(warnings);
	}
	public String getErrorsStr() {
		return getString(errors);
	}
	
	public String getString(List<String> str) {
		String result = "";
		for (String i : str) {
			result += i + "; ";
		}
		if(result.length()>2) {
			return result.substring(0, result.length()-2);
		} else {
			return result;
		}
	}
	
	@Override
	/**
	 * Returns a summary of results
	 */
	public String toString() {
		String r = "";
		
		if(getErrors().size()==0) {
			r = "Validation *PASSED*";
			
		} else {
			r = "Validation *FAILED*";
			if(getErrors().size()>0) {
				r += "\n>Errors: \n   " + getErrorsStr();
			}
		}

		if(getWarnings().size()>0) {
			if(r.length()>0) r+= " ";
			r += "\n>Warnings: \n   " + getWarningsStr();
		}
		
		if(getInfos().size()>0) {
			if(r.length()>0) r+= " ";
			r += "\n>Infos: \n   " + getInfosStr();
		}
		return r.replaceAll(";", "\n   ");
	}

	public void assertNotLowValue(float value, float minForError, float minForWarning, String name) {
		if(value<minForError) {
			addError(name + " value too low: " + value);
		} else if(value<minForWarning) {
			addWarning(name + " value low: " + value);
		}
	}

	public void mergeResults(VerificationResult other) {
		synchronized(other.getErrors()) {
			for (String error : other.getErrors()) {
				addError(error);
			}
		}
		synchronized(other.getWarnings()) {
			for (String warn : other.getWarnings()) {
				addWarning(warn);
			}
		}
		synchronized (other.getInfos()) {
			for (String info : other.getInfos()) {
				addInfo(info);
			}
		}
	}
	
	public void assertRange(float value, float minForError, float minForWarning, float maxForWarning, float maxForError, String name) {
		if(value<minForError) {
			addError(name + " value too low: " + value);
		} else if(value<minForWarning) {
			addWarning(name + " value low: " + value);
		} else if(value>maxForError) {
			addError(name + " value too high: " + value);
		} else if(value>maxForWarning) {
			addWarning(name + " value high: " + value);
		}
	}

	public void assertRange(float value, float minForError, float maxForError, String name) {
		if(value<minForError) {
			addError(name + " value too low: " + value);
		} else if(value>maxForError) {
			addError(name + " value too high: " + value);
		}
	}
	
	public void assertRange(double value, double minForError, double maxForError, String name) {
		if(value<minForError) {
			addError(name + " value too low: " + value);
		} else if(value>maxForError) {
			addError(name + " value too high: " + value);
		}
	}
	
	public void assertNotNull(Object object, String name) {
		if(object==null) {
			addError(name + " cannot be null");
		}
	}
	public void assertValidCoordinate(Coordinates coordinates, String name) {
		if(coordinates.getLatitude()==0) {
			addError(name + ": invalid latitude");
		}
		if(coordinates.getLongitude()==0) {
			addError(name + ": invalid longitude");
		}
	}
	
	public int getOptionPaneResultLevel() {
		if(getErrors().size()>0) {
			return JOptionPane.ERROR_MESSAGE;
		} else if(getWarnings().size()>0) {
			return JOptionPane.WARNING_MESSAGE;
		} else if(getInfos().size()>0) {
			return JOptionPane.INFORMATION_MESSAGE;
		} else {
			return JOptionPane.INFORMATION_MESSAGE;
		}
	}
	
}
