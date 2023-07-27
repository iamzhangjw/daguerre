## Daguerre：集成文件存储服务

本项目整合了市面上主流的 OSS 服务。基于最新的 SpringBoot 框架实现，持久层中间件使用了 MyBatis-Plus，架构简单，代码简洁，基本涵盖了常用使用场景。

### 主要特性

- 集成了市面上主要的 OSS 服务，也可使用本地存储；
- 支持小文件上传和大文件异步分片上传；
- 文件分类存放，可指定文件的有效期；
- 可整个文件或部分下载，也可以获取访问地址。

### 实现原理

目前集成了阿里云、腾讯云、华为云三家厂商的 OSS 服务，另外可通过部署 [MinIO](https://min.io/) 这款开源 OSS 项目实现本地化存储；提供统一 API 调用，封装差异化细节，系统统一分配访问密钥，让业务接入简单。

利用 OSS 服务的预签名 URL 功能生成文件访问地址，可设置 URL 有效时长，减小 URL 泄露造成的危害，同时也能避免产生二次流量消耗（下载无法避免）。唯一的缺点就是 URL 相对来说变长了。

为保证系统安全性，接入要分配 access key/secret 对，系统会存储 OSS 访问信息，发起 API 调用时追加公共 HTTP URL 参数，并结合 key/secret 对和请求参数生成签名，来保证访问的真实可靠。

### 调用规则

使用 HTTP 协议发起请求时，请求 URL 中必须携带 4 个公共参数。

##### 公共参数

| 参数名       | 类型     | 说明                             |
|:----------|:-------|:-------------------------------|
| accessKey | string | 申请的 accessKey                  |
| nonce     | string | 随机字符串，长度没有限制                   |
| timestamp | long   | UNIX 时间戳，长度为 10，和当前时间不能超过 10分钟 |
| sign      | string | 签名                             |


##### 生成签名

取 URL 中参数的 key 和 value 并用 & 按照 key 字母升序连接起来，最后在字符串前加上 #{keySecret}&，使用摘要算法得到签名。注意 value 为空的参数需要过滤掉，body 不参与签名。

举例如下，现有原始 URL 参数如
```
fileType=exe&expireDays=2&accessKey=bzThOgtPBJ4cd5&timestamp=1649302353&nonce=LaR8m
```

- 首先根据 key 做升序排列为
```
accessKey=bzThOgtPBJ4cd5&expireDays=2&fileType=exe&nonce=LaR8m&timestamp=1649302353
```

- 假设 keySecret = v659EXaqAxSpmEwDUBSveESbun6Uz6CL，则最终签名前的原始字符串 source 为
```
v659EXaqAxSpmEwDUBSveESbun6Uz6CL&accessKey=bzThOgtPBJ4cd5&expireDays=2&fileType=exe&nonce=LaR8m&timestamp=1649302353
```

- 最后得到签名 sign = sha1(source)，本例的签名结果是
  `202d0a70c07cd54955267bac333d5d1f`

- 最终的请求 URL 为
```html
http://127.0.0.1:8890/daguerre/oss/u?fileType=exe&expireDays=2&accessKey=bzThOgtPBJ4cd5&timestamp=1649302353&nonce=LaR8m&sign=202d0a70c07cd54955267bac333d5d1f
```

> 摘要算法支持 MD5、SHA1 等，如上例中使用了 SHA1。
