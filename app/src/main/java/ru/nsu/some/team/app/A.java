package ru.nsu.some.team.app;

import ru.nsu.some.team.transformer.Extends;

@Extends({B.class})
public class A {
    public void aMethod() {
        System.out.println("A method");
    }
}
