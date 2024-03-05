package ru.nsu.fit.some.team.multiple.inheritance;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;

public class MultipleInheritanceTransformer implements ClassFileTransformer {
  @Override
  public byte[] transform(
      ClassLoader loader,
      String className,
      Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain,
      byte[] classFileBuffer)
      throws IllegalClassFormatException {

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
    return classFileBuffer;
  }
}
