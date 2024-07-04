package com.gitelliq.gqhc.jersey;

public class EngineLookupException extends Exception {

	public EngineLookupException() {
		super();
	}

	public EngineLookupException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public EngineLookupException(String message, Throwable cause) {
		super(message, cause);
	}

	public EngineLookupException(String message) {
		super(message);
	}

	public EngineLookupException(Throwable cause) {
		super(cause);
	}
}
