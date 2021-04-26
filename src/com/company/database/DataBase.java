package com.company.database;

import com.company.classes.Coordinates;
import com.company.classes.Person;
import com.company.enums.Fields;
import com.company.classes.Worker;

import java.io.File;
import java.io.FileNotFoundException;
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

public class DataBase {

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
     * Initializing database (like constructor)
     * @param filePath
     */
    public void initialize(String filePath){
        //initializing variables
        this.database = new LinkedList<>();
        this.terminal = new Scanner(System.in);
        this.initializationTime = ZonedDateTime.now();
        this.recursionCounter = 0;
        this.scriptName = "";
        this.isInitialized = true;

        System.out.println("Database has been initialized");
        System.out.println("------------------------------------");

        //reading from file and then from terminal
        readFromFile(filePath);
        readFromTerminal();
    }

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

        System.out.println("Database has been initialized without file");
        System.out.println("------------------------------------");
    }

    /**
     * Main input from console(terminal)
     */
    public void readFromTerminal(){
        //cancelling if not initialized
        if(!isInitialized){
            System.out.println("DataBase hasn't been initialized! Cancelling...");
            return;
        }

        //reading from terminal and checking if command exist
        String command;
        while(true) {
            //check when we read from file
            if (!terminal.hasNext()) {
                return;
            }

            //reading command
            command = terminal.nextLine();
            command = command.toLowerCase();

            //skip empty line
            if (command.matches("\\s")) continue;

            try {
                //getting command
                Commands c = Terminal.matchCommand(command);

                switch (c){
                    case HELP:
                        help();
                        break;
                    case INFO:
                        info();
                        break;
                    case SHOW:
                        show();
                        break;
                    case ADD:
                        add();
                        break;
                    case UPDATE:
                        updateById(command);
                        break;
                    case REMOVE_BY_ID:
                        remove(command);
                        break;
                    case CLEAR:
                        clear();
                        break;
                    case SAVE:
                        save();
                        break;
                    case EXECUTE_SCRIPT:
                        executeScript(command);
                        break;
                    case EXIT:
                        System.exit(1);
                        break;
                    case ADD_IF_MAX:
                        addIfMax();
                        break;
                    case REMOVE_GREATER:
                        removeGreater(command);
                        break;
                    case REMOVE_LOWER:
                        removeLower(command);
                        break;
                    case GROUP_COUNTING_BY_POSITION:
                        groupCountingByPosition();
                        break;
                    case COUNT_LESS_THAN_START_DATE:
                        countLessThanStartDate(command);
                        break;
                    case FILTER_GREATER_THAN_START_DATE:
                        filterGreaterThanStartDate(command);
                        break;
                    default:
                        System.out.println("Unknown command");
                        break;
                }
            } catch (UnknownCommandException | OperationCanceledException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Reader from given file.
     * @param filePath
     */
    public void readFromFile(String filePath){
        //cancelling if not initialized
        if(!isInitialized){
            System.out.println("DataBase hasn't been initialized! Cancelling...");
            return;
        }

        //parsing
        try {
            LinkedList<Worker> databaseFromXML = FileParser.xmlToDatabase(filePath);
            if (databaseFromXML != null){
                this.database = databaseFromXML;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
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

    /**
     * The whole code of updating feilds
     * @param index
     * @throws OperationCanceledException
     */
    protected void updateFields(int index) throws OperationCanceledException{
        //checking if element exists
        if (database.get(index) == null){
            System.out.println("Invalid index!");
            return;
        }

        //choosing the field to update
        System.out.println("Which field would you like to update: " + Arrays.toString(Arrays.stream(Fields.getFields()).toArray()) + " ?");
        String choice;
        try {
            while (!Fields.isEnum(choice = terminal.nextLine())){
                System.out.println("Incorrect field. Try again: ");
            }
        } catch (NoSuchElementException e) {
            throw new OperationCanceledException();
        }

        Fields field = Fields.findEnum(choice);

        //updating chosen field
        switch (field){
            case NAME:
                System.out.println("Please, type the new name: ");
                try {
                    database.get(index).setName(Terminal.removeSpaces(Terminal.repeatInputAndExpectRegex("name", "\\s*\\w+\\s*")) );
                } catch (InvalidDataException | OperationCanceledException e) {
                    System.out.println(e.getMessage());
                    return;
                }
                System.out.println(field.toString() + " has been successfully updated!");
                break;
            case SALARY:
                System.out.println("Please, type the new salary: ");
                try {
                    database.get(index).setSalary(Double.parseDouble(Terminal.removeSpaces(Terminal.repeatInputAndExpectRegex("salary", "\\s*\\d+\\.*\\d*\\s*"))));
                } catch (InvalidDataException | OperationCanceledException e) {
                    System.out.println(e.getMessage());
                    return;
                }
                System.out.println(field.toString() + " has been successfully updated!");
                break;
            case POSITION:
                System.out.println("Please, type the new position " + Arrays.toString(Position.values()) + ": ");
                Position newPosition;
                try {
                    newPosition = Position.findEnum(Terminal.removeSpaces(Terminal.repeatInputAndExpectRegexOrNull("position", "\\s*\\w+\\s*")));
                } catch (OperationCanceledException e) {
                    System.out.println(e.getMessage());
                    return;
                }
                this.database.get(index).setPosition(newPosition);
                System.out.println(field.toString() + " has been successfully updated!");
                break;
            case PERSONALITY:
                System.out.println("PLease, type the new height: ");
                Person person = new Person();
                try {
                    person.setHeight(Long.valueOf(Terminal.repeatInputAndExpectRegexOrNull("height", "\\s*[0-9]+\\s*")));
                } catch (Exception e){
                    //pass, because Person is already null
                }

                System.out.println("PLease, type the new weight: ");
                try {
                    person.setWeight((int) Long.parseLong(Terminal.repeatInputAndExpectRegexOrNull("weight", "\\s*[0-9]+\\s*")));
                } catch (Exception e){
                    //pass
                }

                this.database.get(index).setPerson(person);
                System.out.println(field.toString() + " has been successfully updated!");
                break;
            case COORDINATES:
                Coordinates c = new Coordinates(0,0);

                try {
                    System.out.print("X = ");
                    c.setX(
                            Long.parseLong(Terminal.removeSpaces(
                                    Terminal.repeatInputAndExpectRegex("x coordinate", "\\s*\\d+\\s*")
                            ))
                    );
                    System.out.print("Y = ");
                    c.setY((int)
                            Long.parseLong(Terminal.removeSpaces(
                                    Terminal.repeatInputAndExpectRegex("y coordinate", "\\s*\\d+\\s*")
                            ))
                    );
                } catch (InvalidDataException | OperationCanceledException e) {
                    System.out.println(e.getMessage());
                    return;
                }

                this.database.get(index).setCoordinates(c);
                System.out.println(field.toString() + " has been successfully updated!");
                break;
            case STARTDATE:
                System.out.println("Please, write the new start day (yyyy-mm-dd): ");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date;
                try {
                    date = LocalDate.parse(Terminal.removeSpaces(Terminal.repeatInputAndExpectRegex(
                            "start day", "\\s*(?!0000)(\\d{4})-(0[1-9]|1[0-2])-[0-3]\\d\\s*")), formatter);
                } catch (OperationCanceledException e) {
                    System.out.println(e.getMessage());
                    return;
                }

                this.database.get(index).setStartDate( date.atStartOfDay(ZoneId.systemDefault()) );
                System.out.println(field.toString() + " has been successfully updated!");
                break;
            case ENDDATE:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                System.out.println("Please, write the new end day (yyyy-mm-dd): ");
                String enddate;
                try {
                    enddate = Terminal.removeSpaces(
                            Terminal.repeatInputAndExpectRegexOrNull("end day", "\\s*(?!0000)(\\d{4})-(0[1-9]|1[0-2])-[0-3]\\d\\s*")
                    );
                } catch (OperationCanceledException e) {
                    System.out.println(e.getMessage());
                    return;
                }
                if (enddate == null){
                    this.database.get(index).setEndDate(null);
                } else {
                    date = LocalDate.parse(enddate, formatter);
                    this.database.get(index).setEndDate(date.atStartOfDay(ZoneId.systemDefault()));
                }

                System.out.println(field.toString() + " has been successfully updated!");
                break;
        }
        System.out.println("Worker was successfully updated!");
    }

    //terminal commands

    /**
     * small version of update_by_id command - excluding the updating process
     * @param commandWithID
     * @throws OperationCanceledException
     */
    protected void updateById(String commandWithID) throws OperationCanceledException {
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
    }

    public String help(){
        StringBuilder sb = new StringBuilder();

        sb.append("------------------------------------");
        sb.append("Commands: ");
        for (int i = 0; i < Commands.values().length; i++) {
            sb.append(" " + Commands.getCommandsWithDescriptions()[i]);
        }
        sb.append("------------------------------------");

        return sb.toString();
    }

    protected void add() {
        //creating
        Worker newWorker = null;
        try {
            newWorker = new Worker.WorkerBuilderFromTerminal().build();
        } catch (OperationCanceledException | InvalidDataException e) {
            System.out.println(e.getMessage());
            System.out.println("Couldn't add worker");
            return;
        }

        //adding to database
        this.database.add(newWorker);
        System.out.println("New worker was successfully added!");
    }

    public String show(){

        //checking if database is empty
        if (database.isEmpty()){
            return "Database is empty";
        }

        StringBuilder sbb = new StringBuilder();

        sbb.append("------------------------------------");sbb.append("\n");
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
        sbb.append(Terminal.formatAsTable(rows));sbb.append("\n");
        sbb.append("------------------------------------"); sbb.append("\n");

        return sb.toString();
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

    protected void save(){
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

    protected void remove(String commandWithID){
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

    protected void executeScript(String commandWithFilename){
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
    }

    protected void removeGreater(String commandWithSalary){
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

    protected void removeLower(String commandWithSalary){
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

    protected void groupCountingByPosition(){
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

    protected void countLessThanStartDate(String commandWithStartDate){
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

    protected void filterGreaterThanStartDate(String commandWithStartDate){
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

    protected void addIfMax(){
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
