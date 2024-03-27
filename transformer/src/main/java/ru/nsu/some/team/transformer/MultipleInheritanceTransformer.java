package ru.nsu.some.team.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;

public class MultipleInheritanceTransformer implements ClassFileTransformer {

  private void addCallNextMethod(CtClass current) throws CannotCompileException {
    CtField nextClass = CtField.make("public Object nextObject = null;", current);
    current.addField(nextClass);

    CtMethod newMethod =
        CtMethod.make(
            """
private Object callNext(String methodName) throws NoSuchMethodException {
  if (this.nextObject == null) return null;
  return nextObject.getClass().getMethod(methodName, new Class[] {}).invoke(this.nextObject, new Object[] {});
}
""",
            current);
    current.addMethod(newMethod);
  }

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
        addCallNextMethod(currentClass);

        System.out.printf("Found class with Extends annotation: %s\n", currentClass.getName());

        CtMethod dfsMethod =
            CtMethod.make(
                """
public java.util.List dfs(java.util.Set visited, java.util.Set parents) {
    java.util.List result = new java.util.ArrayList();
    if (parents.size() == 0) return result;

    ClassLoader loader = this.getClass().getClassLoader();
    java.util.Iterator iter = parents.iterator();

    Class parentClass;
    do {
      parentClass = (Class) iter.next();
      if (visited.contains(parentClass)) continue;
      visited.add(parentClass);

      loader.loadClass(parentClass.getName());

      if (parentClass.isAnnotationPresent(ru.nsu.some.team.transformer.Extends.class)) {
        ru.nsu.some.team.transformer.Extends annotation = (ru.nsu.some.team.transformer.Extends)
                parentClass.getAnnotation(ru.nsu.some.team.transformer.Extends.class);

        java.util.Set grandParents = new java.util.HashSet(java.util.Arrays.asList(annotation.value()));
        java.util.List subResult = this.getClass().getMethod("dfs", new Class[] {java.util.Set.class, java.util.Set.class}).invoke(this, new Object[] {visited, grandParents});
        result.addAll(subResult);
      }
      result.add(parentClass);
    } while (iter.hasNext());
    return result;
  }
""",
                currentClass);
        currentClass.addMethod(dfsMethod);

        CtConstructor ctConstructor =
            currentClass.getConstructor(Descriptor.ofConstructor(new CtClass[] {}));
        String ctorString =
            """
java.util.List parents = this.getClass().getMethod("dfs", new Class[] {java.util.Set.class, java.util.Set.class}).invoke(this, new Object[] {
            new java.util.HashSet(),
            new java.util.HashSet(
                  java.util.Arrays.asList(((ru.nsu.some.team.transformer.Extends) this.getClass().getAnnotation(ru.nsu.some.team.transformer.Extends.class)).value())
            )
      }
);
java.util.Collections.reverse(parents);
System.out.println("Parents of " + this.getClass().getSimpleName() + " class: " + parents);

ClassLoader loader = this.getClass().getClassLoader();
java.util.List parentObjects = new java.util.ArrayList(parents.size());
for (int i = 0; i < parents.size(); i++) {
  Class parentClass = (Class) parents.get(i);
  loader.loadClass(parentClass.getName().replaceAll("/", "."));
  parentObjects.add(parentClass.getConstructor(new Class[] {}).newInstance(new Object[] {}));
}

if (parentObjects.size() > 0)
  this.getClass().getField("nextObject").set(this, parentObjects.remove(0));
for (int i = 0; i < parentObjects.size() - 1; i++) {
  Object parent = parentObjects.get(i);
  parent.getClass().getField("nextObject").set(parent, parentObjects.get(i + 1));
}
""";
        ctConstructor.insertBeforeBody(ctorString);

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
