/*
 * SonarQube
 * Copyright (C) 2009-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.process.cluster.hz;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.sonar.process.ProcessId;
import org.sonar.process.cluster.hz.HazelcastMember.Attribute;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.sonar.process.cluster.hz.JoinConfigurationType.KUBERNETES;
import static org.sonar.process.cluster.hz.JoinConfigurationType.TCP_IP;

public class HazelcastMemberBuilder {
  private String nodeName;
  private int port;
  private ProcessId processId;
  private String networkInterface;
  private final MembersResolver membersResolver;
  private final List<String> members = new ArrayList<>();
  private final JoinConfigurationType type;

  public HazelcastMemberBuilder(JoinConfigurationType type) {
    this.type = type;
    this.membersResolver = TCP_IP.equals(type) ? new TcpIpMembersResolver() : new NopMembersResolver();
  }

  public HazelcastMemberBuilder setNodeName(String s) {
    this.nodeName = s;
    return this;
  }

  public HazelcastMemberBuilder setProcessId(ProcessId p) {
    if (p == ProcessId.ELASTICSEARCH) {
      throw new IllegalArgumentException("Hazelcast must not be enabled on Elasticsearch node");
    }
    this.processId = p;
    return this;
  }

  public HazelcastMemberBuilder setPort(int i) {
    this.port = i;
    return this;
  }

  public HazelcastMemberBuilder setNetworkInterface(String s) {
    this.networkInterface = s;
    return this;
  }

  /**
   * Adds references to cluster members
   */
  public HazelcastMemberBuilder setMembers(Collection<String> members) {
    this.members.addAll(members);
    return this;
  }

  public HazelcastMember build() {
    Config config = new Config();
    // do not use the value defined by property sonar.cluster.name.
    // Hazelcast does not fail when joining a cluster with different name.
    // Apparently this behavior exists since Hazelcast 3.8.2 (see note
    // at http://docs.hazelcast.org/docs/3.8.6/manual/html-single/index.html#creating-cluster-groups)
    config.setClusterName("SonarQube");

    // Configure network
    NetworkConfig netConfig = config.getNetworkConfig();
    netConfig
      .setPort(port)
      .setPortAutoIncrement(false)
      .setReuseAddress(true);
    netConfig.getInterfaces()
      .setEnabled(true)
      .setInterfaces(singletonList(requireNonNull(networkInterface, "Network interface is missing")));

    JoinConfig joinConfig = netConfig.getJoin();
    joinConfig.getAwsConfig().setEnabled(false);
    joinConfig.getMulticastConfig().setEnabled(false);

    List<String> resolvedNodes = membersResolver.resolveMembers(this.members);
    if (KUBERNETES.equals(type)) {
      joinConfig.getKubernetesConfig().setEnabled(true)
        .setProperty("service-dns", requireNonNull(resolvedNodes.get(0), "Service DNS is missing"))
        .setProperty("service-port", "9003");
    } else {
      joinConfig.getTcpIpConfig().setEnabled(true);
      joinConfig.getTcpIpConfig().setMembers(requireNonNull(resolvedNodes, "Members are missing"));
    }

    // We are not using the partition group of Hazelcast, so disabling it
    config.getPartitionGroupConfig().setEnabled(false);

    // Tweak HazelCast configuration
    config
      // Increase the number of tries
      .setProperty("hazelcast.tcp.join.port.try.count", "10")
      // Don't bind on all interfaces
      .setProperty("hazelcast.socket.bind.any", "false")
      // Don't phone home
      .setProperty("hazelcast.phone.home.enabled", "false")
      // Use slf4j for logging
      .setProperty("hazelcast.logging.type", "slf4j");

    MemberAttributeConfig attributes = config.getMemberAttributeConfig();
    attributes.setAttribute(Attribute.NODE_NAME.getKey(), requireNonNull(nodeName, "Node name is missing"));
    attributes.setAttribute(Attribute.PROCESS_KEY.getKey(), requireNonNull(processId, "Process key is missing").getKey());

    return new HazelcastMemberImpl(Hazelcast.newHazelcastInstance(config));
  }

}
