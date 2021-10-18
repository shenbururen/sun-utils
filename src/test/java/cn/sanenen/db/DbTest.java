package cn.sanenen.db;

import cn.hutool.core.lang.Console;
import org.junit.Test;

import java.sql.SQLException;

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
}
