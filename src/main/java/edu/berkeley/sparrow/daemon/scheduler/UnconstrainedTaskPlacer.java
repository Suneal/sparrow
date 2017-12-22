/*
 * Copyright 2013 The Regents of The University California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.berkeley.sparrow.daemon.scheduler;

import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.*;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.util.MathUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.berkeley.sparrow.daemon.util.Logging;
import edu.berkeley.sparrow.thrift.TEnqueueTaskReservationsRequest;
import edu.berkeley.sparrow.thrift.THostPort;
import edu.berkeley.sparrow.thrift.TSchedulingRequest;
import edu.berkeley.sparrow.thrift.TTaskLaunchSpec;
import edu.berkeley.sparrow.thrift.TTaskSpec;

/**
 * A task placer for jobs whose tasks have no placement constraints.
 */
public class UnconstrainedTaskPlacer implements TaskPlacer {
  private static final Logger LOG = Logger.getLogger(UnconstrainedTaskPlacer.class);

  /** Specifications for tasks that have not yet been launched. */
  List<TTaskLaunchSpec> unlaunchedTasks;

  /**
   * For each node monitor where reservations were enqueued, the number of reservations that were
   * enqueued there.
   */
  private Map<THostPort, Integer> outstandingReservations;

  /** Whether the remaining reservations have been cancelled. */
  boolean cancelled;

  /**
   * Id of the request associated with this task placer.
   */
  String requestId;

  private double probeRatio;

  UnconstrainedTaskPlacer(String requestId, double probeRatio) {
    this.requestId = requestId;
    this.probeRatio = probeRatio;
    unlaunchedTasks = new LinkedList<TTaskLaunchSpec>();
    outstandingReservations = new HashMap<THostPort, Integer>();
    cancelled = false;
  }


  public static double[] pssimplmentation(String workerSpeedMap){
    Properties props = new Properties();
    ArrayList<String> nodeList = new ArrayList<String>();
    ArrayList<Double> workerSpeedList= new ArrayList<Double>();
    for (Map.Entry<Object, Object> e : props.entrySet()) {
      nodeList.add((String) e.getKey());
      workerSpeedList.add(Double.valueOf((String)e.getValue()));
    }

    double sum = 0;
    for(double d : workerSpeedList)
      sum += d;

    double[] cdf_worker_speed = new double[workerSpeedList.size()];
    double cdf = 0;
    int j = 0;
    for (double d: workerSpeedList){
      d = d/sum;
      cdf= cdf+ d;
      cdf_worker_speed[j] = cdf;
      j++;
    }
    return cdf_worker_speed;
  }

  public static int getUniqueReservations(double[] cdf_worker_speed, ArrayList<Integer> workerIndex){
    UniformRealDistribution uniformRealDistribution = new UniformRealDistribution();
    int workerIndexReservation= Math.abs(java.util.Arrays.binarySearch(cdf_worker_speed, uniformRealDistribution.sample()));
    //Maybe this recursive function isn't always necessary. //TODO comeback and fix it
    if(workerIndex.contains(workerIndexReservation)){
      getUniqueReservations(cdf_worker_speed, workerIndex);
    }
    return workerIndexReservation;
  }

