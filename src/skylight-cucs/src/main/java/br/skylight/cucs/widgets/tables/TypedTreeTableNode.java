package br.skylight.cucs.widgets.tables;


public class TypedTreeTableNode<T> extends EditableMutableTreeTableNode {

	private static final long serialVersionUID = 1L;
	
	private ObjectToColumnAdapter<T> adapter;
	
	public TypedTreeTableNode(T userObject, ObjectToColumnAdapter<T> adapter) {
		super(userObject);
		this.adapter = adapter;
	}

	@Override
	public Object getValueAt(int column) {
		return adapter.getValueAt(getUserObject(), column);
	}

	@Override
	public void setValueAt(Object value, int column) {
		adapter.setValueAt(getUserObject(), value, column);
	}
	
	@Override
	public T getUserObject() {
		return (T)super.getUserObject();
	}
	
	@Override
	public String toString() {
		return getValueAt(0) + "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userObject == null) ? 0 : userObject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedTreeTableNode<T> other = (TypedTreeTableNode<T>) obj;
		if (userObject == null) {
			if (other.userObject != null)
				return false;
		} else if (!userObject.equals(other.userObject))
			return false;
		return true;
	}
	
}
