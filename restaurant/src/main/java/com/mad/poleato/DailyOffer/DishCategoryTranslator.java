package com.mad.poleato.DailyOffer;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provide translation from it_IT to en_EN for the dish category
 */
public class DishCategoryTranslator {


    private Map<String, String> dictionary = new HashMap<>();


    public DishCategoryTranslator(){
        //it -> en
        dictionary.put("Antipasti", "Starters");
        dictionary.put("Primi", "Firsts");
        dictionary.put("Secondi", "Seconds");
        dictionary.put("Dolci", "Desserts");
        dictionary.put("Bevande", "Drinks");

        //en -> it
        dictionary.put("Starters", "Antipasti");
        dictionary.put("Firsts", "Primi");
        dictionary.put("Seconds", "Secondi");
        dictionary.put("Desserts", "Dolci");
        dictionary.put("Drinks", "Bevande");
    }

    public String translate(String s){
        if(dictionary.containsKey(s))
            return dictionary.get(s);
        else
            return "";
    }

}
