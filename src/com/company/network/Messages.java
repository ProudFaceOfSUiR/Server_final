package com.company.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

public class Messages implements Serializable {
    private static final long serialVersionUID = 47L;

    private ArrayList<Object> objects = new ArrayList<Object>();

    public void addObject(String str){
        this.objects.add(str);
    }

    public Object getObject(int index){
        return objects.get(index);
    }
}
