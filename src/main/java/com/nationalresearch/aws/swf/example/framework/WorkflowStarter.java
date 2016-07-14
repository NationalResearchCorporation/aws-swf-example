/*
 *    Copyright (c) 2016 National Research Corporation
 *    All rights reserved.
 *
 *    This software is the confidential and proprietary information
 *    of National Research Corporation.
 */
package com.nationalresearch.aws.swf.example.framework;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.amazonaws.services.simpleworkflow.model.Run;
import com.amazonaws.services.simpleworkflow.model.StartWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.WorkflowType;
import com.nationalresearch.aws.swf.example.framework.domain.WorkflowConfig;
import com.nationalresearch.aws.swf.example.framework.util.SwfClient;

/**
 * @author tcollins
 *
 */
public class WorkflowStarter
{

   private WorkflowConfig workflowConfig;
   private String         workFlowId;
   private String         input;

   public WorkflowStarter(WorkflowConfig workflowConfig, String initialInput)
   {
      this.workflowConfig = workflowConfig;
      this.input = initialInput;

      SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssS");
      String unique = format.format(new Date());
      this.workFlowId = workflowConfig.getWorkflowName() + "." + workflowConfig.getWorkflowVersion() + "." + unique;
   }

   public void start()
   {
      System.out.println("Starting workflow: " + workflowConfig.getWorkflowName() + " - " + workflowConfig.getWorkflowVersion());
      System.out.println(" - workFlowId: " + workFlowId);

      WorkflowType workflowType = new WorkflowType()//
                                                    .withName(workflowConfig.getWorkflowName())//
                                                    .withVersion(workflowConfig.getWorkflowVersion());

      StartWorkflowExecutionRequest request = new StartWorkflowExecutionRequest()//
                                                                                 .withDomain(workflowConfig.getDomain())//
                                                                                 .withWorkflowType(workflowType)//
                                                                                 .withWorkflowId(workFlowId)//
                                                                                 .withInput(input)//
                                                                                 .withExecutionStartToCloseTimeout(workflowConfig.getWorkflowStartToCloseTimeout());

      Run run = SwfClient.getSwf().startWorkflowExecution(request);

      System.out.println(" - run id: " + run.getRunId());

   }
}
