package cn.sanenen.queue.exception;

public class FileFormatException extends Exception {
	public FileFormatException() {
		super();
	}

	public FileFormatException(String message) {
		super(message);
	}

	public FileFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileFormatException(Throwable cause) {
		super(cause);
	}
}
