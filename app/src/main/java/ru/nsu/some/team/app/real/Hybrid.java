package ru.nsu.some.team.app.real;

import ru.nsu.some.team.transformer.Extends;

@Extends({Electric.class, InternalCombustion.class})
public class Hybrid extends Vehicle {
    @Override
    public void drive() {
        super.drive();
    }
}
