package org.aopalliance;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class AspectException extends RuntimeException {
	private String message;
	private String stackTrace;
	private Throwable t;

	public AspectException(String paramString) {
		super(paramString);
		this.message = paramString;
		this.stackTrace = paramString;
	}

	public AspectException(String paramString, Throwable paramThrowable) {
		super(paramString + "; nested exception is " + paramThrowable.getMessage());
		this.t = paramThrowable;
		StringWriter localStringWriter = new StringWriter();
		paramThrowable.printStackTrace(new PrintWriter(localStringWriter));
		this.stackTrace = localStringWriter.toString();
	}

	public Throwable getCause() {
		return this.t;
	}

	public String toString() {
		return getMessage();
	}

	public String getMessage() {
		return this.message;
	}

	public void printStackTrace() {
		System.err.print(this.stackTrace);
	}

	public void printStackTrace(PrintStream paramPrintStream) {
		printStackTrace(new PrintWriter(paramPrintStream));
	}

	public void printStackTrace(PrintWriter paramPrintWriter) {
		paramPrintWriter.print(this.stackTrace);
	}
}
