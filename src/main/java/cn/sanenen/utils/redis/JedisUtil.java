package cn.sanenen.utils.redis;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.nosql.redis.RedisDS;
import cn.hutool.log.Log;
import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

	protected Jedis getJedis() {
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
	 * 批量通道模式删除
	 *
	 * @param key key
	 * @return 如果存在则删除该字段并返回1，否则返回0
	 */
	public Long hdel(String key, List<String> fields) {
		try (Jedis jedis = getJedis()) {
			Pipeline pipelined = jedis.pipelined();
			for (String field : fields) {
				pipelined.hdel(key, field);
			}
			pipelined.sync();
			pipelined.close();
		}
		return 1L;
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
	 * 从队列中取出数据。
	 *
	 * @param key   key
	 * @param clazz 需要被转换的对象
	 */
	public <T> T rpop(String key, Class<T> clazz) {
		try (Jedis jedis = getJedis()) {
			String rpop = jedis.rpop(key);
			if (StrUtil.isBlank(rpop)) {
				return null;
			}
			return JSON.parseObject(rpop, clazz);
		}
	}

	/**
	 * 批量从队列中取出数据 6.0以上版本
	 *
	 * @param key   key
	 * @param count 查询出条数
	 * @param clazz 需要被转换的对象
	 */
	public <T> List<T> rpop(String key, int count, Class<T> clazz) {
		try (Jedis jedis = getJedis()) {
			List<String> rpop = jedis.rpop(key, count);
			if (CollUtil.isEmpty(rpop)) {
				return null;
			}
			return rpop.stream().map(s -> JSON.parseObject(s, clazz)).collect(Collectors.toList());
		}
	}

	/**
	 * 批量从队列中取出数据，使用管道方式。
	 *
	 * @param key   key
	 * @param count 查询出条数
	 * @param clazz 需要被转换的对象
	 */
	public <T> List<T> rpopByPip(String key, long count, Class<T> clazz) {
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

	private static String rpopBatchScriptSha = null;

	/**
	 * 批量从队列中取出数据，使用lua脚本方式。
	 * 注意：因为事务关系，单次数量不宜太多。
	 *
	 * @param key   key
	 * @param count 查询出条数
	 * @param clazz 需要被转换的对象
	 */
	public <T> List<T> rpopByLua(String key, long count, Class<T> clazz) {
		try (Jedis jedis = getJedis()) {
			if (rpopBatchScriptSha == null) {
				//这里不用考虑并发，执行多次也没关系。
				rpopBatchScriptSha = jedis.scriptLoad(LuaStr.RPOP_BATCH);
			}
			Object result = jedis.evalsha(rpopBatchScriptSha, Collections.singletonList(key), Collections.singletonList(String.valueOf(count)));
			if (ObjectUtil.isEmpty(result)) {
				return null;
			}
			List<T> results = new ArrayList<>(((List<?>) result).size());
			for (Object obj : (List<?>) result) {
				T t = JSON.parseObject(String.valueOf(obj), clazz);
				results.add(t);
			}
			return results;
		} catch (Exception e) {
			rpopBatchScriptSha = null;
			throw e;
		}
	}

	/**
	 * 批量放入队列数据
	 *
	 * @param key key
	 */
	public <T> Long lpush(String key, List<T> objects) {
		if (CollUtil.isEmpty(objects)) {
			return 0L;
		}
		try (Jedis jedis = getJedis()) {
			String[] strings = objects.stream().map(v -> {
				if (v instanceof String) {
					return String.valueOf(v);
				} else {
					return JSON.toJSONString(v);
				}
			}).toArray(String[]::new);
			return jedis.lpush(key, strings);
		}
	}

	/**
	 * 放入队列数据
	 *
	 * @param key key
	 */
	public <T> Long lpush(String key, T t) {
		if (t == null) {
			return 0L;
		}
		try (Jedis jedis = getJedis()) {
			if (t instanceof String) {
				return jedis.lpush(key, String.valueOf(t));
			} else {
				return jedis.lpush(key, JSON.toJSONString(t));
			}
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

	/**
	 * 将 hashKey 所储存的值减去减量 decrement 。
	 *
	 * @param key       key
	 * @param hKey      hashKey（小key）
	 * @param decrement 减量
	 * @return 减去 decrement 之后， key 的值。
	 */
	public Long hdecrBy(String key, String hKey, long decrement) {
		try (Jedis jedis = getJedis()) {
			return jedis.hincrBy(key, hKey, -decrement);
		}
	}

	/**
	 * 获取hash结构所有值，并转换为对象集合
	 *
	 * @param key   key
	 * @param clazz 对象class
	 * @return 结果集
	 */
	public <T> List<T> hvals(String key, Class<T> clazz) {
		try (Jedis jedis = getJedis()) {
			List<String> hvals = jedis.hvals(key);
			if (CollUtil.isEmpty(hvals)) {
				return null;
			}
			return hvals.stream().map(v -> JSON.parseObject(v, clazz)).collect(Collectors.toList());
		}
	}

	/**
	 * 获取list结构所有值
	 *
	 * @param key key
	 * @return 结果集
	 */
	public List<String> lrangeAll(String key) {
		try (Jedis jedis = getJedis()) {
			List<String> lvals = jedis.lrange(key, 0, -1);
			if (CollUtil.isEmpty(lvals)) {
				return null;
			}
			return lvals;
		}
	}

	/**
	 * 获取list结构所有值
	 *
	 * @param key   key
	 * @param clazz 对象class
	 * @return 结果集
	 */
	public <T> List<T> lrangeAll(String key, Class<T> clazz) {
		List<String> lvals = lrangeAll(key);
		if (CollUtil.isEmpty(lvals)) {
			return null;
		}
		return lvals.stream().map(v -> JSON.parseObject(v, clazz)).collect(Collectors.toList());
	}

	/**
	 * 获取分布式锁
	 *
	 * @param lockKey 锁键
	 * @param appId   应用标识
	 * @return 是否获取锁 true 成功
	 */
	public boolean lock(String lockKey, String appId) {
		return lock(lockKey, appId, 60);
	}

	/**
	 * 获取分布式锁
	 *
	 * @param lockKey    锁键
	 * @param appId      应用标识
	 * @param expireTime 锁超时时间 单位 秒
	 * @return 是否获取锁 true 成功
	 */
	public boolean lock(String lockKey, String appId, long expireTime) {
		try (Jedis jedis = getJedis()) {
			String v = jedis.get(lockKey);
			if (!appId.equals(v)) {
				if (v == null) {
					if (jedis.setnx(lockKey, appId) == 1) {
						jedis.expire(lockKey, expireTime);
						return true;
					}
				}
				return false;
			} else {
				//当前应用已获得锁，重新设置过期时间。
				jedis.expire(lockKey, expireTime);
				return true;
			}
		}
	}

	/**
	 * 释放分布式锁
	 *
	 * @param lockKey 锁key
	 * @param appId   应用id
	 */
	public void releaseLock(String lockKey, String appId) {
		try (Jedis jedis = getJedis()) {
			String v = jedis.get(lockKey);
			if (appId.equals(v)) {
				jedis.del(lockKey);
			}
		}
	}


	//缓存jedis.scriptLoad返回值
	private static String limitScriptSha = null;

	/**
	 * hash结构控制并发或频次
	 * @param key 大key
	 * @param field 小key，一般为 用户名，手机号等。
	 * @param limitCount 并发或频次总量
	 * @return 1 并发或频次已超过。 -1 获取到一并发，或者增加一次。
	 */
	public long hLimit(String key, String field, long limitCount) {
		try (Jedis jedis = getJedis()) {
			//这里不用考虑并发，执行多次也没关系。
			if (limitScriptSha == null) {
				limitScriptSha = jedis.scriptLoad(LuaStr.HASH_LIMIT);
			}
			Object result = jedis.evalsha(limitScriptSha, Arrays.asList(key, field), Collections.singletonList(String.valueOf(limitCount)));
			return NumberUtil.parseInt(String.valueOf(result));
		} catch (Exception e) {
			limitScriptSha = null;
			throw e;
		}
	}

}