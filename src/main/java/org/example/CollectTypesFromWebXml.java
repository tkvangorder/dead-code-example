package org.example;

import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CollectTypesFromWebXml extends XmlVisitor<Set<String>> {

    private static final XPathMatcher FILTER_MATCHER = new XPathMatcher("/web-app/filter");
    private static final XPathMatcher LISTENER_MATCHER = new XPathMatcher("/web-app/listener");
    private static final XPathMatcher SERVLET_MATCHER = new XPathMatcher("/web-app/servlet");

    private static final XPathMatcher EJB_REF_MATCHER = new XPathMatcher("/web-app/ejb-ref");

    @Override
    public Xml visitTag(Xml.Tag tag, Set<String> usedTypes) {
        tag = (Xml.Tag) super.visitTag(tag, usedTypes);
        if (FILTER_MATCHER.matches(getCursor())) {
            Optional<String> type = tag.getChildValue("filter-class");
            type.ifPresent(usedTypes::add);
        } else if (LISTENER_MATCHER.matches(getCursor())) {
            Optional<String> type = tag.getChildValue("listener-class");
            type.ifPresent(usedTypes::add);
        } else if (SERVLET_MATCHER.matches(getCursor())) {
            Optional<String> type = tag.getChildValue("servlet-class");
            type.ifPresent(usedTypes::add);
        } else if (EJB_REF_MATCHER.matches(getCursor())) {
            usedTypes.addAll(getChildValues(Arrays.asList("home", "remote"), tag));
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
