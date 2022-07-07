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
package co.elastic.apm.agent.jaxws;

import org.junit.jupiter.api.BeforeEach;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

class JaxWsTransactionNameInstrumentationTest extends AbstractJaxWsInstrumentationTest {

    @BeforeEach
    void setUp() {
        helloWorldService = new HelloWorldServiceImpl();
    }

    @SOAPBinding(style = SOAPBinding.Style.RPC)
    @WebService(targetNamespace = "elastic")
    public interface HelloWorldService extends BaseHelloWorldService {
        @Override
        @WebMethod
        String sayHello();
    }

    @WebService(serviceName = "HelloWorldService", portName = "HelloWorld", name = "HelloWorld",
        endpointInterface = "co.elastic.apm.agent.jaxws.JaxWsTransactionNameInstrumentationTest.HelloWorldService",
        targetNamespace = "elastic")
    public static class HelloWorldServiceImpl implements HelloWorldService {
        @Override
        public String sayHello() {
            return "Hello World";
        }
    }

}