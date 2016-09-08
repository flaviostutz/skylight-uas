package br.skylight.commons.infra;

/**
 * Fixed size list that supports only additions of items
 */
public class FixedList<T> {

	private int head;
	private int tail;
	private Object[] list;
	private int maxListSize;
	private boolean full;
	
	public FixedList(int maxListSize) {
		this.maxListSize = maxListSize;
		list = new Object[maxListSize];
		clear();
	}
	
	public void clear() {
		head = 0;
		tail = 0;
		full = false;
	}

	public void addItem(T t) {
		if(full) {
			tail++;
			if(tail>(maxListSize-1)) {
				tail = 0;
			}
		}
		if(head>(maxListSize-1)) {
			head = 0;
		}
		list[head] = t;
		head++;//head is always +1
		if(head==maxListSize) {
			full = true;
		}
	}
	
	public boolean isFull() {
		return full;
	}
	
	public boolean isEmpty() {
		return head==tail;
	}
	
	public T getItem(int index) {
		return (T)list[full?(head + index)%maxListSize:index];
	}
	
	public int getSize() {
		return full?maxListSize:head-tail;
	}
	
}
