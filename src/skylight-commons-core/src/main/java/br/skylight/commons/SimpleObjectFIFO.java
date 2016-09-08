package br.skylight.commons;

public class SimpleObjectFIFO {

	private Object[] queue;
	private int capacity;
	private int size;
	private int head;
	private int tail;

	public SimpleObjectFIFO(int cap) {
		capacity = (cap > 0) ? cap : 1; // at least 1
		queue = new Object[capacity];
		head = 0;
		tail = 0;
		size = 0;
	}

	public synchronized int getSize() {
		return size;
	}

	public synchronized boolean isFull() {
		return (size == capacity);
	}

	public synchronized void add(Object obj) {
		
		while (isFull()) {
			try {
				wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e.getMessage());
			}
		}

		queue[head] = obj;
		head = (head + 1) % capacity;
		size++;

		notifyAll(); // let any waiting threads know about change
	}

	public synchronized Object remove() {
		
		while (size == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e.getMessage());
			}
		}

		Object obj = queue[tail];
		queue[tail] = null; // don't block GC by keeping unnecessary reference
		tail = (tail + 1) % capacity;
		size--;

		notifyAll(); // let any waiting threads know about change

		return obj;
	}
}
