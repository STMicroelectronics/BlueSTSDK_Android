# Android_Module_BlueSTSDK

Module for creating the BlueST-SDK library

## For using it

Code compiled using gradle 8.2.1 and JDK 17.0.7

set on Gradle properties the Github Login name and SSO authentication
Example:

GPR_USER=PezzoniL  
GPR_API_KEY=XXXXXXXXXXXXXXXXXXXXXXXX

## Sample app

This package provides a sample application with some basic functionalites:
- boards discovery and connection
- subscribe to each exported bluetooth SDK features and display the sensors' values in textual way


## st-blue-sdk and st-opus libraries

This package provides 2 libraries that could be included on the other STMicroelectronics' applications

For compiling these 2 libraries is necessary to enable all the gradle tasks
So:
1) Android Studio -> Settings -> Experimental 
	and uncheck
	"Only include test tasks in Gradle task list generated during Gradle Sync"

2) Then open the Gradle tab on left and run:
   st_blue_sdk/build/assemble
   st_blue_sdk/publishing/publishToMavenLocal

   st_opus/build/assemble
   st_opus/publishing/publishToMavenLocal