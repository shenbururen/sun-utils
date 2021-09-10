package cn.sanenen.queue.exception;

public class FileEOFException extends Exception {

	public FileEOFException() {
		super();
	}

	public FileEOFException(String message) {
		super(message);
	}

	public FileEOFException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileEOFException(Throwable cause) {
		super(cause);
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}

}
