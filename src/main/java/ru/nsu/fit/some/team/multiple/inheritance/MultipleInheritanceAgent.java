package ru.nsu.fit.some.team.multiple.inheritance;

import java.lang.instrument.Instrumentation;

public class MultipleInheritanceAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new MultipleInheritanceTransformer(), true);
    }

    public static void agentMain(String agentArgs, Instrumentation inst) {

    }

}
