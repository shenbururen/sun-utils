package cn.sanenen.sunutils.db;

import cn.hutool.db.Db;
import cn.sanenen.db.BaseDao;

import javax.sql.DataSource;

/**
 * @author sun
 * @date 2021-10-18
 **/
public class UserDao extends BaseDao<User> {
	public UserDao(String groupName) {
		super(groupName);
	}

	public UserDao(Db db) {
		super(db);
	}

	public UserDao(DataSource dataSource) {
		super(dataSource);
	}
}
