package br.skylight.commons;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.swing.text.MaskFormatter;

public class StringHelper {

	public static MaskFormatter idFormatter;
	static {
		try {
			idFormatter = new MaskFormatter("**:**:**:**");
			idFormatter.setValueContainsLiteralCharacters(false);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static NumberFormat af = NumberFormat.getNumberInstance();
	public static NumberFormat nf = NumberFormat.getNumberInstance();
	public static NumberFormat ff = NumberFormat.getNumberInstance();
	static {
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
		nf.setMaximumIntegerDigits(2);
		nf.setMinimumIntegerDigits(2);

		af.setMaximumFractionDigits(2);
		af.setMinimumFractionDigits(0);
		af.setMaximumIntegerDigits(9);
		af.setMinimumIntegerDigits(1);

		ff.setMaximumFractionDigits(2);
		ff.setMinimumFractionDigits(2);
		ff.setMaximumIntegerDigits(7);
		ff.setMinimumIntegerDigits(7);
		ff.setGroupingUsed(false);
	}

	public static DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, new Locale("pt", "BR"));
	
	public static String formatId(int id) {
		try {
			return idFormatter.valueToString(Integer.toHexString(id).toUpperCase() + "00000000");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static String decapitalize(String name, boolean keepFirstLettersInCapitals) {
		name = name.replace("_", " ");
		String[] words = name.split(" ");
		String result = "";
		for (String w : words) {
			result += w.substring(0,1).toUpperCase() + (w.length()>1?w.substring(1).toLowerCase():"") + " ";
		}
		if(!keepFirstLettersInCapitals) {
			result = result.substring(0,1).toUpperCase() + (result.length()>1?result.substring(1).toLowerCase():"") + " ";
		}
		return result.trim();
	}

	public static String formatFixedString(float value) {
		return ff.format(value);
	}

	public static String toHtml(String message) {
		String r = "<html>";
		if(message!=null) {
			message = message.replaceAll(" ", "&nbsp;");
			StringTokenizer st = new StringTokenizer(message, "\n");
			while(st.hasMoreTokens()) {
				r += st.nextToken() + "<br/>";
			}
		}
		r += "</html>";
		return r;
	}

	public static String formatNumber(double value, int decimalDigits) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(decimalDigits);
		nf.setMaximumFractionDigits(decimalDigits);
		return nf.format(value);
	}

	public static String formatElapsedTime(double timeInSeconds) {
		long hou = (long)timeInSeconds / (60 * 60);
		timeInSeconds -= hou * (60 * 60);
		long min = (long)timeInSeconds / (60);
		timeInSeconds -= min * (60);
		long sec = (long)timeInSeconds;
		String result = "";
		if (hou > 0) {
			result += nf.format(hou) + "h";
		}
		if (min > 0 || hou > 0) {
			result += nf.format(min) + "m";
		}
		result += nf.format(sec) + "s";
		return result;
	}

	public static String formatTimestamp(double timeInSeconds) {
		return df.format(new Date((long)(timeInSeconds*1000.0)));
	}


}
