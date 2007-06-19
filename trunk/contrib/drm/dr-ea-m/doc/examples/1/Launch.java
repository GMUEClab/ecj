import drm.core.Node;
import hwjob.HelloWorld;

public class Launch implements Runnable {

private final Node node;

public Launch( Node node ) {

	this.node = node;
}

public void run() {

	node.launch(
		"DIRECT",
		new HelloWorld("test+"+System.currentTimeMillis(),"1"),
		null );
}
}


