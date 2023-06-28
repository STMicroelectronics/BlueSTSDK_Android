There is a **publish.gradle** file in each module to be published

In this file there are 3 properties related to the artifact to be published that must be updated
when it is necessary to make a new release

- **LIB_GROUP_ID**  the group name
- **LIB_ARTIFACT_ID** the name
- **LIB_VERSION** the version number

For instance: **implementation "com.st.blue.sdk:st-blue-sdk:0.5.0-alpha13"**
com.st.blue.sdk it's the group, st-blue-sdk it's the name and 0.5.0-alpha13 the version

The **GPR_USER**, **GPR_API_KEY** properties, on the other hand, are linked to the user who can
perform a
release.

This property are genereted on the Github user profile (**Settings> Developer Settings> Personal
Access token> Generate new token**)
Choosing the validity period and scope (read/write github packages checkboxes)

These properties can be put as environment variables or as Gradle properties in your global user **
gradle.properties**

For more details on this
topic: [here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)

In the **settings.gradle** file of an application that needs to use these artifacts, the maven is
defined with the credentials to access the artifacts for reading.

