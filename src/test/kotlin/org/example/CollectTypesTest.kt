/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.example

import org.junit.jupiter.api.Test
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import java.nio.file.Paths


class CollectTypesTest : RewriteTest {

    override fun defaults(spec: RecipeSpec) {
        spec.recipe(CollectTypes())
    }

    @Test
    fun justJava() = rewriteRun(
        java(
            """
            package com.somecompany.example;
            import java.util.*;

            public class AnExample {
                public int[] aMethod(String param1, List<Date> param2) {
                    return new int[0];
                }
                protected class InnerExample {
                    public void anInnerMethod() {
                    }
                }
            }
            """
        ),
        text(
            null,
            """
                ---declared-types---
                com.somecompany.example.AnExample
                com.somecompany.example.AnExample${'$'}InnerExample
                ---declared-methods---
                com.somecompany.example.AnExample${'$'}InnerExample{name=anInnerMethod,return=void,parameters=[]}
                com.somecompany.example.AnExample{name=aMethod,return=int[],parameters=[java.lang.String,java.util.List<java.util.Date>]}
            """
        ) { spec -> spec.path(Paths.get("types-report.txt")) },
    )

    @Test
    fun ejbJar() = rewriteRun(
        xml(
            """
                <ejb-jar>
                    <enterprise-beans>
                        <session>
                            <ejb-name>com.example.service.user.UserSession.ejb</ejb-name>
                            <local-home>com.example.service.user.UserSessionHome</local-home>
                            <local>com.example.service.user.UserSessionLocal</local>
                            <ejb-class>com.example.service.user.ejb.UserSessionBean</ejb-class>
                            <session-type>Stateless</session-type>
                            <transaction-type>Container</transaction-type>
                        </session>
                        <entity>
                            <ejb-name>com.example.model.user.User.ejb</ejb-name>
                            <local-home>com.example.model.user.UserHome</local-home>
                            <local>com.example.model.user.cmp.User</local>
                            <ejb-class>com.example.model.user.cmp.UserBean</ejb-class>
                            <persistence-type>Container</persistence-type>
                            <prim-key-class>com.example.model.user.cmp.UserPK</prim-key-class>
                            <reentrant>False</reentrant>
                            <cmp-version>2.x</cmp-version>
                            <abstract-schema-name>UserBean</abstract-schema-name>
                            <cmp-field>
                                <field-name>userType</field-name>
                            </cmp-field>
                            <cmp-field>
                                <field-name>userFirst</field-name>
                            </cmp-field>
                            <cmp-field>
                                <field-name>userLastName</field-name>
                            </cmp-field>
                            <cmp-field>
                                <field-name>phone</field-name>
                            </cmp-field>
                            <query>
                                <query-method>
                                    <method-name>findAll</method-name>
                                    <method-params />
                                </query-method>
                                <ejb-ql>select object(a) from UserBean as a</ejb-ql>
                            </query>
                        </entity>
                    </enterprise-beans>
                </ejb-jar>
            """
        ),
        text(
            null,
            """
                ---used-types---
                com.example.model.user.UserHome
                com.example.model.user.cmp.User
                com.example.service.user.UserSessionHome
                com.example.service.user.UserSessionLocal
                ---declared-jndi-names---
                com.example.model.user.User.ejb
                com.example.service.user.UserSession.ejb
              """
        ) { spec -> spec.path(Paths.get("types-report.txt")) },
    )

    @Test
    fun strutsConfiguration() = rewriteRun(
        xml(
            """
                <struts-config>
                    <form-beans>
                        <form-bean name="EmployeeForm" type="org.apache.struts.action.DynaActionForm">
                            <form-property name="token" type="java.lang.String"/>
                        </form-bean>
                
                        <form-bean name="CustomerForm" type="org.apache.struts.action.DynaActionForm">
                            <form-property name="msgId" type="java.lang.String"/>
                            <form-property name="token" type="java.lang.String"/>
                        </form-bean>
                    </form-beans>
                
                    <action-mappings>
                        <action name="MyForm"
                            path="/myForm"
                            type="com.example.action.ExampleForm"
                            parameter="msgId"
                            roles="EnSession">
                        </action>
                    </action-mappings>
                
                    <controller processorClass="com.example.controller.processor.RequestProcessor"
                        nocache="false"
                        className="com.example.controller.RequestProcessing"
                        locale="true">
                        <set-property value="true" property="config"/>
                    </controller>
                </struts-config>
            """
        ),
        text(
            null,
            """
                ---used-types---
                com.example.action.ExampleForm
                com.example.controller.RequestProcessing
                com.example.controller.processor.RequestProcessor
              """
        ) { spec -> spec.path(Paths.get("types-report.txt")) },
    )

