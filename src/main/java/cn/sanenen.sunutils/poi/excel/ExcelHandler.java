package cn.sanenen.sunutils.poi.excel;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.log.Log;
import cn.hutool.poi.excel.ExcelPicUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.cell.CellUtil;
import cn.sanenen.sunutils.poi.excel.Excel.Type;
import cn.sanenen.sunutils.utils.other.ReflectUtil;
import cn.sanenen.sunutils.poi.excel.Excel.ColumnType;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Excel相关处理
 *
 * @author ruoyi
 */
public class ExcelHandler<T> {
	private static final Log log = Log.get();

	/**
	 * Excel sheet最大行数，默认65536
	 */
	public static final double sheetSize = 65536;

	/**
	 * 工作表名称
	 */
	private String sheetName;

	/**
	 * 导出类型（EXPORT:导出数据；IMPORT：导入模板）
	 */
	private Type type;

	private ExcelReader reader;

	/**
	 * 工作薄对象
	 */
	private Workbook wb;

	/**
	 * 工作表对象
	 */
	private Sheet sheet;

	/**
	 * 样式列表
	 */
	private Map<String, CellStyle> styles;

	/**
	 * 导入导出数据列表
	 */
	private List<T> list;

	/**
	 * 注解列表
	 */
	private List<Object[]> fields;

	/**
	 * 最大高度
	 */
	private short maxHeight;

	/**
	 * 统计列表
	 */
	private Map<Integer, Double> statistics = new HashMap<Integer, Double>();

	/**
	 * 数字格式
	 */
	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("######0.00");

	/**
	 * 实体对象
	 */
	public Class<T> clazz;

	public ExcelHandler(Class<T> clazz) {
		this.clazz = clazz;
	}

	public void init(List<T> list, String sheetName, Type type) {
		if (list == null) {
			list = new ArrayList<T>();
		}
		this.list = list;
		this.sheetName = sheetName;
		this.type = type;
		createExcelField();
		createWorkbook();
	}

	/**
	 * 对excel表单默认第一个索引名转换成list
	 *
	 * @param is 输入流
	 * @return 转换后集合
	 */
	public List<T> importExcel(InputStream is) throws Exception {
		return importExcel(StrUtil.EMPTY, is);
	}

