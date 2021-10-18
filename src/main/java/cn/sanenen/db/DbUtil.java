package cn.sanenen.db;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Entity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库操作方面的工具
 *
 * @author sun
 * @date 2020-05-28
 **/
public class DbUtil {
	private static boolean useMybatisPlusAnnotation = false;

	static {
		try {
			Class<TableField> tableFieldClass = TableField.class;
			useMybatisPlusAnnotation = true;
		} catch (NoClassDefFoundError ignored) {
		}
	}

	/**
	 * 将实体对象转换为可hutool入库对象
	 *
	 * @param obj             需要转换的类对象
	 * @param <T>             类
	 * @param ignoreNullValue 是否忽略空值属性，true 忽略
	 * @return 转换完成的 entity
	 */
	public static <T> Entity toEntity(T obj, boolean ignoreNullValue) {
		Class<T> clazz = ClassUtil.getClass(obj);
		if (clazz == null) {
			return null;
		}
		String tableName = getTableName(clazz);
		Entity entity = Entity.create(tableName);
		Collection<PropDesc> props = BeanUtil.getBeanDesc(clazz).getProps();

		String key;
		Method getter;
		Object value;
		for (PropDesc prop : props) {
			getter = prop.getGetter();
			if (getter != null) {
				try {
					value = getter.invoke(obj);
				} catch (Exception ignore) {
					continue;
				}
				if (value == null && ignoreNullValue) {
					continue;
				}
				
				if (useMybatisPlusAnnotation) {
					TableField tableField = prop.getField().getAnnotation(TableField.class);
					if (tableField != null) {
						if (!tableField.exist()) {
							continue;
						}
						key = tableField.value();
					}else {
						key = StrUtil.toUnderlineCase(prop.getFieldName());
					}
				}else {
					key = StrUtil.toUnderlineCase(prop.getFieldName());
				}
				entity.set(key, value);
			}
		}
		return entity;
	}

	/**
	 * 将实体对象转换为可hutool入库对象
	 * 属性值为null忽略。
	 *
	 * @param obj 需要转换的类对象
	 * @param <T> 类
	 * @return 转换完成的 entity
	 */
	public static <T> Entity toEntity(T obj) {
		return toEntity(obj, true);
	}

	/**
	 * 将实体对象List转换为可hutool入库对象List
	 * 属性值为null忽略。
	 *
	 * @param objs 需要转换的类对象List
	 * @param <T>  类
	 * @return 转换完成的 entity List
	 */
	public static <T> List<Entity> toEntity(Collection<T> objs) {
		if (CollUtil.isEmpty(objs)) {
			return null;
		}
		return objs.stream().map(DbUtil::toEntity).collect(Collectors.toList());
	}

	/**
	 * 将实体对象List转换为可hutool入库对象List
	 *
	 * @param objs            需要转换的类对象List
	 * @param <T>             类
	 * @param ignoreNullValue 是否忽略空值属性，true 忽略
	 * @return 转换完成的 entity List
	 */
	public static <T> List<Entity> toEntity(Collection<T> objs, boolean ignoreNullValue) {
		if (CollUtil.isEmpty(objs)) {
			return null;
		}
		return objs.stream().map(o -> toEntity(o, ignoreNullValue)).collect(Collectors.toList());
	}


	public static <T> String getTableName(Class<T> clazz) {
		String tableName = null;
		if (useMybatisPlusAnnotation) {
			//获取表名
			TableName tableNameA = clazz.getAnnotation(TableName.class);
			if (tableNameA != null) {
				tableName = tableNameA.value();
			}
		}
		if (tableName == null) {
			tableName = StrUtil.toUnderlineCase(clazz.getSimpleName());
		}
		return tableName;
	}

	public static <T> Entity getIdEntity(T obj) {
		Class<T> clazz = ClassUtil.getClass(obj);
		if (clazz == null) {
			return null;
		}
		if (!useMybatisPlusAnnotation) {
			return null;
		}
		String tableName = getTableName(clazz);
		Collection<PropDesc> props = BeanUtil.getBeanDesc(clazz).getProps();
		String key;
		Method getter;
		Object value;
		for (PropDesc prop : props) {
			getter = prop.getGetter();
			if (getter != null) {
				try {
					value = getter.invoke(obj);
				} catch (Exception ignore) {
					continue;
				}
				if (value == null) {
					continue;
				}
				TableId tableField = prop.getField().getAnnotation(TableId.class);
				if (tableField != null) {
					key = tableField.value();
					Entity entity = Entity.create(tableName);
					entity.set(key, value);
					return entity;
				}
			}
		}
		return null;
	}
}
