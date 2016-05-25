/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.slider.common.tools

import org.apache.slider.core.exceptions.BadConfigException
import org.apache.slider.core.exceptions.SliderException
import org.junit.Test

class TestPortScan {
  final shouldFail = new GroovyTestCase().&shouldFail

  @Test
  public void testScanPorts() throws Throwable {
    
    ServerSocket server = new ServerSocket(0)
    
    try {
      int serverPort = server.getLocalPort()
      assert !SliderUtils.isPortAvailable(serverPort)
      int port = SliderUtils.findFreePort(serverPort, 10)
      assert port > 0 && serverPort < port
    } finally {
      server.close()
    }
  }

  @Test
  public void testRequestedPortsLogic() throws Throwable {
    PortScanner portScanner = new PortScanner()
    portScanner.setPortRange("5,6,8-10, 11,14 ,20 - 22")
    List<Integer> ports = portScanner.remainingPortsToCheck
    def expectedPorts = [5,6,8,9,10,11,14,20,21,22]
    assert ports == expectedPorts
  }

  @Test
  public void testRequestedPortsOutOfOrder() throws Throwable {
    PortScanner portScanner = new PortScanner()
    portScanner.setPortRange("8-10,5,6, 11,20 - 22, 14 ")
    List<Integer> ports = portScanner.remainingPortsToCheck
    def expectedPorts = [5,6,8,9,10,11,14,20,21,22]
    assert ports == expectedPorts
  }

  @Test
  public void testFindAvailablePortInRange() throws Throwable {
    ServerSocket server = new ServerSocket(0)
    try {
      int serverPort = server.getLocalPort()

      PortScanner portScanner = new PortScanner()
      portScanner.setPortRange("" + (serverPort-1) + "-" + (serverPort + 3))
      int port = portScanner.availablePort
      assert port != serverPort
      assert port >= serverPort -1 && port <= serverPort + 3
    } finally {
      server.close()
    }
  }

  @Test
  public void testFindAvailablePortInList() throws Throwable {
    ServerSocket server = new ServerSocket(0)
    try {
      int serverPort = server.getLocalPort()

      PortScanner portScanner = new PortScanner()
      portScanner.setPortRange("" + (serverPort-1) + ", " + (serverPort + 1))
      int port = portScanner.availablePort
      assert port != serverPort
      assert port == serverPort -1 || port == serverPort + 1
    } finally {
      server.close()
    }
  }

  @Test
  public void testNoAvailablePorts() throws Throwable {
    ServerSocket server1 = new ServerSocket(0)
    ServerSocket server2 = new ServerSocket(0)
    try {
      int serverPort1 = server1.getLocalPort()
      int serverPort2 = server2.getLocalPort()

      PortScanner portScanner = new PortScanner()
      portScanner.setPortRange("" + serverPort1+ ", " + serverPort2)
      shouldFail(SliderException) {
        portScanner.availablePort
      }
    } finally {
      server1.close()
      server2.close()
    }
  }

  @Test
  public void testPortRemovedFromRange() throws Throwable {
    ServerSocket server = new ServerSocket(0)
    try {
      int serverPort = server.getLocalPort()

      PortScanner portScanner = new PortScanner()
      portScanner.setPortRange("" + (serverPort-1) + "-" + (serverPort + 3))
      int port = portScanner.availablePort
      assert port != serverPort
      assert port >= serverPort -1 && port <= serverPort + 3
      def isPortInList = port in portScanner.remainingPortsToCheck
      assert !isPortInList
    } finally {
      server.close()
    }
  }

  @Test(expected = BadConfigException.class)
  public void testBadRange() {
    PortScanner portScanner = new PortScanner()
    // note the em dash
    portScanner.setPortRange("2000–2010")
  }

  @Test(expected = BadConfigException.class)
  public void testEndBeforeStart() {
    PortScanner portScanner = new PortScanner()
    portScanner.setPortRange("2001-2000")
  }

  @Test(expected = BadConfigException.class)
  public void testEmptyRange() {
    PortScanner portScanner = new PortScanner()
    portScanner.setPortRange("")
  }

  @Test(expected = BadConfigException.class)
  public void testBlankRange() {
    PortScanner portScanner = new PortScanner()
    portScanner.setPortRange(" ")
  }

  @Test
  public void testExtraComma() {
    PortScanner portScanner = new PortScanner()
    portScanner.setPortRange("2000-2001, ")
    List<Integer> ports = portScanner.remainingPortsToCheck
    def expectedPorts = [2000, 2001]
    assert ports == expectedPorts
  }

  @Test
  public void testExtraCommas() {
    PortScanner portScanner = new PortScanner()
    portScanner.setPortRange("2000-2001,, ,2003,")
    List<Integer> ports = portScanner.remainingPortsToCheck
    def expectedPorts = [2000, 2001, 2003]
    assert ports == expectedPorts
  }
}