	/**
	 * 对excel表单指定表格索引名转换成list
	 *
	 * @param sheetName 表格索引名
	 * @param is        输入流
	 * @return 转换后集合
	 */
	public List<T> importExcel(String sheetName, InputStream is) throws Exception {
		this.type = Type.IMPORT;
		List<T> list = new ArrayList<>();
		// 如果指定sheet名,则取指定sheet中的内容 否则默认指向第1个sheet
		if (StrUtil.isNotBlank(sheetName)) {
			reader = ExcelUtil.getReader(is, sheetName);
		} else {
			reader = ExcelUtil.getReader(is);
		}
		this.wb = reader.getWorkbook();
		Sheet sheet = reader.getSheet();
		Map<String, PictureData> pictures = ExcelPicUtil.getPicMap(wb, wb.getSheetIndex(sheet));
		// 获取最后一个非空行的行下标，比如总行数为n，则返回的为n-1
		int rows = sheet.getLastRowNum();
		if (rows > 0) {
			// 定义一个map用于存放excel列的序号和field.
			Map<String, Integer> cellMap = new HashMap<>();
			// 获取表头
			Row heard = sheet.getRow(0);
			heard.cellIterator().forEachRemaining(cell -> {
				if (Objects.nonNull(cell)) {
					String value = CellUtil.getCellValue(cell).toString();
					cellMap.put(value, cell.getColumnIndex());
				}
			});

			Field[] allFields = clazz.getDeclaredFields();
			// 定义一个map用于存放列的序号和field.
			Map<Integer, Field> fieldsMap = new HashMap<>();
			for (Field field : allFields) {
				cn.sanenen.sunutils.poi.excel.Excel attr = field.getAnnotation(cn.sanenen.sunutils.poi.excel.Excel.class);
				if (attr != null && (attr.type() == Type.ALL || attr.type() == type)) {
					// 设置类的私有字段属性可访问.
					field.setAccessible(true);
					Integer column = cellMap.get(attr.name());
					if (column != null) {
						fieldsMap.put(column, field);
					}
				}
			}
			for (int i = 1; i <= rows; i++) {
				// 从第2行开始取数据,默认第一行是表头.
				Row row = sheet.getRow(i);
				// 判断当前行是否是空行
				if (isRowEmpty(row)) {
					continue;
				}
				T entity = null;
				for (Map.Entry<Integer, Field> entry : fieldsMap.entrySet()) {
					Object val = CellUtil.getCellValue(CellUtil.getCell(row, entry.getKey()));

					// 如果不存在实例则新建.
					entity = (entity == null ? clazz.newInstance() : entity);
					// 从map中得到对应列的field.
					Field field = entry.getValue();
					// 取得类型,并根据对象类型设置值.
					Class<?> fieldType = field.getType();
					if (String.class == fieldType) {
						String s = Convert.toStr(val);
						if (StrUtil.endWith(s, ".0")) {
							val = StrUtil.subBefore(s, ".0", false);
						} else {
							String dateFormat = field.getAnnotation(Excel.class).dateFormat();
							if (StrUtil.isNotEmpty(dateFormat)) {
								val = DateUtil.format((Date) val, dateFormat);
							} else {
								val = Convert.toStr(val);
							}
						}
					} else if ((Integer.TYPE == fieldType || Integer.class == fieldType) && StrUtil.isNumeric(Convert.toStr(val))) {
						val = Convert.toInt(val);
					} else if (Long.TYPE == fieldType || Long.class == fieldType) {
						val = Convert.toLong(val);
					} else if (Double.TYPE == fieldType || Double.class == fieldType) {
						val = Convert.toDouble(val);
					} else if (Float.TYPE == fieldType || Float.class == fieldType) {
						val = Convert.toFloat(val);
					} else if (BigDecimal.class == fieldType) {
						val = Convert.toBigDecimal(val);
					} else if (Date.class == fieldType) {
						if (val instanceof String) {
							val = DateUtil.parseDate((String) val);
						} else if (val instanceof Double) {
							val = org.apache.poi.ss.usermodel.DateUtil.getJavaDate((Double) val);
						}
					} else if (Boolean.TYPE == fieldType || Boolean.class == fieldType) {
						val = Convert.toBool(val, false);
					}
					Excel attr = field.getAnnotation(Excel.class);
					String propertyName = field.getName();
					if (StrUtil.isNotEmpty(attr.targetAttr())) {
						propertyName = field.getName() + "." + attr.targetAttr();
					} else if (StrUtil.isNotEmpty(attr.readConverterExp())) {
						val = reverseByExp(Convert.toStr(val), attr.readConverterExp(), attr.separator());
					} else if (ColumnType.IMAGE == attr.cellType() && ObjectUtil.isNotEmpty(pictures)) {
						PictureData image = pictures.get(row.getRowNum() + "_" + entry.getKey());
						byte[] data = image.getData();
						val = Base64.encode(data);
					}
					ReflectUtil.invokeSetter(entity, propertyName, val);
				}
				list.add(entity);
			}
		}
		return list;
	}

	/**
	 * 对list数据源将其里面的数据导入到excel表单
	 *
	 * @param list      导出数据集合
	 * @param sheetName 工作表的名称
	 * @return 结果
	 */
	public Workbook exportExcel(List<T> list, String sheetName) {
		this.init(list, sheetName, Type.EXPORT);
		return exportExcel();
	}

	public void exportExcel(HttpServletResponse response) {
		ServletOutputStream out = null;
		try {
			writeSheet();
			wb.write(response.getOutputStream());
		} catch (Exception e) {
			log.error("导出Excel异常{}", e.getMessage());
		} finally {
			IoUtil.close(wb);
		}
	}

