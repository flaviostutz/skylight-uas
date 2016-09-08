package br.skylight.cucs.mapkit.painters;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.painter.Painter;

public interface LayerPainter<T extends JXMapViewer> extends Painter<T> {

	public int getLayerNumber();

}
