/*
 *    Copyright (c) 2016 National Research Corporation
 *    All rights reserved.
 *
 *    This software is the confidential and proprietary information
 *    of National Research Corporation.
 */
package com.nationalresearch.aws.swf.example.framework.domain;

import java.util.List;

import com.amazonaws.services.simpleworkflow.model.DecisionTask;

/**
 * @author tcollins
 *
 */
public interface Workflow
{
   public void handleDecisionTasks(List<DecisionTask> tasks);
}
