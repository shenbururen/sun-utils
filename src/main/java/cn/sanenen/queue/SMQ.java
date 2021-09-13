package cn.sanenen.queue;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.Log;
import cn.sanenen.queue.data.FileRunner;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 根据https://github.com/tietang/fqueue项目改造而来。
 * 相较于fqueue 读写使用同一把锁，改为 读写 使用各自的锁，切换数据文件时，使用同一把锁。
 * 性能提升大概百分之20。
 * 
 * 内嵌本地持久化的高性能队列,主要解决内存队列（ConcurrentLinkedQueue）不能持久化的问题。
 * 应用启动时调用setting方法设置持久化目录和单文件限制大小，默认"smq"目录
 *
 * @author sun
 */
public class SMQ {
	private static final Log log = Log.get();
	/**
	 * 存储所有的队列服务
	 */
	private static final Map<String, SQueue> queueMap = new ConcurrentHashMap<>();
	private static volatile FileLock fileLock = null;
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	private static final FileRunner fileRunner = new FileRunner();
	/**
	 * 每个队列服务的单个文件存储的大小限制 配置文件中的单位为M
	 */
	private static int dataSize = 1024 * 1024 * 50;
	/**
	 * 数据存储路径
	 */
	private static String dbPath = "smq";

	static {
		executor.execute(fileRunner);
		Runtime.getRuntime().addShutdownHook(new Thread(SMQ::close));
	}

	private SMQ() {}

	/**
	 * @param dbPath 持久化路径
	 */
	public static void setting(String dbPath) {
		SMQ.setting(dbPath, dataSize);
	}

	/**
	 * @param dbPath  持久化路径
	 * @param logSize 持久化文件大小，单位M 最大不能超过2G
	 */
	public static void setting(String dbPath, int logSize) {
		if (logSize > 2048) {
			throw new RuntimeException(logSize + ",不可超过2G。");
		}
		SMQ.dbPath = dbPath;
		SMQ.dataSize = 1024 * 1024 * logSize;
		isLock();
	}

	/**
	 * 判断目录当前应用是否可用。
	 */
	private static void isLock() {
		if (fileLock != null) {
			return;
		} else {
			synchronized (SMQ.class) {
				if (fileLock != null) {
					return;
				}
				try {
					File file = new File(dbPath);
					if (file.exists() || file.mkdirs()) {
						RandomAccessFile rwd = new RandomAccessFile(FileUtil.file(file, "lock.lock"), "rwd");
						FileChannel channel = rwd.getChannel();
						fileLock = channel.tryLock();
						if (fileLock != null) {
							return;
						}
					}
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
		throw new RuntimeException(dbPath + "目录已被使用。");
	}

	public static void close() {
		for (SQueue sQueue : queueMap.values()) {
			sQueue.close();
		}
		fileRunner.close();
		executor.shutdown();
		log.info("close SQueue");
	}

	public static String get(String topic) {
		isLock();
		try {
			byte[] data = getSQueue(topic).readNextAndRemove();
			if (data != null) {
				return new String(data);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public static void put(String topic, String data) {
		isLock();
		try {
			getSQueue(topic).add(data.getBytes());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static long size(String topic) {
		isLock();
		return getSQueue(topic).getQueueSize();
	}


	/**
	 * 获取指定名称的队列存储实例 如果不存存在，根据create参数决定是否创建
	 *
	 * @param topic 队列名
	 */
	private static SQueue getSQueue(String topic) {
		SQueue sQueue = queueMap.get(topic);
		if (sQueue == null) {
			synchronized (topic.intern()) {
				try {
					sQueue = queueMap.get(topic);
					if (sQueue == null) {
						sQueue = new SQueue(dbPath + QueueConstant.FILE_SEPARATOR + topic, dataSize);
						queueMap.put(topic, sQueue);
					}
				} catch (Exception e) {
					log.error(e);
					throw new RuntimeException("SQueue create or get error.");
				}
			}
		}
		return sQueue;
	}
}
