### 说明之前需要最好安装一下android studio的Markdown和protobuffer插件

注意：执行该脚本需要安装python3+环境

使用说明

apk.py 是一段python脚本，输入一个apk文件，一个渠道包列表，输出不同渠道包的apk文件

channel.txt 是一个渠道包列表文件，当我们需要新的渠道时，可添加一行新的渠道名称

empty.txt 是一个默认的文件，主要用于向apk的MATE-INFO添加不同渠道名称命名的空文件来区分不同的渠道

参考：
http://tech.meituan.com/mt-apk-packaging.html
https://github.com/yipianfengye/buildapk