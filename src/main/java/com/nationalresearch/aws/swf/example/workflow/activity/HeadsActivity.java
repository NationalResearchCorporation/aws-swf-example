/*
 *    Copyright (c) 2016 National Research Corporation
 *    All rights reserved.
 *
 *    This software is the confidential and proprietary information
 *    of National Research Corporation.
 */
package com.nationalresearch.aws.swf.example.workflow.activity;

import com.nationalresearch.aws.swf.example.framework.domain.Activity;
import com.nationalresearch.aws.swf.example.framework.domain.ActivityConfig;
import com.nationalresearch.aws.swf.example.framework.util.Json;
import com.nationalresearch.aws.swf.example.workflow.ThreeHeadsWorkflowData;

/**
 * @author tcollins
 *
 */
public class HeadsActivity implements Activity
{
   public static final String NAME = "heads-activity";

   public String execute(String input)
   {
      String result = null;

      try
      {
         // This increments the heads count
         System.out.println("HeadsActivity.execute");

         ThreeHeadsWorkflowData workflowData = (ThreeHeadsWorkflowData) Json.toObject(input, ThreeHeadsWorkflowData.class);
         workflowData.setFlip(null);
         workflowData.setHeadsCnt(workflowData.getHeadsCnt() + 1);

         result = Json.toJson(workflowData);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error in Head Activity", e);
      }

      return result;
   }

   @Override
   public ActivityConfig getActivityConfig()
   {
      return new ActivityConfig(NAME);
   }
}
