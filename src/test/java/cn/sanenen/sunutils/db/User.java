package cn.sanenen.sunutils.db;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author sun
 * @date 2021-10-18
 **/
@Data
@TableName("u_user")
public class User {
	@TableId
	private Long id;
	private String name;
	private String pswd;
	private Date createTime;
}
