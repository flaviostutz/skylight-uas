package br.skylight.commons.dli.subsystemstatus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.Alert;
import br.skylight.commons.dli.annotations.MessageField;
import br.skylight.commons.dli.enums.AlertPriority;
import br.skylight.commons.dli.enums.AlertType;
import br.skylight.commons.dli.enums.Subsystem;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class SubsystemStatusAlert extends Message<SubsystemStatusAlert> {

	@MessageField(number=4)
	public AlertPriority priority;//u1
	@MessageField(number=5)
	public int subsystemStateReportReference;
	@MessageField(number=6)
	public Subsystem subsystemID;//u1
	@MessageField(number=7)
	public AlertType type;//u1
	@MessageField(number=8)
	public int warningID;
	@MessageField(number=9)
	public String text = "";//c80
	@MessageField(number=10)
	public byte persistence;
	
	//transients
	private boolean handled;
	
	public SubsystemStatusAlert() {
		resetValues();
	}
	
	public SubsystemStatusAlert(int warningID, Subsystem subsystemID, AlertPriority priority, byte persistence, AlertType type) {
		this.warningID = warningID;
		this.subsystemID = subsystemID;
		this.priority = priority;
		this.persistence = persistence;
		this.type = type;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M1100;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		priority = AlertPriority.values()[in.readUnsignedByte()];
		subsystemStateReportReference = in.readInt();
		subsystemID = Subsystem.values()[in.readUnsignedByte()];
		type = AlertType.values()[in.readUnsignedByte()];
		warningID = in.readInt();
		text = readNullTerminatedString(in);
		persistence = in.readByte();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(priority.ordinal());
		out.writeInt(subsystemStateReportReference);
		out.writeByte(subsystemID.ordinal());
		out.writeByte(type.ordinal());
		out.writeInt(warningID);
		writeNullTerminatedString(out, text);
		out.writeByte(persistence);
	}

	@Override
	public void resetValues() {
		priority = AlertPriority.CLEARED;
		subsystemStateReportReference = 0;
		subsystemID = null;
		type = null;
		warningID = 0;
		text = "";
		persistence = 0;
	}

	@Override
	public boolean isUseInstanceCacheOptimization() {
		return false;
	}

	public AlertPriority getPriority() {
		return priority;
	}
	
	public void setPriority(AlertPriority priority) {
		this.priority = priority;
	}

	public int getSubsystemStateReportReference() {
		return subsystemStateReportReference;
	}

	public void setSubsystemStateReportReference(int subsystemStateReportReference) {
		this.subsystemStateReportReference = subsystemStateReportReference;
	}

	public Subsystem getSubsystemID() {
		return subsystemID;
	}

	public void setSubsystemID(Subsystem subsystemID) {
		this.subsystemID = subsystemID;
	}

	public AlertType getType() {
		return type;
	}

	public void setType(AlertType type) {
		this.type = type;
	}

	public int getWarningID() {
		return warningID;
	}

	public void setWarningID(int warningID) {
		this.warningID = warningID;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public byte getPersistence() {
		return persistence;
	}

	public void setPersistence(byte persistence) {
		this.persistence = persistence;
	}
	
	public Alert getAlert() {
		for (Alert alert : Alert.values()) {
			if(alert.getSubsystemStatusAlert().getWarningID()==warningID) {
				return alert;
			} else if(warningID>=(Alert.SOFTWARE_WARNING.getSubsystemStatusAlert().getWarningID()*1000) && warningID<=((Alert.SOFTWARE_WARNING.getSubsystemStatusAlert().getWarningID()*1000)+999)) {
				return Alert.SOFTWARE_WARNING;
			}
		}
		return null;
	}
	
	public boolean isHandled() {
		return handled;
	}
	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	public void copyFrom(SubsystemStatusAlert sa) {
		priority = sa.priority;
		subsystemStateReportReference = sa.subsystemStateReportReference;
		subsystemID = sa.subsystemID;
		type = sa.type;
		warningID = sa.warningID;
		text = sa.text;
		persistence = sa.persistence;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + warningID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubsystemStatusAlert other = (SubsystemStatusAlert) obj;
		if (warningID != other.warningID)
			return false;
		return true;
	}

}
