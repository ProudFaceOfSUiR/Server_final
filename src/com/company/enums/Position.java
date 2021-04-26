package com.company.enums;

public enum Position {
    MANAGER,
    LABORER,
    LEAD_DEVELOPER,
    BAKER,
    MANAGER_OF_CLEANING;

    /**
     * Finds enum from string
     * @param s
     * @return
     */
    public static Position findEnum(String s){
        if (s == null){
            return null;
        }

        s = s.toUpperCase();
        Position position = null;
        for (int i = 0; i < values().length; i++) {
            if (s.equals(Position.values()[i].toString())){
                position = Position.values()[i];
            }
        }
        return position;
    }

    /**
     * Checks if string is similar to enum
     * @param s
     * @return
     */
    public static boolean isEnum(String s){
        s = s.toUpperCase();
        for (int i = 0; i < values().length; i++) {
            if (s.equals(Position.values()[i].toString())){
                return true;
            }
        }
        return false;
    }

    public static String[] getPositions()
    {
        String[] commands = new String[Position.values().length];
        Position[] positionsEnum = values();
        for (int i = 0; i < Position.values().length; i++) {
            commands[i] = positionsEnum[i].toString();
        }
        return commands;
    }
}
