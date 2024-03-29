package ru.nsu.some.team.app;

public class X extends Functions {
    public void aMethod() {
        System.out.println("A from X");
    }

    public void bMethod() {
        System.out.println("B from X");
    }

    public Integer sum() {
        return 1;
    }

    public Double returnLast(Double val) {
        return val * 2;
    }
}
