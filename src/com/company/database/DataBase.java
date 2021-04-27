package com.company.database;


import com.company.classes.Worker;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

import com.company.enums.Commands;
import com.company.enums.Position;
import com.company.exceptions.OperationCanceledException;
import com.company.exceptions.UnknownCommandException;

public class DataBase implements Serializable {

    private LinkedList<Worker> database;
    private Scanner terminal;
    private String scriptName;
    private int recursionCounter;

    //check booleans
    private boolean isInitialized;

    private ZonedDateTime initializationTime;

    public DataBase(){}

    //public methods

    /**
     * Initializing database (like constructor), but without a file (if it's not given)
     */
    public void initialize(){
        //initializing variables
        this.database = new LinkedList<>();
        this.terminal = new Scanner(System.in);
        this.initializationTime = ZonedDateTime.now();
        this.recursionCounter = 0;
        this.scriptName = "";
        this.isInitialized = true;

        System.out.println("Database has been initialized");
        System.out.println("------------------------------------");
    }

    public void setDatabase(LinkedList<Worker> database){
        this.database = database;
    }

    //protected methods

    /**
     * Returns index in database by id
     * @param id
     * @return
     */
    public int returnIndexById(long id){
        int index = -1;
        for (int i = 0; i < database.size(); i++) {
            if (database.get(i).getId() == id){
                index = i;
                break;
            }
        }
        return index;
    }

    public Worker getWorkerByIndex(int index){
        return this.database.get(index);
    }

    //terminal commands

    public String help(){
        StringBuilder sb = new StringBuilder();

        sb.append("Commands: ");sb.append("\n");
        for (int i = 0; i < Commands.values().length - 1; i++) {
            sb.append(" " + Commands.getCommandsWithDescriptions()[i]);sb.append("\n");
        }

        return sb.toString();
    }

    public String add(Worker worker) {
        //adding to database
        this.database.add(worker);
        return "New worker was successfully added!";
    }

    public String show(){

        //checking if database is empty
        if (database.isEmpty()){
            return "Database is empty";
        }

        StringBuilder stringBuilder = new StringBuilder();

        List<List<String>> rows = new ArrayList<>();
        List<String> headers = Arrays.asList("Name", "id", "Salary", "Position", "Personality", "Coordinates", "Start Date", "End Date");
        rows.add(headers);
        StringBuilder coord = new StringBuilder();
        ArrayList<String> sb = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Worker worker : database) {
            sb.add(worker.getName());sb.add(String.valueOf(worker.getId()));sb.add(String.valueOf(worker.getSalary()));

            //adding position
            if (worker.getPosition() != null){
                sb.add(worker.getPosition().toString());
            } else {
                sb.add("null");
            }

            //adding personality
            if (worker.getPerson() != null) {
                sb.add(worker.getPerson().getHeight() + ", " + worker.getPerson().getWeight());
            } else {
                sb.add("null");
            }

            coord.append(worker.getCoordinates().getX()).append(", ").append(worker.getCoordinates().getY());
            sb.add(coord.toString());
            coord.delete(0, coord.length());

            sb.add(worker.getStartDate().format(formatter));
            if (worker.getEndDate() != null){
                sb.add(worker.getEndDate().format(formatter));
            } else {
                sb.add("null");
            }

            rows.add((List<String>) sb.clone());
            sb.clear();
        }
        stringBuilder.append(Terminal.formatAsTable(rows));

