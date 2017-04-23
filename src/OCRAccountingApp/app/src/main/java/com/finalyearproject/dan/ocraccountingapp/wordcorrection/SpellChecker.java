package com.finalyearproject.dan.ocraccountingapp.wordcorrection;

import android.app.Activity;

import java.io.*;
import java.util.*;

public class SpellChecker {


    public String correctWord(String line, String dicttype, Activity activity) {

		final String DATA_PATH = activity.getFilesDir() + "/TesseractSample/tessdata/";


        String correctedLine = "";
        SpellChecker s = new SpellChecker();


		System.out.println("WORKING!");
        try {
            // If we're looking for the title
            if(dicttype.equals("title")){
                s.createWordCountMap(DATA_PATH + "titleDict");
            }
            // if we're looking for total
            else{
                s.createWordCountMap(DATA_PATH + "totalDict");
            }
            // store each word in an array
            String[] words = line.split(" ");
            String output;

            for(int i=0; i<words.length; i++) {
                String word = words[i].toLowerCase();
                if(word.length() > 2) {
                    String correction = s.getCorrectedWord(word);
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

	private Map<String, Integer> wordFrequencyMap = new HashMap<String, Integer>();

	private void createWordCountMap(String filename) throws IOException {
		File file = new File(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		String line = null;
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

}
