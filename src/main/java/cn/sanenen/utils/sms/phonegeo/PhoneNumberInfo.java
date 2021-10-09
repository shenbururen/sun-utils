package cn.sanenen.utils.sms.phonegeo;

import lombok.Data;

/**
 * 归属地信息
 */
@Data
public class PhoneNumberInfo {
  private String phoneNumber;
  /**
   * 省
   */
  private String province;
  /**
   * 市
   */
  private String city;
  /**
   * 城市编码，身份证前几位
   */
  private String cityCode;
  /**
   * 邮政编码 
   */
  private String zipCode;
  /**
   * 地区编码 
   */
  private String areaCode;
  /**
   * "移动", "联通", "电信", "电信虚拟运营商", "联通虚拟运营商", "移动虚拟运营商"
   */
  private String phoneType;
}
