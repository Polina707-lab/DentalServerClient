package csdev.server;

import java.io.Serializable;

public class Appointment implements Serializable{
	
private static final long serialVersionUID = 1L;

public String datetime;
public String fullName;
public String phone;
public String complaint;

public Appointment (String datetime, String fullName, String phone, String complaint) {
	this.datetime = datetime;
	this.fullName = fullName;
	this.phone = phone;
	this.complaint = complaint;
}

@Override 
public String toString() {
	return datetime + " - " + fullName + " (" + phone + "), " + complaint;
}

}
