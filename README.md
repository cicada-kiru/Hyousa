# Hyousa
A toy distributed file system
# 快速开始
部署方法:

配置SSH免密登录

配置集群的hosts IP映射

将从节点IP或映射名按行添加到conf下的slaves文件

将主节点IP或映射名添加到conf下的master文件

修改conf下的hyousa.conf的前三行,保证两个文件夹存在并且master的host配置正确

在主节点上配置环境变量HYOUSA_HOME

将hyousa目录复制到集群所有节点（注意文件夹位置要和主节点相同）

执行start_all.sh脚本

执行hyousa查看Usage

执行stop_all.sh关闭所有节点
