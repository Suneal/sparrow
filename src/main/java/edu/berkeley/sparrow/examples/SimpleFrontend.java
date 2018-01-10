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

import java.io.StringReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
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
  public static final int DEFAULT_TASKS_PER_JOB = 10;

  /** Number of tasks. */
  public static final String TOTAL_NO_OF_TASKS = "total_no_tasks";
  public static final int DEFAULT_TOTAL_NO_OF_TASKS = 3500;

  /** Custom Load Info */
  public static final String LOAD = "load";
  public static final double DEFAULT_LOAD = 0.1;

  /** Duration of one task, in milliseconds */
  public static final String TASK_DURATION_MILLIS = "task_duration_millis";
  public static final int DEFAULT_TASK_DURATION_MILLIS = 100;

  /** Host and port where scheduler is running. */
  public static final String SCHEDULER_HOST = "scheduler_host";
  public static final String DEFAULT_SCHEDULER_HOST = "localhost";
  public static final String SCHEDULER_PORT = "scheduler_port";

  private static String SLAVES = "slaves";

  public static Random random = new Random();
  //Get data from exponential Distribution with parameter lambda
  public static double getNext(double lambda) {
    return Math.log(1-random.nextDouble())/(-lambda);
  }
  static ArrayList<Double> taskDurations = new ArrayList<Double>();
  //Worker Speed Mapped to its corresponding worker
  public static Map<String, String> workSpeedMap = new HashMap<String, String>();


  /**
   * Default application name.
   */
  public static final String APPLICATION_ID = "sleepApp";

  private static final Logger LOG = Logger.getLogger(SimpleFrontend.class);

  private static final TUserGroupInfo USER = new TUserGroupInfo();

  private SparrowFrontendClient client;

  /** A runnable which Spawns a new thread to launch a scheduling request. */
  private class JobLaunchRunnable implements Runnable {
    private int tasksPerJob;
    private ArrayList<Double> taskDurations;
    private int i;
    private String workSpeedMap;

    public JobLaunchRunnable(int tasksPerJob, ArrayList<Double> taskDurations, String workSpeedMap ) {
      this.tasksPerJob = tasksPerJob;
      this.taskDurations = taskDurations;
      this.i = 0; //index
      this.workSpeedMap = workSpeedMap;
    }

    @Override
    public void run() {
      // Generate tasks in the format expected by Sparrow. First, pack task parameters.
      ByteBuffer message = ByteBuffer.allocate(16);
      message.putLong(System.currentTimeMillis());
      message.putDouble(taskDurations.get(i));

      i++;

      List<TTaskSpec> tasks = new ArrayList<TTaskSpec>();
      for (int taskId = 0; taskId < tasksPerJob; taskId++) {
        TTaskSpec spec = new TTaskSpec();
        spec.setTaskId(Integer.toString(taskId));
        spec.setMessage(message.array());
        tasks.add(spec);
      }
      long start = System.currentTimeMillis();
      try {
        client.submitJob(APPLICATION_ID, tasks, USER, workSpeedMap);
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
      parser.accepts("w", "configuration file").
              withRequiredArg().ofType(String.class);
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



//      int arrivalPeriodMillis = conf.getInt(JOB_ARRIVAL_PERIOD_MILLIS,
//          DEFAULT_JOB_ARRIVAL_PERIOD_MILLIS);
      int experimentDurationS = conf.getInt(EXPERIMENT_S, DEFAULT_EXPERIMENT_S);

      int tasksPerJob = conf.getInt(TASKS_PER_JOB, DEFAULT_TASKS_PER_JOB);
      int taskDurationMillis = conf.getInt(TASK_DURATION_MILLIS, DEFAULT_TASK_DURATION_MILLIS);
      int totalNoOfTasks = conf.getInt(TOTAL_NO_OF_TASKS, DEFAULT_TOTAL_NO_OF_TASKS);
      double load = conf.getDouble(LOAD,DEFAULT_LOAD);

      //Generate Exponential Data
      int median_task_duration = taskDurationMillis;
      double lambda = 1.0/median_task_duration;
      random.setSeed(123456789);
      double value = 0;
      double sumValue = 0;
      int totalJob = (int) totalNoOfTasks/tasksPerJob;
      for (int l = 0; l < totalJob; l++){
        value = getNext(lambda);
        taskDurations.add(value);
        sumValue +=value;
      }


      //Use this flag to retrieve the backend and worker speed mapping
      Configuration slavesConfig = new PropertiesConfiguration();
      if (options.has("w")) {
        String configFile = (String) options.valueOf("w");
        try {
          slavesConfig = new PropertiesConfiguration(configFile);
        } catch (ConfigurationException e) {
        }
      }

      if (!slavesConfig.containsKey(SLAVES)) {
        throw new RuntimeException("Missing configuration node monitor list");
      }

      //Creates a string which can be parse to compare with respective host IP
      String workSpeed = "";
      for (String node : slavesConfig.getStringArray(SLAVES)) {
        workSpeed = workSpeed + node + ",";
      }

      Properties props = new Properties();
      props.load(new StringReader(workSpeed.replace(",", "\n")));
      //Could have use the properties object but trying to make it consistent with other code.
      ArrayList<Double> workerSpeed = new ArrayList<Double>();
      for (Map.Entry<Object, Object> e : props.entrySet()) {
        workerSpeed.add(Double.valueOf(e.getValue().toString()));
        workSpeedMap.put(e.getKey().toString(), e.getValue().toString());
      }

      double W = 0; //W is total Worker Speed in the system
      for (double m : workerSpeed) {
        W += m;
      }

      double averageTaskDurationMillis = (double)sumValue/totalJob;
      //Get Service Rate
      double serviceRate = W/averageTaskDurationMillis; //When taking 3500 tasks with the given seed for exponential distribution, this is the average we get for task duration
      //Get Arrival Rate
      double arrivalRate = load*serviceRate;
      //Get Arrival Period for individual task
      long arrivalPeriodMillis = (long)(tasksPerJob/arrivalRate);
      //Get Experiment duration based on no. of tasks (in s)
      experimentDurationS = (int)((totalNoOfTasks) *(arrivalPeriodMillis+2)/(1000*tasksPerJob))+10000; //Until all tasks finish
      LOG.debug("Using arrival period of " + arrivalPeriodMillis +
              " milliseconds and running experiment for " + experimentDurationS + " seconds.");


      int schedulerPort = conf.getInt(SCHEDULER_PORT,
          SchedulerThrift.DEFAULT_SCHEDULER_THRIFT_PORT);
      String schedulerHost = conf.getString(SCHEDULER_HOST, DEFAULT_SCHEDULER_HOST);
      client = new SparrowFrontendClient();
      client.initialize(new InetSocketAddress(schedulerHost, schedulerPort), APPLICATION_ID, this);

      JobLaunchRunnable runnable = new JobLaunchRunnable(tasksPerJob, taskDurations, workSpeedMap.toString());
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
    BasicConfigurator.configure();
    new SimpleFrontend().run(args);
  }
}
