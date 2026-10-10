import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class WordDictionary {

    private static final char DOT = '.';

    private TrieNode root;

    public WordDictionary() {
        root = new TrieNode();
    }
    
    public void addWord(String word) {
        TrieNode currentNode = root;
        for (int i = 0; i < word.length(); i++) {
            char val = word.charAt(i);
            TrieNode node = currentNode.childs.get(val);
            if (node == null) {
                node = new TrieNode();
                currentNode.childs.put(val, node);
            }

            currentNode = node;
        }

        currentNode.hasWord = true;
    }
    
    public boolean search(String word) {
        Collection<TrieNode> coll = Arrays.asList(root);
        for (int i = 0; i < word.length(); i++) {
            char val = word.charAt(i);
            Collection<TrieNode> currentColl = new ArrayList<>();
            for (TrieNode node : coll) {
                if (val == DOT) {
                    currentColl.addAll(node.childs.entrySet().stream().map(entry -> entry.getValue()).toList());
                } else {
                    TrieNode found = node.childs.get(val);
                    if (found != null) {
                        currentColl.add(found);
                    }
                }
            }
        }

        return coll.stream().map(item -> item.hasWord).reduce((a,b) -> a || b).orElse(false);
    }
    
    //method from older implementation
    public boolean searchWithoutDots(String word) {
        TrieNode currentNode = root;
        for (int i = 0; i < word.length(); i++) {
            char val = word.charAt(i);
            TrieNode node = currentNode.childs.get(val);
            if (node == null) {
                return Boolean.FALSE;
            }

            currentNode = node;
        }

        return currentNode.hasWord;
    }

    private static class TrieNode {
        Map<Character, TrieNode> childs = new HashMap<>();
        boolean hasWord;
    }
}

/**
 * Your WordDictionary object will be instantiated and called as such:
 * WordDictionary obj = new WordDictionary();
 * obj.addWord(word);
 * boolean param_2 = obj.search(word);
 */