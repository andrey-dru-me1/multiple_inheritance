package ru.nsu.some.team.app;

import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        A a = new A();
        a.aMethod();
        a.getClass().getMethod("bMethod").invoke(a);
        // a.bMethod();
    }
}