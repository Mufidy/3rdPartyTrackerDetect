# 3rdPartyTrackerDetect
## WEB第三方流量测量和追踪者识别方法研究

### 本科毕业设计课题
##### 毕设要求：
* 开发一个运行在客户端的web第三方流量测量器，可以记录HTTP请求的相关头部信息
* 对国内著名的至少100个网站和国外的100个网站进行测量，将第三方流量和第一方流量分类，同时对第三方流量类型进行分类
* 对测量的第三方数据进行分类，识别出具有追踪行为的第三方
* 将测量结果和现有相关方法进行对比分析

##### Reference
* 软件中使用了[FourthParty](http://fourthparty.info/)记录HTTP请求和Cookie信息
* 软件使用[Weka](http://www.cs.waikato.ac.nz/ml/weka/)机器学习技术
* 软件选用支持向量机[SVM](https://www.csie.ntu.edu.tw/~cjlin/libsvm/)作为分类器算法
