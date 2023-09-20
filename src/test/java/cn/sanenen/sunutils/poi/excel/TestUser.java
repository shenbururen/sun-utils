package cn.sanenen.sunutils.poi.excel;

import cn.sanenen.poi.excel.Excel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author sun
 * @date 2021-09-29
 **/
@Data
public class TestUser {
	@Excel(name = "字符串")
	private String strVal;
	@Excel(name = "Long")
	private Long longVal;
	@Excel(name = "Integer")
	private Integer intVal;
	@Excel(name = "Double")
	private Double doubleVal;
	@Excel(name = "BigDecimal")
	private BigDecimal bigDecimalVal;
	@Excel(name = "日期")
	private Date dateVal;
}
