package ru.r2cloud.amsat;

public class AmsatTlmException extends Exception {

	private static final long serialVersionUID = 9090288181654856322L;

	public AmsatTlmException(String message) {
		super(message);
	}
	
	public AmsatTlmException(String message, Throwable e) {
		super(message, e);
	}
	
}
