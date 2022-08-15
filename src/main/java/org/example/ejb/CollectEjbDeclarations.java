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
package org.example.ejb;

import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.*;
import java.util.stream.Collectors;

public class CollectEjbDeclarations extends XmlVisitor<List<EjbDeclaration>> {

    private static final XPathMatcher SESSION_BEAN_MATCHER = new XPathMatcher("/ejb-jar/enterprise-beans/session");
    private static final XPathMatcher ENTITY_BEAN_MATCHER = new XPathMatcher("/ejb-jar/enterprise-beans/entity");

    @Override
    public Xml visitTag(Xml.Tag tag, List<EjbDeclaration> ejbDeclarations) {
        tag = (Xml.Tag) super.visitTag(tag, ejbDeclarations);
        if (SESSION_BEAN_MATCHER.matches(getCursor()) || ENTITY_BEAN_MATCHER.matches(getCursor())) {
            Optional<String> ejbName = tag.getChildValue("ejb-name");
            if (ejbName.isPresent()) {
                ejbDeclarations.add(new EjbDeclaration(ejbName.get(),
                        getChildValues(Arrays.asList("local-home", "home", "local", "remote", " ejb -class", " prim-key-class"), tag)));
            }
        }
        return tag;
    }

    private List<String> getChildValues(List<String> tagNames, Xml.Tag tag) {
       return tagNames.stream()
               .map(tag::getChildValue)
               .filter(Optional::isPresent)
               .map(Optional::get)
               .collect(Collectors.toList());
    }
}
