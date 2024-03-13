package ru.nsu.some.team.transformer;

import java.lang.instrument.Instrumentation;

public class MultipleInheritanceAgent {

    public static void premain(String agentArgs, Instrumentation inst) {

        System.out.println("Hello from premain");

        inst.addTransformer(new MultipleInheritanceTransformer(), true);
    }
}
