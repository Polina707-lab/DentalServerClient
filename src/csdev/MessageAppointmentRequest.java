package csdev;

import java.io.Serializable;

public class MessageAppointmentRequest extends Message implements Serializable{

	private static final long serialVersionUID = 1L;
	
	public String datetime;
	public String fullName;
	public String phone;
	public String complaint;
	
	public MessageAppointmentRequest ( String datetime,String fullName,String phone,String complaint) {
		super(Protocol.CMD_APPOINTMENT);
		this.datetime = datetime;
		this.fullName = fullName;
		this.phone = phone;
		this.complaint = complaint;
	}
}
