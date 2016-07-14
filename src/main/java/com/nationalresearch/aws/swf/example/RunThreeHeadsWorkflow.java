/*
 *    Copyright (c) 2016 National Research Corporation
 *    All rights reserved.
 *
 *    This software is the confidential and proprietary information
 *    of National Research Corporation.
 */
package com.nationalresearch.aws.swf.example;

import com.nationalresearch.aws.swf.example.framework.ActivityPoller;
import com.nationalresearch.aws.swf.example.framework.WorkflowPoller;
import com.nationalresearch.aws.swf.example.framework.WorkflowRegistrator;
import com.nationalresearch.aws.swf.example.framework.WorkflowStarter;
import com.nationalresearch.aws.swf.example.framework.domain.WorkflowConfig;
import com.nationalresearch.aws.swf.example.workflow.ThreeHeadsInARowWorkflow;
import com.nationalresearch.aws.swf.example.workflow.activity.FlipCoinActivity;
import com.nationalresearch.aws.swf.example.workflow.activity.HeadsActivity;
import com.nationalresearch.aws.swf.example.workflow.activity.TailsActivity;

/**
 * @author tcollins
 *
 */
public class RunThreeHeadsWorkflow
{
   public static void main(String[] args)
   {
      try
      {
         // Setup some config
         WorkflowConfig config = new WorkflowConfig();
         config.setDomain("test-domain");
         config.setDomainDescription("A place to play around and test things.  Please use this for testing rather than creating new test domains, since domains can not be deleted.");
         config.setDomainRetentionPeridDays(2);
         config.setTaskListName("test-swf-tim-tasklist");
         config.setWorkflowName("three-heads-in-a-row");

         // Register the workflow and activities with AWS SWF
         WorkflowRegistrator workflowRegistrator = new WorkflowRegistrator(config);
         workflowRegistrator.addActivityConfig(new FlipCoinActivity().getActivityConfig());
         workflowRegistrator.addActivityConfig(new HeadsActivity().getActivityConfig());
         workflowRegistrator.addActivityConfig(new TailsActivity().getActivityConfig());
         workflowRegistrator.register();

         // Start the ActivityPoller
         ActivityPoller activityPoller = new ActivityPoller(config.getDomain(), config.getTaskListName());
         activityPoller.registerActivity(new FlipCoinActivity());
         activityPoller.registerActivity(new HeadsActivity());
         activityPoller.registerActivity(new TailsActivity());
         activityPoller.startPolling();

         // Start the WorkflowPoller
         WorkflowPoller workflowPoller = new WorkflowPoller(config.getDomain(), config.getTaskListName(), new ThreeHeadsInARowWorkflow());
         workflowPoller.startPolling();

         // Start the Workflow
         WorkflowStarter workflowStarter = new WorkflowStarter(config, "{\"json\":\"input\", \"can\":\"go here\"}");
         workflowStarter.start();

      }
      catch (Exception e)
      {
         System.out.println("Error in Main.main: " + e.getMessage());
         e.printStackTrace();
      }

   }
}
