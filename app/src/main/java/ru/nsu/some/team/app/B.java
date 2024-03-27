package ru.nsu.some.team.app;

import ru.nsu.some.team.transformer.Extends;

@Extends({X.class})
public class B implements Functions {
    public void bMethod() {
        System.out.println("B from B");
        callNext("bMethod");
    }

    public void aMethod() {
        System.out.println("A from B");
        callNext("aMethod");
    }
}
