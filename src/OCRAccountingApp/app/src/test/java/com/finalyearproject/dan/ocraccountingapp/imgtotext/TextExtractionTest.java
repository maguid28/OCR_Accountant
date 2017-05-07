package com.finalyearproject.dan.ocraccountingapp.imgtotext;

import android.content.Context;

import com.finalyearproject.dan.ocraccountingapp.util.TimeUtils;

import org.junit.Test;
import org.mockito.Mock;

import java.util.Calendar;

import static org.junit.Assert.*;

public class TextExtractionTest {

    @Mock
    Context mMockContext;

    private String dictFileDir = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/main/assets/tessdata/";

    // The files below are all text extracted from different receipts under different lighting conditions
    private String testInput1Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput1";
    private String testInput2Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput2";
    private String testInput3Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput3";
    private String testInput4Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput4";
    private String testInput5Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput5";
    private String testInput6Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput6";
    private String testInput7Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput7";
    private String testInput8Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput8";


/*-----------------------------Tests corresponding to testInput1----------------------------------*/

    @Test
    public void testGetTitle1() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput1Path, dictFileDir);
        String expected = "Hannons Supervalu ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate1() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput1Path);
        String expected = "04/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal1() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput1Path);
        String expected = "15.19";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory1() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput1Path, "Hannons Supervalu");
        String expected = "food";
        assertEquals(expected, output);
    }



/*-----------------------------Tests corresponding to testInput2----------------------------------*/

    @Test
    public void testGetTitle2() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput2Path, dictFileDir);
        String expected = "Keane Careplus Pharmacy ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate2() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput2Path);
        String expected = "04/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal2() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput2Path);
        String expected = "12.90";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory2() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput2Path, "Keane Careplus Pharmacy");
        String expected = "health";
        assertEquals(expected, output);
    }

/*-----------------------------Tests corresponding to testInput3----------------------------------*/

    @Test
    public void testGetTitle3() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput3Path, dictFileDir);
        String expected = "Keanes Careplus Pharmacy ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate3() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput3Path);
        String expected = "04/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal3() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput3Path);
        String expected = "12.9";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory3() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput3Path, "Keane Careplus Pharmacy");
        String expected = "health";
        assertEquals(expected, output);
    }

/*-----------------------------Tests corresponding to testInput4----------------------------------*/

    @Test
    public void testGetTitle4() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput4Path, dictFileDir);
        String expected = "Cordners Shoes ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate4() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput4Path);
        String expected = "24/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal4() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput4Path);
        String expected = "12.9";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory4() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput4Path, "Cordners Shoes");
        String expected = "clothing";
        assertEquals(expected, output);
    }


    /*-----------------------------Tests corresponding to testInput5----------------------------------*/

    @Test
    public void testGetTitle5() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput5Path, dictFileDir);
        String expected = "Tower Records ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate5() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput5Path);
        String expected = "10/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal5() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput5Path);
        String expected = "19.99";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory5() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput5Path, "Tower Records");
        String expected = "recreation";
        assertEquals(expected, output);
    }


    /*-----------------------------Tests corresponding to testInput6----------------------------------*/

    @Test
    public void testGetTitle6() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput6Path, dictFileDir);
        String expected = "Tower Records ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate6() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput6Path);
        String expected = "10/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal6() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput6Path);
        String expected = "19.99";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory6() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput6Path, "Tower Records");
        String expected = "recreation";
        assertEquals(expected, output);
    }


    /*-----------------------------Tests corresponding to testInput7----------------------------------*/

    @Test
    public void testGetTitle7() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput7Path, dictFileDir);
        String expected = "Hannons Supervalu ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate7() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput7Path);
        String expected = "04/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal7() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput7Path);
        String expected = "15.19";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory7() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput7Path, "Hannons Supervalu");
        String expected = "food";
        assertEquals(expected, output);
    }

    /*-----------------------------Tests corresponding to testInput7----------------------------------*/

    @Test
    public void testGetTitle8() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput8Path, dictFileDir);
        String expected = "Aldi Stores Ireland ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate8() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput8Path);
        String expected = "06/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal8() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput8Path);
        String expected = "78.25";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory8() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput8Path, "Aldi Stores Ireland");
        String expected = "food";
        assertEquals(expected, output);
    }

}