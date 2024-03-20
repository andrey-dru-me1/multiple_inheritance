package ru.nsu.some.team.app;

import ru.nsu.some.team.transformer.Extends;

@Extends({B.class})
public class A implements Functions {
    public void aMethod() {
        System.out.println("A method");
    }

    public void bMethod() {
        System.out.println("B method from A");
        callNext("bMethod");
    }
}
