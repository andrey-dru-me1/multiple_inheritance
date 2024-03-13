package ru.nsu.some.team.transformer;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class MultipleInheritanceTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classFileBuffer)
            throws IllegalClassFormatException {

        byte[] byteCode = classFileBuffer;

        if ("ru.nsu.some.team.app.Main".equals(className.replaceAll("/", "."))) {

            try {
                ClassPool pool = ClassPool.getDefault();
                CtClass ctClass = pool.get("ru.nsu.some.team.app.Main");
                CtMethod myMain = ctClass.getDeclaredMethod("main");
                ctClass.removeMethod(myMain);

                ctClass.addMethod(CtNewMethod.make("public static void main(String[] args) { System.out.println(\"Hello from replaced bytecode\");}", ctClass));

                CtMethod[] methods = ctClass.getDeclaredMethods();

                for (CtMethod method : methods) {
                    System.out.println("!!!!!!! + " + method.getName());
                    if (method.getName().equals("main")) {
                        try {
                            method.insertAfter("System.out.println(\"Logging using Agent\");");
                        } catch (CannotCompileException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    byteCode = ctClass.toBytecode();
                    ctClass.detach();
                    return byteCode;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ctClass.detach();
                return byteCode;
            } catch (NotFoundException e) {
                System.out.println(e.getMessage());
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }

        }
        return byteCode;

//    ClassPool cp = ClassPool.getDefault();
//    String effectiveClassName = className.replaceAll("/", ".");
//    try {
//      CtClass cClass = cp.get(effectiveClassName);
//      if (cClass.hasAnnotation(Extends.class)) {
//        Extends annotation = (Extends) cClass.getAnnotation(Extends.class);
//        for (Class<?> parent : annotation.value()) {
//          loader.loadClass(parent.getName());
//          CtClass parentCClass = cp.getCtClass(parent.getName());
//
//          String fieldName = "parent" + parent.getSimpleName();
//          CtField parentField = CtField.make(String.format("protected %s %s;", parent.getName(), fieldName), cClass);
//
//          CtConstructor ctConstructor = cClass.getConstructor(Descriptor.ofConstructor(new CtClass[] {}));
//          ctConstructor.insertBeforeBody(String.format("this.%s = new %s();", fieldName, parent.getName()));
//
//          for (CtMethod m : parentCClass.getDeclaredMethods()) {
//            CtMethod newMethod = CtMethod.make(String.format("this.%s.%s();", fieldName, m.getName()), cClass);
//            cClass.addMethod(newMethod);
//          }
//        }
//
//        byte[] newByteCode = cClass.toBytecode();
//        cClass.detach();
//        return newByteCode;
//      }
//    } catch (NotFoundException ignore) {
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
        // return classFileBuffer;
    }
}
