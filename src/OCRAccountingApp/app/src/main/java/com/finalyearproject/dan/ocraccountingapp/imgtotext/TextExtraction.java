package com.finalyearproject.dan.ocraccountingapp.imgtotext;

import android.app.Activity;
import android.util.Log;

import com.finalyearproject.dan.ocraccountingapp.wordcorrection.SpellChecker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextExtraction {

    public String getTitle(String dirPath, Activity activity) {
        String title = "";

        try {
            FileReader fr = new FileReader(dirPath);
            BufferedReader br = new BufferedReader(fr);

            String cleanedText = "";

            while (br.ready()) {
                String s = br.readLine();

                String[] words = s.split("\\s");


                for (int x=0; x<words.length; x++) {
                    if(words[x].length() >3 && words[x].length() <12) {

                        // split the letters up individually
                        String[] letters = words[x].split("(?!^)");

                        String word = "";
                        for (int j=0; j<letters.length; j++) {
                            if(letters[j].matches("[a-zA-Z0-9]")){
                                word += letters[j];
                            }
                        }
                        cleanedText += word + " ";
                    }
                }
                cleanedText += "\r\n";
            }
            //System.out.println(cleanedText);
            Log.e("text", cleanedText);
            br.close();

            String lines[] = cleanedText.split("\\r?\\n");
            System.out.println("CHECK LINE 0: " + lines[0]);

            //loop through lines until text is found
            for(int i = 0; i<lines.length;i++){
                if(lines[i].matches("[a-zA-Z0-9]+(\\s[a-zA-Z0-9]+)*?(\\s)?") && lines[i].length()>3) {

                    //ADD DICTIONARY CHECK HERE
                    SpellChecker spellChecker = new SpellChecker();
                    // correct misspelled words
                    String corrected = spellChecker.correctWord(lines[i], "title", activity);
                    return corrected;
                }
            }
        } catch (IOException i) {
            i.printStackTrace();
        }

        return title;
    }


    public String getDate(String dirPath) {
        String date = "";
        String potentials = "";

        try {
            FileReader fr = new FileReader(dirPath);
            BufferedReader br = new BufferedReader(fr);

            while (br.ready()) {
                String s = br.readLine().toLowerCase();;

                String[] words = s.split("\\s");

                // search for the line that contains information about date
                for (int x=0; x<words.length; x++) {
                    if(words[x].matches("[^A-Z]+/[0-9a-z]+/[^A-Z]+")) {

                        String dateOnly = words[x];

                        if(dateOnly.contains("/")) {
                            StringBuilder sb = new StringBuilder(dateOnly);
                            sb.indexOf("/");
                            sb.lastIndexOf("/");
                            System.out.println("first index: " + sb.indexOf("/"));
                            System.out.println("last index: " + sb.lastIndexOf("/"));

                            // remove any extra characters that may have been added on to the end
                            if (dateOnly.length() > sb.lastIndexOf("/") + 5) {
                                System.out.println(dateOnly.substring(0, sb.lastIndexOf("/") + 5));
                                dateOnly = dateOnly.substring(0, sb.lastIndexOf("/") + 5);
                            }

                            // remove extra characters added on the the start of the date
                            if(sb.indexOf("/") != 2) {
                                // find the difference between the correct index and the actual index
                                int temp = sb.indexOf("/") - 2;
                                // remove the difference from the start of the string
                                System.out.println(dateOnly.substring(temp));
                                dateOnly = dateOnly.substring(temp);
                            }
                        }


                        potentials +=dateOnly;
                    }
                }
                potentials += "\r\n";
            }
            System.out.println(potentials);

            br.close();

            String[] datePotentials = potentials.split("\\r?\\n");
            for(int i = 0; i<datePotentials.length;i++){
                if(datePotentials[i].matches("[0-9][0-9]/[0-9][0-9]/[0-9]{4}")) {
                    date = datePotentials[i];
                }
            }

            System.out.println("Date: " + date);

        } catch (IOException i) {
            i.printStackTrace();
        }

        // if date is not found, default to todays day
        if(date.equals("")) {
            date = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
        }

        return date;
    }







    public String getTotal(String dirPath) {
        double total = 0;

        // last resort will contain all words that contain '€'
        String lastResort = "";

        try {
            FileReader fr = new FileReader(dirPath);
            BufferedReader br = new BufferedReader(fr);

            String cleanedText = "";


            while (br.ready()) {
                String s = br.readLine().toLowerCase();
                String[] words = s.split("\\s");

                // search for the line that contains information about total
                for (int x=0; x<words.length; x++) {
                    if(s.contains("total") || s.contains("tot") || s.contains("otal") || s.contains("sale") ) { // || s.contains("tal") || s.matches("\\d[t|l][a-z]{3}[l|1|i]")

                        String extractedPrice = findTotal(words[x]);
                        cleanedText += extractedPrice + " ";

                    }
                    // "tal" is more common than "tot" and "otal", needs to be more specific
                    else if(words[x].matches("[a-z][a-z]tal")) {
                        String extractedPrice = findTotal(words[x+1]);
                        cleanedText += extractedPrice + " ";
                    }
                    else if(words[x].contains("€")){
                        lastResort += words[x] + " ";
                    }
                }
                cleanedText += "\r\n";
            }

            br.close();
            System.out.println("TOTAL: " + cleanedText);

            String lines[] = cleanedText.split("\\r?\\n");
            //loop through lines until text is found

            double[] potentialTotals = new double[lines.length];
            Arrays.fill(potentialTotals,0);

            for(int i = 0; i<lines.length;i++){
                if(lines[i].matches("(\\s)*[0-9]+\\.[0-9][0-9](\\s)*")) {
                    potentialTotals[i] = Double.parseDouble(lines[i]);
                }
            }

            total = 0;
            for(int i=0; i<potentialTotals.length; i++){
                if (potentialTotals[i] > total) {
                    total = potentialTotals[i];
                }
            }

        } catch (IOException i) {
            i.printStackTrace();
        }

        //if no value was found for total yet, check lastResort
        if(total==0) {
            // store every word in lastResort in an array
            String[] potentials = lastResort.split(" ");
            double temp = 0;
            double max = 0;

            for(int i=0; i<potentials.length; i++) {
                String extractedPrice = findTotal(potentials[i]);
                System.out.println(extractedPrice);
                if(extractedPrice.matches("(\\s)*[0-9]+\\.[0-9][0-9](\\s)*")) {
                    temp = Double.parseDouble(extractedPrice);
                    if(temp>max && temp <500){
                        max = temp;
                    }
                }
            }
            return String.valueOf(max);
        }

        String totalString = String.valueOf(total);
        //add an extra 0 if the total ends in a 0 to achieve the format €xx.xx
        if(totalString.substring(totalString.length()-1).equals("0")) {
            totalString = totalString + "0";
        }

        return totalString;
    }


    private String findTotal(String word) {
        //System.out.println("total found here: " + s);
        // '.' is sometimes recognised as ','
        String numberOnly = word.replaceAll(",", ".");
        // '1' is sometimes recognised as 'l'
        numberOnly = numberOnly.replaceAll("l", "1");
        // '1' is sometimes recognised as 'l'
        numberOnly = numberOnly.replaceAll("i", "1");
        // replace chars 'a'-'z' and '-' with .
        numberOnly = numberOnly.replaceAll("[a-z]|-", ".");

        System.out.println("potential total: " + numberOnly);


        if(numberOnly.length()>3) {

            // check if the string contains a '.'
            if(numberOnly.contains(".")) {
                StringBuilder sb1 = new StringBuilder(numberOnly);
                sb1.lastIndexOf(".");
                System.out.println("index: " + sb1.lastIndexOf("."));
                if (numberOnly.length() > sb1.lastIndexOf(".") + 3) {
                    System.out.println(numberOnly.substring(0, sb1.lastIndexOf(".") + 3));
                    numberOnly = numberOnly.substring(0, sb1.lastIndexOf(".") + 3);
                }
            }

            // convert the character at string length-3 to '.'
            System.out.println("LENGTH: " +numberOnly.length());
            StringBuilder sb = new StringBuilder(numberOnly);
            sb.setCharAt(numberOnly.length()-3, '.');
            numberOnly = sb.toString();
        }

        System.out.println("string: " +numberOnly);

        //numberOnly = numberOnly.replaceAll("[^0-9\\.]+", "");

        // Find the part of the string that matches the form x+.xx, e.g. 53.11
        Pattern p = Pattern.compile("[0-9]+[\\.|[a-zA-Z]][0-9][0-9]");
        Matcher m = p.matcher(numberOnly);

        String extractedPrice = "";
        while (m.find()) {
            extractedPrice = m.group(0);
        }
        return extractedPrice;
    }


    public String getCatagory(String dirPath, String titleCorrect) {

        String catagory = "";

        String[] foodArray = {
                "food", "grocery", "fat", "rice", "chips", "fresh", "eggs", "fruit", "meat", "aldi",
                "supervalu", "tesco", "lidl", "dunnes", "spar", "eurospar", "londis", "centra",
                "mace", "gala", "costcutter", "applegreen", "supermacs", "mcdonalds", "rockets", "kfc"
        };
        String[] utilitiesArray = {
                "electricity", "phone", "bill", "electric", "pinergy", "vodafone", "meteor", "maxol",
                "topaz", "eir",
        };
        String[] transportArray = {
                "bus", "train", "petrol", "fuel", "car", "luas"
        };
        String[] clothingArray = {
                "shoes", "boot", "jeans", "top", "dress","penneys", "tk", "maxx"
        };
        String[] recreationArray = {
                "bar", "movie", "cinema", "music", "electronic", "book", "game"
        };
        String[] healthArray = {
                "pharmacy", "doctor", "health", "boots","lloyds","mccabes","hickeys"
        };

        Hashtable<String,String> wordStore = new Hashtable<String,String>();

        for (int i = 0; i < foodArray.length; i++) {
            wordStore.put(foodArray[i],"food");
        }
        for (int i = 0; i < utilitiesArray.length; i++) {
            wordStore.put(utilitiesArray[i],"utilities");
        }
        for (int i = 0; i < transportArray.length; i++) {
            wordStore.put(transportArray[i],"transport");
        }
        for (int i = 0; i < clothingArray.length; i++) {
            wordStore.put(clothingArray[i],"clothing");
        }
        for (int i = 0; i < recreationArray.length; i++) {
            wordStore.put(recreationArray[i], "recreation");
        }
        for (int i = 0; i < healthArray.length; i++) {
            wordStore.put(healthArray[i],"health");
        }

        boolean keyExists = wordStore.containsKey("doctor");
        System.out.println(keyExists);

        titleCorrect = titleCorrect.toLowerCase();
        String[] words = titleCorrect.split(" ");

        for(int i=0; i<words.length; i++) {
            if(wordStore.containsKey(words[i])) {
                catagory = wordStore.get(words[i]);
            }
        }

        // if catagory has still not been decided
        if(catagory.equals("")) {

            String cleanedText = "";

            try {
                BufferedReader br = new BufferedReader(new FileReader(dirPath));

                while (br.ready()) {
                    String s = br.readLine().toLowerCase();

                    String[] linewords = s.split("\\s");

                    for (int x = 0; x < linewords.length; x++) {
                        if (linewords[x].length() > 3 && linewords[x].length() < 12 && linewords[x].matches("[^0-9]+")) {

                            // split the letters up individually
                            String[] letters = linewords[x].split("(?!^)");

                            String word = "";
                            for (int j = 0; j < letters.length; j++) {
                                if (letters[j].matches("[a-zA-Z0-9]")) {
                                    word += letters[j];
                                }
                            }
                            cleanedText += word + " ";
                        }
                    }
                }
                System.out.println(cleanedText);
                br.close();

                String[] wordList = cleanedText.split("\\s");

                for(int i=0; i<wordList.length; i++) {
                    if(wordStore.containsKey(wordList[i])) {
                        System.out.println("found word in wordstore is: " + wordList[i]);
                        catagory = wordStore.get(wordList[i]);
                        return catagory;
                    }
                }

            }catch (IOException ignored) {}
        }
        return catagory;
    }
}
