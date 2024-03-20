package ru.nsu.some.team.app;

import ru.nsu.some.team.transformer.Extends;

@Extends()
public class B implements Functions {
  public void bMethod() {
    System.out.println("B method from B");
  }

  public void aMethod() {
    callNext("aMethod");
  }
}
