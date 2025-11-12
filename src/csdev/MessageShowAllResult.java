package csdev;

import java.io.Serializable;

public class MessageShowAllResult extends MessageResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public String[] allAppointments;

    public MessageShowAllResult(String[] allAppointments) {
        super(Protocol.CMD_SHOW_ALL);
        this.allAppointments = allAppointments;
    }

    public MessageShowAllResult(String errorMessage) {
        super(Protocol.CMD_SHOW_ALL, errorMessage);
    }
}
