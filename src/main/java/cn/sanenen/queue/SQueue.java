package cn.sanenen.queue;

import cn.hutool.log.Log;
import cn.sanenen.queue.data.DataEntity;
import cn.sanenen.queue.data.DataIndex;
import cn.sanenen.queue.data.FileRunner;
import cn.sanenen.queue.exception.FileEOFException;
import cn.sanenen.queue.exception.FileFormatException;

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
	private DataEntity writerHandle;
	private DataEntity readerHandle;
	/**
	 * 文件操作位置信息
	 */
	private int readerIndex;
	private int writerIndex;

	private final Object lock = new Object();

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
		this.fileLimitLength = fileLimitLength;
		File fileDir = new File(dir);
		if (!fileDir.exists() && !fileDir.isDirectory()) {
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
	private void rotateNextLogWriter() throws IOException, FileFormatException {
		writerIndex = writerIndex + 1;
		synchronized (lock) {
			if (readerHandle != writerHandle) {
				writerHandle.close();
			}
			db.putWriterIndex(writerIndex);
			if (readerHandle.getCurrentFileNumber() == writerIndex){
				writerHandle = readerHandle;
			}else {
				writerHandle = createLogEntity(getLogPath(writerIndex), writerIndex);
			}
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
	public synchronized void add(byte[] message) throws IOException, FileFormatException {
		short status = writerHandle.write(message);
		if (status == DataEntity.WRITE_FULL) {
			rotateNextLogWriter();
			status = writerHandle.write(message);
		}
		if (status == DataEntity.WRITE_SUCCESS) {
			db.incrementSize();
		}
	}

	/**
	 * 从队列存储中取出最先入队的数据，并移除它
	 */
	public synchronized byte[] readNextAndRemove() throws IOException, FileFormatException {
		byte[] b = null;
		try {
			b = readerHandle.readNextAndRemove();
		} catch (FileEOFException e) {
			int deleteNum = readerHandle.getCurrentFileNumber();
			int nextFile = deleteNum + 1;
			readerHandle.close();
			FileRunner.addDeleteFile(getLogPath(deleteNum));
			db.putReaderIndex(nextFile);
			synchronized (lock) {
				if (writerHandle.getCurrentFileNumber() == nextFile && writerHandle.getMappedByteBuffer() != null) {
					readerHandle = writerHandle;
				} else {
					readerHandle = createLogEntity(getLogPath(nextFile), nextFile);
				}
			}
			try {
				b = readerHandle.readNextAndRemove();
			} catch (FileEOFException e1) {
				log.error("read new log file FileEOFException error occurred", e1);
			}
		}
		if (b != null) {
			db.decrementSize();
		}
		return b;
	}

	public void close() {
		db.close();
		readerHandle.close();
		writerHandle.close();
	}

	public long getQueueSize() {
		return db.getSize();
	}
}
