package csdev;

import java.io.Serializable;


public class MessageSlotsRequest extends Message implements Serializable {
	
    private static final long serialVersionUID = 1L;

    public MessageSlotsRequest() {
        super(Protocol.CMD_GET_SLOTS);
    }
}
