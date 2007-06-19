import killjob.Killer;
import drm.core.Node;

public class Launch implements Runnable{
	
	private final Node node;
	
	public Launch(Node node){
		this.node = node;
	}
	
	public void run(){
		node.launch("DIRECT", new Killer("killjob","root"),null);
	}
}