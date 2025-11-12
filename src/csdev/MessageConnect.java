package csdev;

import java.io.Serializable;

public class MessageConnect extends Message implements Serializable{

	private static final long serialVersionUID = 1L;
	public String clientName;
	
	public MessageConnect(String clientName) {
		super (Protocol.CMD_CONNECT);
		this.clientName = clientName;
	}
	
	
	
}
