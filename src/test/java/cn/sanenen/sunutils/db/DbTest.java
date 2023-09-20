package cn.sanenen.sunutils.db;

import cn.hutool.core.lang.Console;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * @author sun
 * @date 2021-10-18
 **/
public class DbTest {

	@Test
	public void test1() throws SQLException {
		UserDao mysql = new UserDao("mysql");
		User user = mysql.queryOne("select id,name,pswd,create_time from u_user limit 1");
		Console.log(user);
	}

	@Test
	public void test2() throws SQLException {
		UserDao mysql = new UserDao("mysql");
		List<User> query = mysql.query("select id,name,pswd,create_time from u_user");
		Console.log(query);
	}

	@Test
	public void insert() throws SQLException {
		UserDao mysql = new UserDao("mysql");
		User user = new User();
		user.setId(null);
		user.setName("test11");
		user.setPswd("123456");
		user.setCreateTime(new Date());
		int save = mysql.save(user);
		Console.log(save);
	}

	@Test
	public void update() throws SQLException {
		UserDao mysql = new UserDao("mysql");
		User one = mysql.queryOne("select id,name,pswd,create_time from u_user where id=26");
		User user = new User();
		user.setId(26L);
		user.setName("test11");
		user.setPswd("123456");
		int i = mysql.updateById(user, one);
		Console.log(i);
	}
}
