package ru.nsu.fit.some.team.multiple.inheritance;

@Extends({B.class})
public class A {
  public void aMethod() {
    System.out.println("A method");
  }
}
