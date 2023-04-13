package com.minivv.pilot.model;

import java.util.*;

public class Prompts extends DomainObject {
    private List<Prompt> prompts = new ArrayList<>();

    public Prompts() {
    }

    public Prompts(List<Prompt> prompts) {
        this.prompts = prompts;
    }

    public List<Prompt> getPrompts() {
        return prompts;
    }

    public void setPrompts(List<Prompt> prompts) {
        this.prompts = prompts;
    }

//    public boolean add(Prompt o) {
//        if (prompts.stream().anyMatch(prompt -> prompt.getOption().equals(o.getOption()) || Objects.equals(prompt.getIndex(),o.getIndex()))) {
//            return false;
//        }
//        return prompts.add(o);
//    }


//    public void add(String s, String to,int index) {
//        prompts.add(new Prompt(s, to,index));
//    }

    public void add(String s, String to) {
        prompts.add(new Prompt(s, to));
    }


    public boolean add(Prompt o) {
        if (prompts.stream().anyMatch(prompt -> prompt.getOption().equals(o.getOption()))) {
            return false;
        }
        return prompts.add(o);
    }

    public int size() {
        return prompts.size();
    }

    public Map<String, String> asMap() {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        for (Prompt prompt : prompts) {
            stringStringHashMap.put(prompt.getOption(), prompt.getSnippet());
        }
        return stringStringHashMap;
    }

    public void clear() {
        this.prompts = new ArrayList<>();
    }
}