/*
 *    Copyright (c) 2016 National Research Corporation
 *    All rights reserved.
 *
 *    This software is the confidential and proprietary information
 *    of National Research Corporation.
 */
package com.nationalresearch.aws.swf.example.framework.domain;

/**
 * @author tcollins
 *
 */
public class WorkflowConfig
{
   private String domain;
   private String domainDescription;
   private int    domainRetentionPeridDays    = 3;
   private String taskListName;

   private String workflowName;
   private String workflowVersion             = "0.1";
   private String workflowStartToCloseTimeout = "300"; // 5 mins

   public String getDomain()
   {
      return domain;
   }

   public void setDomain(String domain)
   {
      this.domain = domain;
   }

   public String getDomainDescription()
   {
      return domainDescription;
   }

   public void setDomainDescription(String domainDescription)
   {
      this.domainDescription = domainDescription;
   }

   public int getDomainRetentionPeridDays()
   {
      return domainRetentionPeridDays;
   }

   public void setDomainRetentionPeridDays(int domainRetentionPeridDays)
   {
      this.domainRetentionPeridDays = domainRetentionPeridDays;
   }

   public String getTaskListName()
   {
      return taskListName;
   }

   public void setTaskListName(String taskListName)
   {
      this.taskListName = taskListName;
   }

   public String getWorkflowName()
   {
      return workflowName;
   }

   public void setWorkflowName(String workflowName)
   {
      this.workflowName = workflowName;
   }

   public String getWorkflowVersion()
   {
      return workflowVersion;
   }

   public void setWorkflowVersion(String workflowVersion)
   {
      this.workflowVersion = workflowVersion;
   }

   public String getWorkflowStartToCloseTimeout()
   {
      return workflowStartToCloseTimeout;
   }

   public void setWorkflowStartToCloseTimeout(String workflowStartToCloseTimeout)
   {
      this.workflowStartToCloseTimeout = workflowStartToCloseTimeout;
   }

}
