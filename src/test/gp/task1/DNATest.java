package test.gp.task1;

import com.gp.task1.Constants;
import com.gp.task1.DNA;
import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
/**
 * @author Pavlo Rozbytskyi
 * @version 2.0.1
 */
public class DNATest {
    @Test
    public void initGeneTest(){
        int len  = 200;
        int init = 5;

        DNA gene = new DNA(len, init);

        int should    = 10;
        int countInit = (int) ((init / 100.0) * len);
        Assert.assertEquals(countInit, should);

        Integer [] geneArr = gene.getGene();
        int occurrence = (int) Arrays.stream(geneArr).filter(elem -> 1 == elem).count();
        Assert.assertEquals(occurrence, countInit);

        DNA gene1 = new DNA(200);
        Integer [] geneArr1 = gene1.getGene();
        int occurrence1  = (int) Arrays.stream(geneArr1).filter(elem -> 1 == elem).count();
        Assert.assertEquals(0, occurrence1);
    }

    @Test
    public void calcFitnessTest(){
        int len  = 200;
        int init = 5;

        DNA DNA = new DNA(len, init);

        int expected = (int)((init / 100.0) * len);
        int fitness  = DNA.getFitness();

        Assert.assertEquals(expected, fitness);
    }

    @Test
    public void calcEmptyFitnessTest(){
        int len  = 200;

        DNA DNA = new DNA(len);

        int expected = 0;
        int fitness  = DNA.getFitness();

        Assert.assertEquals(expected, fitness);
    }

    @Test
    public void invertCellTest(){
        int len = 200;

        DNA DNA = new DNA(len);

        int pos  = (int) (Math.random() * DNA.getGene().length);
        int prev = DNA.getGene()[pos];

        DNA.invertCell(pos);
        int actual = DNA.getGene()[pos];

        Assert.assertNotEquals(prev, actual);
    }

    @Test
    public void calcProbabilityTest(){
        int dnaLen   = 200;
        int genesCnt = 200;
        int rank     = 10;

        DNA testDNA  = new DNA(dnaLen);
        testDNA.calcProbability(rank, genesCnt);

        double expected = ((2 - Constants.S)/genesCnt) + (2.0f * rank * (Constants.S - 1))/ (genesCnt * (genesCnt - 1));
        double actual  = testDNA.getPs();

        Assert.assertEquals(expected, actual, 0);
    }
}
