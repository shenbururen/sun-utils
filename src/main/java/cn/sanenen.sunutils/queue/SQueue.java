package cn.sanenen.sunutils.queue;

import cn.hutool.log.Log;
import cn.sanenen.sunutils.queue.data.DataEntity;
import cn.sanenen.sunutils.queue.data.DataIndex;
import cn.sanenen.sunutils.queue.data.FileRunner;
import cn.sanenen.sunutils.queue.exception.FileEOFException;
import cn.sanenen.sunutils.queue.exception.FileFormatException;

import java.io.File;
import java.io.IOException;

/**
 * 完成基于文件的先进先出的读写功能
 * 不允许外部创建 请使用FQS
 *
 * @author sun
 */
public class SQueue {
	private static final Log log = Log.get();
	private static final String dbName = "index.i";

	private final int fileLimitLength;
	private final String path;
	private final DataIndex db;
	/**
	 * 文件操作实例
	 */
	private volatile DataEntity writerHandle;
	private volatile DataEntity readerHandle;
	/**
	 * 文件操作位置信息
	 */
	private int readerIndex;
	private int writerIndex;

	private final Object writerLock = new Object();
	private final Object readerLock = new Object();
	private final Object transitionLock = new Object();
	private volatile boolean closed;

	protected SQueue(String path) throws Exception {
		this(path, 1024 * 1024 * 50);
	}

	/**
	 * 在指定的目录中，以fileLimitLength为单个数据文件的最大大小限制初始化队列存储
	 *
	 * @param dir             队列数据存储的路径
	 * @param fileLimitLength 单个数据文件的大小，不能超过2G
	 */
	protected SQueue(String dir, int fileLimitLength) throws Exception {
		if (fileLimitLength <= DataEntity.DATA_START_POSITION + 4) {
			throw new IllegalArgumentException("fileLimitLength too small");
		}
		this.fileLimitLength = fileLimitLength;
		File fileDir = new File(dir);
		if (fileDir.exists() && !fileDir.isDirectory()) {
			throw new IOException("path is not directory");
		}
		if (!fileDir.exists()) {
			if (!fileDir.mkdirs()) {
				throw new IOException("create dir error");
			}
		}
		path = fileDir.getAbsolutePath();
		// 打开db
		db = new DataIndex(path + QueueConstant.FILE_SEPARATOR + dbName);
		writerIndex = db.getWriterIndex();
		readerIndex = db.getReaderIndex();
		writerHandle = createLogEntity(getLogPath(writerIndex),
				writerIndex);
		if (readerIndex == writerIndex) {
			readerHandle = writerHandle;
		} else {
			readerHandle = createLogEntity(getLogPath(readerIndex),
					readerIndex);
		}
	}

	/**
	 * 创建或者获取一个数据读写实例
	 *
	 * @param dbpath     数据文件完整路径含文件名
	 * @param fileNumber 文件编号
	 */
	private DataEntity createLogEntity(String dbpath, int fileNumber) throws IOException,
			FileFormatException {
		return new DataEntity(dbpath, fileNumber, this.fileLimitLength);
	}

	/**
	 * 一个文件的数据写入达到fileLimitLength的时候，滚动到下一个文件实例
	 */
	private void rotateNextLogWriter(DataEntity writer) throws IOException, FileFormatException {
		synchronized (transitionLock) {
			writer.finishWriting();
			writerIndex = writerIndex + 1;
			db.putWriterIndex(writerIndex);
			if (readerHandle != writer) {
				writer.close();
			}
			writerHandle = createLogEntity(getLogPath(writerIndex), writerIndex);
		}
	}

	private DataEntity rotateNextLogReader(DataEntity reader) throws IOException, FileFormatException {
		synchronized (transitionLock) {
			int deleteNum = reader.getCurrentFileNumber();
			int nextFile = deleteNum + 1;
			// 先同步提交新读索引。崩溃后即使旧文件尚未删除，也不会回退到它。
			db.putReaderIndex(nextFile);
			db.force();
			reader.close();
			FileRunner.addDeleteFile(getLogPath(deleteNum));
			readerIndex = nextFile;
			DataEntity writer = writerHandle;
			readerHandle = writer.getCurrentFileNumber() == nextFile ? writer : createLogEntity(getLogPath(nextFile), nextFile);
			return readerHandle;
		}
	}

	private String getLogPath(int index) {
		return path + QueueConstant.FILE_SEPARATOR + "data_" + index + ".d";
	}

	/**
	 * 向队列存储添加一个byte数组
	 *
	 * @param message 数据
	 */
	public void add(byte[] message) throws IOException, FileFormatException {
		synchronized (writerLock) {
			ensureOpen();
		if (message == null) {
			throw new IllegalArgumentException("message can not be null");
		}
		if (message.length + 4 > fileLimitLength - DataEntity.DATA_START_POSITION) {
			throw new IllegalArgumentException("message length exceeds file limit");
		}
		DataEntity writer = writerHandle;
		short status = writer.write(message, db::incrementSize);
		if (status == DataEntity.WRITE_FULL) {
			rotateNextLogWriter(writer);
			status = writerHandle.write(message, db::incrementSize);
		}
		if (status != DataEntity.WRITE_SUCCESS) {
			throw new IOException("write message failed");
		}
		}
	}

	/**
	 * 从队列存储中取出最先入队的数据，并移除它
	 */
	public byte[] readNextAndRemove() throws IOException, FileFormatException {
		synchronized (readerLock) {
			ensureOpen();
			DataEntity reader = readerHandle;
		byte[] b = null;
		try {
			b = reader.readNextAndRemove();
		} catch (FileEOFException e) {
			reader = rotateNextLogReader(reader);
			try {
				b = reader.readNextAndRemove();
			} catch (FileEOFException e1) {
				log.error("read new log file FileEOFException error occurred", e1);
			}
		}
		if (b != null) {
			db.decrementSize();
		}
		return b;
		}
	}

	private void ensureOpen() { if (closed) throw new IllegalStateException("SQueue is closed"); }

	public void close() {
		synchronized (writerLock) { synchronized (readerLock) { synchronized (transitionLock) {
			if (closed) return;
			closed = true;
			db.close();
			if (readerHandle != null) readerHandle.close();
			if (writerHandle != null && writerHandle != readerHandle) writerHandle.close();
		} } }
	}

	public long getQueueSize() {
		return db.getSize();
	}
}
