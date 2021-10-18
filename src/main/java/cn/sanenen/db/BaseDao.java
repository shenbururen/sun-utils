package cn.sanenen.db;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.log.Log;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sun
 * @date 2021-10-18
 **/
public class BaseDao<T> extends TypeReference<T> {
	protected final Log log = Log.get(this.getClass());
	protected final Db db;
	private final Class<T> tClass = (Class<T>) TypeUtil.getClass(getType());
	private final String tableName = DbUtil.getTableName(tClass);

	public BaseDao(DataSource dataSource) {
		this.db = Db.use(dataSource);
	}

	public BaseDao(String groupName) {
		this.db = Db.use(groupName);
	}

	public BaseDao(Db db) {
		this.db = db;
	}

	public List<T> query(String sql, Object... params) throws SQLException {
		List<Entity> query = db.query(sql, params);
		return toBean(query);
	}

	public T queryOne(String sql, Object... params) throws SQLException {
		Entity entity = db.queryOne(sql, params);
		return entity.toBean(tClass);
	}

	public List<T> list() throws SQLException {
		List<Entity> all = db.findAll(Entity.create(tableName));
		return toBean(all);
	}

	public List<T> toBean(List<Entity> all) {
		return all.stream().map(entity -> {
			try {
				return entity.toBeanWithCamelCase(tClass.newInstance());
			} catch (Exception e) {
				log.error(e);
			}
			return null;
		}).collect(Collectors.toList());
	}

	public int save(T t) throws SQLException {
		return db.insert(DbUtil.toEntity(t));
	}

	public int[] saveBatch(Collection<T> entityList) throws SQLException {
		List<Entity> entity = DbUtil.toEntity(entityList);
		return db.insert(entity);
	}

	public int del(String field, Object value) throws SQLException {
		return db.del(tableName, field, value);
	}

}
