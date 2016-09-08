package br.skylight.commons.io;

import java.io.OutputStream;

public interface SequenceListener {

	/**
	 * Called when a desired sequence is found within an stream.
	 * @param fullSequence Entire sequence, including start/end sequence terminators
	 * @param body Part of the sequence that is between the start and the end terminator
	 * @param os Output of filter
	 */
	public void onSequenceFound(byte[] fullSequence, byte[] body, OutputStream os);

}
