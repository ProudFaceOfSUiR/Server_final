package com.company.classes;

import com.company.database.Terminal;
import com.company.enums.Position;
import com.company.exceptions.InvalidDataException;
import com.company.exceptions.OperationCanceledException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;

public class Worker {
    private Long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private java.time.ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private double salary; //Значение поля должно быть больше 0
    private java.time.ZonedDateTime startDate; //Поле не может быть null
    private java.time.ZonedDateTime endDate; //Поле может быть null
    private Position position; //Поле может быть null
    private Person person; //Поле может быть null

    public Worker(String name, double salary, Position position, Person person, Coordinates coordinates,
                  ZonedDateTime startDate, ZonedDateTime endDate) throws InvalidDataException{
        setName(name);
        setSalary(salary);

        this.coordinates = coordinates;
        this.startDate = startDate;
        this.endDate = endDate;
        this.position = position;
        this.person = person;

        this.creationDate = ZonedDateTime.now();

        Random random = new Random();
        this.id = Math.abs(Long.parseLong(String.valueOf(random.nextLong() + creationDate.getSecond() + creationDate.getMinute() + creationDate.getHour()).substring(0,10)));

    }


    //private constructor for WorkerBuilder
    private Worker(){
        //automatically generated values
        this.creationDate = ZonedDateTime.now();
        Random random = new Random();
        this.id = Math.abs(Long.parseLong(String.valueOf(
                random.nextLong() + creationDate.getSecond() + creationDate.getMinute() + creationDate.getHour())
                .substring(0,10)));
    }

    /**
     * Builder for terminal input. Includes all the souts etc
     */
    public static class WorkerBuilderFromTerminal {

        private Worker worker = new Worker();

        public WorkerBuilderFromTerminal() {
        }

        public Worker build() throws OperationCanceledException, InvalidDataException{
            setName();
            setSalary();
            setPosition();
            setPersonality();
            setCoordinates();
            setDates();
            return worker;
        }

        //protected methods for terminal input

        protected void setName() throws InvalidDataException, OperationCanceledException{
            System.out.print("Please, write the name of a new worker: (String, not Null)");
            worker.setName(
                    Terminal.removeSpaces(
                            Terminal.repeatInputAndExpectRegex("name", "\\s*\\w+\\s*")
                    )
            );
        }

        protected void setSalary() throws OperationCanceledException{
            System.out.print("PLease, input " + worker.getName() + "'s salary: (double, not null, >0)");
            try {
                worker.setSalary(
                        Double.parseDouble(
                                Terminal.removeSpaces(
                                        Terminal.repeatInputAndExpectRegex("salary", "\\s*\\d+\\.*\\d*\\s*"))
                        )
                );
            } catch (InvalidDataException e) {
                System.out.println(e.getMessage());
                setSalary();
            }
        }

        protected void setPosition() throws OperationCanceledException{
            System.out.println("Please, write " + worker.getName() + "'s position " + Arrays.toString(Arrays.stream(Position.getPositions()).toArray()) + ": ");
            worker.setPosition(Position.findEnum(Terminal.repeatInputAndExpectRegexOrNull("position","\\s*\\w+\\s*")));
            if (worker.getPosition() == null){
                System.out.println("Invalid or null position. Null is set");
            }
        }

        protected void setPersonality(){
            Person person = new Person();
            System.out.println("PLease, write " + worker.getName() + "'s height: (Long, >0)");
            try {
                person.setHeight(Long.valueOf(Terminal.repeatInputAndExpectRegexOrNull("height", "\\s*(?!(0))[0-9]+\\s*")));
            } catch (Exception e){
                //pass, because Person is already null
            }

            System.out.println("PLease, write " + worker.getName() + "'s weight: (Integer, >0)");
            try {
                person.setWeight((int) Long.parseLong(Terminal.repeatInputAndExpectRegexOrNull("weight", "\\s*(?!(0))[0-9]+\\s*")));
            } catch (Exception e){
                //pass
            }
            this.worker.setPerson(person);
        }

        protected void setCoordinates() throws OperationCanceledException{
            System.out.println("PLease, input " + worker.getName() + "'s coordinates (not null)");
            Coordinates c = new Coordinates(0,0);

            try {
                System.out.print("X (<768) = ");
                c.setX(
                        Long.parseLong(Terminal.removeSpaces(
                                Terminal.repeatInputAndExpectRegex("x coordinate", "\\s*-?\\d+\\s*")
                        ))
                );
                System.out.print("Y = ");
                c.setY((int)
                        Long.parseLong(Terminal.removeSpaces(
                                Terminal.repeatInputAndExpectRegex("y coordinate", "\\s*-?\\d+\\s*")
                        ))
                );
            } catch (InvalidDataException e) {
                System.out.println(e.getMessage());
                setCoordinates();
            }

            this.worker.setCoordinates(c);
        }

        protected void setDates() throws OperationCanceledException{
            System.out.println("Please, write the start day (yyyy-mm-dd): (not null)");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(Terminal.removeSpaces(Terminal.repeatInputAndExpectRegex(
                    "start day", "\\s*(?!0000)(\\d{4})-(0[1-9]|1[0-2])-[0-3]\\d\\s*")), formatter);

            worker.setStartDate( date.atStartOfDay(ZoneId.systemDefault()) );

            System.out.println("Please, write the end day (yyyy-mm-dd): ");
            String enddate;
            enddate = Terminal.removeSpaces(
                    Terminal.repeatInputAndExpectRegexOrNull("end day", "\\s*(?!0000)(\\d{4})-(0[1-9]|1[0-2])-[0-3]\\d\\s*")
            );
            if (enddate == null){
                worker.setEndDate(null);
            } else {
                date = LocalDate.parse(enddate, formatter);

                worker.setEndDate(date.atStartOfDay(ZoneId.systemDefault()));
            }
        }
    }

    //getters and setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidDataException{
        if (name == null || name.equals("")){
            throw new InvalidDataException("name", "It can't be empty");
        } else {
            this.name = name;
        }
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) throws InvalidDataException{
        if (salary <= 0){
            throw new InvalidDataException("salary","It must be a positive number");
        } else {
            this.salary = salary;
        }
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
