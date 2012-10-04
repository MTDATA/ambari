/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ambari.server.utils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.ambari.server.Role;
import org.apache.ambari.server.actionmanager.HostAction;
import org.apache.ambari.server.actionmanager.HostRoleCommand;
import org.apache.ambari.server.actionmanager.HostRoleStatus;
import org.apache.ambari.server.actionmanager.Stage;
import org.apache.ambari.server.agent.ExecutionCommand;
import org.apache.ambari.server.state.live.svccomphost.ServiceComponentHostInstallEvent;

public class StageUtils {
  public static String getActionId(long requestId, long stageId) {
    return requestId + "-" + stageId;
  }

  public static long[] getRequestStage(String actionId) {
    String [] fields = actionId.split("-");
    long[] requestStageIds = new long[2];
    requestStageIds[0] = Long.parseLong(fields[0]);
    requestStageIds[1] = Long.parseLong(fields[1]);
    return requestStageIds;
  }

  //For testing only
  public static Stage getATestStage(long requestId, long stageId) {
    InetSocketAddress sa = new InetSocketAddress(0);
    String hostname = sa.getAddress().getHostName();
    Stage s = new Stage(requestId, "/tmp", "cluster1");
    s.setStageId(stageId);
    HostAction ha = new HostAction(hostname);
    long now = System.currentTimeMillis();
    HostRoleCommand hrc = new HostRoleCommand(Role.NAMENODE, 
        new ServiceComponentHostInstallEvent("NAMENODE", hostname, now));
    hrc.setStatus(HostRoleStatus.PENDING);
    ha.addHostRoleCommand(hrc);
    ExecutionCommand execCmd = ha.getCommandToHost();
    execCmd.setCommandId(s.getActionId());
    execCmd.setClusterName("cluster1");
    Map<String, List<String>> clusterHostInfo = new TreeMap<String, List<String>>();
    List<String> slaveHostList = new ArrayList<String>();
    slaveHostList.add(hostname);
    slaveHostList.add("host2");
    clusterHostInfo.put("slave_hosts", slaveHostList);
    execCmd.setClusterHostInfo(clusterHostInfo);
    Map<String, String> hdfsSite = new TreeMap<String, String>();
    hdfsSite.put("dfs.block.size", "2560000000");
    Map<String, Map<String, String>> configurations =
        new TreeMap<String, Map<String, String>>();
    configurations.put("hdfs-site", hdfsSite);
    execCmd.setConfigurations(configurations);
    Map<String, String> params = new TreeMap<String, String>();
    params.put("jdklocation", "/x/y/z");
    execCmd.setParams(params);
    Map<String, String> roleParams = new TreeMap<String, String>();
    roleParams.put("format", "false");
    execCmd.addRoleCommand("NAMENODE", "INSTALL", roleParams);
    s.addHostAction(hostname, ha);
    return s;
  }
}