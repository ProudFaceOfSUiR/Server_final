package com.company.classes;

import com.company.exceptions.InvalidDataException;

import java.io.Serializable;

public class Coordinates implements Serializable {
    private long x; //Максимальное значение поля: 768
    private Integer y; //Поле не может быть null

    public Coordinates(long x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public long getX() {
        return x;
    }

    public void setX(long x) throws InvalidDataException {
        if (x > 768){
            throw new InvalidDataException("x", "X cannot be bigger than 768");
        } else{
            this.x = x;
        }
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) throws InvalidDataException {
        if (y == null){
            throw new InvalidDataException("y", "Y cannot be null");
        } else {
            this.y = y;
        }
    }
}
