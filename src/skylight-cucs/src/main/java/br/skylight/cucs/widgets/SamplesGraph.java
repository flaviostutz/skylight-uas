package br.skylight.cucs.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.GridBagLayout;

public class SamplesGraph extends JPanel {

	private Map<Object,SamplesGraphModel> models = new HashMap<Object,SamplesGraphModel>();
	private Color graphBackground = getBackground();  //  @jve:decl-index=0:
	
	public SamplesGraph() {
		initialize();
	}

	private void initialize() {
        this.setLayout(new GridBagLayout());
        this.setSize(new Dimension(198, 44));
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(graphBackground);
		g2.fillRect(0, 0, getWidth(), getHeight());
		for (SamplesGraphModel e : models.values()) {
			float dx = (float)getWidth()/((float)e.getSamples().length);
			float dy = (float)getHeight()/(float)(e.getMaxValue()-e.getMinValue());
			Polygon p = new Polygon();
			p.addPoint(0, getHeight());
			for(int i=0; i<e.getSamples().length; i++) {
				p.addPoint((int)(dx*i), getHeight()-(int)(-e.getMinValue() + dy*e.getSamples()[i]));
			}
			
			//draw polygon
			p.addPoint(getWidth(), getHeight());
			g2.setColor(e.getColor());
			g2.fill(p);
			g2.setColor(e.getColor().darker());
			g2.draw(p);
			
			//draw last value stick
			g2.setColor(e.getColor().darker());
			g2.fillRect(getWidth()-(int)dx, getHeight()-(int)(-e.getMinValue() + dy*e.getSamples()[e.getSamples().length-1]), getWidth(), getHeight());
		}
		setBackground(new Color(0,0,0,0));
		super.paint(g);
	}

	public void addModel(Object id, SamplesGraphModel model) {
		model.setSamplesGraph(this);
		models.put(id, model);
	}
	
	public SamplesGraphModel getModel(Object id) {
		return models.get(id);
	}
	
	public Color getGraphBackground() {
		return graphBackground;
	}
	
	public void setGraphBackground(Color graphBackground) {
		this.graphBackground = graphBackground;
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		final SamplesGraph s = new SamplesGraph();
		final SamplesGraphModel m1 = new SamplesGraphModel();
		m1.setColor(new Color(0,1,0,0.7F));
		final SamplesGraphModel m2 = new SamplesGraphModel();
		m2.setColor(new Color(0,0.7F,0,0.7F));
		final SamplesGraphModel m3 = new SamplesGraphModel();
		m3.setColor(new Color(0,1,0,0.7F));
		s.addModel("test1", m1);
		s.addModel("test2", m2);
//		s.addModel("test3", m3);
		Thread t = new Thread() {
			public void run() {
				while(true) {
					m1.addSample(50 + (1-(2*Math.random())));
					m2.addSample(10 + (1-(5*Math.random())));
					m3.addSample(70 + (1-(1*Math.random())));
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		};
		t.start();
		f.add(s);
		f.setSize(300,300);
		f.setVisible(true);
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
