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
package org.example;

import org.openrewrite.*;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.maven.MavenParser;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.Comparator;
import java.util.List;

public class CollectEjbEndpoints extends Recipe {

    private static final XPathMatcher SESSION_BEAN_MATCHER = new XPathMatcher("/ejb-jar/enterprise-beans/sesssion");
    private static final XPathMatcher ENTITY_BEAN_MATCHER = new XPathMatcher("/ejb-jar/enterprise-beans/entity");

    @Override
    public String getDisplayName() {
        return "Add missing `@Override` to overriding and implementing methods";
    }


    private class EjbBeanVisitor extends XmlVisitor<ExecutionContext> {

        @Override
        public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
            if (SESSION_BEAN_MATCHER.matches(getCursor())) {

            } else if (ENTITY_BEAN_MATCHER.matches(getCursor())) {

            }
            return tag;
        }
    }
}
