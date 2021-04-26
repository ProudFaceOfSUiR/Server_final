package com.company.database;

import com.company.classes.Coordinates;
import com.company.classes.Person;
import com.company.classes.Worker;
import com.company.enums.Position;
import com.company.exceptions.OperationCanceledException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

public class FileParser {

    /**
     * Check if file path is valid
     * @param filePath
     * @return
     */
    public static boolean pathCheck(String filePath){
        if (!filePath.matches("\\s*\\w+.xml")){
            System.out.println("Invalid path. Couldn't get file");
            return false;
        }
        return true;
    }

    /**
     * Checks if we have permissions to read file
     * @param filePath
     * @return
     */
    public static boolean permissionToReadCheck(Path filePath){
        if (!Files.isReadable((filePath))){
            System.out.println("File is restricted from editing.");
            return false;
        } else return true;
    }

    /**
     * Checks if file already exists
     * @param filePath
     * @return
     */
    public static boolean alreadyExistCheck(String filePath) {
        File f = new File(filePath);
        return f.exists() && !f.isDirectory();
    }

    /**
     * question if we want to overwrite file
     * @param filePath
     * @return
     * @throws OperationCanceledException
     */
    public static boolean overWriteFile(String filePath) throws OperationCanceledException{
        //check if file exists
        if (alreadyExistCheck(filePath)) {
            //giving the choice
            return Terminal.binaryChoice("overwrite the existing file");
        } else return true; //"overwriting" nonexistent file
    }

    /**
     * Gives path from class Path from string with path
     * @param path
     * @return
     */
    public static Path getPath(String path){
        File f = new File(path);
        Path p;
        try {
            p = f.toPath();
        } catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        if (Files.notExists(p)){
            System.out.println("File doesn't exist!");
            return null;
        } else return p.normalize();
    }

