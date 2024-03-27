package ru.nsu.some.team.transformer;

import javassist.CannotCompileException;
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

    private void addCallNextMethod(CtClass current) throws CannotCompileException {
        CtField nextClass = CtField.make("public Object nextObject = null;", current);
        current.addField(nextClass);

        CtMethod newMethod =
                CtMethod.make(
                        """
                                private Object callNext(String methodName) throws NoSuchMethodException {
                                  System.out.println("Next " + this.nextObject);
                                  if (this.nextObject == null) return null;
                                  return nextObject.getClass().getMethod(methodName, new Class[] {}).invoke(this.nextObject, new Object[] {});
                                }
                                """,
                        current);
        current.addMethod(newMethod);
    }

    private void addDfsMethod(CtClass currentClass) throws CannotCompileException {
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
    }

    private void addConstructor(CtClass currentClass) throws CannotCompileException, NotFoundException {
        CtConstructor ctConstructor =
                currentClass.getConstructor(Descriptor.ofConstructor(new CtClass[]{}));
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
                            
                        if (parentObjects.size() > 0) {
                          Object newObject = parentObjects.get(0);
                          this.getClass().getField("nextObject").set(this, newObject);
                          System.out.println("Set next " + this + " " + newObject);
                        }
                        for (int i = 0; i < parentObjects.size() - 1; i++) {
                          Object parent = parentObjects.get(i);
                          Object newObject = parentObjects.get(i + 1);
                          parent.getClass().getField("nextObject").set(parent, newObject);
                          System.out.println("Set next " + parent + " " + newObject);
                        }
                        """;
        ctConstructor.insertBeforeBody(ctorString);
    }

    private void addMissingFields(CtClass currentClass) throws CannotCompileException, ClassHierarchyException, NotFoundException {
        CtClass[] interfaces = currentClass.getInterfaces();

        if (interfaces.length == 0) {
            throw new ClassHierarchyException(String.format("Class %s doesn't have root interface", currentClass.getName()));
        }
        CtClass rootInterface = interfaces[0];
        CtMethod[] interfaceMethods = rootInterface.getDeclaredMethods();

        List<String> currentMethods = Arrays.stream(currentClass.getDeclaredMethods())
                .map(CtMethod::getName)
                .toList();

        for (CtMethod m : interfaceMethods) {
            if (!currentMethods.contains(m.getName())) {
                String methodString = String.format(
                        "public void %s() { this.callNext(\"%s\"); }",
                        m.getName(), m.getName());
                System.out.println("Generate: " + methodString);
                CtMethod newMethod = CtMethod.make(methodString, currentClass);
                currentClass.addMethod(newMethod);
            }
        }
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

                String superclassName = String.format("%sSuperclass", currentClass.getName());
                System.out.println("Generate superclass " + superclassName);
                CtClass superclass = cp.makeClass(superclassName);
                System.out.println(superclassName);
                System.out.println(superclass.getName());
                superclass.toClass(loader);
                //superclass.writeFile();
                //loader.loadClass(superclassName);


                addMissingFields(currentClass);
                addDfsMethod(currentClass);
                addConstructor(currentClass);

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
