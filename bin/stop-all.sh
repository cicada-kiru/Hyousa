#!/bin/bash
hyousa=${HYOUSA_HOME}
jar=${hyousa}/hyousa.jar
slaves=${hyousa}/conf/slaves
conf=${hyousa}/conf/hyousa.conf

master=`cat ${hyousa}/conf/master`
echo stoping master
ssh $master ${hyousa}/bin/hyousa dump
ssh $master kill -9 `ssh $master jps | awk '$2=="HyousaMaster"{print $1}'`
echo stoping slaves
for slave in `cat $slaves`
do
  echo stoping slave on $slave
  ssh $slave kill -9 `ssh $slave jps | awk '$2=="HyousaSlave"{print $1}'`
done
