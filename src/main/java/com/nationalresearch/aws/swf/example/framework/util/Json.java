/*
 *    Copyright (c) 2016 National Research Corporation
 *    All rights reserved.
 *
 *    This software is the confidential and proprietary information
 *    of National Research Corporation.
 */
package com.nationalresearch.aws.swf.example.framework.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author tcollins
 *
 */
public class Json
{
   private ObjectMapper objectMapper;

   private static Json  instance;

   // private constructor / class is meant to be used statically
   private Json()
   {
      objectMapper = new ObjectMapper();
   }

   public static String toJson(Map map) throws JsonProcessingException
   {
      return getJson()._toJson(map);
   }

   public static String toJson(Object obj) throws JsonProcessingException
   {
      return getJson()._toJson(obj);
   }

   public static Map toMap(String jsonStr) throws JsonParseException, JsonMappingException, IOException
   {
      return getJson()._toMap(jsonStr);
   }

   public static Object toObject(String jsonStr, Class objClass) throws JsonParseException, JsonMappingException, IOException
   {
      return getJson()._toObject(jsonStr, objClass);
   }

   private static Json getJson()
   {
      if (instance == null)
      {
         instance = new Json();
      }
      return instance;
   }

   private String _toJson(Map map) throws JsonProcessingException
   {
      if (map == null)
      {
         map = new HashMap();
      }

      return objectMapper.writeValueAsString(map);
   }

   private String _toJson(Object obj) throws JsonProcessingException
   {
      return objectMapper.writeValueAsString(obj);
   }

   private Map _toMap(String jsonStr) throws JsonParseException, JsonMappingException, IOException
   {
      if (jsonStr == null)
      {
         return new HashMap();
      }
      return (Map) objectMapper.readValue(jsonStr, Map.class);
   }

   private Object _toObject(String jsonStr, Class objClass) throws JsonParseException, JsonMappingException, IOException
   {
      return objectMapper.readValue(jsonStr, objClass);
   }

}
