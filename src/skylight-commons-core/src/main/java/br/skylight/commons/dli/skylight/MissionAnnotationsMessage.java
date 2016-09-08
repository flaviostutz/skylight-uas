package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import br.skylight.commons.Path;
import br.skylight.commons.Placemark;
import br.skylight.commons.Region;
import br.skylight.commons.TextAnnotation;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.infra.IOHelper;

public class MissionAnnotationsMessage extends Message<MissionAnnotationsMessage> {

	private ArrayList<TextAnnotation> texts = new ArrayList<TextAnnotation>();
	private ArrayList<Placemark> placemarks = new ArrayList<Placemark>();
	private ArrayList<Region> polygons = new ArrayList<Region>();
	private ArrayList<Path> paths = new ArrayList<Path>();
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2016;
	}

	@Override
	public void resetValues() {
		texts.clear();
		placemarks.clear();
		polygons.clear();
		paths.clear();
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		IOHelper.readArrayList(in, TextAnnotation.class, texts);
		IOHelper.readArrayList(in, Placemark.class, placemarks);
		IOHelper.readArrayList(in, Region.class, polygons);
		IOHelper.readArrayList(in, Path.class, paths);
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		IOHelper.writeArrayList(out, texts);
		IOHelper.writeArrayList(out, placemarks);
		IOHelper.writeArrayList(out, polygons);
		IOHelper.writeArrayList(out, paths);
	}
	
	public ArrayList<TextAnnotation> getTexts() {
		return texts;
	}
	public ArrayList<Placemark> getPlacemarks() {
		return placemarks;
	}
	public ArrayList<Region> getPolygons() {
		return polygons;
	}
	public ArrayList<Path> getPaths() {
		return paths;
	}

}
