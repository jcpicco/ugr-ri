import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.util.*;

public final class FourLettersFilter extends TokenFilter {
    private CharTermAttribute charTerm;


    public FourLettersFilter(TokenStream input){
        super(input);
        this.charTerm = addAttribute(CharTermAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException{
        if (!input.incrementToken())  {
            System.out.println(" Cond1");
            return false;

        if(charTerm.length() >=4){
            System.out.println(" Cond2");
            char buffer[] = charTerm.buffer();
            char newBuffer[] = new char[4];

            for(int i=0 ; i < 4 ; i++){
                newBuffer[i] = buffer[charTerm.length()-4+i];
            }

            charTerm.setEmpty();
            charTerm.copyBuffer(newBuffer, 0, newBuffer.length);

            return true;
        }
        
        else{
            System.out.println(" Cond3");
            charTerm.setEmpty();
            return true;
        }
    }
    }
}