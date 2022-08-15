package org.example;

import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.*;

public class CollectTypesFromStrutsXml extends XmlVisitor<Set<String>> {

    private static final XPathMatcher ACTION_MATCHER = new XPathMatcher("/struts-config/action-mappings/action");
    private static final XPathMatcher CONTROLLER_MATCHER = new XPathMatcher("/struts-config/controller");

    @Override
    public Xml visitTag(Xml.Tag tag, Set<String> usedTypes) {
        tag = (Xml.Tag) super.visitTag(tag, usedTypes);
        if (ACTION_MATCHER.matches(getCursor())) {
            for (Xml.Attribute attribute : tag.getAttributes()) {
                if ("type".equals(attribute.getKeyAsString())) {
                    usedTypes.add(attribute.getValueAsString());
                }
            }
        } else if (CONTROLLER_MATCHER.matches(getCursor())) {
            for (Xml.Attribute attribute : tag.getAttributes()) {
                if ("processorClass".equals(attribute.getKeyAsString()) || "className".equals(attribute.getKeyAsString())) {
                    usedTypes.add(attribute.getValueAsString());
                }
            }
        }
        return tag;
    }
}
