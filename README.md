# Whale-ApiUse

## 说明

Whale-ApiUse是burp插件，可以用来测试wx公众号，微信小程序，钉钉，企业微信等的接口，通过输入key和secret进行测试，之所以做这个首先这是一个练手项目，后续也会继续维护更新，如果有好的建议欢迎留言。其次由于现有的工具都要额外下载，用起来繁琐，所以我想着通过一个插的形式进行开发，方便使用。

## 使用说明

使用，选择相对应的应用，然后输出key和secret，点击获取accesstoken。

![image-20240106212632494](img/README/image-20240106212632494.png)

如果key和secret有无则报错。

![image-20240106212654363](img/README/image-20240106212654363.png)

然后获取到的accesstoken可以直接选择后续的模块进行利用，本插件是直接获取输出框的值然后进行访问的。

小程序调用的官方网址：https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/operation/getDomainInfo.html

微信小程序目前提供的接口仅用于验证，剩下的操作可以查看上述网址进行操作

企业微信官方网址：https://developer.work.weixin.qq.com/resource/devtool

钉钉官方网址：https://open.dingtalk.com/document/orgapp/api-overview

案例链接：

https://xz.aliyun.com/t/11092#toc-0

## 版本更新

### 1.1

1.1版本首先完善了1.0未完成的操作，然后补充了钉钉进去，还补充了发包的操作。飞书还待完善，但是其他基本可以使用了

## 后续开发

目前是1.0，但是其实还有一些没完成，后续开发首先要补全当前的功能，然后再补充其他应用的key和secret的使用。
