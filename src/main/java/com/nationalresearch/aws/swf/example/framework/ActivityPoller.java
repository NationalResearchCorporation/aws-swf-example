/*
 *    Copyright (c) 2016 National Research Corporation
 *    All rights reserved.
 *
 *    This software is the confidential and proprietary information
 *    of National Research Corporation.
 */
package com.nationalresearch.aws.swf.example.framework;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.model.ActivityTask;
import com.amazonaws.services.simpleworkflow.model.PollForActivityTaskRequest;
import com.amazonaws.services.simpleworkflow.model.RespondActivityTaskCompletedRequest;
import com.amazonaws.services.simpleworkflow.model.RespondActivityTaskFailedRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.nationalresearch.aws.swf.example.framework.domain.Activity;
import com.nationalresearch.aws.swf.example.framework.util.SwfClient;

/**
 * @author tcollins
 *
 */
public class ActivityPoller
{
   protected PollerRunnable pollerRunnable;

   public ActivityPoller(String domain, String taskListName)
   {
      this.pollerRunnable = new PollerRunnable(domain, taskListName);
   }

   public void registerActivity(Activity activity)
   {
      this.pollerRunnable.registerActivity(activity);
   }

   public void startPolling()
   {
      _log("Starting the poller");
      Thread t = new Thread(pollerRunnable, pollerRunnable.getThreadName());
      t.start();
   }

   public void stopPolling()
   {
      pollerRunnable.stopPolling();
   }

   private static void _log(String str)
   {
      // TODO.. need to use real logging
      System.out.println("ActivityPoller: " + str);
   }

   class PollerRunnable implements Runnable
   {
      private boolean              run = true;

      protected String             domain;
      protected String             taskListName;

      protected Map<String, Class> activityMap;

      public PollerRunnable(String domain, String taskListName)
      {
         super();
         this.domain = domain;
         this.taskListName = taskListName;
         this.activityMap = new HashMap<String, Class>();
      }

      public void registerActivity(Activity activity)
      {
         this.activityMap.put(activity.getActivityConfig().getName(), activity.getClass());
      }

      @Override
      public void run()
      {
         _startPollingLoop();
      }

      public void stopPolling()
      {
         _log("Poller will stop after next long poll is complete. (this could take up to 60 seconds)");
         run = false;
      }

      private void _startPollingLoop()
      {
         AmazonSimpleWorkflowClient swf = SwfClient.getSwf();

         PollForActivityTaskRequest activityTaskRequest = new PollForActivityTaskRequest()//
                                                                                          .withDomain(domain)//
                                                                                          .withTaskList(new TaskList().withName(taskListName));

         while (run)
         {

            ActivityTask task = swf.pollForActivityTask(activityTaskRequest);

            if (task != null && task.getTaskToken() != null)
            {
               try
               {
                  Activity activity = _findAndBuildActivity(task.getActivityType().getName());
                  if (activity != null)
                  {
                     String output = activity.execute(task.getInput());

                     swf.respondActivityTaskCompleted(new RespondActivityTaskCompletedRequest()//
                                                                                               .withTaskToken(task.getTaskToken())//
                                                                                               .withResult(output));

                  }
                  else
                  {
                     _log("WARN: Could not find a registered activity for: " + task.getActivityType().getName());
                  }
               }
               catch (Exception e)
               {
                  _log("ERROR: Executing activity : " + task.getActivityType().getName() + " - " + e.getMessage());

                  swf.respondActivityTaskFailed(new RespondActivityTaskFailedRequest()//
                                                                                      .withTaskToken(task.getTaskToken())//
                                                                                      .withReason("Activity Failed: " + task.getActivityType().getName())//
                                                                                      .withDetails(e.getMessage()));
               }

            }

         }
         _log("Poller has stopped");
      }

      private Activity _findAndBuildActivity(String name)
      {
         Class clazz = this.activityMap.get(name);
         if (clazz != null)
         {
            try
            {
               return (Activity) clazz.newInstance();
            }
            catch (Exception e)
            {
               _log("ERROR: Could not instantiate activity class: " + clazz + " - " + e.getMessage());
            }
         }
         return null;
      }

      public String getThreadName()
      {
         return "activity-poller::" + domain + "::" + taskListName;
      }

   }

}
