package ru.nsu.some.team.app.model;

public class A extends Functions {
    @Override
    public void foo() {
        System.out.println("Foo from A");
    }

    @Override
    public void bar() {
        System.out.println("Bar from A");
    }

    @Override
    public Integer sum(Integer add) {
        return add;
    }
}
