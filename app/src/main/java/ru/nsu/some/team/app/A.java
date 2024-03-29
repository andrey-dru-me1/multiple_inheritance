package ru.nsu.some.team.app;

import ru.nsu.some.team.transformer.Extends;

@Extends({X.class})
public class A extends Functions {
    public void aMethod() {
        System.out.println("A from A");
        super.aMethod();
    }

//    public void bMethod() {
//        System.out.println("B from A");
//        callNext("bMethod");
//    }
}
