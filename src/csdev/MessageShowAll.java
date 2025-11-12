package csdev;

import java.io.Serializable;

public class MessageShowAll extends Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public MessageShowAll() {
        super(Protocol.CMD_SHOW_ALL);
    }
}
