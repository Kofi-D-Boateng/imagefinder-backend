package com.kboat.imagefinder.model.preprocessor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
/*

    This class takes in a string and will process it into
    a list. After tokenizing the string, if the array length
    is greater than 2 we will invoke multithreading to process
    the multiple strings at once.
 */

public class RegexProcessor {
    private String regexPattern;
    private final Set<Character> specialCharSet;
    public RegexProcessor(String regexPattern) {
        this.regexPattern = regexPattern;
        this.specialCharSet = this.createHashSet();
    }

    public Set<String> processString(String stringToProcess){
        Set<String> set = new HashSet<>();
        String[] listOfUrls = stringToProcess.split(",");
        if(listOfUrls.length < 2){
            boolean isValid = listOfUrls[0].matches(regexPattern);
            if(isValid){
                set.add(stringToProcess);
            }
        }else{
            for(String url:listOfUrls){
                boolean isValid = url.matches(regexPattern);
                if(isValid){
                    set.add(url);
                }
            }

        }
        return set;
    }

    public Set<String> processStrings(String[] strings){
        Set<String> set = new HashSet<>();
        if(strings.length < 2){
            boolean isValid = strings[0].matches(regexPattern);
            if(isValid){
                set.add(strings[0]);
            }
        }else{
            for(String string:strings){
                boolean isValid = string.matches(regexPattern);
                if(isValid){
                    set.add(string);
                }
            }
        }
        return set;
    }

    public Set<String> processStrings(List<String> strings){
        Set<String> set = new HashSet<>();
        if(strings.size() < 2){
            boolean isValid = strings.get(strings.size()-1).matches(regexPattern);
            if(isValid){
                set.add(strings.get(strings.size()-1));
            }
        }else{
            for(String string:strings){
                boolean isValid = string.matches(regexPattern);
                if(isValid){
                    set.add(string);
                }
            }
        }
        return set;
    }

    public void changeRegexPattern(String newRegexPattern){
        if(newRegexPattern.equals(regexPattern)) return;

        StringBuilder newPattern = new StringBuilder();
        System.out.println("regexPattern = " + regexPattern);

        for(int i=0; i< regexPattern.length();i++){
            /*
               If the character is not a digit or letter,
               we will escape it to create the regrex pattern.
            */
            if(i-1 > 0 && !Character.isLetterOrDigit(regexPattern.charAt(i)) && this.specialCharSet.contains(regexPattern.charAt(i))){
                newPattern.append('\\');
            }
            newPattern.append(regexPattern.charAt(i));
        }
        if(newPattern.charAt(newPattern.length()-1) == '/'){
            int count = 2;
            while(count-- > 0){
                newPattern.deleteCharAt(newPattern.length()-1);
            }
        }

        System.out.println("newPattern = " + newPattern);
        regexPattern = newPattern.toString();
    }

    private Set<Character> createHashSet(){
        Set<Character> set = new HashSet<>();
        set.add('.');
        set.add(';');
        set.add('&');
        set.add('/');
        set.add('$');
        set.add('%');
        set.add('*');
        set.add('@');
        set.add('#');
        set.add('!');
        set.add('+');
        set.add('=');
        return set;
    }

}
