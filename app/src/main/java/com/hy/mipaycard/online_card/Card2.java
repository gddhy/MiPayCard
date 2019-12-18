package com.hy.mipaycard.online_card;

public class Card2 {
    String cardName;
    String link;
    String about;
    String userName;
    String email;

    public Card2(String cardName, String link, String userName, String about, String email){
        this.cardName = cardName;
        this.link = link;
        this.userName = userName;
        this.about = about;
        this.email = email;
    }

    public String getCardName() {
        return cardName;
    }

    public String getLink() {
        return link;
    }

    public String getAbout() {
        return about;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }
    public String getFileName(){
        String name = link.substring(link.lastIndexOf("/")+1);
        String end = "";
        if(name.contains(".")){
            end = name.substring(name.lastIndexOf("."));
        }
        return cardName + end;
    }
}