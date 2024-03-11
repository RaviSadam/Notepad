import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Trie{
    public HashMap<Character,Trie> chars;
    public List<Integer> nodes;
    Trie(){
        this.chars=new HashMap<>();
        this.nodes=new ArrayList<>();
    }
}
class AutoRecommendation {
    Trie root=null;
    AutoRecommendation(){
        this.root=new Trie();
    }

    //insert word into trie
    public void insertString(String word,int end){
        Trie temp=this.root;
        for(char character:word.toCharArray()){
            if(temp.chars.getOrDefault(character,null)==null){
                temp.chars.put(character,new Trie());
            }
            temp=temp.chars.get(character);
        }
        temp.nodes.add(end);
    }

    //search for the  word in trie
    public List<Integer> search(String word){
        Trie temp=this.root;
        for(char character:word.toCharArray()){
            if(temp.chars.getOrDefault(character,null)==null){
                temp.chars.put(character,new Trie());
            }
            temp=temp.chars.get(character);
        }
        return temp.nodes;
    }


    //
    public List<String> getRecommendation(String word){
        List<String> res=new ArrayList<>();
        Trie temp=root;
        for(char character:word.toCharArray()){
            if(temp.chars.getOrDefault(character,null)==null){
                temp.chars.put(character,new Trie());
            }
            temp=temp.chars.get(character);
        }
        res.add(word);
        return null;
    }
}
