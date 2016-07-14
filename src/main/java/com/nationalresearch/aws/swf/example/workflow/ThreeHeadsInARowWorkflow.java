/*
 *    Copyright (c) 2016 National Research Corporation
 *    All rights reserved.
 *
 *    This software is the confidential and proprietary information
 *    of National Research Corporation.
 */
package com.nationalresearch.aws.swf.example.workflow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.simpleworkflow.model.ActivityType;
import com.amazonaws.services.simpleworkflow.model.CompleteWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.DecisionType;
import com.amazonaws.services.simpleworkflow.model.EventType;
import com.amazonaws.services.simpleworkflow.model.FailWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.amazonaws.services.simpleworkflow.model.RespondDecisionTaskCompletedRequest;
import com.amazonaws.services.simpleworkflow.model.ScheduleActivityTaskDecisionAttributes;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.nationalresearch.aws.swf.example.framework.domain.ActivityConfig;
import com.nationalresearch.aws.swf.example.framework.domain.Workflow;
import com.nationalresearch.aws.swf.example.framework.util.Json;
import com.nationalresearch.aws.swf.example.framework.util.SwfClient;
import com.nationalresearch.aws.swf.example.workflow.activity.FlipCoinActivity;
import com.nationalresearch.aws.swf.example.workflow.activity.HeadsActivity;
import com.nationalresearch.aws.swf.example.workflow.activity.TailsActivity;

/**
 * @author tcollins
 *
 */
public class ThreeHeadsInARowWorkflow implements Workflow
{

   @Override
   public void handleDecisionTasks(List<DecisionTask> tasks)
   {
      // build a state object from the history events in the decision task list
      WorkflowState workflowState = _buildWorkflowState(tasks);

      // handle the current state
      _handleWorkflowState(workflowState, tasks.get(0));

   }

   private WorkflowState _buildWorkflowState(List<DecisionTask> tasks)
   {

      WorkflowState workflowState = new WorkflowState();

      for (DecisionTask decisionTask : tasks)
      {
         List<HistoryEvent> historyEvents = decisionTask.getEvents();

         for (HistoryEvent historyEvent : historyEvents)
         {
            EventType eventType = EventType.valueOf(historyEvent.getEventType());
            workflowState.eventHistoryCount = workflowState.eventHistoryCount + 1;

            switch (eventType)
            {
               case WorkflowExecutionStarted:
                  workflowState.workflowStarted = workflowState.workflowStarted + 1;
                  workflowState.workflowInput = historyEvent.getWorkflowExecutionStartedEventAttributes().getInput();
                  break;

               case WorkflowExecutionCompleted:
                  workflowState.workflowCompleted = workflowState.workflowCompleted + 1;
                  workflowState.workflowCompletedResult = historyEvent.getWorkflowExecutionCompletedEventAttributes().getResult();
                  break;

               case ActivityTaskScheduled:
                  workflowState.activitiesScheduled = workflowState.activitiesScheduled + 1;
                  workflowState.scheduleActivityEventIdActivityTypeMap.put(historyEvent.getEventId(), historyEvent.getActivityTaskScheduledEventAttributes().getActivityType());
                  break;

               case ActivityTaskStarted:
                  workflowState.activitiesStarted = workflowState.activitiesStarted + 1;
                  break;

               case ActivityTaskCompleted:
                  workflowState.activitiesCompleted = workflowState.activitiesCompleted + 1;
                  workflowState.activityCompleteResult = historyEvent.getActivityTaskCompletedEventAttributes().getResult();
                  workflowState.activityCompleteActivityType = workflowState.scheduleActivityEventIdActivityTypeMap.get(historyEvent.getActivityTaskCompletedEventAttributes().getScheduledEventId());
                  break;

               case ActivityTaskFailed:
                  workflowState.activitiesFailed = workflowState.activitiesFailed + 1;
                  workflowState.activityFailedReason = historyEvent.getActivityTaskFailedEventAttributes().getReason();
                  workflowState.activityFailedDetails = historyEvent.getActivityTaskFailedEventAttributes().getDetails();
                  break;

               case ActivityTaskTimedOut:
                  workflowState.activitiesTimedOut = workflowState.activitiesTimedOut + 1;
                  workflowState.activityTimedOutDetails = historyEvent.getActivityTaskTimedOutEventAttributes().getDetails();
                  break;

               case ScheduleActivityTaskFailed:
                  workflowState.scheduleActivityFailed = workflowState.scheduleActivityFailed + 1;
                  break;

               default :
                  //_log(eventType.name() + " -- doing nothing");
                  break;
            }
         }
      }

      return workflowState;

   }

