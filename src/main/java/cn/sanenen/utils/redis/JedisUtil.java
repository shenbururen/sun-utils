package cn.sanenen.utils.redis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.nosql.redis.RedisDS;
import cn.hutool.log.Log;
import com.alibaba.fastjson.JSON;
import redis.clients.jedis.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * redis工具类
 */
public class JedisUtil {
	protected final Log log = Log.get(this.getClass());

	private JedisPool jedisPool;
	private RedisDS redisDS;

	public JedisUtil(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public JedisUtil(RedisDS redisDS) {
		this.redisDS = redisDS;
	}

	public void close() {
		if (jedisPool != null) {
			jedisPool.close();
		} else {
			redisDS.close();
		}
	}

	public Jedis getJedis() {
		if (jedisPool != null) {
			return jedisPool.getResource();
		} else {
			return redisDS.getJedis();
		}
	}

	/**
	 * 删除key
	 *
	 * @param keys key
	 */
	public Long del(String... keys) {
		try (Jedis jedis = getJedis()) {
			return jedis.del(keys);
		}
	}

	/**
	 * 判断key是否存在
	 *
	 * @param key key
	 */
	public Boolean exists(String key) {
		try (Jedis jedis = getJedis()) {
			return jedis.exists(key);
		}
	}

	/**
	 * 设置过期时间
	 *
	 * @param key     key
	 * @param seconds 过期时间 秒
	 */
	public Long expire(String key, Long seconds) {
		try (Jedis jedis = getJedis()) {
			return jedis.expire(key, seconds);
		}
	}

	/**
	 * 重命名key
	 *
	 * @param oldkey oldkey
	 * @param newkey newkey
	 * @return 修改成功时，返回 1 。如果 newkey 已经存在，返回 0 。
	 */
	public Long renamenx(String oldkey, String newkey) {
		try (Jedis jedis = getJedis()) {
			return jedis.renamenx(oldkey, newkey);
		}
	}

	/**
	 * 查询key过期时间
	 *
	 * @param key key
	 * @return 如果未设置过期时间则返回-1，如果密钥不存在，则返回-2
	 */
	public Long ttl(String key) {
		try (Jedis jedis = getJedis()) {
			return jedis.ttl(key);
		}
	}

	/**
	 * 获取值
	 *
	 * @param key key
	 */
	public String get(String key) {
		try (Jedis jedis = getJedis()) {
			return jedis.get(key);
		}
	}

	/**
	 * 设置值
	 *
	 * @param key key
	 */
	public void set(String key, String value) {
		try (Jedis jedis = getJedis()) {
			jedis.set(key, value);
		}
	}

	/**
	 * 将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位)。
	 *
	 * @param key key
	 */
	public void setex(String key, long seconds, String value) {
		try (Jedis jedis = getJedis()) {
			jedis.setex(key, seconds, value);
		}
	}

	/**
	 * 将 key 的值设为 value ，当且仅当 key 不存在。
	 *
	 * @param key key
	 * @return 设置成功，返回 1 。设置失败，返回 0 。
	 */
	public Long setnx(String key, String value) {
		try (Jedis jedis = getJedis()) {
			return jedis.setnx(key, value);
		}
	}

	/**
	 * 将 key 中储存的数字值减一。
	 *
	 * @param key key
	 * @return 执行 DECR 命令之后 key 的值。
	 */
	public Long decr(String key) {
		try (Jedis jedis = getJedis()) {
			return jedis.decr(key);
		}
	}

	/**
	 * 将 key 所储存的值减去减量 decrement 。
	 *
	 * @param key key
	 * @return 减去 decrement 之后， key 的值。
	 */
	public Long decrBy(String key, long decrement) {
		try (Jedis jedis = getJedis()) {
			return jedis.decrBy(key, decrement);
		}
	}

	/**
	 * 将 key 中储存的数字值增一。
	 *
	 * @param key key
	 * @return 执行 INCR 命令之后 key 的值。
	 */
	public Long incr(String key) {
		try (Jedis jedis = getJedis()) {
			return jedis.incr(key);
		}
	}

	/**
	 * 将 key 所储存的值加上增量 increment 。
	 *
	 * @param key key
	 * @return 加上 increment 之后， key 的值。
	 */
	public Long incrBy(String key, long increment) {
		try (Jedis jedis = getJedis()) {
			return jedis.incrBy(key, increment);
		}
	}

	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)。
	 *
	 * @param key key
	 */
	public String getSet(String key, String value) {
		try (Jedis jedis = getJedis()) {
			return jedis.getSet(key, value);
		}
	}

