import contributionjob.Contributor;
import drm.core.Node;

public class Launch implements Runnable{
	
	private final Node node;
	
	public Launch(Node node){
		this.node = node;
	}
	
	public void run(){
		node.launch("DIRECT", 
				new Contributor("contributionjob","root"),null);
	}
}