    @Test
    fun webConfiguration() = rewriteRun(
        xml(
            """
                <web-app>
                    <context-param>
                        <param-name>application</param-name>
                        <param-value>exampleApplication</param-value>
                    </context-param>
                
                    <filter>
                        <filter-name>CookieFilter</filter-name>
                        <filter-class>com.example.servletfilter.CookieFilter</filter-class>
                    </filter>
                
                    <filter-mapping>
                        <filter-name>CookieSecureFilter</filter-name>
                        <url-pattern>/*</url-pattern>
                    </filter-mapping>
                
                    <listener>
                        <listener-class>com.example.SessionListener</listener-class>
                    </listener>
                
                    <servlet>
                        <servlet-name>ServletStartupCollecteurStatistique</servlet-name>
                        <servlet-class>com.example.ExampleServlet</servlet-class>
                        <load-on-startup>0</load-on-startup>
                    </servlet>
                
                    <ejb-ref>
                        <ejb-ref-name>ejb/com.example.pattern.ServiceActivator.ejb</ejb-ref-name>
                        <ejb-ref-type>Session</ejb-ref-type>
                        <home>com.example.pattern.ejb.EJBServiceActivatorHome</home>
                        <remote>com.example.pattern.ejb.EJBServiceActivatorRemote</remote>
                    </ejb-ref>
                
                </web-app>
            """
        ),
        text(
            null,
            """
                ---used-types---
                com.example.ExampleServlet
                com.example.SessionListener
                com.example.pattern.ejb.EJBServiceActivatorHome
                com.example.pattern.ejb.EJBServiceActivatorRemote
                com.example.servletfilter.CookieFilter
                ---used-jndi-names---
                ejb/com.example.pattern.ServiceActivator.ejb
              """
        ) { spec -> spec.path(Paths.get("types-report.txt")) },
    )

