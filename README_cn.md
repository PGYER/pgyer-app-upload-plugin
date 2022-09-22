
### 简单介绍

蒲公英平台可以让开发者和企业将应用上传到网站，生成安装链接和二维码用户在手机上打开安装链接，或扫码二维码，即可开始安装！

因此，这款pgyer-app-upload的Jenkins插件可以让开发者将apk/ipa文件上传到蒲公英平台！**并且这款插件可以将蒲公英平台返回的应用信息解析后注入到Jenkins的全局变量中，这样你就可以很方便的在其他构建步骤中使用这些返回的信息**。

### 使用指南
你可以在Jenkins的job配置页面的`构建后操作`操作中点击添加构建步骤选择`pgyer app upload`。然后你就可以看到类似下面图片的操作界面：

![](./images/setting-screenshot.png)

### 参数介绍
需要填写的字段|字段的解释
----:|:----------
api_key|(必填) API Key，用来识别API调用者的身份，<br/>如不特别说明，每个接口中都需要含有此参数。<br/>对于同一个蒲公英的注册用户来说，这个值在固定的。<br/>[点击获取_api_key](https://www.pgyer.com/account/api)
scandir|`(必填)` 需要上传的apk/ipa文件所在的文件夹或者父文件夹，<br/>当前默认路径是`${WORKSPACE}`，它代表了当前项目的绝对路径。<br/>这个功能的实现使用了ant框架的DirectoryScanner类，[点击查看DirectoryScanner类](https://ant.apache.org/manual/api/org/apache/tools/ant/DirectoryScanner.html)，<br/>这个字段就是DirectoryScanner类中的basedir方法的参数[点击查看basedir方法](https://ant.apache.org/manual/api/org/apache/tools/ant/DirectoryScanner.html#basedir)
file wildcard|`(必填)` 需要上传的apk/ipa文件的名字，支持通配符，<br/>就像这样: \*\*/\*.apk<br/>或者像这样： \*\*/Test?/\*_sign.apk，<br/>这个功能的实现使用了ant框架的DirectoryScanner类，[点击查看DirectoryScanner类](https://ant.apache.org/manual/api/org/apache/tools/ant/DirectoryScanner.html)，<br/>这个字段就是DirectoryScanner类中的includes方法的参数，[点击查看includes方法](https://ant.apache.org/manual/api/org/apache/tools/ant/DirectoryScanner.html#includes)
installType|`(选填)` 应用安装方式，值为(1,2,3)。<br/>1：公开，2：密码安装，3：邀请安装。<br/>默认为1公开
password|`(选填)` 设置App安装密码，如果不想设置密码，请传空字符串，或不传。
updateDescription|`(选填)` 版本更新描述，请传空字符串，或不传。
channelShortcut|`(选填)` 所需更新的指定渠道的下载短链接，只可指定一个渠道，字符串型，如：abcd
qrcodePath|`(选填)` 如果你需要下载蒲公英返回的二维码，那么这里填写二维码的存储路径，<br/>如果你不需要下载，那么你不需要在这里填写任何内容。
envVarsPath |`(选填)` 如果你想存储蒲公英返回的上传信息，那么这里填写保存信息的文件路径，<br/>如果你不需要保存，那么你不需要在这里填写任何内容。

### 运行截图
![](./images/pgyer-app-upload-running-log.png)

当你的应用上传成功后，在Jenkins中你就能看到上面图片中的信息。同时，你就可以在其他构建步骤中使用蒲公英返回来的信息，返回数据如图：

![](./images/pgyer-app-upload-backdata.png)

### Change Log
版本 1.0(2020-08-15)
-新上传APK/IPA文件到Pgyer插件
