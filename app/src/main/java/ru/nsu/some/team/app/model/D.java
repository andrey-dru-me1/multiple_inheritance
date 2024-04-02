package ru.nsu.some.team.app.model;

import ru.nsu.some.team.transformer.Extends;

@Extends({B.class, C.class})
public class D extends Functions {

    @Override
    public void foo() {
        System.out.println("Foo from D");
        super.foo();
    }

    @Override
    public void bar() {
        System.out.println("Bar from D");
        super.bar();
    }
}
