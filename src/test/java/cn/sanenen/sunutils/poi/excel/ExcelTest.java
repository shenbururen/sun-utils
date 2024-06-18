package cn.sanenen.sunutils.poi.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author sun
 * @date 2021-09-29
 **/
public class ExcelTest {
	@Test
	public void excelImportList() throws IOException {
		TestUser testUser = new TestUser();
		testUser.setStrVal("111");
		testUser.setLongVal(0L);
		testUser.setIntVal(0);
		testUser.setDoubleVal(0.0D);
		testUser.setBigDecimalVal(new BigDecimal("0"));
		testUser.setDateVal(new Date());
		
		ExcelHandler<TestUser> excelHandler = new ExcelHandler<>(TestUser.class);
		Workbook workbook = excelHandler.exportExcel(CollUtil.newArrayList(testUser), "test");
		BufferedOutputStream outputStream = FileUtil.getOutputStream("E:\\tmp\\test.xlsx");
		workbook.write(outputStream);
		IoUtil.close(outputStream);
		IoUtil.close(workbook);
	}
	@Test
	public void excelImport() throws IOException {
		ExcelHandler<TestUser> excelHandler = new ExcelHandler<>(TestUser.class);
		Workbook workbook = excelHandler.importTemplateExcel("测试模版");
		BufferedOutputStream outputStream = FileUtil.getOutputStream("F:\\tmp\\test.xlsx");
		workbook.write(outputStream);
		IoUtil.close(outputStream);
		IoUtil.close(workbook);
	}

	@Test
	public void excelExport() throws Exception {
		BufferedInputStream inputStream = FileUtil.getInputStream("F:\\tmp\\test.xlsx");
		ExcelHandler<TestUser> excelHandler = new ExcelHandler<>(TestUser.class);
		List<TestUser> testUsers = excelHandler.importExcel(inputStream);
		testUsers.forEach(System.out::println);
		IoUtil.close(inputStream);
	}
}
