package cn.sanenen.db;

import lombok.Data;

import java.util.Date;

/**
 * @author sun
 * @date 2021-10-18
 **/
@Data
public class User {
	private Long id;
	private String name;
	private String pswd;
	private Date createTime;
}
