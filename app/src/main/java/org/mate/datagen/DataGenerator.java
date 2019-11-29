package org.mate.datagen;

/**
 * Created by marceloe on 14/10/16.
 */

import org.mate.MATE;

import java.util.List;
import java.util.Random;


/**
 * Created by marceloe on 14/10/16.
 */

/**
 * Created by marceloe on 23/09/16.
 */
public class DataGenerator {

    public static List<String> words = null;

    public DataGenerator(){

        if (words==null)
            loadWords();
    }

    public void loadWords(){

        words = Dictionary.getWords();

    }

    public String getRandomText(int maxLength){
        Random random = new Random();
        if (maxLength ==-1)
            maxLength = random.nextInt(32);
        String rtext = "";
        boolean end = false;
        while (!end){
            String nextWord = words.get(random.nextInt(words.size()));
            if ((rtext+nextWord+" ").length()<=maxLength)
                rtext+=nextWord+" ";
            else
                end=true;
        }
        if (rtext.length()==0)
            rtext+=words.get(random.nextInt(words.size()));
        else
            rtext=rtext.substring(0,rtext.length()-1);
        return rtext;
    }

    public String getRandomString(int size){

        Random random = new Random();

        int opt = random.nextInt(2);

        if (size==-1){
            size=random.nextInt(16);
        }


        String randomString = "";
        switch (opt){
            case 0:
                for (int i=0; i<size; i++)
                    randomString+=getRandomChar();
                break;
            case 1:
                randomString = getRandomNumber(size);
                break;
        }

        return randomString;
    }



    public String getRandomValidString(int size){

        return getRandomText(size);

    }

    public String getRandomValidNumber() {

        Random random = new Random();

        // produce a number between 0-1000
        int number = Math.abs((int) (random.nextGaussian() * 1000));
        MATE.log_acc("Generated Random number: " + number);
        return String.valueOf(number);
    }

    public String getRandomNumber(int size){

        String randomNumber = "";
        Random random = new Random();

        if (size ==-1)
            size = random.nextInt(32);

        String numbers = "0123456789";
        String chars = " abcdefghijklmnopqrstuvxwyzABCDEFGHIJKLMNOPQRSTUVXYWZ";


        for (int i=0; i<size; i++){
            randomNumber += numbers.charAt(random.nextInt(numbers.length()));
        }

        //defines if it is a valid or invalid number
        if (randomNumber.length()>0 && random.nextBoolean()){
            Character randomChar = randomNumber.charAt(random.nextInt(randomNumber.length()));
            randomNumber.replace(randomChar, chars.charAt(random.nextInt(chars.length())));
        }

        return randomNumber;
    }

    public String getRandomValidNumber(int size){

        String randomNumber = "";
        Random random = new Random();

        if (size ==-1)
            size = random.nextInt(32);

        String numbers = "0123456789";

        for (int i=0; i<size; i++){
            randomNumber += numbers.charAt(random.nextInt(numbers.length()));
        }

        return randomNumber;
    }

    public char getRandomChar() {
        Random r = new Random();
        char randomChar = (char) (48 + r.nextInt(47));
        return randomChar;
    }

    public String getRandomEmail(int size){
        String randomString = this.getRandomString(size);
        Random random = new Random();
        boolean insertSymbol = random.nextBoolean();
        if (insertSymbol)
            randomString+="@";

        boolean insertDomainName = random.nextBoolean();
        if (insertDomainName)
            randomString+=this.getRandomString(random.nextInt(10));

        boolean insertDomainExtension = random.nextBoolean();
        if (insertDomainExtension)
            randomString+=this.getRandomString(3);

        return randomString;
    }

    public String getRandomUri(int size){
        String randomString = this.getRandomString(size);
        Random random = new Random();
        boolean insertSymbol = random.nextBoolean();
        if (insertSymbol)
            randomString+="/";

        boolean insertDomainName = random.nextBoolean();
        if (insertDomainName)
            randomString+=this.getRandomString(random.nextInt(10));

        boolean insertDomainExtension = random.nextBoolean();
        if (insertDomainExtension)
            randomString+=this.getRandomString(3);

        return randomString;
    }

    public String getRandomValidEmail(int size){
        String randomValidString = this.getRandomValidString(size);
        randomValidString+="@";
        randomValidString +=this.getRandomValidString(10);
        randomValidString += ".";
        randomValidString += this.getRandomValidString(5);
        randomValidString = randomValidString.replace(" ","");
        return randomValidString;
    }

}
