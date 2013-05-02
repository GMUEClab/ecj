package ec.app.push;

import ec.gp.push.*;
import org.spiderland.Psh.*;

public class Atan extends PushInstruction
    {
    public void Execute(Interpreter interpeter) 
        {
        floatStack stack = interpeter.floatStack();

        if (stack.size() >= 1)
            {
            // we're good
            float slope = stack.pop();
            stack.push((float)Math.atan(slope));
            }
        else stack.push(0);
        }
    }
