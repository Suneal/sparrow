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
import java.net.InetSocketAddress;
import java.util.*;

import org.apache.commons.math3.distribution.UniformRealDistribution;
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

    /**
     * Specifications for tasks that have not yet been launched.
     */
    List<TTaskLaunchSpec> unlaunchedTasks;

    /**
     * For each node monitor where reservations were enqueued, the number of reservations that were
     * enqueued there.
     */
    private Map<THostPort, Integer> outstandingReservations;

    /**
     * Whether the remaining reservations have been cancelled.
     */
    boolean cancelled;

    /**
     * Id of the request associated with this task placer.
     */
    String requestId;

    private double probeRatio;
    private String workerSpeedMap;
    private List<InetSocketAddress> globalNodeList = Lists.newArrayList();
    private HashMap<String, Double> estimatedWorkerSpeedMap;


    //Test case in sparrow/src/test/java/edu/berkeley/sparrow/daemon/scheduler/TestPSS.java
    public static double[] getCDFWokerSpeed(ArrayList<Double> workerSpeedList) throws IOException {

        //Gets the CDF of workers Speed
        double sum = 0;
        for (double d : workerSpeedList)
            sum += d;

        double[] cdf_worker_speed = new double[workerSpeedList.size()];
        double cdf = 0;
        int j = 0;
        for (double d : workerSpeedList) {
            d = d / sum;
            cdf = cdf + d;
            cdf_worker_speed[j] = cdf;
            j++;
        }
        //CDF of worker speed + PSS based on Qiong's python pss file
        return cdf_worker_speed;
    }

    //Test case in sparrow/src/test/java/edu/berkeley/sparrow/daemon/scheduler/TestPSS.java
    //Gets index  where cdf allows retrieving index having higher workerspeed with higher probability
    public static int getIndexFromPSS(double[] cdf_worker_speed, ArrayList<Integer> workerIndex) {
        UniformRealDistribution uniformRealDistribution = new UniformRealDistribution();
        int workerIndexReservation = java.util.Arrays.binarySearch(cdf_worker_speed, uniformRealDistribution.sample());
        if (workerIndexReservation < 0) {
            workerIndexReservation = Math.abs(workerIndexReservation) - 1;
        } else {
            workerIndexReservation = Math.abs(workerIndexReservation);
        }
        //This doesn't allow probing the same nodemonitor twice
        if (workerIndex.contains(workerIndexReservation)) {
            workerIndexReservation = getIndexFromPSS(cdf_worker_speed, workerIndex);
        }
        return workerIndexReservation;
    }


    UnconstrainedTaskPlacer(String requestId, double probeRatio) {
        this.requestId = requestId;
        this.probeRatio = probeRatio;
        //not sure we need this here but I didn't change since I already added it
        unlaunchedTasks = new LinkedList<TTaskLaunchSpec>();
        outstandingReservations = new HashMap<THostPort, Integer>();
        cancelled = false;
    }

    // Have replaced Test cases with empty. Might need to change later.
    @Override
    public Map<InetSocketAddress, TEnqueueTaskReservationsRequest>
    getEnqueueTaskReservationsRequests(
            TSchedulingRequest schedulingRequest, String requestId,
            Collection<InetSocketAddress> nodes, THostPort schedulerAddress, HashMap<String, Double> estimatedWorkSpeed) {
        LOG.debug(Logging.functionCall(schedulingRequest, requestId, nodes, schedulerAddress));

        LOG.debug("Qiong Updated Estimated WorkerSpeed Map: "+ estimatedWorkSpeed.toString());


        // Get a random subset of nodes by shuffling list.
        List<InetSocketAddress> nodeList = Lists.newArrayList(nodes);


        //So that the node Object can be loaded later
        HashMap<String, InetSocketAddress> nodeToInetMap = new HashMap<String, InetSocketAddress>();
        for (InetSocketAddress node : nodeList) {
            nodeToInetMap.put(node.getAddress().getHostAddress(), node);
        }

        ArrayList<String> backendList = new ArrayList<String>();
        ArrayList<Double> workerSpeedList = new ArrayList<Double>();

        for (Map.Entry<String, Double> entry : estimatedWorkSpeed.entrySet()) {
            backendList.add(entry.getKey());
            workerSpeedList.add(entry.getValue());
        }


        double[] cdf_worker_speed = null;

        try {
            //gets cdf of worker speed in the range of 0 to 1
            cdf_worker_speed = getCDFWokerSpeed(workerSpeedList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<InetSocketAddress> subNodeList = new ArrayList<InetSocketAddress>();

        //This was used to make sure probes aren't sent to same worker
        ArrayList<Integer> workerIndex = new ArrayList<Integer>();

        int numTasks = schedulingRequest.getTasks().size();
        int reservationsToLaunch = (int) Math.ceil(probeRatio * numTasks);
        LOG.debug("Request " + requestId + ": Creating " + reservationsToLaunch +
                " task reservations for " + numTasks + " tasks");

        //Qiong : Might need to handle this reservation < nodes
//    Collections.shuffle(nodeList);
//    if (reservationsToLaunch < nodeList.size())
//      nodeList = nodeList.subList(0, reservationsToLaunch);
        if (nodes.size() > reservationsToLaunch) {
            for (int i = 0; i < reservationsToLaunch; i++) {
                int workerIndexReservation = getIndexFromPSS(cdf_worker_speed, workerIndex);
                workerIndex.add(workerIndexReservation); //Chosen workers based on proportional sampling
            }

            //After PSS, we're getting the index of worker with higher probability
            //Nodelist contains the list of workers and workerIndex contains indices from that node list
            //So this comparision should make sense but using hashmap would be a better idea.
            for (int j = 0; j < workerIndex.size(); j++) {
                String hostFromWorkerSpeed = backendList.get(workerIndex.get(j));
                subNodeList.add(nodeToInetMap.get(hostFromWorkerSpeed));
            }
            nodeList = subNodeList;
        }

        for (TTaskSpec task : schedulingRequest.getTasks()) {
            TTaskLaunchSpec taskLaunchSpec = new TTaskLaunchSpec(task.getTaskId(),
                    task.bufferForMessage());
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