	public void exportExcel(HttpServletResponse response, List<T> list, String sheetName) {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Disposition", StrUtil.format("attachment;filename={}",
				URLUtil.encode(sheetName + ".xlsx", CharsetUtil.CHARSET_UTF_8)));
		this.init(list, sheetName, Type.EXPORT);
		exportExcel(response);
	}

	/**
	 * 对list数据源将其里面的数据导入到excel表单
	 *
	 * @param sheetName 工作表的名称
	 * @return 结果
	 */
	public Workbook importTemplateExcel(String sheetName) {
		this.init(null, sheetName, Type.IMPORT);
		return exportExcel();
	}

	/**
	 * 对list数据源将其里面的数据导入到excel表单
	 *
	 * @param sheetName 工作表的名称
	 * @return 结果
	 */
	public void importTemplateExcel(HttpServletResponse response, String sheetName) {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Disposition", StrUtil.format("attachment;filename={}",
				URLUtil.encode(sheetName + ".xlsx", CharsetUtil.CHARSET_UTF_8)));
		this.init(null, sheetName, Type.IMPORT);
		exportExcel(response);
	}

	public static InputStream workbookToStream(Workbook workbook) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			//创建临时文件
			workbook.write(out);
			byte[] bookByteAry = out.toByteArray();
			return new ByteArrayInputStream(bookByteAry);
		}
	}

	/**
	 * 对list数据源将其里面的数据导入到excel表单
	 *
	 * @return 结果
	 */
	public void exportExcel(OutputStream out) {
		try {
			writeSheet();
			wb.write(out);
		} catch (Exception e) {
			log.error("导出Excel异常{}", e.getMessage());
		} finally {
			IOUtils.closeQuietly(wb);
		}
	}

	/**
	 * 对list数据源将其里面的数据导入到excel表单
	 *
	 * @return 结果
	 */
	public Workbook exportExcel() {
		try {
			writeSheet();
			return wb;
		} catch (Exception e) {
			log.error("导出Excel异常{}", e.getMessage());
			throw new UtilException("导出Excel失败，请联系网站管理员！");
		}
	}

	/**
	 * 创建写入数据到Sheet
	 */
	public void writeSheet() {

		// 取出一共有多少个sheet.
		int sheetNo = (int)Math.ceil(list.size() / sheetSize);
		for (int index = 0; index < sheetNo; index++) {
			createSheet(sheetNo, index);

			// 产生一行
			Row row = sheet.createRow(0);
			int column = 0;
			// 写入各个字段的列头名称
			for (Object[] os : fields) {
				Excel excel = (Excel) os[1];
				this.createCell(excel, row, column++);
			}
			if (Type.EXPORT.equals(type)) {
				fillExcelData(index, row);
				addStatisticsRow();
			}
		}
	}

	/**
	 * 填充excel数据
	 *
	 * @param index 序号
	 * @param row   单元格行
	 */
	public void fillExcelData(int index, Row row) {
		int startNo = index * (int) sheetSize;
		int endNo = Math.min(startNo + (int) sheetSize, list.size());
		for (int i = startNo; i < endNo; i++) {
			row = sheet.createRow(i + 1 - startNo);
			// 得到导出对象.
			T vo = (T) list.get(i);
			int column = 0;
			for (Object[] os : fields) {
				Field field = (Field) os[0];
				Excel excel = (Excel) os[1];
				// 设置实体类私有属性可访问
				field.setAccessible(true);
				this.addCell(excel, row, vo, field, column++);
			}
		}
	}

	/**
	 * 创建表格样式
	 *
	 * @param wb 工作薄对象
	 * @return 样式列表
	 */
	private Map<String, CellStyle> createStyles(Workbook wb) {
		// 写入各条记录,每条记录对应excel表中的一行
		Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
		CellStyle style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		Font dataFont = wb.createFont();
		dataFont.setFontName("Arial");
		dataFont.setFontHeightInPoints((short) 10);
		style.setFont(dataFont);
		styles.put("data", style);

		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		Font headerFont = wb.createFont();
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setBold(true);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		style.setFont(headerFont);
		styles.put("header", style);

		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		Font totalFont = wb.createFont();
		totalFont.setFontName("Arial");
		totalFont.setFontHeightInPoints((short) 10);
		style.setFont(totalFont);
		styles.put("total", style);

		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(HorizontalAlignment.LEFT);
		styles.put("data1", style);

		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(HorizontalAlignment.CENTER);
		styles.put("data2", style);

		style = wb.createCellStyle();
		style.cloneStyleFrom(styles.get("data"));
		style.setAlignment(HorizontalAlignment.RIGHT);
		styles.put("data3", style);

		return styles;
	}

	/**
	 * 创建单元格
	 */
	public Cell createCell(Excel attr, Row row, int column) {
		// 创建列
		Cell cell = row.createCell(column);
		// 写入列信息
		cell.setCellValue(attr.name());
		setDataValidation(attr, row, column);
		cell.setCellStyle(styles.get("header"));
		return cell;
	}

	/**
	 * 设置单元格信息
	 *
	 * @param value 单元格值
	 * @param attr  注解相关
	 * @param cell  单元格信息
	 */
	public void setCellVo(Object value, Excel attr, Cell cell) {
		if (ColumnType.STRING == attr.cellType()) {
			cell.setCellValue(Objects.isNull(value) ? attr.defaultValue() : value + attr.suffix());
		} else if (ColumnType.NUMERIC == attr.cellType()) {
			if (Objects.nonNull(value)) {
				cell.setCellValue(StrUtil.contains(Convert.toStr(value), ".") ? Convert.toDouble(value) : Convert.toInt(value));
			}
		} else if (ColumnType.IMAGE == attr.cellType()) {
			ClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, (short) cell.getColumnIndex(), cell.getRow().getRowNum(), (short) (cell.getColumnIndex() + 1), cell.getRow().getRowNum() + 1);
			String imageBase64 = Convert.toStr(value);
			if (StrUtil.isNotEmpty(imageBase64)) {
				byte[] data = Base64.decode(imageBase64);
				getDrawingPatriarch(cell.getSheet()).createPicture(anchor,
						cell.getSheet().getWorkbook().addPicture(data, getImageType(data)));
			}
		}
	}

	/**
	 * 获取画布
	 */
	public static Drawing<?> getDrawingPatriarch(Sheet sheet) {
		if (sheet.getDrawingPatriarch() == null) {
			sheet.createDrawingPatriarch();
		}
		return sheet.getDrawingPatriarch();
	}

	/**
	 * 获取图片类型,设置图片插入类型
	 */
	public int getImageType(byte[] value) {
		String type = getFileExtendName(value);
		if ("JPG".equalsIgnoreCase(type)) {
			return Workbook.PICTURE_TYPE_JPEG;
		} else if ("PNG".equalsIgnoreCase(type)) {
			return Workbook.PICTURE_TYPE_PNG;
		}
		return Workbook.PICTURE_TYPE_JPEG;
	}

	/**
	 * 获取文件类型
	 *
	 * @param photoByte 文件字节码
	 * @return 后缀（不含".")
	 */
	public static String getFileExtendName(byte[] photoByte) {
		String strFileExtendName = "JPG";
		if ((photoByte[0] == 71) && (photoByte[1] == 73) && (photoByte[2] == 70) && (photoByte[3] == 56)
				&& ((photoByte[4] == 55) || (photoByte[4] == 57)) && (photoByte[5] == 97)) {
			strFileExtendName = "GIF";
		} else if ((photoByte[6] == 74) && (photoByte[7] == 70) && (photoByte[8] == 73) && (photoByte[9] == 70)) {
			strFileExtendName = "JPG";
		} else if ((photoByte[0] == 66) && (photoByte[1] == 77)) {
			strFileExtendName = "BMP";
		} else if ((photoByte[1] == 80) && (photoByte[2] == 78) && (photoByte[3] == 71)) {
			strFileExtendName = "PNG";
		}
		return strFileExtendName;
	}

	/**
	 * 创建表格样式
	 */
	public void setDataValidation(Excel attr, Row row, int column) {
		if (attr.name().indexOf("注：") >= 0) {
			sheet.setColumnWidth(column, 6000);
		} else {
			// 设置列宽
			sheet.setColumnWidth(column, (int) ((attr.width() + 0.72) * 256));
		}
		// 如果设置了提示信息则鼠标放上去提示.
		if (StrUtil.isNotEmpty(attr.prompt())) {
			// 这里默认设了2-101列提示.
			setXSSFPrompt(sheet, "", attr.prompt(), 1, 100, column, column);
		}
		// 如果设置了combo属性则本列只能选择不能输入
		if (attr.combo().length > 0) {
			// 这里默认设了2-101列只能选择不能输入.
			setXSSFValidation(sheet, attr.combo(), 1, 100, column, column);
		}
	}

	/**
	 * 添加单元格
	 */
	public Cell addCell(Excel attr, Row row, T vo, Field field, int column) {
		Cell cell = null;
		try {
			// 设置行高
			row.setHeight(maxHeight);
			// 根据Excel中设置情况决定是否导出,有些情况需要保持为空,希望用户填写这一列.
			if (attr.isExport()) {
				// 创建cell
				cell = row.createCell(column);
				int align = attr.align().value();
				cell.setCellStyle(styles.get("data" + (align >= 1 && align <= 3 ? align : "")));

				// 用于读取对象中的属性
				Object value = getTargetValue(vo, field, attr);
				String dateFormat = attr.dateFormat();
				String readConverterExp = attr.readConverterExp();
				String separator = attr.separator();
				if (StrUtil.isNotEmpty(dateFormat) && Objects.nonNull(value)) {
					cell.setCellValue(DateUtil.format((Date) value, dateFormat));
				} else if (StrUtil.isNotEmpty(readConverterExp) && Objects.nonNull(value)) {
					cell.setCellValue(convertByExp(Convert.toStr(value), readConverterExp, separator));
				} else if (value instanceof BigDecimal && -1 != attr.scale()) {
					cell.setCellValue((((BigDecimal) value).setScale(attr.scale(), attr.roundingMode())).toString());
				} else {
					// 设置列类型
					setCellVo(value, attr, cell);
				}
				addStatisticsData(column, Convert.toStr(value), attr);
			}
		} catch (Exception e) {
			log.error("导出Excel失败{}", e);
		}
		return cell;
	}

	/**
	 * 设置 POI XSSFSheet 单元格提示
	 *
	 * @param sheet         表单
	 * @param promptTitle   提示标题
	 * @param promptContent 提示内容
	 * @param firstRow      开始行
	 * @param endRow        结束行
	 * @param firstCol      开始列
	 * @param endCol        结束列
	 */
	public void setXSSFPrompt(Sheet sheet, String promptTitle, String promptContent, int firstRow, int endRow,
	                          int firstCol, int endCol) {
		DataValidationHelper helper = sheet.getDataValidationHelper();
		DataValidationConstraint constraint = helper.createCustomConstraint("DD1");
		CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
		DataValidation dataValidation = helper.createValidation(constraint, regions);
		dataValidation.createPromptBox(promptTitle, promptContent);
		dataValidation.setShowPromptBox(true);
		sheet.addValidationData(dataValidation);
	}

	/**
	 * 设置某些列的值只能输入预制的数据,显示下拉框.
	 *
	 * @param sheet    要设置的sheet.
	 * @param textlist 下拉框显示的内容
	 * @param firstRow 开始行
	 * @param endRow   结束行
	 * @param firstCol 开始列
	 * @param endCol   结束列
	 * @return 设置好的sheet.
	 */
	public void setXSSFValidation(Sheet sheet, String[] textlist, int firstRow, int endRow, int firstCol, int endCol) {
		DataValidationHelper helper = sheet.getDataValidationHelper();
		// 加载下拉列表内容
		DataValidationConstraint constraint = helper.createExplicitListConstraint(textlist);
		// 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
		CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
		// 数据有效性对象
		DataValidation dataValidation = helper.createValidation(constraint, regions);
		// 处理Excel兼容性问题
		if (dataValidation instanceof XSSFDataValidation) {
			dataValidation.setSuppressDropDownArrow(true);
			dataValidation.setShowErrorBox(true);
		} else {
			dataValidation.setSuppressDropDownArrow(false);
		}

		sheet.addValidationData(dataValidation);
	}

	/**
	 * 解析导出值 0=男,1=女,2=未知
	 *
	 * @param propertyValue 参数值
	 * @param converterExp  翻译注解
	 * @param separator     分隔符
	 * @return 解析后值
	 */
	public static String convertByExp(String propertyValue, String converterExp, String separator) {
		StringBuilder propertyString = new StringBuilder();
		String[] convertSource = converterExp.split(",");
		for (String item : convertSource) {
			String[] itemArray = item.split("=");
			if (StrUtil.containsAny(separator, propertyValue)) {
				for (String value : propertyValue.split(separator)) {
					if (itemArray[0].equals(value)) {
						propertyString.append(itemArray[1] + separator);
						break;
					}
				}
			} else {
				if (itemArray[0].equals(propertyValue)) {
					return itemArray[1];
				}
			}
		}
		return StrUtil.strip(propertyString.toString(), null, separator);
	}

	/**
	 * 反向解析值 男=0,女=1,未知=2
	 *
	 * @param propertyValue 参数值
	 * @param converterExp  翻译注解
	 * @param separator     分隔符
	 * @return 解析后值
	 */
	public static String reverseByExp(String propertyValue, String converterExp, String separator) {
		StringBuilder propertyString = new StringBuilder();
		String[] convertSource = converterExp.split(",");
		for (String item : convertSource) {
			String[] itemArray = item.split("=");
			if (StrUtil.containsAny(separator, propertyValue)) {
				for (String value : propertyValue.split(separator)) {
					if (itemArray[1].equals(value)) {
						propertyString.append(itemArray[0]).append(separator);
						break;
					}
				}
			} else {
				if (itemArray[1].equals(propertyValue)) {
					return itemArray[0];
				}
			}
		}
		return StrUtil.strip(propertyString.toString(), null, separator);
	}

	/**
	 * 合计统计信息
	 */
	private void addStatisticsData(Integer index, String text, Excel entity) {
		if (entity != null && entity.isStatistics()) {
			Double temp = 0D;
			if (!statistics.containsKey(index)) {
				statistics.put(index, temp);
			}
			try {
				temp = Double.valueOf(text);
			} catch (NumberFormatException e) {
			}
			statistics.put(index, statistics.get(index) + temp);
		}
	}

	/**
	 * 创建统计行
	 */
	public void addStatisticsRow() {
		if (statistics.size() > 0) {
			Row row = sheet.createRow(sheet.getLastRowNum() + 1);
			Set<Integer> keys = statistics.keySet();
			Cell cell = row.createCell(0);
			cell.setCellStyle(styles.get("total"));
			cell.setCellValue("合计");

			for (Integer key : keys) {
				cell = row.createCell(key);
				cell.setCellStyle(styles.get("total"));
				cell.setCellValue(DOUBLE_FORMAT.format(statistics.get(key)));
			}
			statistics.clear();
		}
	}

	/**
	 * 编码文件名
	 */
	public String encodingFilename(String filename) {
		filename = UUID.randomUUID().toString() + "_" + filename + ".xlsx";
		return filename;
	}

	/**
	 * 获取bean中的属性值
	 *
	 * @param vo    实体对象
	 * @param field 字段
	 * @param excel 注解
	 * @return 最终的属性值
	 */
	private Object getTargetValue(T vo, Field field, Excel excel) throws Exception {
		Object o = field.get(vo);
		if (StrUtil.isNotEmpty(excel.targetAttr())) {
			String target = excel.targetAttr();
			if (target.contains(".")) {
				String[] targets = target.split("[.]");
				for (String name : targets) {
					o = getValue(o, name);
				}
			} else {
				o = getValue(o, target);
			}
		}
		return o;
	}

	/**
	 * 以类的属性的get方法方法形式获取值
	 *
	 * @param o
	 * @param name
	 * @return value
	 */
	private Object getValue(Object o, String name) throws Exception {
		if (Objects.nonNull(o) && StrUtil.isNotEmpty(name)) {
			Class<?> clazz = o.getClass();
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			o = field.get(o);
		}
		return o;
	}

	/**
	 * 得到所有定义字段
	 */
	private void createExcelField() {
		this.fields = new ArrayList<Object[]>();
		List<Field> tempFields = new ArrayList<>();
		tempFields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
		tempFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		for (Field field : tempFields) {
			// 单注解
			if (field.isAnnotationPresent(Excel.class)) {
				putToField(field, field.getAnnotation(Excel.class));
			}

			// 多注解
			if (field.isAnnotationPresent(Excels.class)) {
				Excels attrs = field.getAnnotation(Excels.class);
				Excel[] excels = attrs.value();
				for (Excel excel : excels) {
					putToField(field, excel);
				}
			}
		}
		this.fields = this.fields.stream().sorted(Comparator.comparing(objects -> ((Excel) objects[1]).sort())).collect(Collectors.toList());
		this.maxHeight = getRowHeight();
	}

	/**
	 * 根据注解获取最大行高
	 */
	public short getRowHeight() {
		double maxHeight = 0;
		for (Object[] os : this.fields) {
			Excel excel = (Excel) os[1];
			maxHeight = Math.max(maxHeight, excel.height());
		}
		return (short) (maxHeight * 20);
	}

	/**
	 * 放到字段集合中
	 */
	private void putToField(Field field, Excel attr) {
		if (attr != null && (attr.type() == Type.ALL || attr.type() == type)) {
			this.fields.add(new Object[]{field, attr});
		}
	}

	/**
	 * 创建一个工作簿
	 */
	public void createWorkbook() {
		this.wb = new SXSSFWorkbook(500);
	}

	/**
	 * 创建工作表
	 *
	 * @param sheetNo sheet数量
	 * @param index   序号
	 */
	public void createSheet(int sheetNo, int index) {
		this.sheet = wb.createSheet();
		this.styles = createStyles(wb);
		// 设置工作表的名称.
		if (sheetNo == 0) {
			wb.setSheetName(index, sheetName);
		} else {
			wb.setSheetName(index, sheetName + index);
		}
	}

	/**
	 * 获取单元格值
	 *
	 * @param row    获取的行
	 * @param column 获取单元格列号
	 * @return 单元格值
	 */
	public Object getCellValue(Row row, int column) {
		if (row == null) {
			return row;
		}
		Object val = "";
		try {
			Cell cell = row.getCell(column);
			if (Objects.nonNull(cell)) {
				if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
					val = cell.getNumericCellValue();
					if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
						val = org.apache.poi.ss.usermodel.DateUtil.getJavaDate((Double) val); // POI Excel 日期格式转换
					} else {
						if ((Double) val % 1 != 0) {
							val = new BigDecimal(val.toString());
						} else {
							val = new DecimalFormat("0").format(val);
						}
					}
				} else if (cell.getCellType() == CellType.STRING) {
					val = cell.getStringCellValue();
				} else if (cell.getCellType() == CellType.BOOLEAN) {
					val = cell.getBooleanCellValue();
				} else if (cell.getCellType() == CellType.ERROR) {
					val = cell.getErrorCellValue();
				}

			}
		} catch (Exception e) {
			return val;
		}
		return val;
	}

	/**
	 * 判断是否是空行
	 *
	 * @param row 判断的行
	 */
	private boolean isRowEmpty(Row row) {
		if (row == null) {
			return true;
		}
		for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
			Cell cell = row.getCell(i);
			if (cell != null && cell.getCellType() != CellType.BLANK) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取Excel2003图片
	 *
	 * @param sheet    当前sheet对象
	 * @param workbook 工作簿对象
	 * @return Map key:图片单元格索引（1_1）String，value:图片流PictureData
	 */
	public static Map<String, PictureData> getSheetPictures03(HSSFSheet sheet, HSSFWorkbook workbook) {
		Map<String, PictureData> sheetIndexPicMap = new HashMap<>();
		List<HSSFPictureData> pictures = workbook.getAllPictures();
		if (!pictures.isEmpty()) {
			for (HSSFShape shape : sheet.getDrawingPatriarch().getChildren()) {
				HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
				if (shape instanceof HSSFPicture) {
					HSSFPicture pic = (HSSFPicture) shape;
					int pictureIndex = pic.getPictureIndex() - 1;
					HSSFPictureData picData = pictures.get(pictureIndex);
					String picIndex = anchor.getRow1() + "_" + String.valueOf(anchor.getCol1());
					sheetIndexPicMap.put(picIndex, picData);
				}
			}
			return sheetIndexPicMap;
		} else {
			return sheetIndexPicMap;
		}
	}

	/**
	 * 获取Excel2007图片
	 *
	 * @param sheet    当前sheet对象
	 * @param workbook 工作簿对象
	 * @return Map key:图片单元格索引（1_1）String，value:图片流PictureData
	 */
	public static Map<String, PictureData> getSheetPictures07(XSSFSheet sheet, XSSFWorkbook workbook) {
		Map<String, PictureData> sheetIndexPicMap = new HashMap<>();
		for (POIXMLDocumentPart dr : sheet.getRelations()) {
			if (dr instanceof XSSFDrawing) {
				XSSFDrawing drawing = (XSSFDrawing) dr;
				List<XSSFShape> shapes = drawing.getShapes();
				for (XSSFShape shape : shapes) {
					if (shape instanceof XSSFPicture) {
						XSSFPicture pic = (XSSFPicture) shape;
						XSSFClientAnchor anchor = pic.getPreferredSize();
						CTMarker ctMarker = anchor.getFrom();
						String picIndex = ctMarker.getRow() + "_" + ctMarker.getCol();
						sheetIndexPicMap.put(picIndex, pic.getPictureData());
					}
				}
			}
		}
		return sheetIndexPicMap;
	}
}
