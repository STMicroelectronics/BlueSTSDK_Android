# Android_Module_BlueSTSDK

Module for creating the BlueST-SDK library

## For using it

Code compiled using gradle 8.1.1 and JDK 11

set on Gradle properties the Github Login name and SSO authentication
Example:

GPR_USER=PezzoniL  
GPR_API_KEY=XXXXXXXXXXXXXXXXXXXXXXXX

## For making the publication on local maven:

Gradle  
st_blu_sdk  
task/build/assemble

Gradle  
st_blu_sdk  
task/pulishing/publishToMavenLocal

Or

Gradle  
st_blu_sdk  
task/pulishing/publishToMavenLocal  