	/**
	 * @param key key
	 * @return 如果存在则删除该字段并返回1，否则返回0
	 */
	public Long hdel(String key, String field) {
		try (Jedis jedis = getJedis()) {
			return jedis.hdel(key, field);
		}
	}

	/**
	 * 查看哈希表 key 中，给定域 field 是否存在。
	 *
	 * @param key   key
	 * @param field 小key
	 */
	public Boolean hexists(String key, String field) {
		try (Jedis jedis = getJedis()) {
			return jedis.hexists(key, field);
		}
	}

	/**
	 * @param key   key
	 * @param field 小key
	 */
	public String hget(String key, String field) {
		try (Jedis jedis = getJedis()) {
			return jedis.hget(key, field);
		}
	}

	/**
	 * @param key key
	 */
	public Map<String, String> hgetAll(String key) {
		try (Jedis jedis = getJedis()) {
			return jedis.hgetAll(key);
		}
	}

	/**
	 * 为哈希表 key 中的域 field 的值加上增量 increment 。
	 *
	 * @param key key
	 * @return 执行 HINCRBY 命令之后，哈希表 key 中域 field 的值。
	 */
	public Long hincrBy(String key, String field, long value) {
		try (Jedis jedis = getJedis()) {
			return jedis.hincrBy(key, field, value);
		}
	}

	/**
	 * 返回哈希表 key 中的所有域。
	 *
	 * @param key key
	 */
	public Set<String> hkeys(String key) {
		try (Jedis jedis = getJedis()) {
			return jedis.hkeys(key);
		}
	}

	/**
	 * 返回哈希表 key 中域的数量。
	 *
	 * @param key key
	 */
	public Long hlen(String key) {
		try (Jedis jedis = getJedis()) {
			return jedis.hlen(key);
		}
	}

	/**
	 * 将哈希表 key 中的域 field 的值设为 value 。
	 *
	 * @param key key
	 */
	public Long hset(String key, String field, String value) {
		try (Jedis jedis = getJedis()) {
			return jedis.hset(key, field, value);
		}
	}

	/**
	 * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。
	 *
	 * @param key key
	 */
	public Long hsetnx(String key, String field, String value) {
		try (Jedis jedis = getJedis()) {
			return jedis.hsetnx(key, field, value);
		}
	}

	/**
	 * 返回哈希表 key 中所有域的值。
	 *
	 * @param key key
	 */
	public List<String> hvals(String key) {
		try (Jedis jedis = getJedis()) {
			return jedis.hvals(key);
		}
	}

	/**
	 * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。
	 *
	 * @param key key
	 */
	public void sadd(String key, String... member) {
		try (Jedis jedis = getJedis()) {
			jedis.sadd(key, member);
		}
	}

	/**
	 * 返回集合 key 的基数(集合中元素的数量)。
	 *
	 * @param key key
	 */
	public Long scard(String key) {
		try (Jedis jedis = getJedis()) {
			return jedis.scard(key);
		}
	}

	/**
	 * 判断 member 元素是否集合 key 的成员。
	 *
	 * @param key key
	 */
	public Boolean sismember(String key, String member) {
		try (Jedis jedis = getJedis()) {
			return jedis.sismember(key, member);
		}
	}

	/**
	 * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。
	 *
	 * @param key key
	 */
	public Long srem(String key, String... member) {
		try (Jedis jedis = getJedis()) {
			return jedis.srem(key, member);
		}
	}