   private void _handleWorkflowState(WorkflowState workflowState, DecisionTask decisionTask)
   {
      _log("STATE -> " + workflowState);

      try
      {
         if (workflowState.activitiesFailed > 0)
         {
            _log("Activity failed.. Making call to SWF to fail this workflow");
            _respondWithFailedWorkflowDecision(workflowState.activityFailedDetails, workflowState.activityFailedReason, decisionTask);
         }
         else if (workflowState.activitiesTimedOut > 0)
         {
            _log("Activity timed out.. Making call to SWF to fail this workflow");
            _respondWithFailedWorkflowDecision(workflowState.activityTimedOutDetails, "Activity timed out", decisionTask);
         }
         else if (workflowState.workflowCompleted == 1)
         {
            ThreeHeadsWorkflowData workflowData = _toWorkFlowData(workflowState.workflowCompletedResult);
            _log("Workflow completed.. it took " + workflowData.getFlipHistory().size() + " flips and " + workflowState.activitiesCompleted + " activities to flip heads three times in a row");
         }
         else if (workflowState.activityCompleteResult != null)
         {
            ThreeHeadsWorkflowData workflowData = _toWorkFlowData(workflowState.activityCompleteResult);
            _makeNextActivityDecision(workflowData, workflowState, decisionTask);
         }
         else if (workflowState.workflowStarted == 1 && !workflowState.hasActivityFailedOrTimedOut())
         {
            // init the workflow by kicking off the Flip Coin Activity with a new ThreeHeadsWorkflowData object
            _respondWithScheduleActivityDecision(new FlipCoinActivity().getActivityConfig(), new ThreeHeadsWorkflowData(), decisionTask);
         }
      }
      catch (Exception e)
      {
         _log("ERROR: error in _activityTaskCompleted " + e.getMessage());
      }

   }

   private void _makeNextActivityDecision(ThreeHeadsWorkflowData workflowData, WorkflowState workflowState, DecisionTask decisionTask) throws JsonProcessingException
   {
      _log("_makeNextDecision: Flip=" + workflowData.getFlip() + ", Head Cnt=" + workflowData.getHeadsCnt());

      if (workflowData.getHeadsCnt() >= 3)
      {
         // we have three or more heads, lets end the workflow
         _log("Workflow completed.. it took " + workflowData.getFlipHistory().size() + " flips and " + workflowState.activitiesCompleted + " activities to flip heads three times in a row... Sending complete workflow decision");
         _log(" -> Flip History -> " + workflowData.getFlipHistory());

         CompleteWorkflowExecutionDecisionAttributes attrs = new CompleteWorkflowExecutionDecisionAttributes()//
                                                                                                              .withResult(Json.toJson(workflowData));

         Decision decision = new Decision()//
                                           .withDecisionType(DecisionType.CompleteWorkflowExecution)//
                                           .withCompleteWorkflowExecutionDecisionAttributes(attrs);

         _respondWithDecision(decisionTask, decision);
      }
      else if (workflowData.getFlip() != null)
      {
         // we have a flip do the flip task
         if (workflowData.getFlip().equals("HEADS"))
         {
            // call heads task
            _respondWithScheduleActivityDecision(new HeadsActivity().getActivityConfig(), workflowData, decisionTask);
         }
         else
         {
            // call tails task
            _respondWithScheduleActivityDecision(new TailsActivity().getActivityConfig(), workflowData, decisionTask);
         }
      }
      else
      {
         // we need to flip 
         _respondWithScheduleActivityDecision(new FlipCoinActivity().getActivityConfig(), workflowData, decisionTask);
      }

   }

   private void _respondWithFailedWorkflowDecision(String details, String reason, DecisionTask decisionTask)
   {
      FailWorkflowExecutionDecisionAttributes attrs = new FailWorkflowExecutionDecisionAttributes()//
                                                                                                   .withDetails(details)//
                                                                                                   .withReason(reason);

      Decision decision = new Decision()//
                                        .withDecisionType(DecisionType.FailWorkflowExecution)//
                                        .withFailWorkflowExecutionDecisionAttributes(attrs);

      _respondWithDecision(decisionTask, decision);
   }

