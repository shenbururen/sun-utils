# sun-utils
一些工具类。
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