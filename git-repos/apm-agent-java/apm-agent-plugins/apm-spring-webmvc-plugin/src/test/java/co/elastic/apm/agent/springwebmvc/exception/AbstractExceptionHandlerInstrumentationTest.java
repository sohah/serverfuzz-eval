/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.agent.springwebmvc.exception;

import co.elastic.apm.agent.MockReporter;
import co.elastic.apm.agent.MockTracer;
import co.elastic.apm.agent.bci.ElasticApmAgent;
import co.elastic.apm.agent.configuration.SpyConfiguration;
import co.elastic.apm.agent.impl.ElasticApmTracer;
import co.elastic.apm.agent.servlet.JavaxServletInstrumentation;
import co.elastic.apm.agent.springwebmvc.ExceptionHandlerInstrumentation;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.stagemonitor.configuration.ConfigurationRegistry;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(value = SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@TestConfiguration
public abstract class AbstractExceptionHandlerInstrumentationTest {

    protected static MockReporter reporter;
    protected static ElasticApmTracer tracer;
    protected static ConfigurationRegistry config;
    protected MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    @BeforeClass
    @BeforeAll
    public static void setUpAll() {
        MockTracer.MockInstrumentationSetup mockInstrumentationSetup = MockTracer.createMockInstrumentationSetup();
        reporter = mockInstrumentationSetup.getReporter();
        tracer = mockInstrumentationSetup.getTracer();
        config = mockInstrumentationSetup.getConfig();
        ElasticApmAgent.initInstrumentation(tracer, ByteBuddyAgent.install(),
            Arrays.asList(new JavaxServletInstrumentation(), new ExceptionHandlerInstrumentation()));
    }

    @AfterClass
    @AfterAll
    public static void afterAll() {
        ElasticApmAgent.reset();
    }

    @Before
    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        reporter.reset();
        SpyConfiguration.reset(config);
    }

    protected void assertExceptionCapture(Class exceptionClazz, MockHttpServletResponse response, int statusCode, String responseContent, String exceptionMessage) throws UnsupportedEncodingException {
        assertEquals(1, reporter.getTransactions().size());
        assertEquals(0, reporter.getSpans().size());
        assertEquals(1, reporter.getErrors().size());
        assertEquals(exceptionMessage, reporter.getErrors().get(0).getException().getMessage());
        assertEquals(exceptionClazz, reporter.getErrors().get(0).getException().getClass());
        assertEquals(statusCode, response.getStatus());
        assertEquals(statusCode, reporter.getFirstTransaction().getContext().getResponse().getStatusCode());
        assertEquals(responseContent, response.getContentAsString());
    }

}