	/**
	 * 从队列中取出数据
	 *
	 * @param key   key
	 * @param count 查询出条数
	 * @param clazz 需要被转换的对象
	 */
	public <T> List<T> rpop(String key, long count, Class<T> clazz) {
		try (Jedis jedis = getJedis()) {
			Long size = jedis.llen(key);
			if (size == null || size <= 0) {
				return null;
			}
			if (size < count) {
				count = size;
			}
			List<Response<String>> responses = new ArrayList<>();
			Pipeline p = jedis.pipelined();
			for (int i = 0; i < count; i++) {
				responses.add(p.rpop(key));
			}
			p.sync();
			List<T> resultList = new ArrayList<>();
			for (Response<String> response : responses) {
				String v = response.get();
				if (StrUtil.isNotBlank(v)) {
					try {
						T info = JSON.parseObject(v, clazz);
						if (info != null) {
							resultList.add(info);
						}
					} catch (Exception e) {
						log.error(e, "json转换出错,已跳过该数据:{}", v);
					}
				}
			}
			p.close();
			return resultList;
		}
	}

	/**
	 * 从队列中取出数据
	 *
	 * @param key key
	 */
	public <T> Long lpush(String key, List<T> objects) {
		if (CollUtil.isEmpty(objects)) {
			return 0L;
		}
		try (Jedis jedis = getJedis()) {
			String[] strings = objects.stream()
					.map(JSON::toJSONString)
					.toArray(String[]::new);
			return jedis.lpush(key, strings);
		}
	}

	/**
	 * 迭代哈希表中的键值对。
	 *
	 * @param key     大key
	 * @param cursor  光标。等于 ScanParams.SCAN_POINTER_START 时，说明数据已读完。
	 * @param count   查询出条数
	 * @param pattern 如 * ，pre_*
	 */
	public ScanResult<Entry<String, String>> hscan(String key, String cursor, int count, String pattern) {
		try (Jedis jedis = getJedis()) {
			ScanParams params = new ScanParams();
			params.count(count);
			if (pattern != null) {
				params.match(pattern);
			}
			return jedis.hscan(key, cursor, params);
		}
	}

	/**
	 * key模糊查找
	 *
	 * @param cursor  光标。等于 ScanParams.SCAN_POINTER_START 时，说明数据已读完。
	 * @param count   查询出条数
	 * @param pattern 如 * ，pre_*
	 */
	public ScanResult<String> scan(String cursor, int count, String pattern) {
		try (Jedis jedis = getJedis()) {
			ScanParams scanParams = new ScanParams().match(pattern).count(count);
			return jedis.scan(cursor, scanParams);
		}
	}

	/**
	 * hash结构 key模糊查找
	 *
	 * @param keys 要查找的key 例: aaa* ,aaa开头的所有key
	 * @return 匹配到的key
	 */
	public List<Entry<String, String>> hkeys(String key, String keys) {
		try (Jedis jedis = getJedis()) {
			List<Entry<String, String>> list = new ArrayList<>();
			String cursor = ScanParams.SCAN_POINTER_START;
			ScanParams scanParams = new ScanParams().match(keys).count(10000);
			do {
				ScanResult<Entry<String, String>> hscan = jedis.hscan(key, cursor, scanParams);
				list.addAll(hscan.getResult());
				cursor = hscan.getCursor();
			} while (!ScanParams.SCAN_POINTER_START.equals(cursor));
			return list;
		}
	}

	/**
	 * key模糊查找
	 *
	 * @param keys 要查找的key 例: aaa* ,aaa开头的所有key
	 * @return 匹配到的key
	 */
	public Set<String> keys(String keys) {
		TreeSet<String> set = new TreeSet<>();
		try (Jedis jedis = getJedis()) {
			String cursor = ScanParams.SCAN_POINTER_START;
			ScanParams scanParams = new ScanParams().match(keys).count(10000);
			do {
				ScanResult<String> scan = jedis.scan(cursor, scanParams);
				set.addAll(scan.getResult());
				cursor = scan.getCursor();
			} while (!ScanParams.SCAN_POINTER_START.equals(cursor));
		}
		return set;
	}

}