  @Override
  public Map<InetSocketAddress, TEnqueueTaskReservationsRequest>
      getEnqueueTaskReservationsRequests(
          TSchedulingRequest schedulingRequest, String requestId,
          Collection<InetSocketAddress> nodes, THostPort schedulerAddress) {
    LOG.debug(Logging.functionCall(schedulingRequest, requestId, nodes, schedulerAddress));

    List<InetSocketAddress> nodeList = Lists.newArrayList(nodes);
    List<InetSocketAddress> subNodeList = new ArrayList<InetSocketAddress>();

    double[] cdf_worker_speed = pssimplmentation(schedulingRequest.getTasks().get(0).workSpeed);
    ArrayList<Integer> workerIndex = new ArrayList<Integer>();
    int numTasks = schedulingRequest.getTasks().size();
    int reservationsToLaunch = (int) Math.ceil(probeRatio * numTasks);
    LOG.debug("Request " + requestId + ": Creating " + reservationsToLaunch +
            " task reservations for " + numTasks + " tasks");

    if (nodes.size() > reservationsToLaunch) {
      for (int i = 0; i < reservationsToLaunch; i++) {
        int workerIndexReservation = getUniqueReservations(cdf_worker_speed, workerIndex);
        workerIndex.add(workerIndexReservation);
      }
      for (int i = 0; i < nodeList.size(); i++) {
        for (int j = 0; j < workerIndex.size(); j++) {
          if (i == j) {
            subNodeList.add(nodeList.get(j));
          }
        }
      }
      nodeList = subNodeList;
    }
//    else if (reservationsToLaunch < nodeList.size()){
//      // Get a random subset of nodes by shuffling list.
//      Collections.shuffle(nodeList);
//      nodeList = nodeList.subList(0, reservationsToLaunch);
//    }

    for (TTaskSpec task : schedulingRequest.getTasks()) {
      TTaskLaunchSpec taskLaunchSpec = new TTaskLaunchSpec(task.getTaskId(),
                                                           task.bufferForMessage(), task.getWorkSpeed());
      unlaunchedTasks.add(taskLaunchSpec);
    }

    HashMap<InetSocketAddress, TEnqueueTaskReservationsRequest> requests = Maps.newHashMap();

    int numReservationsPerNode = 1;
    if (nodeList.size() < reservationsToLaunch) {
    	numReservationsPerNode = reservationsToLaunch / nodeList.size();
    }
    StringBuilder debugString = new StringBuilder();
    for (int i = 0; i < nodeList.size(); i++) {
      int numReservations = numReservationsPerNode;
      if (reservationsToLaunch % nodeList.size() > i)
    	++numReservations;
      InetSocketAddress node = nodeList.get(i);
      debugString.append(node.getAddress().getHostAddress() + ":" + node.getPort());
      debugString.append(";");
      // TODO: this needs to be a count!
      outstandingReservations.put(
          new THostPort(node.getAddress().getHostAddress(), node.getPort()),
          numReservations);
      TEnqueueTaskReservationsRequest request = new TEnqueueTaskReservationsRequest(
          schedulingRequest.getApp(), schedulingRequest.getUser(), requestId,
          schedulerAddress, numReservations);
      requests.put(node, request);
    }
    LOG.debug("Request " + requestId + ": Launching enqueueReservation on " +
        nodeList.size() + " node monitors: " + debugString.toString());
    return requests;
  }

  @Override
  public List<TTaskLaunchSpec> assignTask(THostPort nodeMonitorAddress) {
	Integer numOutstandingReservations = outstandingReservations.get(nodeMonitorAddress);
	if (numOutstandingReservations == null) {
		LOG.error("Node monitor " + nodeMonitorAddress +
		    " not in list of outstanding reservations");
		return Lists.newArrayList();
	}
    if (numOutstandingReservations == 1) {
      outstandingReservations.remove(nodeMonitorAddress);
    } else {
      outstandingReservations.put(nodeMonitorAddress, numOutstandingReservations - 1);
    }

    if (unlaunchedTasks.isEmpty()) {
      LOG.debug("Request " + requestId + ", node monitor " + nodeMonitorAddress.toString() +
               ": Not assigning a task (no remaining unlaunched tasks).");
      return Lists.newArrayList();
    } else {
      TTaskLaunchSpec launchSpec = unlaunchedTasks.get(0);
      unlaunchedTasks.remove(0);
      LOG.debug("Request " + requestId + ", node monitor " + nodeMonitorAddress.toString() +
                ": Assigning task");
      return Lists.newArrayList(launchSpec);
    }
  }

  @Override
  public boolean allTasksPlaced() {
    return unlaunchedTasks.isEmpty();
  }

  @Override
  public Set<THostPort> getOutstandingNodeMonitorsForCancellation() {
    if (!cancelled) {
      cancelled = true;
      return outstandingReservations.keySet();
    }
    return new HashSet<THostPort>();
  }
}
