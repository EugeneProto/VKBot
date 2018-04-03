package bot.tasks;

import java.util.Random;

/**
 * Simple game just for fun.
 */
public class GuessNumber {
    private int number,countOfTryings;

    public GuessNumber() {
        number=new Random().nextInt(101);
        countOfTryings=0;
    }
    public boolean checkStatement(char operation,int input){
        countOfTryings++;
        if (operation=='>') return number>input;
        else if(operation=='<') return number<input;
        else return false;
    }
    public boolean checkNumber(int input){
        countOfTryings++;
        return number==input;
    }

    public int countOfTryings() {
        return countOfTryings;
    }
}
