package cn.sanenen.sunutils.queue;

/**
 * 内嵌持久化队列所需常量
 *
 * @author sun
 * @date 2021-09-08
 **/
public interface QueueConstant {
	/**
	 * 魔法值
	 */
	String MAGIC = "sunQueue";
	/**
	 * 版本
	 */
	int VERSION = 3;

	String FILE_SEPARATOR = System.getProperty("file.separator");
}
