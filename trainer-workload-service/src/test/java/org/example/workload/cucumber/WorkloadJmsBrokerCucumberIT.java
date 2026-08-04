package org.example.workload.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/workload_jms_integration.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "org.example.workload.jmscucumber,org.example.workload.cucumber.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/workload-jms-broker.html, json:target/cucumber-reports/workload-jms-broker.json")
public class WorkloadJmsBrokerCucumberIT {
}