    @Test
    fun allConfiguration() = rewriteRun(
        java(
            """
            package com.somecompany.example;
            import java.util.*;

            public class AnExample {
                public int[] aMethod(String param1, List<Date> param2) {
                    return new int[0];
                }
                protected class InnerExample {
                    public void anInnerMethod() {
                    }
                }
            }
            """
        ),
        xml(
            """
                <ejb-jar>
                    <enterprise-beans>
                        <session>
                            <ejb-name>com.example.service.user.UserSession.ejb</ejb-name>
                            <local-home>com.example.service.user.UserSessionHome</local-home>
                            <local>com.example.service.user.UserSessionLocal</local>
                            <ejb-class>com.example.service.user.ejb.UserSessionBean</ejb-class>
                            <session-type>Stateless</session-type>
                            <transaction-type>Container</transaction-type>
                        </session>
                        <entity>
                            <ejb-name>com.example.model.user.User.ejb</ejb-name>
                            <local-home>com.example.model.user.UserHome</local-home>
                            <local>com.example.model.user.cmp.User</local>
                            <ejb-class>com.example.model.user.cmp.UserBean</ejb-class>
                            <persistence-type>Container</persistence-type>
                            <prim-key-class>com.example.model.user.cmp.UserPK</prim-key-class>
                            <reentrant>False</reentrant>
                            <cmp-version>2.x</cmp-version>
                            <abstract-schema-name>UserBean</abstract-schema-name>
                            <cmp-field>
                                <field-name>userType</field-name>
                            </cmp-field>
                            <cmp-field>
                                <field-name>userFirst</field-name>
                            </cmp-field>
                            <cmp-field>
                                <field-name>userLastName</field-name>
                            </cmp-field>
                            <cmp-field>
                                <field-name>phone</field-name>
                            </cmp-field>
                            <query>
                                <query-method>
                                    <method-name>findAll</method-name>
                                    <method-params />
                                </query-method>
                                <ejb-ql>select object(a) from UserBean as a</ejb-ql>
                            </query>
                        </entity>
                    </enterprise-beans>
                </ejb-jar>
            """
        ),
        xml(
            """
                <struts-config>
                    <form-beans>
                        <form-bean name="EmployeeForm" type="org.apache.struts.action.DynaActionForm">
                            <form-property name="token" type="java.lang.String"/>
                        </form-bean>
                
                        <form-bean name="CustomerForm" type="org.apache.struts.action.DynaActionForm">
                            <form-property name="msgId" type="java.lang.String"/>
                            <form-property name="token" type="java.lang.String"/>
                        </form-bean>
                    </form-beans>
                
                    <action-mappings>
                        <action name="MyForm"
                            path="/myForm"
                            type="com.example.action.ExampleForm"
                            parameter="msgId"
                            roles="EnSession">
                        </action>
                    </action-mappings>
                
                    <controller processorClass="com.example.controller.processor.RequestProcessor"
                        nocache="false"
                        className="com.example.controller.RequestProcessing"
                        locale="true">
                        <set-property value="true" property="config"/>
                    </controller>
                </struts-config>
            """),
        xml(
            """
                <web-app>
                    <context-param>
                        <param-name>application</param-name>
                        <param-value>exampleApplication</param-value>
                    </context-param>
                
                    <filter>
                        <filter-name>CookieFilter</filter-name>
                        <filter-class>com.example.servletfilter.CookieFilter</filter-class>
                    </filter>
                
                    <filter-mapping>
                        <filter-name>CookieSecureFilter</filter-name>
                        <url-pattern>/*</url-pattern>
                    </filter-mapping>
                
                    <listener>
                        <listener-class>com.example.SessionListener</listener-class>
                    </listener>
                
                    <servlet>
                        <servlet-name>ServletStartupCollecteurStatistique</servlet-name>
                        <servlet-class>com.example.ExampleServlet</servlet-class>
                        <load-on-startup>0</load-on-startup>
                    </servlet>
                
                    <ejb-ref>
                        <ejb-ref-name>ejb/com.example.pattern.ServiceActivator.ejb</ejb-ref-name>
                        <ejb-ref-type>Session</ejb-ref-type>
                        <home>com.example.pattern.ejb.EJBServiceActivatorHome</home>
                        <remote>com.example.pattern.ejb.EJBServiceActivatorRemote</remote>
                    </ejb-ref>
                
                </web-app>
            """
        ),
        text(
            null,
            """
                ---declared-types---
                com.somecompany.example.AnExample
                com.somecompany.example.AnExample${'$'}InnerExample
                ---declared-methods---
                com.somecompany.example.AnExample${'$'}InnerExample{name=anInnerMethod,return=void,parameters=[]}
                com.somecompany.example.AnExample{name=aMethod,return=int[],parameters=[java.lang.String,java.util.List<java.util.Date>]}
                ---used-types---
                com.example.ExampleServlet
                com.example.SessionListener
                com.example.action.ExampleForm
                com.example.controller.RequestProcessing
                com.example.controller.processor.RequestProcessor
                com.example.model.user.UserHome
                com.example.model.user.cmp.User
                com.example.pattern.ejb.EJBServiceActivatorHome
                com.example.pattern.ejb.EJBServiceActivatorRemote
                com.example.service.user.UserSessionHome
                com.example.service.user.UserSessionLocal
                com.example.servletfilter.CookieFilter
                ---declared-jndi-names---
                com.example.model.user.User.ejb
                com.example.service.user.UserSession.ejb
                ---used-jndi-names---
                ejb/com.example.pattern.ServiceActivator.ejb
              """
        ) { spec -> spec.path(Paths.get("types-report.txt")) }
    )

}
