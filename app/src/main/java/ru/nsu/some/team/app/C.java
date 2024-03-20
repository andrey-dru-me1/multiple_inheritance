package ru.nsu.some.team.app;

import ru.nsu.some.team.transformer.Extends;

@Extends({A.class, B.class})
public class C implements Functions {
  public void cMethod() {
    System.out.println("C method");
  }

  public void bMethod() {
    System.out.println("B method from C");
    callNext("bMethod");
  }

  public void aMethod() {
    callNext("aMethod");
  }
}
