package bwsal_bridge;

@SuppressWarnings("serial")
public class BridgeRuntimeException extends RuntimeException {
	public BridgeRuntimeException() {}
	
	public BridgeRuntimeException(String message) { super(message); }
	
	public BridgeRuntimeException(Throwable cause) { super(cause); }
	
	public BridgeRuntimeException(String message, Throwable cause) { super(message, cause); }
}
