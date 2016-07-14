/*
 *    Copyright (c) 2016 National Research Corporation
 *    All rights reserved.
 *
 *    This software is the confidential and proprietary information
 *    of National Research Corporation.
 */
package com.nationalresearch.aws.swf.example.workflow.activity;

import java.security.SecureRandom;

import com.nationalresearch.aws.swf.example.framework.domain.Activity;
import com.nationalresearch.aws.swf.example.framework.domain.ActivityConfig;
import com.nationalresearch.aws.swf.example.framework.util.Json;
import com.nationalresearch.aws.swf.example.workflow.ThreeHeadsWorkflowData;

/**
 * @author tcollins
 *
 */
public class FlipCoinActivity implements Activity
{
   private static SecureRandom numberGenerator = null;
   public static final String  NAME            = "flip-coin-activity";

   public String execute(String input)
   {
      String result = null;

      try
      {
         // This flip a coin
         System.out.println("FlipCoinActivity.execute");

         ThreeHeadsWorkflowData workflowData = (ThreeHeadsWorkflowData) Json.toObject(input, ThreeHeadsWorkflowData.class);

         // Random chance at getting an error
         int x = (Math.abs(_getSecureRandom().nextInt()) % 17);
         if (x == 0)
         {
            // simulate an activity task error
            throw new RuntimeException("Oh no! thumb got tired and couldn't flip the coin anymore.");
         }

         // Random coin flip
         int i = (Math.abs(_getSecureRandom().nextInt()) % 2);
         String flip = null;

         if (i == 1)
         {
            flip = "HEADS";
         }
         else
         {
            flip = "TAILS";
         }

         workflowData.setFlip(flip);

         // update the flipHistory list
         workflowData.getFlipHistory().add(flip);

         result = Json.toJson(workflowData);
      }
      catch (RuntimeException rte)
      {
         // just re-throw the runtime exception
         throw rte;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error in Flip Coin Activity", e);
      }

      return result;
   }

   @Override
   public ActivityConfig getActivityConfig()
   {
      return new ActivityConfig(NAME);
   }

   private static SecureRandom _getSecureRandom()
   {
      if (numberGenerator == null)
      {
         numberGenerator = new SecureRandom();
      }
      return numberGenerator;
   }

}