        return stringBuilder.toString();
    }

    public String info(){
        StringBuilder sb = new StringBuilder();

        sb.append(("------------------------------------")); sb.append("\n");
        sb.append(("Type: Linked List")); sb.append("\n");
        sb.append(("Initialization date: " + initializationTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM)))); sb.append("\n");
        sb.append(("Number of Workers: " + this.database.size())); sb.append("\n");
        sb.append(("------------------------------------")); sb.append("\n");

        return sb.toString();
    }

    public void clear(){
        //asking if user really wants to clear the database
        try {
            if ( Terminal.binaryChoice("clear the database") ){
                database.clear();
                System.out.println("The database was successfully cleared");
            } else {
                System.out.println("Operation cancelled");
            }
        } catch (OperationCanceledException e) {
            //catch EOF
            System.out.println(e.getMessage());
        }
    }

    public void save(){
        //input file name
        System.out.print("Please, type the name of a new file: ");
        String newFilename;
        try {
            newFilename = Terminal.removeSpaces(Terminal.repeatInputAndExpectRegex("filename", "\\s*\\w+\\s*")) + ".xml";
        } catch (OperationCanceledException e) {
            System.out.println(e.getMessage());
            return;
        }

        //checking if user wants to overwrite existing file
        //if not - throw exception == canceling
        try {
            if (FileParser.overWriteFile(newFilename)){
                FileParser.dataBasetoXML(FileParser.dataBaseToString(this.database), newFilename);
            }
        } catch (OperationCanceledException e) {
            System.out.println(e.getMessage());
        }
    }

    public String remove(String commandWithID){
        //removing spaces and "remove" word to turn into long
        commandWithID = Terminal.removeString(commandWithID, "remove_by_id");
        if (commandWithID.isEmpty()){
            return "Invalid id. Operation canceled";
        }
        long id = Long.parseLong(commandWithID);

        //trying to find element
        if (returnIndexById(id) != -1){
            this.database.remove(returnIndexById(id));
            return "Worker was successfully deleted from the database";
        } else {
            return "Element not found";
        }
    }

    /*public void executeScript(String commandWithFilename){
        //removing spaces and "remove" word to turn into long
        commandWithFilename = Terminal.removeString(commandWithFilename, "execute_script") + ".txt";

        //catching recursion
        if (this.scriptName.equals(commandWithFilename)){
            this.recursionCounter++;
        } else {
            this.scriptName = commandWithFilename;
            this.recursionCounter = 0;
        }

        //stopping if recursion detected
        if (this.recursionCounter > 10){
            System.out.println("Executing stopped to avoid stack overflow");
            this.scriptName = commandWithFilename;
            this.recursionCounter = 0;
            this.terminal = new Scanner(System.in);
            Terminal.changeScanner(this.terminal);
            readFromTerminal();
            return;
        }

        //new file and check if it exist
        File f = new File(commandWithFilename);
        if ( FileParser.alreadyExistCheck(commandWithFilename)){
            try {
                //changing terminal scanner on file's
                //IMPORTANT: THE LINK TO DATABASE'S SCANNER IS GIVEN, NOT NEW
                this.terminal = new Scanner(f);
                Terminal.changeScanner(this.terminal);
                while (this.terminal.hasNext()) {
                    readFromTerminal();
                }
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
        //chaging terminal back
        this.terminal = new Scanner(System.in);
        Terminal.changeScanner(this.terminal);
        //continue reading
        readFromTerminal();
    }*/

    public String removeGreater(String commandWithSalary){
        //removing spaces and "update" word to turn into long
        commandWithSalary = Terminal.removeString(commandWithSalary, "remove_greater");
        double salary = Double.parseDouble(commandWithSalary);

        int toRemoveCounter = 0;
        long toRemoveID[] = new long[this.database.size()];
        for (int i = 0; i < this.database.size(); i++) {
            if (this.database.get(i).getSalary() > salary){
                toRemoveID[toRemoveCounter] = this.database.get(i).getId();
                toRemoveCounter++;
            }
        }

        for (int i = 0; i < toRemoveCounter; i++) {
            this.database.remove(returnIndexById(toRemoveID[i]));
        }

        return "Workers with salary greater " + salary + " were successfully removed!";
    }

    public String removeLower(String commandWithSalary){
        //removing spaces and "update" word to turn into long
        commandWithSalary = Terminal.removeString(commandWithSalary, "remove_lower");
        double salary = Double.parseDouble(commandWithSalary);

        int toRemoveCounter = 0;
        long toRemoveID[] = new long[this.database.size()];
        for (int i = 0; i < this.database.size(); i++) {
            if (this.database.get(i).getSalary() < salary){
                toRemoveID[toRemoveCounter] = this.database.get(i).getId();
                toRemoveCounter++;
            }
        }

        for (int i = 0; i < toRemoveCounter; i++) {
            this.database.remove(returnIndexById(toRemoveID[i]));
        }
        return "Workers with salary lower " + salary + " were successfully removed!";
    }

    public String groupCountingByPosition(){
        StringBuilder out = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        for (Position p: Position.values()) {
            out.append("-----------" + p.toString() + "-----------");out.append("\n");
            for (Worker worker : this.database) {
                if (worker.getPosition() != null) {
                    if (worker.getPosition().equals(p)){
                        sb.append(worker.getName()).append(" ").append(worker.getId());
                    }
                }
                out.append(sb.toString());out.append("\n");
                sb.delete(0, sb.length());
            }
        }
        return out.toString();
    }

    public String countLessThanStartDate(String commandWithStartDate){
        //removing spaces and "count_less_than_start_date" word to turn into date
        commandWithStartDate = Terminal.removeString(commandWithStartDate, "count_less_than_start_date");
        if (!commandWithStartDate.matches("\\s*(?!0000)(\\d{4})-(0[1-9]|1[0-2])-[0-3]\\d\\s*")){
            return "Invalid date!";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(commandWithStartDate, formatter);
        ZonedDateTime z = date.atStartOfDay(ZoneId.systemDefault());

        int counter = 0;
        for (Worker w:this.database) {
            if (w.getStartDate().isBefore(z)){
                counter++;
            }
        }
        return "There are " + counter + " workers with StartDate less than " + commandWithStartDate;
    }

    public String filterGreaterThanStartDate(String commandWithStartDate){
        //removing spaces and "count_less_than_start_date" word to turn into date
        commandWithStartDate = Terminal.removeString(commandWithStartDate, "filter_greater_than_start_date");
        if (!commandWithStartDate.matches("\\s*(?!0000)(\\d{4})-(0[1-9]|1[0-2])-[0-3]\\d\\s*")){
            return "Invalid date!";
        }

        StringBuilder out = new StringBuilder();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(commandWithStartDate, formatter);
        ZonedDateTime z = date.atStartOfDay(ZoneId.systemDefault());

        out.append("-----Workers with date after " + commandWithStartDate + "-----");out.append("\n");
        for (Worker w:this.database) {
            if (w.getStartDate().isAfter(z)){
                out.append(w.getName() + " " + w.getId());out.append("\n");
            }
        }
        return out.toString();
    }

    public String addIfMax(Worker newWorker){
        for (Worker w: this.database) {
            if (w.getSalary() > newWorker.getSalary()){
                return "New worker hasn't got the max salary!";
            }
        }

        this.database.add(newWorker);
        return "Worker has been successfully added!";
    }
}
