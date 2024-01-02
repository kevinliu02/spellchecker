import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Scanner;

public class Dictionary {

	private Set<String> dictionary;

	public Dictionary() {
		dictionary = new HashSet<>();
	}

	public Dictionary(String filePath) {
		dictionary = new HashSet<>(); 
		File dict = new File(filePath);
		Scanner sDict = null;
		try{
			sDict = new Scanner(dict);
		} catch (FileNotFoundException e){}
		while (sDict.hasNextLine()){
			dictionary.add(sDict.nextLine());
		}
	}
	
	public Dictionary(ArrayList<String> wordList) {
		dictionary.addAll(wordList);
	}

	public void addWord(String word) {
		dictionary.add(word);
	}

	public void removeWord(String word) {
		dictionary.remove(word);
	}

	public void updateDictionary(String word, String replacedWord) {
		dictionary.remove(word);
		dictionary.add(replacedWord);
	}

	public Set<String> getDictionary() {
		return dictionary;
	}

	public boolean hasWord(String word) {
		return dictionary.contains(word);
	}
}
