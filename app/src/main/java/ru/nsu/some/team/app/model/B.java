package ru.nsu.some.team.app.model;

import ru.nsu.some.team.transformer.Extends;

@Extends({A.class})
public class B extends Functions {

    @Override
    public void foo() {
        System.out.println("Foo from B");
        super.foo();
    }

    @Override
    public Integer sum(Integer add) {
        return 1 + super.sum(add);
    }
}
