#AWS SWF Example

This app demonstrates how to use AWS Simple Workflow with the standard SDK.  This is written in java, but since it uses the standard SDK it could easily be ported to C#. 

This app runs a workflow that is trying to flip "heads" three times in a row.  We have three activities (tasks) that are performed in this workflow.

 -  FlipCoinActivity : Flips either "HEADS" or "TAILS"
 -  HeadsActivity : Increments the headsCnt property
 -  TailsActivity : Sets the headsCnt property to zero


The workflow looks like this...

![Workflow Image](flip-coin-workflow.png?raw=true)


--------

##Code Layout

I created a lightweight framework to handle some of the boilerplate of using the AWS SDK for SWF.  These classes are under the "framework" directory.

The example workflow and activities are under the "workflow" directory.

The initial "main" entry to kick off the entire example is from the **RunThreeHeadsWorkflow** class.  This class does the following...

 -  Registers the domain, workflow type and activity types with AWS. (This means manual configuration in AWS is not necessary)
 -  Starts the ActivityPoller to poll AWS for new activity tasks
 -  Starts the WorkPollers to poll AWS for new decision tasks
 -  Starts the workflow

The workflow class **ThreeHeadsInARowWorkflow** is what handles the decisions and schedules all the activities.  This class is called from the WorkflowPoller ever time new decision tasks are available.  This class does the following..

 - Loops over history events in the decision tasks to populate a **WorkflowState** object
 - Using the WorkflowState object decides what needs to be done next.
     - if an activity failed.. fail the workflow
     - if an activity times out.. fail the workflow
     - if we have a activity result (return value) figure out what activity needed to be called next
           - if we have 3 or more heads.. complete the workflow
           - if we have a heads... trigger HeadsActivity
           - if we have a tails... trigger TailsActivity
           - If we don't have heads or tails... trigger FlipCoinActivity
     - if workflow has been started and we don't have an activity result and no errors, trigger the first activity.  


##How to run it

TODO
