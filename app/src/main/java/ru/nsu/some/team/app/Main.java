package ru.nsu.some.team.app;

import ru.nsu.some.team.app.model.D;
import ru.nsu.some.team.app.model.Functions;
import ru.nsu.some.team.app.real.Hybrid;
import ru.nsu.some.team.app.real.Vehicle;

public class Main {
    public static void main(String[] args){
        Vehicle hybrid = new Hybrid();
        hybrid.drive();
        System.out.println();

        Functions functions = new D();
        functions.foo();
        System.out.println();

        functions.bar();
        System.out.println();

        functions.baz();
        System.out.println();

        System.out.println("Result: " + functions.sum(5));
    }
}