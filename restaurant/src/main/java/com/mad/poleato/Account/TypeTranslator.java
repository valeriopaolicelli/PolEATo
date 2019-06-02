package com.mad.poleato.Account;

import java.util.HashMap;
import java.util.Map;


/**
 * This class provide translation from it_IT and en_EN (and vice versa) for the types of cooking
 */
public class TypeTranslator {


    private Map<String, String> dictionary;

    public TypeTranslator(){

        dictionary = new HashMap<>();

        //en -> it
        dictionary.put("italian", "italiano");
        dictionary.put("chinese", "cinese");
        dictionary.put("japanese", "giapponese");
        dictionary.put("american", "americano");
        dictionary.put("mexican", "messicano");

        //it->en
        dictionary.put("italiano", "italian");
        dictionary.put("cinese", "chinese");
        dictionary.put("giapponese", "japanese");
        dictionary.put("americano", "american");
        dictionary.put("messicano", "mexican");

        //single
        dictionary.put("pizza", "pizza");
        dictionary.put("kebab", "kebab");
        dictionary.put("thai", "thai");
        dictionary.put("hamburger", "hamburger");

    }

    public String translate(String s){
        if(dictionary.containsKey(s))
            return dictionary.get(s);
        else
            return "";
    }

}
