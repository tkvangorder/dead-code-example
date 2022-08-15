package org.example;

import org.example.ejb.CollectEjbDeclarations;
import org.example.ejb.EjbDeclaration;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.internal.TypesInUse;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.text.CreateTextFile;
import org.openrewrite.text.PlainText;
import org.openrewrite.xml.tree.Xml;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CollectTypes extends Recipe {
    @Override
    public boolean causesAnotherCycle() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return "This recipe will collect declared and used types for Java-based projects";
    }

    @Override
    protected List<SourceFile> visit(List<SourceFile> before, ExecutionContext ctx) {

        SortedSet<String> declaredTypes = new TreeSet<>();
        SortedSet<String> usedTypes = new TreeSet<>();
        SortedSet<String> declaredMethods = new TreeSet<>();
        SortedSet<String> usedMethods = new TreeSet<>();
        SortedSet<String> declaredJndiNames = new TreeSet<>();
        SortedSet<String> usedJndiNames = new TreeSet<>();

        List<EjbDeclaration> ejbDeclarations = new ArrayList<>();

        CollectClassDeclarations collectClassDeclarations = new CollectClassDeclarations();
        CollectEjbDeclarations collectEjbDeclarations = new CollectEjbDeclarations();
        CollectTypesFromWebXml collectTypesFromWebXml = new CollectTypesFromWebXml();
        CollectJndiNamesFromWebXml collectJndiFromWebXml = new CollectJndiNamesFromWebXml();
        CollectTypesFromStrutsXml collectTypesFromStrutsXml = new CollectTypesFromStrutsXml();

        for (SourceFile s : before) {
            if (s instanceof JavaSourceFile) {
                TypesInUse types = ((JavaSourceFile) s).getTypesInUse();
                usedTypes.addAll(types.getTypesInUse().stream().map(Object::toString).filter(n -> n.startsWith("com.somecompany")).collect(Collectors.toSet()));
                usedMethods.addAll(types.getUsedMethods().stream().map(Object::toString).filter(n -> n.startsWith("com.somecompany")).collect(Collectors.toSet()));
                declaredMethods.addAll(types.getDeclaredMethods().stream().map(Object::toString).filter(n -> n.startsWith("com.somecompany")).collect(Collectors.toSet()));
                collectClassDeclarations.visit(s, declaredTypes);
            } else if (s instanceof Xml.Document) {
                collectEjbDeclarations.visit(s, ejbDeclarations);
                collectTypesFromWebXml.visit(s, usedTypes);
                collectTypesFromStrutsXml.visit(s, usedTypes);
                collectJndiFromWebXml.visit(s, usedJndiNames);
            }
        }

        for (EjbDeclaration declaration : ejbDeclarations) {
            declaredJndiNames.add(declaration.getJndiName());
            usedTypes.addAll(declaration.getFullyQualifiedNames());
        }
        StringBuffer report = new StringBuffer();

        if (!declaredTypes.isEmpty()) {
            report.append("---declared-types---\n");
            declaredTypes.forEach(t -> report.append(t).append("\n"));
        }
        if (!declaredMethods.isEmpty()) {
            report.append("---declared-methods---\n");
            declaredMethods.forEach(t -> report.append(t).append("\n"));
        }
        if (!usedTypes.isEmpty()) {
            report.append("---used-types---\n");
            usedTypes.forEach(t -> report.append(t).append("\n"));
        }
        if (!usedMethods.isEmpty()) {
            report.append("---used-methods---\n");
            usedMethods.forEach(t -> report.append(t).append("\n"));
        }
        if (!declaredJndiNames.isEmpty()) {
            report.append("---declared-jndi-names---\n");
            declaredJndiNames.forEach(t -> report.append(t).append("\n"));
        }
        if (!usedJndiNames.isEmpty()) {
            report.append("---used-jndi-names---\n");
            usedJndiNames.forEach(t -> report.append(t).append("\n"));
        }

        if (report.length() > 0) {
            return addFile(report.toString(), "types-report.txt", before);
        }
        return before;
    }

    private static class CollectClassDeclarations extends JavaIsoVisitor<Set<String>> {
        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, Set<String> declaredTypes) {
            if (classDecl.getType() != null) {
                declaredTypes.add(classDecl.getType().toString());
            }
            return super.visitClassDeclaration(classDecl, declaredTypes);
        }
    }

    private static List<SourceFile> addFile(String content, String relativePath, List<SourceFile> sourceFiles) {
        Path contentPath = Paths.get(relativePath);
        for (SourceFile s : sourceFiles) {
            if (s.getSourcePath().equals(contentPath)) {
                return sourceFiles;
            }
        }
        PlainText reportFile = CreateTextFile.createNewFile(relativePath, content);
        return ListUtils.concat(sourceFiles, reportFile);
    }
}
