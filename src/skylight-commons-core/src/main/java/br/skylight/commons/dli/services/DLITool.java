package br.skylight.commons.dli.services;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import br.skylight.commons.infra.IOHelper;

public class DLITool {

	public static void main(String[] args) throws IOException {
		FileInputStream fis = new FileInputStream(new File("d://class.java"));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IOHelper.readInputStreamIntoOutputStream(fis, bos);
		String contents = new String(bos.toByteArray());

		BufferedReader sr = new BufferedReader(new StringReader(contents));
		String readStats = "";
		String writeStats = "";
		String resetStats = "";
		String line;
		while ((line = sr.readLine()) != null) {
			line = line.trim();
			String p[] = line.split(" ");
			if(p.length<3) {
				continue;
			}
			String type = p[1];
			String d[] = p[2].split("//");
			int size = -1;
			if (d.length > 1)
				size = Integer.parseInt(d[1].substring(1, 2));
			String name = d[0].substring(0, d[0].length() - 1);

			// double
			if (type.equals("double")) {
				readStats += "\t\t" + name + " = in.readDouble();\n";
				writeStats += "\t\tout.writeDouble(" + name + ");\n";
				resetStats += "\t\t" + name + " = 0;\n";

			// float
			} else if (type.equals("float")) {
				readStats += "\t\t" + name + " = in.readFloat();\n";
				writeStats += "\t\tout.writeFloat(" + name + ");\n";
				resetStats += "\t\t" + name + " = 0;\n";

			// String
			} else if (type.equals("String")) {
				readStats += "\t\t" + name + " = readNullTerminatedString(in);\n";
				writeStats += "\t\twriteNullTerminatedString(out, " + name + ");\n";
				resetStats += "\t\t" + name + " = \"\";\n";

			// byte
			} else if (type.equals("byte")) {
				readStats += "\t\t" + name + " = in.readByte();\n";
				writeStats += "\t\tout.writeByte(" + name + ");\n";
				resetStats += "\t\t" + name + " = (byte)0;\n";
				
			// normal integer
			} else if (type.equals("int") && size == -1) {
				readStats += "\t\t" + name + " = in.readInt();\n";
				writeStats += "\t\tout.writeInt(" + name + ");\n";
				resetStats += "\t\t" + name + " = 0;\n";

			// unsigned byte
			} else if (type.equals("int") && size == 1) {
				readStats += "\t\t" + name + " = in.readUnsignedByte();\n";
				writeStats += "\t\tout.writeByte(" + name + ");\n";
				resetStats += "\t\t" + name + " = (byte)0;\n";

			// unsigned short
			} else if (type.equals("int") && size == 2) {
				readStats += "\t\t" + name + " = in.readUnsignedShort();\n";
				writeStats += "\t\tout.writeShort(" + name + ");\n";
				resetStats += "\t\t" + name + " = 0;\n";

			// unsigned integer
			} else if (type.equals("long") && size == 4) {
				readStats += "\t\t" + name + " = readUnsignedInt(in);\n";
				writeStats += "\t\tout.writeInt((int)" + name + ");\n";
				resetStats += "\t\t" + name + " = 0;\n";

			// enum
			} else {
				readStats += "\t\t" + name + " = " + type + ".values()[in.readUnsignedByte()];\n";
				writeStats += "\t\tout.writeByte(" + name + ".ordinal());\n";
				resetStats += "\t\t" + name + " = "+ type +".values()[0];\n";
			}
		}

		System.out.println("	@Override");
		System.out.println("	public void readState(DataInputStream in) throws IOException {");
		System.out.println("		super.readState(in);");
		System.out.print(readStats);
		System.out.println("	}");
		System.out.println("");
		System.out.println("	@Override");
		System.out.println("	public void writeState(DataOutputStream out) throws IOException {");
		System.out.println("		super.writeState(out);");
		System.out.print(writeStats);
		System.out.println("	}");
		System.out.println("");
		System.out.println("	@Override");
		System.out.println("	public void resetValues() {");
		System.out.print(resetStats);
		System.out.println("	}");
		System.out.println("");
		System.out.println("//TODO implement hashCode() and equals()");

		bos.close();
		fis.close();
	}

}
