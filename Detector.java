

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Detector {

	private Dictionary globalDictionary;
	private Dictionary userDictionary;
	private Dictionary ignoredWords;
	
	private HashMap<String, List<Integer>> wordList;  // renamed from textWordList

	public Detector() {
		String globalFilePath = System.getProperty("user.dir") + "/src/main/java/dictionary/words_alpha.txt";
		String localFilePath = System.getProperty("user.dir") + "/src/main/java/dictionary/my_words.txt";
		globalDictionary = new Dictionary(globalFilePath);
		userDictionary = new Dictionary(localFilePath); 
		ignoredWords = new Dictionary();
		wordList = new HashMap<String, List<Integer>>();
	}


	public Detector(String globalFile, String localFile, HashMap<String, List<Integer>> wordList) {
		globalDictionary = new Dictionary(globalFile);
		userDictionary = new Dictionary(localFile); 
		ignoredWords = new Dictionary();
		this.wordList = wordList;
	}
	
	
	public Detector(Dictionary globalDictionary, Dictionary localDictionary, HashMap<String, List<Integer>> wordList) {
		this.globalDictionary = globalDictionary;
		this.userDictionary = localDictionary; 
		ignoredWords = new Dictionary();
		this.wordList = wordList;
	}
	
	public Dictionary getGlobalDictionary() {
		return globalDictionary;
	}
	
	public Dictionary getUserDictionary() {
		return userDictionary;
	}
	
	public Dictionary getIgnoredWords() {
		return ignoredWords;
	}
	
	public void ignoreOnce(String currWord) {
		// Do something to remove the error symbols in the current word
	}
	
	public void ignoreAll(String currWord) {
		ignoredWords.addWord(currWord);
	}
	
	public void addUserWord(String currWord) {
		userDictionary.addWord(currWord);
	}
	
	public void removeUserWord(String currWord) {
		userDictionary.removeWord(currWord);
	}
	
	public void clearUserDictionary() {
		userDictionary = new Dictionary();
	}
	
	public void updateTextList(HashMap<String, List<Integer>> textList) {
		wordList = textList;
	}
	
	public boolean isValidWord(String currWord, int index) {
		return getErrorType(currWord, index).equals("No Error");
	}

	public String getErrorType(String currWord, int index) {
		
		String cleanedWord = cleanPunctuation(currWord); 
		
		if (isXmlWord(cleanedWord)) return "No Error"; 
		if (!isCorrectSpelling(cleanedWord)) return "Spelling Error";
		if (isDoubleWord(cleanedWord, index)) return "Double Word Error";
		if (!isCaseCorrect(cleanedWord, index)) return "Capitalization Error";
		return "No Error";
	}

	private boolean isCorrectSpelling(String currWord) {
		if (ignoredWords == null) return globalDictionary.hasWord(currWord.toLowerCase()) || userDictionary.hasWord(currWord.toLowerCase());
		else return globalDictionary.hasWord(currWord.toLowerCase()) 
				|| userDictionary.hasWord(currWord.toLowerCase())
				|| ignoredWords.hasWord(currWord.toLowerCase());
	}
	
	private String cleanPunctuation(String word) {
		
		String cleanedWord = word; 
		if(isWordEndsWithPunctuation(word)) {
			cleanedWord = word.substring(0, word.length() -1);
		}
		return cleanedWord; 
	}
	
	private boolean isWordEndsWithPunctuation(String word) {
		
        String punctuation = ".,;:!?";
        return punctuation.contains(word.substring(word.length() - 1));
		
	}
	
	private boolean isXmlWord(String word) {
		return word.startsWith("<") && word.endsWith(">");
	}

	// Later issue
	private boolean isDoubleWord(String currWord, int index) {
		if (wordList == null) return false; 
		//return (wordList.get(currWord).contains(index-1) && wordList.get(currWord).contains(index-2));
		return false;
	}

	private boolean isCaseCorrect(String currWord, int index) { 

		if (!isFirstWord(currWord, index)) {
			return (currWord.toLowerCase().equals(currWord)) || currWord.toUpperCase().equals(currWord);
		} else {
			return (currWord.substring(0, 1).toUpperCase().equals(currWord.substring(0, 1))
					&& currWord.substring(1).toLowerCase().equals(currWord.substring(1))) 
					|| currWord.toUpperCase().equals(currWord);
		}
	}

	private boolean isFirstWord(String currWord, int index) {
		if(wordList == null) return false; // For test cases
		int currIndex = index - 1;
		if(currIndex == 0) return true; 
		String prevWord = findKey(wordList, currIndex-1).trim();
		return prevWord.endsWith(".") || prevWord.endsWith("!") || prevWord.endsWith("?");
	}
	
	private String findKey(HashMap<String, List<Integer>> wordList, int index) {
		for (String key : wordList.keySet()) {
			if (wordList.get(key).contains(index)) return key;
		}
		return "";
	}
}