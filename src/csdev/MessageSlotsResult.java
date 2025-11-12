package csdev;

import java.io.Serializable;

public class MessageSlotsResult extends MessageResult implements Serializable{

	private static final long serialVersionUID = 1L;
	public String[] availableSlots;
	
	public MessageSlotsResult(String[] slots) {
		super(Protocol.CMD_GET_SLOTS);
		this.availableSlots = slots;
	}
	
	public MessageSlotsResult(String errorMessage) {
		super(Protocol.CMD_GET_SLOTS, errorMessage);
	}
	
	
}
