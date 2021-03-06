package com.gp.task1;

import java.util.Arrays;
/**
 * @author Pavlo Rozbytskyi
 * @version 2.0.1
 */
public class DNA {
    private int len;
    private double ps;      // probability to be chosen
    private double psCum;   // cumulated probability
    private Integer fitness;
    private Integer [] gene;
    private boolean best;

    // fitness will be calculated after creating gene
    public DNA(int len, int initRate){
        this.len     = len;
        this.fitness = 0;
        initGene(initRate);
        calcFitness();
    }

    public DNA(int len){
        this.len = len;
        this.fitness = 0;
        initGene();
    }

    private void initGene(){
        this.gene = new Integer[len];
        Arrays.fill(gene, 0);
    }

    public void calcFitness(){
        this.fitness = (int) Arrays.stream(gene).filter(elem -> 1 == elem).count();
    }
    /**
     *  initRate represents percent of all cells set on 1
     * @param initRate integer represents percent
     */
    private void initGene(int initRate){
        initGene();

        int initCount = (int) ((initRate / 100.0) * len);
        int randPos   = 0;

        while(initCount > 0){
            randPos = (int) (Math.random() * 200);

            if(setCell(randPos))
                initCount--;
        }
        calcFitness();
    }

    // please unset best gene after each generation
    public void setBest(){
        this.best = true;
    }

    public void unsetBest(){
        this.best = false;
    }

    public boolean isBest(){
        return best;
    }

    public void invertCell(int pos){
        gene[pos] = gene[pos] == 0 ? 1 : 0;
    }
    // returns true if cell can be set
    private boolean setCell(int pos){
        try {
            if(gene[pos] == 0){
                gene[pos] = 1;

                return true;
            }else{
                return false;
            }
        }catch (ArrayIndexOutOfBoundsException e){
            return false;
        }
    }

    /**
     * sets gene and recalculates fitness
     * @param gene gene must be set
     */
    public void setGene(Integer [] gene){
        this.gene = gene;

        calcFitness();
    }

    public Integer[] getGene() {
        return gene;
    }

    public Integer getFitness() {
        calcFitness();

        return fitness;
    }

    @Override
    public String toString() {
        return Arrays.toString(gene) + "\n";
    }

    public void calcProbability(int r, int n){
        this.ps = ((2 - Constants.S) / n) + (2.0f * r * (Constants.S - 1)) / (n * (n - 1));
    }

    public double getPs(){
        return ps;
    }

    public void calcCumulProbability(double prevPs){
        psCum = prevPs + ps;
    }

    public double getPsCum(){
        return psCum;
    }

    public void clearPs(){
        ps = 0;
        psCum = 0;
    }

    public void printRank(){
        System.out.println("ps: " + ps + " psCum: " + psCum);
    }
}