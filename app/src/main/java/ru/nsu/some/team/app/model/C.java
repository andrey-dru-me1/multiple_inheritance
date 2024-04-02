package ru.nsu.some.team.app.model;

import ru.nsu.some.team.transformer.Extends;

@Extends({A.class})
public class C extends Functions {

    @Override
    public void foo() {
        System.out.println("Foo from C");
        super.foo();
    }

    @Override
    public void bar() {
        System.out.println("Bar from C");
        super.bar();
    }

    @Override
    public void baz() {
        System.out.println("Baz from C");
        super.baz();
    }
}
