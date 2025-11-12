package csdev;

import java.io.Serializable;

public class MessageCancelAppointment extends Message implements Serializable {
    private static final long serialVersionUID = 1L;
    public String datetime; 

    public MessageCancelAppointment(String datetime) {
        super(Protocol.CMD_CANCEL_APPOINTMENT);
        this.datetime = datetime;
    }
}
