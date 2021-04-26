package com.company.database;

import com.company.classes.Coordinates;
import com.company.classes.Person;
import com.company.enums.Fields;
import com.company.classes.Worker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

import com.company.enums.Commands;
import com.company.enums.Position;
import com.company.exceptions.InvalidDataException;
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
    protected int returnIndexById(long id){
        int index = -1;
        for (int i = 0; i < database.size(); i++) {
            if (database.get(i).getId() == id){
                index = i;
                break;
            }
        }
        return index;
    }

    //terminal commands

    /*protected void updateById(String commandWithID) throws OperationCanceledException {
        //removing spaces and "update" word to turn into long
        commandWithID = Terminal.removeString(commandWithID, "update");
        if (commandWithID.isEmpty() || !commandWithID.matches("\\d+")){
            System.out.println("Invalid id");
            return;
        }
        long id = Long.parseLong(commandWithID);

        //trying to find element
        if (returnIndexById(id) != -1){
            updateFields(returnIndexById(id));
        } else {
            System.out.println("Element not found");
        }
    }*/

    public String help(){
        StringBuilder sb = new StringBuilder();

        sb.append("------------------------------------");sb.append("\n");
        sb.append("Commands: ");sb.append("\n");
        for (int i = 0; i < Commands.values().length; i++) {
            sb.append(" " + Commands.getCommandsWithDescriptions()[i]);sb.append("\n");
        }
        sb.append("------------------------------------");sb.append("\n");

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

        stringBuilder.append("------------------------------------");stringBuilder.append("\n");
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
        stringBuilder.append(Terminal.formatAsTable(rows));stringBuilder.append("\n");
        stringBuilder.append("------------------------------------"); stringBuilder.append("\n");

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

    public void remove(String commandWithID){
        //removing spaces and "remove" word to turn into long
        commandWithID = Terminal.removeString(commandWithID, "remove_by_id");
        if (commandWithID.isEmpty()){
            System.out.println("Invalid id. Operation canceled");
            return;
        }
        long id = Long.parseLong(commandWithID);

        //trying to find element
        if (returnIndexById(id) != -1){
            try {
                if (Terminal.binaryChoice("delete worker")){
                    this.database.remove(returnIndexById(id));
                    System.out.println("Worker was successfully deleted from the database");
                } else {
                    System.out.println("Operation canceled");
                }
            } catch (OperationCanceledException e) {
                //catching EOF
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Element not found");
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

    public void removeGreater(String commandWithSalary){
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

        System.out.println("Workers with salary greater " + salary + " were successfully removed!");
    }

    public void removeLower(String commandWithSalary){
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
        System.out.println("Workers with salary lower " + salary + " were successfully removed!");
    }

    public void groupCountingByPosition(){
        StringBuilder sb = new StringBuilder();
        for (Position p: Position.values()) {
            System.out.println("-----------" + p.toString() + "-----------");
            for (Worker worker : this.database) {
                if (worker.getPosition() != null) {
                    if (worker.getPosition().equals(p)){
                        sb.append(worker.getName()).append(" ").append(worker.getId());
                    }
                }
                System.out.println(sb.toString());
                sb.delete(0, sb.length());
            }
        }
    }

    public void countLessThanStartDate(String commandWithStartDate){
        //removing spaces and "count_less_than_start_date" word to turn into date
        commandWithStartDate = Terminal.removeString(commandWithStartDate, "count_less_than_start_date");
        if (!commandWithStartDate.matches("\\s*(?!0000)(\\d{4})-(0[1-9]|1[0-2])-[0-3]\\d\\s*")){
            System.out.println("Invalid date!");
            return;
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
        System.out.println("There are " + counter + " workers with StartDate less than " + commandWithStartDate);
    }

    public void filterGreaterThanStartDate(String commandWithStartDate){
        //removing spaces and "count_less_than_start_date" word to turn into date
        commandWithStartDate = Terminal.removeString(commandWithStartDate, "filter_greater_than_start_date");
        if (!commandWithStartDate.matches("\\s*(?!0000)(\\d{4})-(0[1-9]|1[0-2])-[0-3]\\d\\s*")){
            System.out.println("Invalid date!");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(commandWithStartDate, formatter);
        ZonedDateTime z = date.atStartOfDay(ZoneId.systemDefault());

        System.out.println("-----Workers with date after " + commandWithStartDate + " -----");
        for (Worker w:this.database) {
            if (w.getStartDate().isAfter(z)){
                System.out.println(w.getName() + " " + w.getId());
            }
        }
        System.out.println("-------------------------");
    }

    public void addIfMax(){
        Worker newWorker = null;
        try {
            newWorker = new Worker.WorkerBuilderFromTerminal().build();
        } catch (OperationCanceledException | InvalidDataException e) {
            System.out.println(e.getMessage());
            System.out.println("Couldn't add worker");
            return;
        }

        for (Worker w: this.database) {
            if (w.getSalary() > newWorker.getSalary()){
                System.out.println("New worker hasn't got the max salary!");
                return;
            }
        }

        this.database.add(newWorker);
    }
}
