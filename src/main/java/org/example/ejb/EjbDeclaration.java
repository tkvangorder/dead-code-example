package org.example.ejb;

import lombok.Value;

import java.util.List;

@Value
public class EjbDeclaration {
    String jndiName;
    List<String> fullyQualifiedNames;

}
