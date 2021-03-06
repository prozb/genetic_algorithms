package com.gp.task1;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.*;

/**
 * @author Pavlo Rozbytskyi
 * @version 3.0.1
 */
public class Main {
    private static int generationCount;
    private static int geneLen;
    private static int replicationSchema;
    private static int crossOverSchema;
    private static int maxGenerations;
    private static int initRate;
    private static int runsNum;
    private static int pointsPos;
    private static long startTime;
    private static float mutationRate;
    private static float recombinationRate;
    private static float lastPc;
    private static boolean protect;

    private static Point [] points;
    private static String [] arguments;
    private static BufferedWriter writer;
    private static StringBuilder sBuilder;

    private static Date date;
    private static SimpleDateFormat sdfDate;
    private static SimpleDateFormat sdfFile;

    public static final Logger logger = Logger.getLogger(Main.class);

    // TESTING PARAMETERS: --pm=0.02 --pc=0.5 --genecount=200 --genelen=200 --maxgen=1000 --runs=10 --protect=best --initrate=5 --crossover_scheme=1 --replication_scheme=1
    public static void main(String[] args) throws InterruptedException, IOException {
        arguments = args;
        sBuilder  = new StringBuilder();
        date      = new Date();
        sdfDate   = new SimpleDateFormat(Constants.DATE_PATTERN);
        sdfFile   = new SimpleDateFormat(Constants.DATE_FILE_PATTERN);

        //process time
        startTime    = System.nanoTime();

        BasicConfigurator.configure();
        processUserInput();
        int permNumber  =  (int)((((Constants.PM_MAX - Constants.PM_MIN) / Constants.PM_STEP) *
                                  ((Constants.PC_MAX - Constants.PC_MIN) / Constants.PC_STEP)) + 0.5f);

        //creates array with points with pm and pc to pass into each simulation
        calculatePointsToProcess(permNumber);
        //creating executors to execute all simulations tasks
        ExecutorService executors = Executors.newFixedThreadPool(Constants.THREADS_NUM);
        List<Callable<String>> simulations = new ArrayList<>();

        logger.info("Program started at " + sdfDate.format(date));
        logger.info("Creating " + Constants.THREADS_NUM + " threads");

        //creating simulations to be executed by thread pool
        if(Constants.GRAPH_SIMULATION) {
            for (int i = 0; i < permNumber; i++) {
                Callable<String> simulation = new Simulation(geneLen, generationCount, mutationRate, recombinationRate,
                        runsNum, replicationSchema, crossOverSchema, maxGenerations, initRate, protect, points[i],
                        Constants.GRAPH_SIMULATION, i);
                simulations.add(simulation);
            }
        }else{
            Callable<String> simulation = new Simulation(geneLen, generationCount, mutationRate, recombinationRate,
                    runsNum, replicationSchema, crossOverSchema, maxGenerations, initRate, protect, new Point(recombinationRate, mutationRate),
                    Constants.GRAPH_SIMULATION, 0);
            simulations.add(simulation);
        }

        executors.invokeAll(simulations)
                .stream()
                .map(future -> {
                    try{
                        return future.get();
                    }catch (Exception e){
                        throw new IllegalStateException(e);
                    }
                })
                .forEach(Main::pushIntoBuilder);
        executors.shutdown();

        logger.info("Finished " + Simulation.counter + " simulations");
        if(Constants.GRAPH_SIMULATION) {
            exportBufferToFile();
        }else{
            exportBufferToFile();
            logger.info("Average " + sBuilder.toString() + " generations to achieve max fitness");
        }
        logger.info("Time: complete program duration " + TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime) + "s");
        logger.info("Program execution finished at " + sdfDate.format(new Date()));
    }

    //pushing string into builder after simulation completed
    private static void pushIntoBuilder(String s){
        if(Constants.GRAPH_SIMULATION) {
            String[] strings = s.trim().replaceAll("\t+", ",").split(",");
            if (strings.length > 0) {
                if (lastPc == 0) {
                    lastPc = Float.parseFloat(strings[0]);
                }
                if (lastPc < Float.parseFloat(strings[0])) {
                    sBuilder.append("\n");
                    lastPc = Float.parseFloat(strings[0]);
                }
            }
        }

        sBuilder.append(s);
    }

    //creating array with points to pass into each task
    private static void calculatePointsToProcess(int pointsNum){
        points = new Point[pointsNum];
        //scale factors just to iterate over integers, not floats
        for(int i = (int) (Constants.PC_MIN * Constants.SCALE_FACTOR); i < (int)(Constants.PC_MAX * Constants.SCALE_FACTOR); i += (int)(Constants.PC_STEP * Constants.SCALE_FACTOR)){
            for(int j = (int)(Constants.PM_MIN * Constants.SCALE_FACTOR); j < (int)(Constants.PM_MAX * Constants.SCALE_FACTOR); j += (int)(Constants.PM_STEP * Constants.SCALE_FACTOR)){
                points[pointsPos++] = new Point(j / (Constants.SCALE_FACTOR + 0.0f),
                                                 i / (Constants.SCALE_FACTOR + 0.0f));
            }
        }
        logger.info("Created " + points.length + " simulations");
    }

    //exporting string builder to file
    private static void exportBufferToFile() throws IOException {
            String fileName = "plot" + sdfFile.format(date) + ".txt";
            logger.info("Creating file " + fileName);
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(sBuilder.toString());
            writer.flush();
            writer.close();
            logger.info("Exported data to file " + fileName);
    }

    private static void processUserInput(){
        if(arguments.length <= 1){
            printHelpMessage();
            printError("No parameters!");
        }else if(arguments[1].equals("--help")){
            printHelpMessage();
            System.exit(99);
        }else if(arguments.length < Constants.NUM_OF_ARGS){
            printHelpMessage();
            printError("Not enough parameters!");
        }else if(arguments.length > Constants.NUM_OF_ARGS){
            printHelpMessage();
            printError("To many parameters!");
        }else {
            generationCount   = (int) getValue("--genecount");
            geneLen           = (int) getValue("--genelen");
            crossOverSchema   = (int) getValue("--crossover_scheme");
            replicationSchema = (int) getValue("--replication_scheme");
            initRate          = (int) getValue("--initrate");
            maxGenerations    = (int) getValue("--maxgen");
            runsNum           = (int) getValue("--runs");
            mutationRate      = getValue("--pm");
            recombinationRate = getValue("--pc");
            protect = getStringValue("--protect").equals("best");
        }
    }

    private static float getValue(String arg){
        Optional<String> geneLenStringOptional = Arrays.stream(arguments).filter(elem -> elem.contains(arg)).findFirst();
        String geneLenString = "";

        if(geneLenStringOptional.isPresent()){
            geneLenString = geneLenStringOptional.get().trim().replace(" ", "");
        }
        int pos = geneLenString.indexOf(arg) + 1 + arg.length();
        String num = geneLenString.substring(pos);

        float val = 0;
        try {
            val = Float.parseFloat(num);
        }catch (NumberFormatException e){
            printError("Number \"" + arg + "\" not found!");
        }
        return val;
    }

    private static String getStringValue(String arg){
        Optional<String> geneLenStringOptional = Arrays.stream(arguments).filter(elem -> elem.contains(arg)).findFirst();
        String geneLenString = "";

        if(geneLenStringOptional.isPresent()){
            geneLenString = geneLenStringOptional.get().trim().replace(" ", "");
        }
        int pos = geneLenString.indexOf(arg) + 1 + arg.length();
        String val = geneLenString.substring(pos).trim();

        if(val.equals("")){
            logger.error("Incorrect protect option input");
            printError("protect option can not be empty!");
        }

        return val.trim();
    }

    /**
     * Prints error out logs it and terminates program
     * @param err
     */
    public static void printError(String err){
        logger.error(err);
        System.out.println("Error: " + err);
        System.exit(99);
    }

    private static void printHelpMessage(){
        System.out.println("\nusage:\n--------------------------------------------" +
                " \n [--help]\nto process simulation you must enter all following args\n" +
                "[--pc] recombination rate (float number)\n" +
                "[--pm] mutations rate (float number)\n" +
                "[--initrate] initiations rate (integer number)\n" +
                "[--genecnt] count of genes in generation (integer number)\n" +
                "[--genelen] length of the gene (integer number)\n" +
                "[--crossover_scheme] (integer number)\n" +
                "[--replication_scheme] (integer number)\n" +
                "[--maxgen] maximum generations (integer number)\n" +
                "[--runs] number of runs (integer number)\n" +
                "[--protect] the best gene is crossover and mutation protected (best|none)");
    }
}
