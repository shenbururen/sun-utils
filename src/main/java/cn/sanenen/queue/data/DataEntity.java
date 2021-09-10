package cn.sanenen.queue.data;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.sanenen.queue.QueueConstant;
import cn.sanenen.queue.exception.FileEOFException;
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

/**
 * raFile.write(QueueConstant.MAGIC.getBytes());0
 * raFile.writeInt(this.fileLimitLength);8
 * raFile.writeInt(readerPosition);12
 * raFile.writeInt(writerPosition);16
 * raFile.writeInt(endPosition);20
 *
 * @author sun
 */
@Data
public class DataEntity {
	private static final Log log = Log.get();
	public static final int DATA_START_POSITION = 24;
	public static final byte WRITE_SUCCESS = 1;
	public static final byte WRITE_FULL = 3;
	
	private RandomAccessFile raFile;
	private FileChannel fc;
	private MappedByteBuffer mappedByteBuffer;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private ByteBuffer readerBuffer;
	private ByteBuffer writerBuffer;

	private int currentFileNumber;

	private final int fileLimitLength;
	private int readerPosition = DataEntity.DATA_START_POSITION;
	private int writerPosition = DataEntity.DATA_START_POSITION;
	private int endPosition = -1;


	public DataEntity(String path, int fileNumber,
	                  int fileLimitLength) throws IOException, FileFormatException {
		this.currentFileNumber = fileNumber;
		File file = new File(path);
		if (file.exists() == false) {
			this.fileLimitLength = fileLimitLength;
			if (file.createNewFile() == false) {
				throw new IOException("create Index File error.");
			}
			raFile = new RandomAccessFile(file, "rwd");
			raFile.write(QueueConstant.MAGIC.getBytes());
			raFile.writeInt(this.fileLimitLength);
			raFile.writeInt(readerPosition);
			raFile.writeInt(writerPosition);
			raFile.writeInt(endPosition);
		} else {
			raFile = new RandomAccessFile(file, "rwd");
			if (raFile.length() < DataEntity.DATA_START_POSITION) {
				throw new FileFormatException("file length error");
			}
			byte[] header = new byte[DataEntity.DATA_START_POSITION];
			raFile.read(header);
			ByteBuffer buffer = ByteBuffer.wrap(header);

			byte[] b = new byte[QueueConstant.MAGIC.getBytes().length];
			buffer.get(b);
			if (QueueConstant.MAGIC.equals(new String(b)) == false) {
				throw new FileFormatException("file format error");
			}
			this.fileLimitLength = buffer.getInt();
			readerPosition = buffer.getInt();
			writerPosition = buffer.getInt();
			endPosition = buffer.getInt();
		}
		fc = raFile.getChannel();
		mappedByteBuffer = fc.map(MapMode.READ_WRITE, 0, this.fileLimitLength);
		readerBuffer = mappedByteBuffer.duplicate();
		writerBuffer = mappedByteBuffer.duplicate();
		executor.execute(new Sync());
	}

	public byte[] readNextAndRemove() throws FileEOFException {
		//当前文件已写满，判断是否已经读完。
		if (this.endPosition != -1 && this.readerPosition >= this.endPosition) {
			throw new FileEOFException("file eof");
		}
		//当前文件还未写满，判断是否已经读完。
		if (this.endPosition == -1 && this.readerPosition >= this.writerPosition) {
			return null;
		}
		readerBuffer.position(this.readerPosition);
		int length = readerBuffer.getInt();
		byte[] b = new byte[length];
		readerBuffer.get(b);

		readerPosition += length + 4;
		readerBuffer.position(12);
		readerBuffer.putInt(readerPosition);
		return b;
	}

	public byte write(byte[] data) {
		int increment = data.length + 4;
		if (isFull(increment)) {
			mappedByteBuffer.position(20);
			mappedByteBuffer.putInt(writerPosition);
			endPosition = writerPosition;
			return WRITE_FULL;
		}
		writerBuffer.position(writerPosition);
		writerBuffer.putInt(data.length);
		writerBuffer.put(data);
		
		writerPosition += increment;
		writerBuffer.position(16);
		writerBuffer.putInt(writerPosition);
		return WRITE_SUCCESS;
	}

	private boolean isFull(int increment) {
		return this.fileLimitLength < this.writerPosition + increment;
	}

	private class Sync implements Runnable {
		@Override
		public void run() {
			while (true) {
				if (mappedByteBuffer != null) {
					try {
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

	public void close() {
		try {
			if (mappedByteBuffer == null) {
				return;
			}
			mappedByteBuffer.force();
			MappedByteBufferUtil.clean(mappedByteBuffer);
			mappedByteBuffer = null;
			executor.shutdown();
			fc.close();
			raFile.close();
		} catch (IOException e) {
			log.error("close logentity file error:", e);
		}
	}
}
