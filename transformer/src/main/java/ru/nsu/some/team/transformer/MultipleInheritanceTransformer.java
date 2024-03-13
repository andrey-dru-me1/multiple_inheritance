package ru.nsu.some.team.transformer;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

public class MultipleInheritanceTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classFileBuffer) {
        ClassPool cp = ClassPool.getDefault();
        String effectiveClassName = className.replaceAll("/", ".");
        try {
            CtClass currentClass = cp.get(effectiveClassName);

            if (currentClass.hasAnnotation(Extends.class)) {
                Extends annotation = (Extends) currentClass.getAnnotation(Extends.class);

                System.out.printf("Found class with Extends annotation: %s\n", currentClass.getName());

                for (Class<?> parent : annotation.value()) {
                    System.out.printf("Processing parent %s\n", parent.getName());

                    loader.loadClass(parent.getName());
                    CtClass parentClass = cp.getCtClass(parent.getName());

                    String fieldName = "parent" + parent.getSimpleName();
                    String parentFieldString = String.format("protected %s %s;", parent.getName(), fieldName);
                    CtField parentField = CtField.make(parentFieldString, currentClass);
                    currentClass.addField(parentField);
                    System.out.println("Generate: " + parentFieldString);

                    CtConstructor ctConstructor =
                            currentClass.getConstructor(Descriptor.ofConstructor(new CtClass[]{}));
                    String ctorString = String.format("this.%s = new %s();", fieldName, parent.getName());
                    System.out.println("Generate: " + ctorString);
                    ctConstructor.insertBeforeBody(ctorString);

                    List<String> currentMethods = Arrays.stream(currentClass.getDeclaredMethods())
                            .map(CtMethod::getName)
                            .toList();

                    for (CtMethod m : parentClass.getDeclaredMethods()) {
                        if (!currentMethods.contains(m.getName())) {
                            String methodString = String.format(
                                    "protected void %s() { this.%s.%s(); }",
                                    m.getName(), fieldName, m.getName());
                            System.out.println("Generate: " + methodString);
                            CtMethod newMethod = CtMethod.make(methodString, currentClass);
                            currentClass.addMethod(newMethod);
                        }
                    }
                }

                byte[] newByteCode = currentClass.toBytecode();
                currentClass.detach();
                return newByteCode;
            }
        } catch (NotFoundException ignore) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classFileBuffer;
    }
}
