package ru.nsu.some.team.app;

public interface Functions {
  default void aMethod() {}

  default void bMethod() {}

  default Object callNext(String methodName) {
    return null;
  }
}
