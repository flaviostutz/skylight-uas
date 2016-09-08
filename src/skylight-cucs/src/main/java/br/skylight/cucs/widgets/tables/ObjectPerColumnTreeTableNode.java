package br.skylight.cucs.widgets.tables;


public class ObjectPerColumnTreeTableNode extends EditableMutableTreeTableNode {

	public ObjectPerColumnTreeTableNode(Object ... values) {
		super(null, values.length);
		super.setUserObject(values);
	}
	
	@Override
	public int getColumnCount() {
		return ((Object[])getUserObject()).length;
	}

	@Override
	public Object getValueAt(int i) {
		Object[] o = ((Object[])getUserObject());
		if(i<o.length) {
			return o[i];
		} else {
			return null;
		}
	}
	
	@Override
	public void setValueAt(Object obj, int i) {
		Object[] o = ((Object[])getUserObject());
		if(i<o.length) {
			o[i] = obj;
		}
	}
	
}
