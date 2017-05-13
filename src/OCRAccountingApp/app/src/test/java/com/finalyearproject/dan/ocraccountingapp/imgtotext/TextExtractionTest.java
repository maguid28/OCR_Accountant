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
    private String testInput9Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput9";
    private String testInput10Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput10";
    private String testInput11Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput11";
    private String testInput12Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput12";
    private String testInput13Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput13";
    private String testInput14Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput14";
    private String testInput15Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput15";
    private String testInput16Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput16";
    private String testInput17Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput17";
    private String testInput18Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput18";
    private String testInput19Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput19";
    private String testInput20Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput20";
    private String testInput21Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput21";
    private String testInput22Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput22";
    private String testInput23Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput23";
    private String testInput24Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput24";
    private String testInput25Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput25";
    private String testInput26Path = "/Users/daniel/2017-ca400-maguid28/src/OCRAccountingApp/app/src/test/java/com/finalyearproject/dan/ocraccountingapp/imgtotext/testInput26";


/*-----------------------------Tests corresponding to testInput1----------------------------------*/

    @Test
    public void testGetTitle01() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput1Path, dictFileDir);
        String expected = "Hannons Supervalu ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate01() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput1Path);
        String expected = "04/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal01() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput1Path);
        String expected = "15.19";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory01() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput1Path, "Hannons Supervalu");
        String expected = "food";
        assertEquals(expected, output);
    }



/*-----------------------------Tests corresponding to testInput2----------------------------------*/

    @Test
    public void testGetTitle02() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput2Path, dictFileDir);
        String expected = "Keane Careplus Pharmacy ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate02() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput2Path);
        String expected = "04/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal02() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput2Path);
        String expected = "12.9";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory02() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput2Path, "Keane Careplus Pharmacy");
        String expected = "health";
        assertEquals(expected, output);
    }

/*-----------------------------Tests corresponding to testInput3----------------------------------*/

    @Test
    public void testGetTitle03() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput3Path, dictFileDir);
        String expected = "Keanes Careplus Pharmacy ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate03() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput3Path);
        String expected = "04/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal03() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput3Path);
        String expected = "12.9";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory03() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput3Path, "Keane Careplus Pharmacy");
        String expected = "health";
        assertEquals(expected, output);
    }

