package com.finalyearproject.dan.ocraccountingapp.imgtotext;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextExtraction {

    private Map<String, Integer> wordFrequencyMap = new HashMap<>();

    public String getTitle(String dirPath, String dataPath) {
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
            System.out.println("text: " + cleanedText);
            br.close();

            // split text up by lines
            String lines[] = cleanedText.split("\\r?\\n");

            LinkedList<String> potentials = new LinkedList<String>();

            int count;
            // prevents out of bounds exception
            if(lines.length<3) count = lines.length;
            else count = 3;


            // loop through first 3 lines
            for(int i = 0; i<count;i++){
                if(lines[i].matches("[a-zA-Z0-9]+(\\s[a-zA-Z0-9]+)*?(\\s)?") && lines[i].length()>3) {

                    // ADD DICTIONARY CHECK HERE
                    // correct misspelled words
                    String corrected = correctWord(lines[i], dataPath);

                    potentials.add(corrected);
                }
            }

            // keep track of the most number of words present in dictionary on any one line
            int largest = 0;
            // Store the best match for the title
            String bestMatch = "";

            for(int i=0; i<potentials.size(); i++) {

                // keep track of the line with the most words present in the dictionary
                int realWordCount = 0;

                System.out.println("potential: " + potentials.get(i));
                String[] temp = potentials.get(i).split(" ");


                for(int j=0; j<temp.length; j++) {
                    System.out.println(wordFrequencyMap.containsKey(temp[j].toLowerCase()));
                    // count how many words on line are present in the dictionary
                    if(wordFrequencyMap.containsKey(temp[j].toLowerCase())){
                        realWordCount++;
                    }
                }

                // if line has the most words present in dict
                if(realWordCount>largest) {
                    largest=realWordCount;
                    bestMatch = potentials.get(i);
                }
            }
            return bestMatch;



        } catch (IOException i) {
            i.printStackTrace();
        }

        return title;
    }



    private String correctWord(String line, final String DATA_PATH) {

        //final String DATA_PATH = activity.getFilesDir() + "/TesseractSample/tessdata/";

        String correctedLine = "";


        System.out.println("WORKING!");
        try {

            createWordCountMap(DATA_PATH + "titleDict");

            // store each word in an array
            String[] words = line.split(" ");
            String output;

            for(int i=0; i<words.length; i++) {
                String word = words[i].toLowerCase();
                if(word.length() > 2) {
                    String correction = getCorrectedWord(word);
                    //capatilise the first letter of each word
                    output = correction.substring(0, 1).toUpperCase() + correction.substring(1);
                }
                else {
                    output = word.toUpperCase();
                }
                correctedLine += output + " ";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return correctedLine;
    }


    private void createWordCountMap(String filename) throws IOException {
        File file = new File(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file)));
        String line;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.toLowerCase().split("[\\s\\p{Punct}]+");
            for (String word : tokens) {
                if (word.length() != 0) {
                    wordFrequencyMap
                            .put(word,
                                    ((wordFrequencyMap.containsKey(word)) ? wordFrequencyMap
                                            .get(word) + 1 : 1));
                }
            }
        }

    }

    private Map<Integer, Set<String>> getPossibleWords(String wrongWord) {
        Map<Integer, Set<String>> possibleWordsMap = new HashMap<Integer, Set<String>>();
        Set<String> editDist1Words = getEditDistance1WordList(wrongWord);
        Set<String> editDist2Words = new HashSet<String>();
        for (String word : editDist1Words) {
            Set<String> temp = getEditDistance1WordList(word);
            temp.retainAll(wordFrequencyMap.keySet());
            editDist2Words.addAll(temp);
        }
        editDist1Words.retainAll(wordFrequencyMap.keySet());
        possibleWordsMap.put(1, editDist1Words);
        possibleWordsMap.put(2, editDist2Words);
        return possibleWordsMap;
    }

    private Set<String> getEditDistance1WordList(String wrongWord) {
        Set<String> wordSet = new HashSet<String>();
        String alphabet = "abcdefghijklmnopqrstuvwxyz";

        for (int j = 0; j < alphabet.length(); j++)
            wordSet.add(wrongWord + alphabet.charAt(j));
        for (int i = 0; i < wrongWord.length(); i++) {

            for (int j = 0; j < alphabet.length(); j++) {
                wordSet.add(wrongWord.substring(0, i) + alphabet.charAt(j)
                        + wrongWord.substring(i));
                wordSet.add(wrongWord.substring(0, i) + alphabet.charAt(j)
                        + wrongWord.substring(i + 1));

            }

            wordSet.add(wrongWord.substring(0, i) + wrongWord.substring(i + 1));

            if (i < wrongWord.length() - 1)
                wordSet.add(wrongWord.substring(0, i) + wrongWord.charAt(i + 1)
                        + wrongWord.charAt(i) + wrongWord.substring(i + 2));

        }

        return wordSet;
    }

    private String getCorrectedWord(String wrongWord) {
        Map<Integer, Set<String>> possibleWords = getPossibleWords(wrongWord);
        int max = Integer.MIN_VALUE;
        String correction = wrongWord;

        if (wordFrequencyMap.containsKey(wrongWord))
            return wrongWord;
        else if (!possibleWords.get(1).isEmpty()) {

            for (String word : possibleWords.get(1)) {
                if (wordFrequencyMap.get(word) > max) {
                    max = wordFrequencyMap.get(word);
                    correction = word;
                }
            }
            return correction;
        } else if (!possibleWords.get(2).isEmpty()) {
            for (String word : possibleWords.get(2)) {
                if (wordFrequencyMap.get(word) > max) {
                    max = wordFrequencyMap.get(word);
                    correction = word;
                }
            }

            return correction;
        } else
            return correction;
    }

    @Override
    public String toString() {
        return "SpellChecker [wordFrequencyMap=" + wordFrequencyMap + "]";
    }





























    public String getDate(String dirPath) {
        String date = "";
        String potentials = "";

        try {
            FileReader fr = new FileReader(dirPath);
            BufferedReader br = new BufferedReader(fr);

            while (br.ready()) {
                String s = br.readLine().toLowerCase();

                String[] words = s.split("\\s");

                // search for the line that contains information about date
                for (int x=0; x<words.length; x++) {
                    if(words[x].matches("[0-9a-zA-Z]+/[0-9a-zA-Z]{2}/[0-9a-zA-Z]+")
                            || words[x].matches("[0-9a-zA-Z]+[.][0-9a-zA-Z]{2}[.][0-9a-zA-Z]+")
                            || words[x].matches("[0-9a-zA-Z]+[-][0-9a-zA-Z]{2}[-][0-9a-zA-Z]+")) {

                        String dateOnly = words[x];

                        // replace mistaken 1's
                        dateOnly = dateOnly.replaceAll("l", "1");
                        dateOnly = dateOnly.replaceAll("I", "1");
                        dateOnly = dateOnly.replaceAll("i", "1");
                        // replace mistaken 0's
                        dateOnly = dateOnly.replaceAll("o", "0");
                        dateOnly = dateOnly.replaceAll("O", "0");
                        //change '.' and '-' to '/'
                        dateOnly = dateOnly.replaceAll("[.]", "/");
                        dateOnly = dateOnly.replaceAll("-", "/");

                        dateOnly = dateOnly.replaceAll(",", "");

                        System.out.println(dateOnly);


                        try {
                            if (dateOnly.contains("/")) {
                                StringBuilder sb = new StringBuilder(dateOnly);
                                sb.indexOf("/");
                                sb.lastIndexOf("/");
                                System.out.println("first index: " + sb.indexOf("/"));
                                System.out.println("last index: " + sb.lastIndexOf("/"));

                                // if the date is in the form xx/xx/20xx
                                if (dateOnly.substring(sb.lastIndexOf("/") + 1, sb.lastIndexOf("/") + 3).equals("20")) {
                                    // remove any extra characters that may have been added on to the end
                                    if (dateOnly.length() > sb.lastIndexOf("/") + 5) {
                                        System.out.println(dateOnly.substring(0, sb.lastIndexOf("/") + 5));
                                        dateOnly = dateOnly.substring(0, sb.lastIndexOf("/") + 5);
                                    }
                                }
                                // if the date is in the form xx/xx/xx
                                else {
                                    if (dateOnly.length() > sb.lastIndexOf("/") + 3) {
                                        System.out.println(dateOnly.substring(0, sb.lastIndexOf("/") + 3));
                                        dateOnly = dateOnly.substring(0, sb.lastIndexOf("/") + 3);
                                    }
                                }

                                // remove extra characters added on the the start of the date
                                if (sb.indexOf("/") != 2) {
                                    // find the difference between the correct index and the actual index
                                    int temp = sb.indexOf("/") - 2;
                                    // remove the difference from the start of the string
                                    System.out.println(dateOnly.substring(temp));
                                    dateOnly = dateOnly.substring(temp);
                                }
                            }
                        } catch (Exception ignored) {}


                        potentials +=dateOnly + "\r\n";
                    }
                }
                potentials += "\r\n";
            }
            System.out.println("date pots: " + potentials);

            br.close();

            // store all found potential dates in an array
            String[] datePotentials = potentials.split("\\r?\\n");
            // create another array to store the formatted dates
            //String[] formattedDatePotentials = new String[datePotentials.length];
            LinkedList<String> formattedDatePotentials = new LinkedList<String>();

            for(int i = 0; i<datePotentials.length;i++){
                if(datePotentials[i].matches("[0-9][0-9]/[0-9][0-9]/[0-9]{4}")) {
                    formattedDatePotentials.add(datePotentials[i]);
                }
                // if date is in the form xx/xx/xx
                if(datePotentials[i].matches("[0-9][0-9]/[0-9][0-9]/[0-9]{2}")) {
                    String[] temp = datePotentials[i].split("/");
                    // get date in the form xx/xx/20xx
                    formattedDatePotentials.add(temp[0] + "/" + temp[1] + "/20" + temp[2]);
                }
                // if date is in the form xx/xx/xxx usually if that last char was missed
                if(datePotentials[i].matches("[0-9][0-9]/[0-9][0-9]/[0-9]{3}")) {
                    String[] temp = datePotentials[i].split("/");
                    // get the current year
                    String tempYear = new SimpleDateFormat("yyyy").format(Calendar.getInstance().getTime());
                    // extract the last character
                    String lastChar = tempYear.substring(tempYear.length()-1);
                    // get date in the form xx/xx/xxxx
                    formattedDatePotentials.add(temp[0] + "/" + temp[1] + "/" + temp[2] + lastChar);
                }
            }

            //todays date
            String todayString = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
            int today = Integer.parseInt(todayString);

            int theDate = 0;

            // search formattedDatePotentials for the date closest to todays date
            for(int i = 0; i<formattedDatePotentials.size(); i++) {

                String[] tempStore = formattedDatePotentials.get(i).split("/");
                String rightFormat = tempStore[2] + tempStore[1] + tempStore[0];
                int tempDate = Integer.parseInt(rightFormat);

                if((today-tempDate < today-theDate) && tempDate<today) {
                    theDate = tempDate;
                    date = formattedDatePotentials.get(i);
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

        System.out.println("date is: " + date);

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

                        // accounts for totals that have become split up e.g. €1 2 . 3 4
                        if(words[x].contains("€")) {
                            s = s.replaceAll(" ", "");
                            String[] temp = s.split("€");
                            String extractedPrice = findTotal(temp[1]);
                            cleanedText += extractedPrice + " \r\n";
                        }

                        System.out.println("words[x] = " + s);
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
                    System.out.println("potential total: " + potentialTotals[i]);
                }
            }

            total = 0;
            for(int i=0; i<potentialTotals.length; i++){
                if (potentialTotals[i] > total) {
                    total = potentialTotals[i];
                }
            }

            System.out.println("total is = " + total);

            // check if first char of potential total was read falsely
            for(int i=0; i<potentialTotals.length; i++){

                String temp = String.valueOf(total);
                temp = temp.substring(1, temp.length());

                double intTemp = Double.parseDouble(temp);

                if(intTemp==potentialTotals[i]) {
                    total = intTemp;
                    break;
                }
            }


        } catch (IOException i) {
            i.printStackTrace();
        }

        //if no value was found for total yet, check lastResort
        if(total==0) {
            // store every word in lastResort in an array
            String[] potentials = lastResort.split(" ");
            double temp;
            double max = 0;
            double secondLargest = 0;

            // loop through potentials and find the second largest number.
            // largest number will be the cash tendered
            for(int i=0; i<potentials.length; i++) {
                System.out.println("pot:" + potentials[i] );
                String extractedPrice = findTotal(potentials[i]);
                if(extractedPrice.matches("(\\s)*[0-9]+\\.[0-9][0-9](\\s)*")) {
                    temp = Double.parseDouble(extractedPrice);
                    if(temp>max && temp <500){

                        secondLargest = max;

                        max = temp;
                    }
                }
            }
            if(secondLargest!=0) {
                return String.valueOf(secondLargest);
            }
            else {
                return String.valueOf(max);
            }
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






    public String getCategory(String dirPath, String titleCorrect) {

        System.out.println("TITLE CORRECT:" + titleCorrect);

        String category = "";

        String[] foodArray = {
                "food", "grocery", "fat", "rice", "chips", "fresh", "eggs", "fruit", "meat", "aldi",
                "supervalu", "tesco", "lidl", "dunnes", "spar", "eurospar", "londis", "centra",
                "mace", "gala", "costcutter", "applegreen", "supermacs", "mcdonalds", "rockets", "kfc"
        };
        String[] utilitiesArray = {
                "electricity", "bill", "electric", "pinergy", "vodafone", "meteor", "maxol",
                "topaz", "eir", "hardware", "electrical"
        };
        String[] transportArray = {
                "bus", "train", "petrol", "fuel", "car", "luas"
        };
        String[] clothingArray = {
                "shoes", "boot", "jeans", "top", "dress","penneys", "tk", "maxx"
        };
        String[] recreationArray = {
                "bar", "tower records", "movie", "cinema", "music", "electronic", "book", "books", "game", "accessories"
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
                category = wordStore.get(words[i]);
            }
        }
        System.out.println("Category: " + category);

        // if catagory has still not been decided
        if(category.equals("")) {

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
                        category = wordStore.get(wordList[i]);
                        return category;
                    }
                }

            }catch (IOException ignored) {}
        }
        return category;
    }
}
