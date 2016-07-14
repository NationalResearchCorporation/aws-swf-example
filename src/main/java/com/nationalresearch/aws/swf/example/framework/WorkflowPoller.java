/*
 *    Copyright (c) 2016 National Research Corporation
 *    All rights reserved.
 *
 *    This software is the confidential and proprietary information
 *    of National Research Corporation.
 */
package com.nationalresearch.aws.swf.example.framework;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.PollForDecisionTaskRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.nationalresearch.aws.swf.example.framework.domain.Workflow;
import com.nationalresearch.aws.swf.example.framework.util.SwfClient;

/**
 * @author tcollins
 *
 */
public class WorkflowPoller
{
   protected PollerRunnable pollerRunnable;

   public WorkflowPoller(String domain, String taskListName, Workflow workflow)
   {
      this.pollerRunnable = new PollerRunnable(domain, taskListName, workflow);
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
      System.out.println("WorkflowPoller: " + str);
   }

   class PollerRunnable implements Runnable
   {

      private boolean    run = true;

      protected String   domain;
      protected String   taskListName;
      protected Workflow workflow;

      public PollerRunnable(String domain, String taskListName, Workflow workflow)
      {
         super();
         this.domain = domain;
         this.taskListName = taskListName;
         this.workflow = workflow;
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

      private PollForDecisionTaskRequest _newDecisionRequest()
      {
         PollForDecisionTaskRequest decisionTaskRequest = new PollForDecisionTaskRequest()//
                                                                                          .withDomain(domain)//
                                                                                          .withTaskList(new TaskList().withName(taskListName));

         return decisionTaskRequest;
      }

      private void _startPollingLoop()
      {
         AmazonSimpleWorkflowClient swf = SwfClient.getSwf();
         List<DecisionTask> tasks = new ArrayList<DecisionTask>();

         while (run)
         {
            tasks.clear();

            // get the first page
            DecisionTask task = swf.pollForDecisionTask(_newDecisionRequest());
            if (task != null && task.getTaskToken() != null)
            {
               tasks.add(task);
            }

            // if we have more pages get them all 
            if (task.getNextPageToken() != null)
            {
               while (task.getNextPageToken() != null)
               {
                  task = swf.pollForDecisionTask(_newDecisionRequest().withNextPageToken(task.getNextPageToken()));
                  if (task != null && task.getTaskToken() != null)
                  {
                     tasks.add(task);
                  }
               }
            }

            if (task != null && task.getTaskToken() != null)
            {
               this.workflow.handleDecisionTasks(tasks);
            }

         }

         _log("Poller has stopped");
      }

      public String getThreadName()
      {
         return "workflow-poller::" + domain + "::" + taskListName;
      }

   }

}
