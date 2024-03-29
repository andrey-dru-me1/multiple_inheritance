package ru.nsu.some.team.transformer;

import javassist.*;
import javassist.bytecode.Descriptor;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

public class MultipleInheritanceTransformer implements ClassFileTransformer {

    private void addCallNextMethod(CtClass current) throws CannotCompileException {
        System.out.println("Add callNext to " + current.getName());
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
        System.out.println("Add DFS to " + currentClass.getName());
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

    private void addDefaultConstructor(CtClass currentClass) throws CannotCompileException {
        System.out.println("Add default constructor to " + currentClass.getName());

        CtConstructor defaultConstructor = CtNewConstructor.make("public " + currentClass.getSimpleName() + "() {}", currentClass);
        currentClass.addConstructor(defaultConstructor);
    }

    private void addConstructor(CtClass currentClass) throws CannotCompileException, NotFoundException {
        System.out.println("Add DFS to constructor of " + currentClass.getName());

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

    private void addMissingMethods(CtClass currentClass, CtMethod[] methodSet) throws CannotCompileException, ClassHierarchyException, NotFoundException {
        List<String> currentMethods = Arrays.stream(currentClass.getDeclaredMethods())
                .map(CtMethod::getName)
                .toList();

        for (CtMethod m : methodSet) {
            if (!currentMethods.contains(m.getName())) {
                String methodString = String.format(
                        "public void %s() { super.%s(); }",
                        m.getName(), m.getName());
                System.out.println("Generate: " + methodString);
                CtMethod newMethod = CtMethod.make(methodString, currentClass);
                currentClass.addMethod(newMethod);
            }
        }
    }

    private void generateSuperclass(CtClass currentClass, ClassPool classPool, ClassLoader loader, CtMethod[] methodSet) throws CannotCompileException, NotFoundException {
        String superclassName = String.format("%sSuperclass", currentClass.getName());
        System.out.println("Generate superclass " + superclassName);
        CtClass superclass = classPool.makeClass(superclassName);

        addDefaultConstructor(superclass);

        addCallNextMethod(superclass);

        for (CtMethod m : methodSet) {
            String methodString = String.format(
                    "public void %s() { this.callNext(\"%s\"); }",
                    m.getName(), m.getName());
            System.out.println("Generate in superclass: " + methodString);
            CtMethod newMethod = CtMethod.make(methodString, superclass);
            superclass.addMethod(newMethod);
        }

        superclass.setSuperclass(currentClass.getSuperclass());
        currentClass.setSuperclass(superclass);

        addDfsMethod(superclass);
        addConstructor(superclass);

        superclass.toClass(loader, this.getClass().getProtectionDomain());
    }

    private CtMethod[] getMethodSet(CtClass currentClass) throws ClassHierarchyException {
        CtClass superclass;

        try {
            superclass = currentClass.getSuperclass();
        } catch (NotFoundException e) {
            throw new ClassHierarchyException(String.format("Class %s doesn't have root class", currentClass.getName()));
        }

        return superclass.getDeclaredMethods();
    }

    @Override
    public byte[] transform(
            ClassLoader classLoader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classFileBuffer) {
        ClassPool classPool = ClassPool.getDefault();
        String effectiveClassName = className.replaceAll("/", ".");
        try {
            CtClass currentClass = classPool.get(effectiveClassName);

            if (currentClass.hasAnnotation(Extends.class)) {
                System.out.printf("Found class with Extends annotation: %s\n", currentClass.getName());

                CtMethod[] methodSet = getMethodSet(currentClass);

                generateSuperclass(currentClass, classPool, classLoader, methodSet);

                addMissingMethods(currentClass, methodSet);

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
