package ru.nsu.some.team.app;

import ru.nsu.some.team.transformer.Extends;

@Extends({X.class})
public class B extends Functions {
    public void bMethod() {
        System.out.println("B from B");
        super.bMethod();
    }

    public void aMethod() {
        System.out.println("A from B");
        super.aMethod();
    }
}
