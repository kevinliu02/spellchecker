
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Corrector {

	private Detector detector;

	public Corrector() {
		detector = new Detector();
	}
	
	public Corrector(Dictionary globalDict, Dictionary userDict, HashMap<String, List<Integer>> textList) {
		detector = new Detector(globalDict, userDict, textList);
	}

	/*public String correctWord(String currWord) {
		String errorType = detector.getErrorType(currWord);
		if (errorType.equals("Spelling Error"))
			return suggestCorrectSpellings(currWord).get(0);
		else if (errorType.equals("Double Word Error"))
			return correctDoubleWord(currWord);
		else if (errorType.equals("Capitalization Error"))
			return correctCapitalization(currWord);
		else
			return currWord;
	}*/
	
	public Detector getDetector() {
		return detector;
	}

public List<String> suggestCorrectSpellings(String currWord, int index) {
		
		Set<String> correctedSpellingsSet = new HashSet<>();
		
		correctedSpellingsSet.addAll(getRemoveLetterCorrectionSet(currWord, index));
		correctedSpellingsSet.addAll(getInsertLetterCorrectionSet(currWord, index));
		correctedSpellingsSet.addAll(getInsertSpaceCorrectionSet(currWord, index));
		correctedSpellingsSet.addAll(getSwapLetterCorrectionSet(currWord, index));

		List<String> correctedSpellingsList = new ArrayList<String>(correctedSpellingsSet); 
		
		Collections.sort(correctedSpellingsList);
		return correctedSpellingsList; 
	}
	
	public Set<String> getRemoveLetterCorrectionSet(String currWord, int index) {
		
		Set<String> correctedSet = new HashSet<>(); 
		for(int i = 0; i < currWord.length(); i++) {
			String newWord = removeLetter(currWord, i); 
			if(detector.getErrorType(newWord, index).equals("No Error")) {
				correctedSet.add(newWord);
			}
		}
		
		return correctedSet; 
	}
	
	public Set<String> getInsertLetterCorrectionSet(String currWord, int index) {
		
		Set<String> correctedSet = new HashSet<>(); 
		for(int i = 0; i < currWord.length(); i++) {
			String[] newWords = insertLetter(currWord, i); 
			for(String newWord : newWords) {
				if(detector.getErrorType(newWord, index).equals("No Error")) {
					correctedSet.add(newWord);
				}
			}
		}
		return correctedSet; 
	}

	public Set<String> getSwapLetterCorrectionSet(String currWord, int index) {
		
		Set<String> correctedSet = new HashSet<>(); 
		for(int i = 0; i < currWord.length() - 1; i++ ) {
			String newWord = swapLetters(currWord, i);
			if(detector.getErrorType(newWord, index).equals("No Error")) {
				correctedSet.add(newWord);
			}
		}
		return correctedSet; 
	}

	public Set<String> getInsertSpaceCorrectionSet(String currWord, int index) {

		Set<String> correctedSet = new HashSet<>(); 
		for(int i = 0; i < currWord.length() - 1; i++ ) {
			String newWord = insertSpace(currWord, i);
			boolean validWord = true; 
			for(String splitWord : newWord.split(" ")) {
				if(detector.getErrorType(splitWord, index).equals("No Error")) {
					validWord &= true; 
				} else {
					validWord &= false; 
				}
			}
			if(validWord) {
				correctedSet.add(newWord);
			}
		}
			return correctedSet; 
	}
	
	
	private String removeLetter(String word, int index) {
		
		char[] letters = word.toCharArray();
		char[] newLetters = new char[letters.length - 1];
		for(int i = 0; i < letters.length; i++) {
			if(i < index) {
				newLetters[i] = letters[i];
			}
			// ignore if i = index; 
			if (i > index) {
				newLetters[i - 1] = letters[i]; 
			}
		}
				
		return String.valueOf(newLetters);
	}
	
	private String[] insertLetter(String word, int index) {
		char[] alphabet = "abcdefghijklmnopqrstuvwxyz-".toCharArray();
		String[] newWords = new String[27];
		for(int i = 0; i < 27; i++) {
			newWords[i] = word.substring(0, index + 1) + alphabet[i] + word.substring(index + 1);
		}
		return newWords; 
	}
	
	private String swapLetters(String word, int index) {
		
		char[] letters = word.toCharArray();
		char temp = letters[index];
		letters[index] = letters[index +1];
		letters[index + 1] = temp; 
		return String.valueOf(letters); 
		
	}
	
	private String insertSpace(String word, int index) {
		return word.substring(0, index + 1) + " " + word.substring(index + 1);
	}
	
	
	private String correctDoubleWord(String currWord) {
		return ""; // Remove instance of that word
	}

	private String suggestCapitalization(String currWord, String prevWord) {
		if (this.isFirstWordInSentence(currWord, prevWord))
			return currWord.substring(0, 1).toUpperCase() + currWord.substring(1).toLowerCase();
		else
			return currWord.toLowerCase();
	}

	private boolean isFirstWordInSentence(String currWord, String prevWord) {
		return (prevWord.endsWith(".") || prevWord.endsWith("?") || prevWord.endsWith("!"));
	}

}
