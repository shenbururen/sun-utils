package cn.sanenen.queue.data;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.sanenen.queue.QueueConstant;
import cn.sanenen.queue.exception.FileFormatException;
import cn.sanenen.queue.util.MappedByteBufferUtil;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据索引文件
 * 非线程安全
 * <p>
 * dbRandFile.write(QueueConstant.MAGIC.getBytes());0
 * dbRandFile.writeInt(QueueConstant.VERSION); 8
 * dbRandFile.writeInt(readerIndex); 12
 * dbRandFile.writeInt(writerIndex); 16
 * dbRandFile.writeLong(0); 20
 *
 * @author sun
 */
@Data
public class DataIndex {
	private static final Log log = Log.get();
	public final static int INDEX_LIMIT_LENGTH = 28;

	private RandomAccessFile dbRandFile;
	private FileChannel fc;
	private MappedByteBuffer mappedByteBuffer;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private boolean syncRunFlag = true;

	private ByteBuffer readerBuffer;
	private ByteBuffer writerBuffer;

	private int readerIndex = 1;
	private int writerIndex = 1;
	private AtomicLong size = new AtomicLong();

	public long getSize() {
		return size.get();
	}

	public DataIndex(String path) throws IOException, FileFormatException {
		File dbFile = new File(path);
		if (dbFile.exists() == false) {
			if (dbFile.createNewFile() == false) {
				throw new IOException("create Index File error.");
			}
			dbRandFile = new RandomAccessFile(dbFile, "rwd");
			dbRandFile.write(QueueConstant.MAGIC.getBytes());
			dbRandFile.writeInt(QueueConstant.VERSION);
			dbRandFile.writeInt(readerIndex);
			dbRandFile.writeInt(writerIndex);
			dbRandFile.writeLong(0);
		} else {
			dbRandFile = new RandomAccessFile(dbFile, "rwd");
			if (dbRandFile.length() < DataIndex.INDEX_LIMIT_LENGTH) {
				throw new FileFormatException("index file format error");
			}
			byte[] b = new byte[DataIndex.INDEX_LIMIT_LENGTH];
			dbRandFile.read(b);
			ByteBuffer buffer = ByteBuffer.wrap(b);
			b = new byte[QueueConstant.MAGIC.getBytes().length];
			buffer.get(b);
			if (QueueConstant.MAGIC.equals(new String(b)) == false) {
				throw new FileFormatException("index file magic error");
			}
			if (QueueConstant.VERSION != buffer.getInt()) {
				throw new FileFormatException("index file version error");
			}
			readerIndex = buffer.getInt();
			writerIndex = buffer.getInt();
			size.set(buffer.getLong());

		}
		fc = dbRandFile.getChannel();
		mappedByteBuffer = fc.map(MapMode.READ_WRITE, 0, DataIndex.INDEX_LIMIT_LENGTH);
		readerBuffer = mappedByteBuffer.duplicate();
		writerBuffer = mappedByteBuffer.duplicate();
		executor.execute(new Sync());
	}

	/**
	 * 记录读取文件索引
	 */
	public void putReaderIndex(int index) {
		readerBuffer.position(12);
		readerBuffer.putInt(index);
		this.readerIndex = index;
	}

	/**
	 * 记录写文件索引
	 */
	public void putWriterIndex(int index) {
		writerBuffer.position(16);
		writerBuffer.putInt(index);
		this.writerIndex = index;
	}

	public void incrementSize() {
		size.incrementAndGet();
	}

	public void decrementSize() {
		size.decrementAndGet();
	}

	private class Sync implements Runnable {
		@Override
		public void run() {
			while (syncRunFlag) {
				if (mappedByteBuffer != null) {
					try {
						mappedByteBuffer.position(20);
						mappedByteBuffer.putLong(size.get());
						mappedByteBuffer.force();
					} catch (Exception e) {
						break;
					}
					ThreadUtil.sleep(10);
				} else {
					break;
				}
			}
		}
	}

	/**
	 * 关闭索引文件
	 */
	public void close() {
		try {
			mappedByteBuffer.force();
			MappedByteBufferUtil.clean(mappedByteBuffer);
			fc.close();
			dbRandFile.close();
			fc = null;
			mappedByteBuffer = null;
			dbRandFile = null;
			syncRunFlag = false;
			executor.shutdown();
		} catch (IOException e) {
			log.error("close index file error:", e);
		}
	}
}
