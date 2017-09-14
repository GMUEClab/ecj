package ec.app.push;

import ec.gp.push.*;
import org.spiderland.Psh.*;

public class Print extends PushInstruction
    {
    public void Execute(Interpreter interpeter) 
        {
        floatStack stack = interpeter.floatStack();

        if (stack.size() > 0)
            {
            System.err.println(stack.top());
            }
        else System.err.println("empty");
        }
    }
