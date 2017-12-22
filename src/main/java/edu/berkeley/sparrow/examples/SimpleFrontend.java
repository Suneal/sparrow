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

package edu.berkeley.sparrow.examples;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.berkeley.sparrow.daemon.SparrowConf;
import edu.berkeley.sparrow.daemon.util.ConfigUtil;
import edu.berkeley.sparrow.daemon.util.MinMax;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import edu.berkeley.sparrow.api.SparrowFrontendClient;
import edu.berkeley.sparrow.daemon.scheduler.SchedulerThrift;
import edu.berkeley.sparrow.daemon.util.Serialization;
import edu.berkeley.sparrow.thrift.FrontendService;
import edu.berkeley.sparrow.thrift.TFullTaskId;
import edu.berkeley.sparrow.thrift.TTaskSpec;
import edu.berkeley.sparrow.thrift.TUserGroupInfo;

/**
 * Simple frontend that runs jobs composed of sleep tasks.
 */
public class SimpleFrontend implements FrontendService.Iface {
  /** Amount of time to launch tasks for. */
  public static final String EXPERIMENT_S = "experiment_s";
  public static final int DEFAULT_EXPERIMENT_S = 300;

  public static final String JOB_ARRIVAL_PERIOD_MILLIS = "job_arrival_period_millis";
  public static final int DEFAULT_JOB_ARRIVAL_PERIOD_MILLIS = 100;

  /** Number of tasks per job. */
  public static final String TASKS_PER_JOB = "tasks_per_job";
  public static final int DEFAULT_TASKS_PER_JOB = 1;

  /** Duration of one task, in milliseconds */
  public static final String TASK_DURATION_MILLIS = "task_duration_millis";
  public static final int DEFAULT_TASK_DURATION_MILLIS = 100;

  /** Host and port where scheduler is running. */
  public static final String SCHEDULER_HOST = "scheduler_host";
  public static final String DEFAULT_SCHEDULER_HOST = "localhost";
  public static final String SCHEDULER_PORT = "scheduler_port";

  /**
   * Default application name.
   */
  public static final String APPLICATION_ID = "sleepApp";

  private static final Logger LOG = Logger.getLogger(SimpleFrontend.class);

  private static final TUserGroupInfo USER = new TUserGroupInfo();

  private SparrowFrontendClient client;

  public static int[] unidenticalWorkSpeeds(int no_of_elements, int exponent){
    ZipfDistribution zipfDistribution = new ZipfDistribution(no_of_elements,exponent);
    int[] worker_speeds = zipfDistribution.sample(no_of_elements);
    int max = MinMax.getMaxValue(worker_speeds);
    int min = MinMax.getMinValue(worker_speeds);
    if(max == min){
      worker_speeds= unidenticalWorkSpeeds(no_of_elements, exponent);
    }
    return  worker_speeds;
  }


  public static  int RATIO_BETWEEN_MAX_MIN = 100;
  public static int TOTAL_WORKERS = 10;
  public static  double upper_bound = 1.0;
  public static  double lower_bound = upper_bound/RATIO_BETWEEN_MAX_MIN;
  public static Map<String, String> workSpeedMap = new HashMap<String, String>();


  /** A runnable which Spawns a new thread to launch a scheduling request. */
  private class JobLaunchRunnable implements Runnable {
    private int tasksPerJob;
    private int taskDurationMillis;
    private String workerSpeed;
    public JobLaunchRunnable(int tasksPerJob, int taskDurationMillis, String workerSpeed) {
      this.tasksPerJob = tasksPerJob;
      this.taskDurationMillis = taskDurationMillis;
      this.workerSpeed = workerSpeed;
    }

    @Override
    public void run() {
      // Generate tasks in the format expected by Sparrow. First, pack task parameters.
      ByteBuffer message = ByteBuffer.allocate(4);
      message.putInt(taskDurationMillis);

      List<TTaskSpec> tasks = new ArrayList<TTaskSpec>();
      for (int taskId = 0; taskId < tasksPerJob; taskId++) {
        TTaskSpec spec = new TTaskSpec();
        spec.setTaskId(Integer.toString(taskId));
        spec.setMessage(message.array());
        spec.setWorkSpeed(workerSpeed);
        tasks.add(spec);
      }
      long start = System.currentTimeMillis();
      try {
        client.submitJob(APPLICATION_ID, tasks, USER);
      } catch (TException e) {
        LOG.error("Scheduling request failed!", e);
      }
      long end = System.currentTimeMillis();
      LOG.debug("Scheduling request duration " + (end - start));
    }
  }

