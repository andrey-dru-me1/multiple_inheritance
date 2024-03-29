package ru.nsu.some.team.app;

public class Main {
    public static void main(String[] args){
        C c = new C();
        c.aMethod();
        c.bMethod();
        System.out.println(c.sum());
        System.out.println(c.returnLast(5.6));
    }
}