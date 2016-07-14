# AWS SWF Example

This app demonstrates how to use AWS Simple Workflow with the standard SDK.  This is written in java, but since it uses the standard SDK it could easily be ported to C#. 

This app runs a workflow that is trying to flip "heads" three times in a row.  We have three activities (tasks) that are performed in this workflow.

 -  FlipCoinActivity : Flips either "HEADS" or "TAILS"
 -  HeadsActivity : Increments the headsCnt property
 -  TailsActivity : Sets the headsCnt property to zero


The workflow looks like this...

![Workflow Image](flip-coin-workflow.png?raw=true)


--------

## Code Layout

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


## How to run it

#### Prerequisite
 - java 7 or 8 (should work, tested on 1.8.0_74)

#### Setup
 1. Clone this repo to your local machine.
 
    ``clone git@github.com:NationalResearchCorporation/aws-swf-example.git``


 2. Run gradle to build the project. From the root of the project run..
 
    ``gradlew.bat clean build copyDeps``  *(windows)*
    
    ``./gradlew clean build copyDeps``  *(mac/linux)*
    

 3. Copy the sample.system.properties and name it system.properties (keeping it in the project root directory). Edit the system.properties to use your aws access key and secret key
 
 4. Run the app
 
    ``java -cp "build\libs\*" com.nationalresearch.aws.swf.example.RunThreeHeadsWorkflow`` *(windows ?)*
    
    ``java -cp "build/libs/*" com.nationalresearch.aws.swf.example.RunThreeHeadsWorkflow`` *(mac/linux)*



