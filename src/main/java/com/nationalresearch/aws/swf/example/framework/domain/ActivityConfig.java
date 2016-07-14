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
public class ActivityConfig
{
   private String name;
   private String version             = "0.1";
   private String startTimeout        = "30";  // 30 seconds
   private String startToCloseTimeout = "180"; // 3 mins
   private String closeTimeout        = "210"; // 3 mins, 30 seconds
   private String heartbeatTimeout    = "30";  // 30 seconds

   public ActivityConfig(String name)
   {
      super();
      this.name = name;
   }

   public ActivityConfig(String name, String version)
   {
      super();
      this.name = name;
      this.version = version;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getVersion()
   {
      return version;
   }

   public void setVersion(String version)
   {
      this.version = version;
   }

   public String getStartTimeout()
   {
      return startTimeout;
   }

   public void setStartTimeout(String startTimeout)
   {
      this.startTimeout = startTimeout;
   }

   public String getStartToCloseTimeout()
   {
      return startToCloseTimeout;
   }

   public void setStartToCloseTimeout(String startToCloseTimeout)
   {
      this.startToCloseTimeout = startToCloseTimeout;
   }

   public String getCloseTimeout()
   {
      return closeTimeout;
   }

   public void setCloseTimeout(String closeTimeout)
   {
      this.closeTimeout = closeTimeout;
   }

   public String getHeartbeatTimeout()
   {
      return heartbeatTimeout;
   }

   public void setHeartbeatTimeout(String heartbeatTimeout)
   {
      this.heartbeatTimeout = heartbeatTimeout;
   }

}
