package cn.sanenen.sunutils.db;

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
	
	public Db db(){
		return db;
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

	private List<T> toBean(List<Entity> all) {
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

	/**
	 * 根据id更新
	 *
	 * @param record 准备更新的对象
	 * @return 更新条数
	 * @throws SQLException 异常
	 */
	public int updateById(T record) throws SQLException {
		return updateById(record, true);
	}

	/**
	 * 根据id更新
	 *
	 * @param record          准备更新的对象
	 * @param ignoreNullValue true 忽略null值
	 * @return 更新条数
	 * @throws SQLException 异常
	 */
	public int updateById(T record, boolean ignoreNullValue) throws SQLException {
		return updateById(record, ignoreNullValue, null);
	}

	/**
	 * 根据id更新
	 *
	 * @param record   准备更新的对象
	 * @param dbRecord 数据库对象。不为null时，将会去除掉与record中值相同的字段。避免不必要的更新
	 * @return 更新条数
	 * @throws SQLException 异常
	 */
	public int updateById(T record, T dbRecord) throws SQLException {
		return updateById(record, true, dbRecord);
	}

	/**
	 * 根据id更新
	 *
	 * @param record          准备更新的对象
	 * @param ignoreNullValue true 忽略null值
	 * @param dbRecord        数据库对象。不为null时，将会去除掉与record中值相同的字段。避免不必要的更新
	 * @return 更新条数
	 * @throws SQLException 异常
	 */
	public int updateById(T record, boolean ignoreNullValue, T dbRecord) throws SQLException {
		return update(DbUtil.toEntity(record, ignoreNullValue),
				DbUtil.getIdEntity(record),
				DbUtil.toEntity(dbRecord, ignoreNullValue));
	}

	/**
	 * 根据条件更新
	 *
	 * @param record 准备更新的对象
	 * @param where  条件对象，自行创建。
	 * @return 更新条数
	 * @throws SQLException 异常
	 */
	public int update(T record, T where) throws SQLException {
		return update(record, where, true);
	}

	/**
	 * 根据条件更新
	 *
	 * @param record          准备更新的对象
	 * @param where           条件对象，自行创建。
	 * @param ignoreNullValue true 忽略null值
	 * @return 更新条数
	 * @throws SQLException 异常
	 */
	public int update(T record, T where, boolean ignoreNullValue) throws SQLException {
		return update(record, where, ignoreNullValue, null);
	}

	/**
	 * 根据条件更新
	 *
	 * @param record   准备更新的对象
	 * @param where    条件对象，自行创建。
	 * @param dbRecord 数据库对象。不为null时，将会去除掉与record中值相同的字段。避免不必要的更新
	 * @return 更新条数
	 * @throws SQLException 异常
	 */
	public int update(T record, T where, T dbRecord) throws SQLException {
		return update(record, where, true, dbRecord);
	}

	/**
	 * 根据条件更新
	 *
	 * @param record          准备更新的对象
	 * @param where           条件对象，自行创建。
	 * @param ignoreNullValue true 忽略null值
	 * @param dbRecord        数据库对象。不为null时，将会去除掉与record中值相同的字段。避免不必要的更新
	 * @return 更新条数
	 * @throws SQLException 异常
	 */
	public int update(T record, T where, boolean ignoreNullValue, T dbRecord) throws SQLException {
		return update(DbUtil.toEntity(record, ignoreNullValue)
				//where 条件不忽略null值。
				, DbUtil.toEntity(where, false)
				, DbUtil.toEntity(dbRecord, ignoreNullValue));
	}

	private int update(Entity recordEntity, Entity whereEntity, Entity dbRecordEntity) throws SQLException {
		//不允许无条件更新
		if (whereEntity == null || recordEntity == null) {
			return 0;
		}
		if (dbRecordEntity != null) {
			recordEntity.removeEqual(dbRecordEntity);
		}
		return db.update(recordEntity, whereEntity);
	}
}
