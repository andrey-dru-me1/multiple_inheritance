package ru.nsu.some.team.app.real;

import ru.nsu.some.team.transformer.Extends;

@Extends({Vehicle.class})
public class InternalCombustion extends Vehicle {
    @Override
    public void drive() {
        System.out.println("Drive with gasoline engine");
        super.drive();
    }
}
