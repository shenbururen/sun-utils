# sun-utils
一些工具类。具体可看源代码及测试用例，文档可能没那么及时。
### 使用
```xml
<dependency>
    <groupId>cn.sanenen</groupId>
    <artifactId>sun-utils</artifactId>
    <version>3.1.5</version>
</dependency>
```
### 更新日志

3.1.6:
- ExcelHandler 头遍历修复

3.1.5:
- 修改SMQ的注释，修改idea警告。
- SMQ内存队列添加开关。
- 添加OtherUtil类，比例计算方法。
- hutool-all升级到 5.8.33。

3.1.4:
- hutool-all升级到 5.8.32。
- 添加ByteArrayMultipartFile,MultipartFile的内存实现。
- spring-context和spring-web降低版本至5.3.39以支持java8。
- poi-ooxml升级到5.3.0。
- lombok升级到1.18.34。


3.1.3:
- spring-context升级到5.3.32
- poi-ooxml升级到5.2.5
- hutool-all升级到 5.8.27
- lombok升级到1.18.32
- 修正excel导出生成两个sheet的问题。

3.1.2:
- jedisUtil封装一个setnxex方法
- dateUtil 添加获得当前时间到当天结束的剩余秒数
- okhttp 4.9.3 升级到4.12.0
- jedis 4.4.3升级到4.4.6
- hutool-all 5.8.22升级到5.8.25

3.1.1:
- 升级lombok版本到1.18.30
- 添加Automatic-Module 自动模块名称

3.1.0:
- 不可描述的更新

3.0.0:
- 此版本仅仅是更改了包名，升级了一些依赖避免漏洞，无功能新增
- 包名增加sunutils，主要是与我其他项目有冲突，无奈更改包名
- mysql-connector-java升级至8.0.33
- spring-context升级至5.3.30
- lombok升级至1.18.28
- hutool-all升级至5.8.22

2.3.2:
- 更新号码归属地测试数据 phone.dat 至202302
- RedisUtil添加hincrByFloat方法
- hutool版本升级至5.8.21
- jedis版本升级至4.4.3
- jackson版本升级至2.15.2
- RedisUtil添加hash结构,如果值存在则设置值，不存在则不设置的方法

2.3.1:
- OkhttpUtil添加连接池配置。
- RedisUtil lpush String类型判断改为hutool的isSimpleValueType
- RedisUtil hget添加转对象方法
- SMQ修正设置文件大小的问题
- SMQ memoryQueueSize 小于0时禁用内存队列

2.3.0:
- 新增一个LogUtil。日志打印时，值长度处理的工具方法。

2.2.0:
- redisUtil添加set集合获取所有。
- redisUtil添加批量添加hash类型值。
- redisUtil添加zset相关的一些基础操作(增、删、查)。使用zset结构可以实现hash结构小key过期时间功能。
- PhoneNumberGeo号码地区查找，文件读取方式修正，防止将资源文件打包到jar内时读取失败。

2.1.8:
- 升级hutool-5.8.0 版本至5.8.4。
- 升级lombok-1.18.20 版本至1.18.24。
- ExcelHandler添加servlet支持。
- ExcelHandler添加Workbook转InputStream的方法。

2.1.7:
- 升级hutool 版本至5.8.0 修复redis无法设置maxWaitMillis的问题。
- redisUtil增加hash结构并发或频次限制业务操作方法。

2.1.6:
- MsgIDUtil 增加生成四字节消息头id（循环使用）。
- redisUtil lpush 判断如果是字符串类型，则不再进行json转换。
- redisUtil 添加通道模式批量删除hash 小key方法。
- redisUtil 添加获取list结构所有值。

2.1.5:
- 扩展hutool 日志工具类，增加 判断是否在拦截时段内的方法。
- 升级mysql连接驱动至8.0.28。

2.1.4:
- 再次修正持久化队列，关闭应用时可能出现hs_err错误文件的问题。

2.1.3:
- 修正持久化队列，关闭应用时可能出现hs_err错误文件的问题。


```
├─src
├─main
├─java
  └─cn
      └─sanenen.sunutils
          │  SunSetting.java 工具类配置读取，如需要个性化配置，
            在项目根目录增加sun.setting，可配置项参考测试目录的sun.setting
          │  
          ├─thread
          │      AsyncBlockedThreadPoolExecutor.java spring"@async"阻塞式线程池实现
          │      BlockedThreadPoolExecutor.java 阻塞式线程池
          │      StandardThread.java 标准后台业务线程类
          │      
          └─utils
              ├─http
              │      HttpUtil.java httpclient封装
              │      OkHttpUtil.java okhttp封装
              │      
              ├─json
              │      JacksonUtil.java jackson封装
              │      
              ├─other
              │      IntervalSecondSpeeder.java 流速控制 滑动窗口算法实现
              │      
              ├─redis
              │      JedisUtil.java jiedis封装，增加keys scan非阻塞实现方式。
              │      
              └─sms 短信内容相关处理
                      SignUtil.java 签名处理工具类，前置、后置、提取、移除。
                      TemplateUtil.java 内容是否匹配模版判断。
                      

```
# 感谢 JetBrains 免费的开源授权

<a href="https://www.jetbrains.com/?from=sun-utils" target="_blank">
<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg" alt="JetBrains Logo (Main) logo.">
</a>