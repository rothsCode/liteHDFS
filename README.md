# liteHDFS
common:封装 文件目录树  netty 
client:文件操作客户端
dataNode:文件存储节点多副本
nameNode:文件目录树存储节点以及文件注册心跳监控调度中心

TODO
1:dataNode的注册以及心跳过程
2：文件目录树构造以及netty通信封装
3：client 文件操作方法构造以及启动
4:用户权限以及容量配额体系
5：nameNode集群分片模式
6:backup主备切换 -- 拉取editLog日志， 定时合成image
