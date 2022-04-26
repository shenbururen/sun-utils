package cn.sanenen.utils.redis;

/**
 * @author sun
 * @date 2021-10-20
 **/
public interface LuaStr {
	/**
	 * 批量从队列取数据的lua脚本
	 */
	String RPOP_BATCH = 
			"local key = KEYS[1]\n" +
			"local maxnum = ARGV[1]\n" +
			"local count = redis.call('llen',key)\n" +
			"if tonumber(count) == 0 then\n" +
			"   return nil\n" +
			"end\n" +
			"if tonumber(count) < tonumber(maxnum) then\n" +
			"   maxnum = tonumber(count)\n" +
			"end\n" +
			"local result = {}\n" +
			"for i = 1,tonumber(maxnum) do\n" +
			"    local val = redis.call('rpop',key)\n" +
			"    if val == nil then\n" +
			"        return result\n" +
			"    else\n" +
			"        result[i] = val\n" +
			"    end\n" +
			"end\n" +
			"return result";
	/**
	 * hash结构并发控制脚本
	 */
	String HASH_LIMIT =
			"local key = KEYS[1]\n" +
					"local field = KEYS[2]\n" +
					"local limit = ARGV[1]\n" +
					"local cur = redis.call('hget', key, field)\n" +
					"if cur then\n" +
					"    if tonumber(limit) <= tonumber(cur) then\n" +
					"        return 1\n" +
					"    end\n" +
					"end\n" +
					"redis.call('hincrBy', key, field, 1)\n" +
					"return -1\n";
}
