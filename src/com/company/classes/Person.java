package com.company.classes;

import com.company.exceptions.InvalidDataException;

public class Person {
    private Long height; //Поле может быть null, Значение поля должно быть больше 0
    private Integer weight; //Поле может быть null, Значение поля должно быть больше 0

    public Person(){
    }

    public Person(Long height, Integer weight) {
        this.height = height;
        this.weight = weight;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) throws InvalidDataException {
        if (height <= 0){
            throw new InvalidDataException("height", "Height must be positive number");
        } else{
            this.height = height;
        }
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) throws InvalidDataException {
        if (weight <= 0){
            throw new InvalidDataException("weight", "weight must be positive number");
        } else{
            this.weight = weight;
        }
    }
}
