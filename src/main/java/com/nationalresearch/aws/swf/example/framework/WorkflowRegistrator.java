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

import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.amazonaws.services.simpleworkflow.model.DomainAlreadyExistsException;
import com.amazonaws.services.simpleworkflow.model.RegisterActivityTypeRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterDomainRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterWorkflowTypeRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.amazonaws.services.simpleworkflow.model.TypeAlreadyExistsException;
import com.nationalresearch.aws.swf.example.framework.domain.ActivityConfig;
import com.nationalresearch.aws.swf.example.framework.domain.WorkflowConfig;
import com.nationalresearch.aws.swf.example.framework.util.SwfClient;

/**
 * @author tcollins
 *
 */
public class WorkflowRegistrator
{
   private WorkflowConfig       workflowConfig;
   private List<ActivityConfig> activityConfigs;

   public WorkflowRegistrator(WorkflowConfig workflowConfig)
   {
      this.workflowConfig = workflowConfig;
      this.activityConfigs = new ArrayList<ActivityConfig>();
   }

   public void register()
   {
      registerDomain();
      registerWorkflowType();
      registerActivities();
   }

   public void addActivityConfig(ActivityConfig activityConfig)
   {
      this.activityConfigs.add(activityConfig);
   }

   private void registerDomain()
   {
      try
      {
         System.out.println("Registering the domain '" + workflowConfig.getDomain() + "'.");
         SwfClient.getSwf()
                  .registerDomain(new RegisterDomainRequest()//
                                                             .withName(workflowConfig.getDomain())//
                                                             .withDescription(workflowConfig.getDomainDescription())//
                                                             .withWorkflowExecutionRetentionPeriodInDays(String.valueOf(workflowConfig.getDomainRetentionPeridDays())));
      }
      catch (DomainAlreadyExistsException e)
      {
         System.out.println("Domain already exists!");
      }
   }

   private void registerActivities()
   {
      for (ActivityConfig activityConfig : activityConfigs)
      {
         registerActivityType(activityConfig);
      }
   }

   private void registerActivityType(ActivityConfig config)
   {
      try
      {
         System.out.println("Registering the activity type '" + config.getName() + "-" + config.getVersion() + "'.");
         SwfClient.getSwf()
                  .registerActivityType(new RegisterActivityTypeRequest()//
                                                                         .withDomain(workflowConfig.getDomain())//
                                                                         .withName(config.getName())//
                                                                         .withVersion(config.getVersion())//
                                                                         .withDefaultTaskList(new TaskList().withName(workflowConfig.getTaskListName()))//
                                                                         .withDefaultTaskScheduleToStartTimeout(config.getStartTimeout())//
                                                                         .withDefaultTaskStartToCloseTimeout(config.getStartToCloseTimeout())//
                                                                         .withDefaultTaskScheduleToCloseTimeout(config.getCloseTimeout())//
                                                                         .withDefaultTaskHeartbeatTimeout(config.getHeartbeatTimeout()));
      }
      catch (TypeAlreadyExistsException e)
      {
         System.out.println("Activity type '" + config.getName() + "-" + config.getVersion() + "' already exists!");
      }
   }

   public void registerWorkflowType()
   {
      try
      {
         System.out.println("Registering the workflow type '" + workflowConfig.getWorkflowName() + "-" + workflowConfig.getWorkflowVersion() + "'.");
         SwfClient.getSwf()
                  .registerWorkflowType(new RegisterWorkflowTypeRequest()//
                                                                         .withDomain(workflowConfig.getDomain())//
                                                                         .withName(workflowConfig.getWorkflowName())//
                                                                         .withVersion(workflowConfig.getWorkflowVersion())//
                                                                         .withDefaultChildPolicy(ChildPolicy.TERMINATE)//
                                                                         .withDefaultTaskList(new TaskList().withName(workflowConfig.getTaskListName()))//
                                                                         .withDefaultTaskStartToCloseTimeout(workflowConfig.getWorkflowStartToCloseTimeout()));
      }
      catch (TypeAlreadyExistsException e)
      {
         System.out.println("Workflow type already exists!");
      }
   }

}
