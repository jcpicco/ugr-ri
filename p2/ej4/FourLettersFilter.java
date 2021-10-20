import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.util.*;

public final class FourLettersFilter extends TokenFilter {
    private CharTermAttribute charTerm;
    private PositionIncrementAttribute posicion;


    public FourLettersFilter(TokenStream input){
        super(input);
        this.charTerm = addAttribute(CharTermAttribute.class);
        this.posicion = addAttribute(PositionIncrementAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException{
        int saltos = 0;

        while(input.incrementToken()){
            if (charTerm.length() >= 4) {
                if (saltos != 0) {
                    posicion.setPositionIncrement(posicion.getPositionIncrement() + saltos);
                }
                
                char buffer[] = charTerm.buffer();
                char newBuffer[] = new char[4];

                for (int i = 0; i < 4 ; i++) {
                    newBuffer[i] = buffer[charTerm.length() - 4 + i];
                }

                charTerm.setEmpty();
                charTerm.copyBuffer(newBuffer, 0, newBuffer.length);
                return true;
            }

            saltos += posicion.getPositionIncrement();
        }

        return false;
    }
}