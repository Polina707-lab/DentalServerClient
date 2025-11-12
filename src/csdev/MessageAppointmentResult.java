package csdev;

import java.io.Serializable;

public class MessageAppointmentResult extends MessageResult implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public MessageAppointmentResult() {
		super(Protocol.CMD_APPOINTMENT);
	}
	

	public MessageAppointmentResult(String errorMessage) {
		super(Protocol.CMD_APPOINTMENT, errorMessage);
	}
}
