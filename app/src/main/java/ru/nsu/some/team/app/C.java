package ru.nsu.some.team.app;

import ru.nsu.some.team.transformer.Extends;

@Extends({A.class, B.class})
public class C extends Functions {
    public void bMethod() {
        System.out.println("B from C");
        super.bMethod();
    }

    public void aMethod() {
        System.out.println("A from C");
        super.aMethod();
    }
}