    /**
     * Parses XML file to LinkedList database
     * @param filepath
     * @return
     * @throws Exception
     */
    public static LinkedList<Worker> xmlToDatabase(String filepath) throws Exception{

        if (!pathCheck(filepath)){
            throw new Exception("Invalid path. Operation cancelled");
        }

        if (getPath(filepath) == null){
            throw new Exception("Invalid path. Operation cancelled");
        }

        if (!permissionToReadCheck(getPath(filepath))){
            throw new Exception("File is restricted from editing. Operation cancelled");
        }

        LinkedList<Worker> database = new LinkedList<>();
        File file = new File(filepath);
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("worker");

            //fields of a worker
            String name;
            double salary;

            //position
            String positionString;
            Position position;

            //personality
            Person person = null;

            //coordinates
            Coordinates coordinates;

            //dates
            ZonedDateTime startdate;
            ZonedDateTime endDate = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            //counter of successfully added workers
            int successfullyAddedWorkers = 0;

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    name = Terminal.removeSpaces(eElement.getElementsByTagName("name").item(0).getTextContent());

                    //adding name
                    if (name.matches("\\s*")) {
                        System.out.println("Invalid name. Couldn't add worker");
                        continue;
                    }

                    //addding salary
                    try {
                        if (eElement.getElementsByTagName("salary").item(0).getTextContent().matches("\\s*[0-9]+.*[0-9]*\\s*")) {
                            salary = Double.parseDouble(Terminal.removeSpaces(eElement.getElementsByTagName("salary").item(0).getTextContent()));
                        } else {
                            System.out.println(name + "'s salary is invalid. Couldn't add worker");
                            continue;
                        }
                    } catch (Exception e){
                        System.out.println("Something went wrong with the " + name +  "'s salary: " + e.getMessage());
                        continue;
                    }

                    try {
                        //adding position
                        positionString = eElement.getElementsByTagName("position").item(0).getTextContent();
                        position = Position.findEnum(positionString);
                    } catch (Exception e){
                        System.out.println("Something went wrong with the " + name + "'s position: " + e.getMessage());
                        continue;
                    }

                    try {
                        //adding personal qualities
                        if (eElement.getElementsByTagName("person").item(0).getTextContent().isEmpty() ||
                                !eElement.getElementsByTagName("person").item(0).getTextContent().matches("\\s*\\d+,\\d+\\s*")) {
                            //do nothing cause person is already null
                        } else {
                            String[] heightWeight = eElement.getElementsByTagName("person").item(0).getTextContent().split(",");
                            person = new Person(Long.valueOf(Terminal.removeSpaces(heightWeight[0])), Integer.valueOf(Terminal.removeSpaces(heightWeight[1])));
                        }
                    }catch (NullPointerException e){
                        person = null;
                    }
                    catch (Exception e){
                        System.out.println("Something went wrong with the " + name + "'s personality: " + e.getMessage());
                        continue;
                    }

                    try {
                        //adding coordinates
                        if (eElement.getElementsByTagName("coordinates").item(0).getTextContent().isEmpty() ||
                                !eElement.getElementsByTagName("coordinates").item(0).getTextContent().matches("\\s*\\d+,\\d+\\s*")) {
                            System.out.println("Invalid coordinates. Couldn't add worker");
                            continue;
                        } else {
                            String[] xy = eElement.getElementsByTagName("coordinates").item(0).getTextContent().split(",");
                            coordinates = new Coordinates(Long.parseLong(Terminal.removeSpaces(xy[0])), Integer.valueOf(Terminal.removeSpaces(xy[1])));
                        }
                    } catch (Exception e){
                        System.out.println("Something went wrong with the " + name + "'s coordinates: " + e.getMessage());
                        continue;
                    }

                    try {
                        //adding start date
                        if (eElement.getElementsByTagName("startdate").item(0).getTextContent().isEmpty() ||
                                !eElement.getElementsByTagName("startdate").item(0).getTextContent().matches("\\s*(?!0000)(\\d{4})-(0[1-9]|1[0-2])-[0-3]\\d\\s*")) {
                            System.out.println("Invalid startDate. Couldn't add worker");
                            continue;
                        } else {
                            LocalDate date = LocalDate.parse(
                                    Terminal.removeSpaces(
                                            eElement.getElementsByTagName("startdate").item(0).getTextContent()),
                                    formatter);
                            startdate = date.atStartOfDay(ZoneId.systemDefault());
                        }
                    } catch (Exception e){
                        System.out.println("Something went wrong with the " + name + "'s startdate: " + e.getMessage());
                        continue;
                    }

                    try {
                        //adding enddate
                        if (!eElement.getElementsByTagName("enddate").item(0).getTextContent().isEmpty() &&
                                !eElement.getElementsByTagName("enddate").item(0).getTextContent().matches("\\s*(?!0000)(\\d{4})-(0[1-9]|1[0-2])-[0-3]\\d\\s*")) {
                            System.out.println("Invalid endDate. Couldn't add worker");
                            continue;
                        } else if (eElement.getElementsByTagName("enddate").item(0).getTextContent().matches("\\s*(?!0000)(\\d{4})-(0[1-9]|1[0-2])-[0-3]\\d\\s*")) {
                            LocalDate date = LocalDate.parse(
                                    Terminal.removeSpaces(
                                            eElement.getElementsByTagName("enddate").item(0).getTextContent()),
                                    formatter);
                            endDate = date.atStartOfDay(ZoneId.systemDefault());
                        }
                    } catch (NullPointerException e){
                        endDate = null;
                    }
                    catch (Exception e){
                        System.out.println("Something went wrong with the " + name + "'s enddate: " + e.getMessage());
                        continue;
                    }

                    try {
                        //adding worker
                        database.add(new Worker(name, salary, position, person, coordinates, startdate, endDate));
                        successfullyAddedWorkers++;
                    } catch (Exception e){
                        System.out.println("Something went wrong while adding a new worker: " + e.getMessage());
                    }
                }
            }

            System.out.println("DataBase has been successfully filled with " + successfullyAddedWorkers + " workers");
            System.out.println("------------------------------------");
            return database;
        } catch (Exception e) {
            System.out.println("Something went wrong :0 Operation cancelled");
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Makes a big string with the whole database
     * @param database
     * @return
     */
    public static String dataBaseToString(LinkedList<Worker> database){
        StringBuilder sb = new StringBuilder();

        //writing preamble
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
        sb.append("<database>").append("\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //writing workers
        for (Worker w: database) {
            sb.append("\t").append("<worker>").append("\n");

            sb.append("\t\t").append("<name>").append(w.getName()).append("</name>").append("\n");
            sb.append("\t\t").append("<salary>").append(w.getSalary()).append("</salary>").append("\n");

            if (w.getPosition() != null){
                sb.append("\t\t").append("<position>").append(w.getPosition().toString()).append("</position>").append("\n");
            }

            sb.append("\t\t").append("<coordinates>").append(w.getCoordinates().getX()).append(",").append(w.getCoordinates().getY()).append("</coordinates>").append("\n");

            sb.append("\t\t").append("<startdate>").append(w.getStartDate().format(formatter)).append("</startdate>").append("\n");

            if (w.getEndDate() != null){
                sb.append("\t\t").append("<enddate>").append(w.getStartDate().format(formatter)).append("</enddate>").append("\n");
            }

            sb.append("\t").append("</worker>").append("\n");
        }

        sb.append("</database>");
        return sb.toString();
    }

    /**
     * Parses database to XML
     * @param database
     * @param filename
     */
    public static void dataBasetoXML(String database, String filename){
        try {
            // Creates a FileWriter
            FileWriter file = new FileWriter(filename);

            // Creates a BufferedWriter
            BufferedWriter buffer = new BufferedWriter(file);

            //writing and flushing to file
            buffer.write(database);
            buffer.flush();

            System.out.println("Database was successfully saved to a new file!");
            buffer.close();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
