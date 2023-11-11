package com.example.input;

public class Child extends Base {
    public Base base;
    @Override
    public void f(String name) {
        System.out.println("Child: " + name);
    }

    private String getFullname() {
        return "1";
    }

    private void setFullname(String x) {

    }

    private int getAge() {
        return 0;
    }

    private void setName(String name) {

    }
}
