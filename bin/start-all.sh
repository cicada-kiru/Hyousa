#!/bin/bash
hyousa=${HYOUSA_HOME}
jar=${hyousa}/hyousa.jar
slaves=${hyousa}/conf/slaves
conf=${hyousa}/conf/hyousa.conf

echo starting master
ssh `cat ${hyousa}/conf/master` java -classpath $jar hyousa.dfs.server.master.HyousaMaster $conf &
usleep 1000000
echo starting slaves
for slave in `cat $slaves`
do
  echo starting slave on $slave
  ssh $slave java -classpath $jar hyousa.dfs.server.slave.HyousaSlave $conf &
done
