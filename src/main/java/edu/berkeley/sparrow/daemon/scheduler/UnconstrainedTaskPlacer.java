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

import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.*;

import org.apache.commons.lang.StringUtils;
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



  public static double[] pssimplmentation(String workerSpeedMap) throws IOException {

    //Gets the workeSpeed Map String and converts it into hash
    workerSpeedMap = workerSpeedMap.substring(1, workerSpeedMap.length()-1);           //remove curly brackets
    String[] keyValuePairs = workerSpeedMap.split(",");              //split the string to create key-value pairs
    ArrayList<String> nodeList = new ArrayList<String>();
    ArrayList<Double> workerSpeedList= new ArrayList<Double>();
    for(String pair : keyValuePairs)                        //iterate over the pairs
    {
      String[] entry = pair.split("=");                   //split the pairs to get key and value
      nodeList.add((String) entry[0].trim());
      workerSpeedList.add(Double.valueOf((String)entry[1].trim()));
    }

    //Gets the CDF of workers Speed
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
    //CDF of worker speed + PSS based on Qiong's python pss file
    return cdf_worker_speed;
  }


  //Get workerSpeed where cdf allows retrieving higher workerspeed with higher probability
  public static int getUniqueReservations(double[] cdf_worker_speed, ArrayList<Integer> workerIndex){
    UniformRealDistribution uniformRealDistribution = new UniformRealDistribution();
    int workerIndexReservation= Math.abs(java.util.Arrays.binarySearch(cdf_worker_speed, uniformRealDistribution.sample()));
    //This doesn't allow calling the same nodemonitor twice
    if(workerIndex.contains(workerIndexReservation)){
      workerIndexReservation = getUniqueReservations(cdf_worker_speed, workerIndex);
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

    List<InetSocketAddress> newNodeList = Lists.newArrayList();


    //Doing this so that index of workspeed matches the assignment

    String workerSpeedMap = schedulingRequest.getTasks().get(0).workSpeed;

    //This is repeated ; better make a function
    workerSpeedMap = workerSpeedMap.substring(1, workerSpeedMap.length()-1);           //remove curly brackets
    String[] keyValuePairs = workerSpeedMap.split(",");              //split the string to create key-value pairs
    ArrayList<String> frontEndodeList = new ArrayList<String>();
    ArrayList<Double> workerSpeedList= new ArrayList<Double>();

    for(String pair : keyValuePairs)                        //iterate over the pairs
    {
      String[] entry = pair.split("=");                   //split the pairs to get key and value
      frontEndodeList.add((String) entry[0].trim());
      workerSpeedList.add(Double.valueOf((String)entry[1].trim()));
//      System.out.println(entry[0] + ":" + entry[1]);
    }

    for (String fNode: frontEndodeList) {
      for (InetSocketAddress node : nodeList) {
//       System.out.println("SystemLogging: HOST ADDRESS: " + node.getAddress().getHostAddress());
//       System.out.println("SystemLogging: FNode: " + fNode);
        if(node.getAddress().getHostAddress().equalsIgnoreCase(fNode)){
          newNodeList.add(node);
        }
      }
    }
//    System.out.println("NAYA NODELIST: " + newNodeList.toString());
    nodeList= newNodeList;

    double[] cdf_worker_speed = new double[10]; //TODO Currently 10 workers
    try {
      cdf_worker_speed = pssimplmentation(schedulingRequest.getTasks().get(0).workSpeed);
//      System.out.println("CDF WorkerSpeed String" + cdf_worker_speed.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
    ArrayList<Integer> workerIndex = new ArrayList<Integer>();

    int numTasks = schedulingRequest.getTasks().size();
    int reservationsToLaunch = (int) Math.ceil(probeRatio * numTasks);

    LOG.debug("Request " + requestId + ": Creating " + reservationsToLaunch +
            " task reservations for " + numTasks + " tasks");

    if (nodes.size() > reservationsToLaunch) {
      for (int i = 0; i < reservationsToLaunch; i++) {
        int workerIndexReservation = getUniqueReservations(cdf_worker_speed, workerIndex);
        workerIndex.add(workerIndexReservation); //Chosen workers based on proportional sampling
      }
//      System.out.println("WorkerIndex ==>"+ workerIndex.toString());
//      System.out.println("NodeList ==>"+ nodeList.toString());

      //After PSS, we're getting the index of worker with higher probability
      //Nodelist contains the list of workers and workerIndex contains indices from that node list
      //So this comparision should make sense but using hashmap would be a better idea.
      for (int j = 0; j < workerIndex.size(); j++) {
        subNodeList.add(nodeList.get(workerIndex.get(j) -1));
      }
      nodeList = subNodeList;

/*      for (int i = 0; i < nodeList.size(); i++) {
        for (int j = 0; j < workerIndex.size(); j++) {
          if (i == workerIndex.get(j)) {
            subNodeList.add(nodeList.get(j));
          }
        }
      }
      nodeList = subNodeList;*/
    }
//    System.out.println("New NodeList ==>"+ nodeList.toString());
//TODO handle case when nodelist size is less than reservations
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
