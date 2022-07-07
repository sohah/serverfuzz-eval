# APM Java Agent Load Generator

This directory contains configuration files for load-testing the APM Java Agent.

## Load Tests

Load tests are run using two bare-metal workers. One worker is dedicated to the role of load generation, which other worker is dedicated to the role of hosting the application under test, which is instrumented by the APM Java Agent.

Load generation is performed using [Locust](https://locust.io/) and the application which is instrumented by the APM Java Agent and placed under load is [Spring Petclinic](https://projects.spring.io/spring-petclinic/).

## Orchestration

Because of the nature of these tests, they require the use of a dedicated orchestration layer. For this project, that orchestrator is called Bandstand. It is an internal project to Elastic.

Bandstand performs two primary functions.

1. *Service discovery* The pipeline utilizes a load-generation machine and an application server which run in parallel to each other. These machines need to know how to find each other and Bandstand gives them that information.
2. *Orchestration* We need to have a system which tells various services when to start an stop based on the state of other services. For example, we can't start load-generation until we are assured that the application is up and in a coherent state. By using an independent orchestrator, we can allow each service to report on its own state. This avoid a number of otherwise difficult-to-maintain dependencies between service state through the lifetime of the load generation.

While the orchestration layer is currently relatively lightweight and simple, it is built to be able to be easily extended for more sophisticated needs, such as multiple test runs inside a single test execution.

The current design uses a persistant orchestator. However, it is a future goal to have a dynamic orchestrator which is unique to each test execution and is spun up and torn down alongside the rest of the services.

## Running Load Tests

To run a load test, navigate to the [APM Java Agent CI](https://apm-ci.elastic.co/job/apm-agent-java/) and look for the `Load Test` pipeline. Tests can be executed with the following parameters:

|Parameter|Description|
|:-------:|:---------|
|`apm_version`|The version of the APM Java Agent to use. After checking out the agent from source control, this is the git tag or ref that will be used.|
|`jvm_version`|The version of the JVM which will be used. This will be used to run the application.|
|`jvm_options`|Extra JVM options, for example `-Xmx512m`.|
|`concurrent_requests`|The number of concurrent, simulated users which will make requests against the test application.|
|`duration`|The duration for the load test. Specify this value using time units: (300s, 20m, 3h, 1h30m, etc.)|
|`agent_config`|To override the defaults of the APM Java Agent config, paste a configuration file.|
|`locustfile`|A Locust file controls the specifics of how load will be applied to the test application, such as which URLs are requested and at what rate. If this option is not specified, then `.ci/load/scripts/locustfile.py` will be used. For more information on writing a locustfile, [please see the documentation](https://docs.locust.io/en/stable/writing-a-locustfile.html).|


After specifying the options, a test run will begin. One can view the progress of the test run by following along in the Jenkins Blue Ocean interface for the given job after it is launched. Typically, it takes several minutes for the machines to be provisioned before tests can begin.

### JVM versions

The list of JVMs that we can use for testing is very large and comes from https://jvm-catalog.elastic.co/, thus JVM version is provided
by ID, you can get a list of supported values using the following command:

```shell
curl -s https://jvm-catalog.elastic.co/jdks/tags/linux,x86_64 | jq '.[] | .id'
```


## Viewing test results

Because the [test application](https://projects.spring.io/spring-petclinic/) is instrumented with [JFR](https://docs.oracle.com/javacomponents/jmc-5-4/jfr-runtime-guide/about.htm#JFRUH170), it is necessary to have a copy of the [JDK Mission Control application](https://www.baeldung.com/java-flight-recorder-monitoring#3-visualize-data) which is distributed with the JDK.

The results of the JFR instrumentation are available after a test run is complete by navigating to Jenkins and viewing the job results. A link to `Artifacts` will bring you to a page which shows the files generated by the test run. Download `src/spring-petclinic/flight.jfr` and then view its contents with JDK MIssion Control to examine the moment-by-moment profile of the application under test.

To view the load generation output, see the console output in Jenkins for the `Load Generation` step.

## Hacking

*Caution*: The following requires access to libraries which are internal to Elastic. Do not attempt to follow these instructions if you are an open-source user
of the APM Java Agent. Instead, contact the core development team with questions.

To set up a local development environment for executing load tests and for developing the test framework, perform the following steps:

1. Checkout the Bandstand application and build a docker container from it tagged as `bandstand`.
2. Checkout the [fork of the APM Pipeline library](https://github.com/cachedout/apm-pipeline-library-1/tree/perf) which contains the necessary modifications to provision a load-testing environment locally.
3. In the `apm-pipeline-library` checkout from step 2, create a soft link from `local/jenkins_home` to the your checkout of the `apm-java-agent` codebase.
4. Start up Jenkins using `make start` from your copy of the APM Pipeline library. Start up a worker as well, using the modified `Vagrantfile` from step 2.
5. Modify the Jenkinsfile to use Bandstand. Set `ORCH_URL` to `10.0.2.2:8000` to use the local instance.
   
To verify this environment, run `docker ps` and ensure you have a copy of `bandstand` running. It may be necessary to manipulate certain variables in the pipeline to avoid looking up certain credentials from the production secret store.