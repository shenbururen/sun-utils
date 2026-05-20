package cn.sanenen.sunutils.queue;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.Log;
import cn.sanenen.sunutils.queue.data.FileRunner;
import cn.sanenen.sunutils.utils.other.DbLog;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 根据<a href="https://github.com/tietang/fqueue">...</a>项目改造而来。
 * 相较于fqueue 读写使用同一把锁，改为 读写 使用各自的锁，切换数据文件时，使用同一把锁。
 * 性能提升大概百分之20。
 * <p>
 * 内嵌本地持久化的高性能队列,主要解决内存队列（ConcurrentLinkedQueue）不能持久化的问题。
 * 应用启动时调用setting方法设置持久化目录和单文件限制大小，默认"smq"目录
 * <pre>
 *     SMQ.setting("/data/nnn");
 *     //文件大小改为100M,禁用内存队列
 *     SMQ.setting("/data/nnn", 100, 0);
 * </pre>
 * @author sun
 */
public class SMQ {
	private static final Log log = Log.get();
	/**
	 * 存储所有的队列服务
	 */
	private static final Map<String, SQueue> queueMap = new ConcurrentHashMap<>();
	/**
	 * 内存缓存队列
	 */
	private static final Map<String, Queue<String>> memoryQueueMap = new ConcurrentHashMap<>();
	private static volatile FileLock fileLock = null;
	private static volatile FileChannel lockChannel = null;
	private static volatile RandomAccessFile lockFile = null;
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	private static FileRunner fileRunner = new FileRunner();
	private static boolean fileRunnerStarted = false;
	/**
	 * 每个队列服务的单个文件存储的大小限制 配置文件中的单位为M
	 */
	private static int dataSize = 1024 * 1024 * 50;
	/**
	 * 内存缓存队列大小，超过此大小，放入持久化队列。
	 */
	private static int memoryQueueSize = 50;
	/**
	 * 配置是否使用内存队列，默认false 不启用
	 */
	private static boolean useMemoryQueue = false;
	/**
	 * 数据存储路径
	 */
	private static String dbPath = "smq";
	private static volatile boolean closed = false;

	static {
		startFileRunner();
		Runtime.getRuntime().addShutdownHook(new Thread(SMQ::close));
	}

	private SMQ() {}

	/**
	 * 配置是否使用内存队列，默认false 不启用
	 */
	public static void useMemoryQueue(boolean useMemoryQueue){
		SMQ.useMemoryQueue = useMemoryQueue;
	}

	/**
	 * @param dbPath 持久化路径
	 */
	public static void setting(String dbPath) {
		SMQ.setting(dbPath, 50, memoryQueueSize);
	}
	/**
	 * @param dbPath  持久化路径
	 * @param logSize 持久化文件大小，单位M 最大不能超过2048M（2G）
	 */
	public static void setting(String dbPath, int logSize, int memoryQueueSize){
		SMQ.setting(dbPath, logSize, memoryQueueSize, false);
	}

	/**
	 * @param dbPath  持久化路径
	 * @param logSize 持久化文件大小，单位M 最大不能超过2048M（2G）
	 */
	public static void setting(String dbPath, int logSize, int memoryQueueSize,boolean useMemoryQueue) {
		if (logSize <= 0) {
			throw new IllegalArgumentException(logSize + ",必须大于0。哎呀");
		}
		if (logSize > 2048) {
			throw new IllegalArgumentException(logSize + ",不可超过2G。");
		}
		if (memoryQueueSize < 0) {
			throw new IllegalArgumentException(memoryQueueSize + ",内存队列大小不能小于0。");
		}
		if (fileLock != null && !SMQ.dbPath.equals(dbPath)) {
			close();
		}
		SMQ.dbPath = dbPath;
		SMQ.dataSize = 1024 * 1024 * logSize;
		SMQ.memoryQueueSize = memoryQueueSize;
		SMQ.useMemoryQueue = useMemoryQueue;
		closed = false;
		startFileRunner();
		isLock();
	}

	private static synchronized void startFileRunner() {
		if (executor == null || executor.isShutdown() || executor.isTerminated()) {
			executor = Executors.newSingleThreadExecutor();
			fileRunner = new FileRunner();
			fileRunnerStarted = false;
		}
		if (fileRunnerStarted) {
			return;
		}
		executor.execute(fileRunner);
		fileRunnerStarted = true;
	}