   private void _respondWithScheduleActivityDecision(ActivityConfig activityConfig, ThreeHeadsWorkflowData workflowData, DecisionTask decisionTask) throws JsonProcessingException
   {
      ScheduleActivityTaskDecisionAttributes attrs = _activityAttributesFromActivity(activityConfig, workflowData);

      Decision decision = new Decision()//
                                        .withDecisionType(DecisionType.ScheduleActivityTask)//
                                        .withScheduleActivityTaskDecisionAttributes(attrs);

      _respondWithDecision(decisionTask, decision);
   }

   private void _respondWithDecision(DecisionTask decisionTask, Decision decision)
   {
      List<Decision> decisions = new ArrayList<Decision>();
      decisions.add(decision);
      _respondWithDecisions(decisionTask, decisions);
   }

   private void _respondWithDecisions(DecisionTask decisionTask, List<Decision> decisions)
   {
      RespondDecisionTaskCompletedRequest request = new RespondDecisionTaskCompletedRequest()//
                                                                                             .withTaskToken(decisionTask.getTaskToken())//
                                                                                             .withDecisions(decisions);

      SwfClient.getSwf().respondDecisionTaskCompleted(request);
   }

   private ScheduleActivityTaskDecisionAttributes _activityAttributesFromActivity(ActivityConfig activityConf, ThreeHeadsWorkflowData workflowData) throws JsonProcessingException
   {
      ActivityType type = new ActivityType()//
                                            .withName(activityConf.getName())//
                                            .withVersion(activityConf.getVersion());

      ScheduleActivityTaskDecisionAttributes attrs = new ScheduleActivityTaskDecisionAttributes().withActivityType(type)//                                                                                                                                     
                                                                                                 .withActivityId(UUID.randomUUID().toString())//
                                                                                                 .withInput(Json.toJson(workflowData));

      return attrs;

   }

   private ThreeHeadsWorkflowData _toWorkFlowData(String json) throws JsonParseException, JsonMappingException, IOException
   {
      ThreeHeadsWorkflowData workflowData = (ThreeHeadsWorkflowData) Json.toObject(json, ThreeHeadsWorkflowData.class);
      return workflowData;
   }

   private void _log(String str)
   {
      // TODO.. need to use real logging
      System.out.println("ThreeHeadsInARowWorkflow: " + str);
   }

   class WorkflowState
   {
      int                     eventHistoryCount                      = 0;

      int                     workflowStarted                        = 0;
      int                     workflowCompleted                      = 0;

      int                     activitiesStarted                      = 0;
      int                     activitiesScheduled                    = 0;
      int                     activitiesCompleted                    = 0;
      int                     activitiesFailed                       = 0;
      int                     activitiesTimedOut                     = 0;
      int                     scheduleActivityFailed                 = 0;

      String                  workflowInput;
      String                  activityCompleteResult;
      ActivityType            activityCompleteActivityType;
      String                  workflowCompletedResult;

      String                  activityFailedReason;
      String                  activityFailedDetails;
      String                  activityTimedOutDetails;

      Map<Long, ActivityType> scheduleActivityEventIdActivityTypeMap = new HashMap<Long, ActivityType>();

      public boolean hasActivityFailedOrTimedOut()
      {
         return ((activitiesFailed + activitiesTimedOut) > 0);
      }

      @Override
      public String toString()
      {
         return "WorkflowState [eventHistoryCount=" + eventHistoryCount + ", workflowStarted=" + workflowStarted + ", workflowCompleted=" + workflowCompleted + ", activitiesStarted=" + activitiesStarted + ", activitiesScheduled=" + activitiesScheduled + ", activitiesCompleted=" + activitiesCompleted + ", activitiesFailed=" + activitiesFailed + ", activitiesTimedOut=" + activitiesTimedOut + ", scheduleActivityFailed=" + scheduleActivityFailed + ", workflowInput=" + workflowInput
               + ", activityCompleteResult=" + activityCompleteResult + ", activityCompleteActivityType=" + activityCompleteActivityType + ", workflowCompletedResult=" + workflowCompletedResult + ", activityFailedReason=" + activityFailedReason + ", activityFailedDetails=" + activityFailedDetails + ", activityTimedOutDetails=" + activityTimedOutDetails + "]";
      }

   }

}
