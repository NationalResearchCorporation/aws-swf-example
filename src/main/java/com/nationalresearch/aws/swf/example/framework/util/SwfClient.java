/*
 *    Copyright (c) 2016 National Research Corporation
 *    All rights reserved.
 *
 *    This software is the confidential and proprietary information
 *    of National Research Corporation.
 */
package com.nationalresearch.aws.swf.example.framework.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;

/**
 * @author tcollins
 *
 */
public class SwfClient
{
   private static SwfClient           instance;

   private AmazonSimpleWorkflowClient swf;

   // private constructor / class is meant to be used statically
   private SwfClient()
   {
      loadSysProps();
      initSWF();
   }

   public static AmazonSimpleWorkflowClient getSwf()
   {
      if (instance == null)
      {
         instance = new SwfClient();
      }
      return instance.swf;
   }

   private void loadSysProps()
   {
      // looks for accessKey and secret key in the system.properties file
      // aws.accessKeyId and aws.secretKey

      try
      {
         File sysPropFile = new File(System.getProperty("user.dir"), "system.properties");

         // props from sys.props file
         Properties overrideProps = new Properties();
         overrideProps.load(new FileInputStream(sysPropFile));

         // clobber current sys props with props from file
         Properties existingProps = System.getProperties();
         existingProps.putAll(overrideProps);

         System.setProperties(existingProps);
      }
      catch (IOException e)
      {
         System.out.println("Error trying to load the 'system.properties' file");
         e.printStackTrace();
      }
   }

   private void initSWF()
   {
      AWSCredentialsProviderChain credChain = new AWSCredentialsProviderChain(new SystemPropertiesCredentialsProvider());
      swf = new AmazonSimpleWorkflowClient(credChain);
   }
}
