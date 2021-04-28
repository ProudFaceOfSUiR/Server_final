package com.company.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

public class Messages implements Serializable {
    private static final long serialVersionUID = 47L;
    private ArrayList<Object> objects;

    public Messages() {
        this.objects = new ArrayList<Object>();
    }

    public void addObject(Object o){
        this.objects.add(o);
    }

    public Object getObject(int index){
        return objects.get(index);
    }

    public void clear(){
        this.objects.clear();
    }
}