	/**
	 * 判断目录当前应用是否可用。
	 */
	private static void isLock() {
		if (closed) {
			closed = false;
			startFileRunner();
		}
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
						lockFile = new RandomAccessFile(FileUtil.file(file, "lock.lock"), "rwd");
						lockChannel = lockFile.getChannel();
						fileLock = lockChannel.tryLock();
						if (fileLock != null) {
							return;
						}
						closeLock();
					}
				} catch (Exception e) {
					closeLock();
					log.error(e);
				}
			}
		}
		throw new RuntimeException(dbPath + "目录已被使用。");
	}

	public static synchronized void close() {
		if (closed) {
			return;
		}
		for (Map.Entry<String, Queue<String>> entry : memoryQueueMap.entrySet()) {
			Queue<String> queue = entry.getValue();
			if (queue.isEmpty()) {
				continue;
			}
			SQueue sQueue = getSQueue(entry.getKey());
			while (true) {
				String poll = queue.poll();
				if (poll == null) {
					break;
				}
				try {
					sQueue.add(poll.getBytes(StandardCharsets.UTF_8));
				} catch (Exception e) {
					DbLog.log("saveSQueue error:{}", poll);
				}
			}
		}

		for (SQueue sQueue : queueMap.values()) {
			sQueue.close();
		}
		queueMap.clear();
		memoryQueueMap.clear();
		fileRunner.close();
		executor.shutdown();
		fileRunnerStarted = false;
		closeLock();
		closed = true;
		log.info("close SQueue");
	}

	private static void closeLock() {
		try {
			if (fileLock != null) {
				fileLock.release();
			}
		} catch (IOException e) {
			log.error("release queue lock error", e);
		} finally {
			fileLock = null;
		}
		try {
			if (lockChannel != null) {
				lockChannel.close();
			}
		} catch (IOException e) {
			log.error("close queue lock channel error", e);
		} finally {
			lockChannel = null;
		}
		try {
			if (lockFile != null) {
				lockFile.close();
			}
		} catch (IOException e) {
			log.error("close queue lock file error", e);
		} finally {
			lockFile = null;
		}
	}

	public static String pop(String topic) {
		isLock();
		checkTopic(topic);
		try {
			if (useMemoryQueue && memoryQueueSize > 0) {
				//取内存队列
				Queue<String> queue = getQueue(topic);
				String poll = queue.poll();
				if (poll != null) {
					return poll;
				}
			}
			byte[] data = getSQueue(topic).readNextAndRemove();
			if (data != null) {
				return new String(data, StandardCharsets.UTF_8);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public static void push(String topic, String data) {
		isLock();
		checkTopic(topic);
		try {
			if (useMemoryQueue && memoryQueueSize > 0) {
				Queue<String> queue = getQueue(topic);
				if (queue.size() <= memoryQueueSize) {
					queue.offer(data);
					return;
				}
			}
			getSQueue(topic).add(data.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static long size(String topic) {
		isLock();
		checkTopic(topic);
		if (useMemoryQueue && memoryQueueSize > 0) {
			return getQueue(topic).size() + getSQueue(topic).getQueueSize();
		} else {
			return getSQueue(topic).getQueueSize();
		}
	}


	/**
	 * 获取指定名称的队列存储实例 如果不存存在，根据create参数决定是否创建
	 *
	 * @param topic 队列名
	 */
	private static SQueue getSQueue(String topic) {
		checkTopic(topic);
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

	/**
	 * 获取指定名称的内存队列存储实例 如果不存存在，根据create参数决定是否创建
	 *
	 * @param topic 队列名
	 */
	private static Queue<String> getQueue(String topic) {
		checkTopic(topic);
		Queue<String> queue = memoryQueueMap.get(topic);
		if (queue != null) {
			return queue;
		} else {
			memoryQueueMap.putIfAbsent(topic, new ConcurrentLinkedQueue<>());
		}
		return memoryQueueMap.get(topic);
	}

	private static void checkTopic(String topic) {
		if (topic == null || topic.trim().isEmpty()) {
			throw new IllegalArgumentException("topic不能为空");
		}
		if (!topic.matches("[A-Za-z0-9._-]+")) {
			throw new IllegalArgumentException("topic只能包含字母、数字、点、下划线和中划线");
		}
		if (topic.contains("..")) {
			throw new IllegalArgumentException("topic不能包含..");
		}
	}
}
