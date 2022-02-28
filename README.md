# sun-utils
一些工具类。具体可看源代码及测试用例，文档可能没那么及时。
### 使用
```xml
<dependency>
    <groupId>cn.sanenen</groupId>
    <artifactId>sun-utils</artifactId>
    <version>2.1.5</version>
</dependency>
```
### 更新日志
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
      └─sanenen
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