package org.example;

import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.Optional;
import java.util.Set;

public class CollectJndiNamesFromWebXml extends XmlVisitor<Set<String>> {
    private static final XPathMatcher EJB_REF_MATCHER = new XPathMatcher("/web-app/ejb-ref");

    @Override
    public Xml visitTag(Xml.Tag tag, Set<String> jndiNames) {
        tag = (Xml.Tag) super.visitTag(tag, jndiNames);
        if (EJB_REF_MATCHER.matches(getCursor())) {
            Optional<String> type = tag.getChildValue("ejb-ref-name");
            type.ifPresent(jndiNames::add);
        }
        return tag;
    }
}
