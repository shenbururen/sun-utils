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
}