  public void run(String[] args) {
    try {
      OptionParser parser = new OptionParser();
      parser.accepts("c", "configuration file").withRequiredArg().ofType(String.class);
      parser.accepts("help", "print help statement");
      OptionSet options = parser.parse(args);

      if (options.has("help")) {
        parser.printHelpOn(System.out);
        System.exit(-1);
      }

      // Logger configuration: log to the console
      BasicConfigurator.configure();
      LOG.setLevel(Level.DEBUG);

      Configuration conf = new PropertiesConfiguration();

      if (options.has("c")) {
        String configFile = (String) options.valueOf("c");
        conf = new PropertiesConfiguration(configFile);
      }

      if (!conf.containsKey(SparrowConf.STATIC_NODE_MONITORS)) {
        throw new RuntimeException("Missing configuration node monitor list");
      }
      Set<InetSocketAddress> backends =ConfigUtil.parseBackends(conf);
      TOTAL_WORKERS = backends.size();

      //2 is the exponent for Zipf's Distribution
      int[] worker_speeds = unidenticalWorkSpeeds(backends.size(),2);

      double[] new_worker_speeds =  new double[TOTAL_WORKERS];
      for (int i= 0; i< worker_speeds.length; i++){
        new_worker_speeds[i] = (double) 1.0/worker_speeds[i];
      }

      double minValue = MinMax.getMinValue(new_worker_speeds);
      double maxValue = MinMax.getMaxValue(new_worker_speeds);

      double[] final_worker_speeds = new double[TOTAL_WORKERS];
      for (int i= 0; i< new_worker_speeds.length; i++){
        final_worker_speeds[i] = ((new_worker_speeds[i] - minValue)/
                (maxValue - minValue))* (upper_bound - lower_bound)+ lower_bound;

      }

      int i = 0;
      for (String node: conf.getStringArray(SparrowConf.STATIC_NODE_MONITORS)) {
        workSpeedMap.put(node,String.valueOf(final_worker_speeds[i]));
        i++;
      }

      int arrivalPeriodMillis = conf.getInt(JOB_ARRIVAL_PERIOD_MILLIS,
          DEFAULT_JOB_ARRIVAL_PERIOD_MILLIS);
      int experimentDurationS = conf.getInt(EXPERIMENT_S, DEFAULT_EXPERIMENT_S);
      LOG.debug("Using arrival period of " + arrivalPeriodMillis +
          " milliseconds and running experiment for " + experimentDurationS + " seconds.");
      int tasksPerJob = conf.getInt(TASKS_PER_JOB, DEFAULT_TASKS_PER_JOB);
      int taskDurationMillis = conf.getInt(TASK_DURATION_MILLIS, DEFAULT_TASK_DURATION_MILLIS);

      int schedulerPort = conf.getInt(SCHEDULER_PORT,
          SchedulerThrift.DEFAULT_SCHEDULER_THRIFT_PORT);
      String schedulerHost = conf.getString(SCHEDULER_HOST, DEFAULT_SCHEDULER_HOST);
      client = new SparrowFrontendClient();
      client.initialize(new InetSocketAddress(schedulerHost, schedulerPort), APPLICATION_ID, this);

      JobLaunchRunnable runnable = new JobLaunchRunnable(tasksPerJob, taskDurationMillis, workSpeedMap.toString());
      ScheduledThreadPoolExecutor taskLauncher = new ScheduledThreadPoolExecutor(1);
      taskLauncher.scheduleAtFixedRate(runnable, 0, arrivalPeriodMillis, TimeUnit.MILLISECONDS);

      long startTime = System.currentTimeMillis();
      LOG.debug("sleeping");
      while (System.currentTimeMillis() < startTime + experimentDurationS * 1000) {
        Thread.sleep(100);
      }
      taskLauncher.shutdown();
    }
    catch (Exception e) {
      LOG.error("Fatal exception", e);
    }
  }

  @Override
  public void frontendMessage(TFullTaskId taskId, int status, ByteBuffer message)
      throws TException {
    // We don't use messages here, so just log it.
    LOG.debug("Got unexpected message: " + Serialization.getByteBufferContents(message));
  }

  public static void main(String[] args) {
    new SimpleFrontend().run(args);
  }
}
