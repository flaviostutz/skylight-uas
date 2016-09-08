package br.skylight.commons.plugins.streamchannel;

public interface FileTransferOperatorListener {

	public void onDataTransfer(int percent);
	public void onTransferCheckedAndComplete();
	public void onTransferFailed(String message);
	public void onTransferMessage(String message);

}
