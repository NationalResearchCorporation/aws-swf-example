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
public interface Activity
{
   public ActivityConfig getActivityConfig();
   public String execute(String input);
}
