/*
 *    Copyright (c) 2016 National Research Corporation
 *    All rights reserved.
 *
 *    This software is the confidential and proprietary information
 *    of National Research Corporation.
 */
package com.nationalresearch.aws.swf.example.workflow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tcollins
 *
 * An object representation of the JSON data that is passed around from Activity to Activity
 */
public class ThreeHeadsWorkflowData
{
   protected int          headsCnt = 0;
   protected String       flip;
   protected List<String> flipHistory;

   public ThreeHeadsWorkflowData()
   {
      // init an empty list
      flipHistory = new ArrayList<String>();
   }

   public String getFlip()
   {
      return flip;
   }

   public void setFlip(String flip)
   {
      this.flip = flip;
   }

   public int getHeadsCnt()
   {
      return headsCnt;
   }

   public void setHeadsCnt(int headsCnt)
   {
      this.headsCnt = headsCnt;
   }

   public List<String> getFlipHistory()
   {
      return flipHistory;
   }

   public void setFlipHistory(List<String> flipHistory)
   {
      this.flipHistory = flipHistory;
   }

}
