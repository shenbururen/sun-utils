package cn.sanenen.sunutils.cache;

import cn.hutool.cache.impl.CacheObj;
import cn.hutool.cache.impl.TimedCache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 扩展hutool包的过期缓存，增加getAndRemove方法。固定使用ConcurrentHashMap。
 * @author sun
 * @date 2021-09-08
 **/
public class TimedCacheSub<K, V> extends TimedCache<K, V> {
	public TimedCacheSub(long timeout) {
		super(timeout, new ConcurrentHashMap<>());
	}

	/**
	 * 获取并移除缓存。
	 */
	public V getAndRemove(K key) {
		CacheObj<K, V> co = super.cacheMap.remove(key);
		if (co != null) {
			return co.getValue();
		}
		return null;
	}
}