/*-----------------------------Tests corresponding to testInput4----------------------------------*/

    @Test
    public void testGetTitle04() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput4Path, dictFileDir);
        String expected = "Cordners Shoes ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate04() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput4Path);
        String expected = "24/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal04() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput4Path);
        String expected = "60.00";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory04() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput4Path, "Cordners Shoes");
        String expected = "clothing";
        assertEquals(expected, output);
    }


    /*-----------------------------Tests corresponding to testInput5----------------------------------*/

    @Test
    public void testGetTitle05() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput5Path, dictFileDir);
        String expected = "Tower Records ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate05() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput5Path);
        String expected = "10/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal05() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput5Path);
        String expected = "19.99";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory05() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput5Path, "Tower Records");
        String expected = "recreation";
        assertEquals(expected, output);
    }


    /*-----------------------------Tests corresponding to testInput6----------------------------------*/

    @Test
    public void testGetTitle06() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput6Path, dictFileDir);
        String expected = "Tower Records ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate06() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput6Path);
        String expected = "10/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal06() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput6Path);
        String expected = "19.99";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory06() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput6Path, "Tower Records");
        String expected = "recreation";
        assertEquals(expected, output);
    }


    /*-----------------------------Tests corresponding to testInput7----------------------------------*/

    @Test
    public void testGetTitle07() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput7Path, dictFileDir);
        String expected = "Hannons Supervalu ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate07() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput7Path);
        String expected = "04/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal07() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput7Path);
        String expected = "15.19";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory07() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput7Path, "Hannons Supervalu");
        String expected = "food";
        assertEquals(expected, output);
    }

    /*-----------------------------Tests corresponding to testInput8----------------------------------*/

    @Test
    public void testGetTitle08() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput8Path, dictFileDir);
        String expected = "Aldi Stores Ireland ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate08() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput8Path);
        String expected = "23/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal08() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput8Path);
        String expected = "78.25";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory08() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput8Path, "Aldi Stores Ireland");
        String expected = "food";
        assertEquals(expected, output);
    }


    /*-----------------------------Tests corresponding to testInput9----------------------------------*/

    @Test
    public void testGetTitle09() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput9Path, dictFileDir);
        String expected = "Tower Records ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate09() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput9Path);
        String expected = "10/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal09() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput9Path);
        String expected = "19.99";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory09() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput9Path, "Tower Records");
        String expected = "recreation";
        assertEquals(expected, output);
    }

    /*-----------------------------Tests corresponding to testInput10----------------------------------*/

    @Test
    public void testGetTitle10() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput10Path, dictFileDir);
        String expected = "Eurospar Santry ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate10() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput10Path);
        String expected = "12/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal10() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput10Path);
        String expected = "7.56";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory10() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput10Path, "Eurospar Santry");
        String expected = "food";
        assertEquals(expected, output);
    }
    /*-----------------------------Tests corresponding to testInput11----------------------------------*/

    @Test
    public void testGetTitle11() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput11Path, dictFileDir);
        String expected = "Tower Records ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate11() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput11Path);
        String expected = "10/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal11() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput11Path);
        String expected = "19.99";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory11() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput11Path, "Tower Records");
        String expected = "recreation";
        assertEquals(expected, output);
    }
    /*-----------------------------Tests corresponding to testInput12----------------------------------*/

    @Test
    public void testGetTitle12() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput12Path, dictFileDir);
        String expected = "Spar Shanowen ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate12() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput12Path);
        String expected = "21/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal12() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput12Path);
        String expected = "9.00";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory12() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput12Path, "Spar Shanowen");
        String expected = "food";
        assertEquals(expected, output);
    }
    /*-----------------------------Tests corresponding to testInput13----------------------------------*/

    @Test
    public void testGetTitle13() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput13Path, dictFileDir);
        String expected = "Cordners Shoes ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate13() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput13Path);
        String expected = "24/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal13() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput13Path);
        String expected = "60.00";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory13() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput13Path, "Cordners Shoes");
        String expected = "clothing";
        assertEquals(expected, output);
    }
    /*-----------------------------Tests corresponding to testInput14----------------------------------*/

    @Test
    public void testGetTitle14() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput14Path, dictFileDir);
        String expected = "Cordners Shoes ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate14() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput14Path);
        String expected = "24/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal14() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput14Path);
        String expected = "60.00";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory14() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput14Path, "Cordners Shoes");
        String expected = "clothing";
        assertEquals(expected, output);
    }
    /*-----------------------------Tests corresponding to testInput15----------------------------------*/

    @Test
    public void testGetTitle15() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput15Path, dictFileDir);
        String expected = "Cordners Shoes ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate15() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput15Path);
        String expected = "24/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal15() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput15Path);
        String expected = "60.00";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory15() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput15Path, "Cordners Shoes");
        String expected = "clothing";
        assertEquals(expected, output);
    }
    /*-----------------------------Tests corresponding to testInput16----------------------------------*/

    @Test
    public void testGetTitle16() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput16Path, dictFileDir);
        String expected = "Claires Accessories ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate16() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput16Path);
        String expected = "05/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal16() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput16Path);
        String expected = "18.97";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory16() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput16Path, "Claires Accessories");
        String expected = "recreation";
        assertEquals(expected, output);
    }
    /*-----------------------------Tests corresponding to testInput17----------------------------------*/

    @Test
    public void testGetTitle17() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput17Path, dictFileDir);
        String expected = "Claires Accessories ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate17() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput17Path);
        String expected = "05/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal17() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput17Path);
        String expected = "18.97";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory17() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput17Path, "Claires Accessories");
        String expected = "recreation";
        assertEquals(expected, output);
    }
    /*-----------------------------Tests corresponding to testInput18----------------------------------*/

    @Test
    public void testGetTitle18() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput18Path, dictFileDir);
        String expected = "Tower Records ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate18() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput18Path);
        String expected = "10/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal18() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput18Path);
        String expected = "19.99";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory18() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput18Path, "Tower Records");
        String expected = "recreation";
        assertEquals(expected, output);
    }
    /*-----------------------------Tests corresponding to testInput19----------------------------------*/

    @Test
    public void testGetTitle19() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput19Path, dictFileDir);
        String expected = "Eurospar Santry ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate19() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput19Path);
        String expected = "12/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal19() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput19Path);
        String expected = "7.56";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory19() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput19Path, "Eurospar Santry");
        String expected = "food";
        assertEquals(expected, output);
    }

    /*-----------------------------Tests corresponding to testInput20----------------------------------*/

    @Test
    public void testGetTitle20() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput20Path, dictFileDir);
        String expected = "Aldi Stores Ireland ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate20() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput20Path);
        String expected = "06/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal20() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput20Path);
        String expected = "2.18";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory20() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput20Path, "Aldi Stores Ireland");
        String expected = "food";
        assertEquals(expected, output);
    }

    /*-----------------------------Tests corresponding to testInput21----------------------------------*/

    @Test
    public void testGetTitle21() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput21Path, dictFileDir);
        String expected = "Spar Shanowen ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate21() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput21Path);
        String expected = "21/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal21() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput21Path);
        String expected = "9.00";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory21() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput21Path, "Spar Shanowen");
        String expected = "food";
        assertEquals(expected, output);
    }

    /*-----------------------------Tests corresponding to testInput22----------------------------------*/

    @Test
    public void testGetTitle22() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput22Path, dictFileDir);
        String expected = "Spar Shanowen ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate22() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput22Path);
        String expected = "26/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal22() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput22Path);
        String expected = "5.19";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory22() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput22Path, "Spar Shanowen");
        String expected = "food";
        assertEquals(expected, output);
    }

    /*-----------------------------Tests corresponding to testInput23----------------------------------*/

    @Test
    public void testGetTitle23() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput23Path, dictFileDir);
        String expected = "Aldi Stores Ireland ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate23() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput23Path);
        String expected = "06/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal23() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput23Path);
        String expected = "2.18";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory23() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput23Path, "Aldi Stores Ireland");
        String expected = "food";
        assertEquals(expected, output);
    }
    /*-----------------------------Tests corresponding to testInput24----------------------------------*/

    @Test
    public void testGetTitle24() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput24Path, dictFileDir);
        String expected = "Tower Records ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate24() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput24Path);
        String expected = "10/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal24() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput24Path);
        String expected = "19.99";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory24() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput24Path, "Tower Records");
        String expected = "recreation";
        assertEquals(expected, output);
    }
    /*-----------------------------Tests corresponding to testInput25----------------------------------*/

    @Test
    public void testGetTitle25() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput25Path, dictFileDir);
        String expected = "Eurospar Santry ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate25() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput25Path);
        String expected = "12/04/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal25() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput25Path);
        String expected = "7.56";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory25() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput25Path, "Eurospar Santry");
        String expected = "food";
        assertEquals(expected, output);
    }

    /*-----------------------------Tests corresponding to testInput26----------------------------------*/

    @Test
    public void testGetTitle26() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTitle(testInput26Path, dictFileDir);
        String expected = "Tesco ";
        assertEquals(expected, output);
    }

    @Test
    public void testGetDate26() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getDate(testInput26Path);
        String expected = "25/03/2017";
        assertEquals(expected, output);
    }

    @Test
    public void testGetTotal26() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getTotal(testInput26Path);
        String expected = "18.81";
        assertEquals(expected, output);
    }

    @Test
    public void testGetCategory26() throws Exception {

        TextExtraction textExtraction = new TextExtraction();
        String output = textExtraction.getCategory(testInput26Path, "Tesco");
        String expected = "food";
        assertEquals(expected, output);
    }

}