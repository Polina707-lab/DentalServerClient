package csdev;

import java.io.Serializable;

public class MessageCancelResult extends MessageResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public MessageCancelResult() {
        super(Protocol.CMD_CANCEL_APPOINTMENT);
    }

    public MessageCancelResult(String errorMessage) {
        super(Protocol.CMD_CANCEL_APPOINTMENT, errorMessage);
    }
}
