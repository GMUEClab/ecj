/*
DRM, distributed resource machine supporting special distributed applications
Copyright (C) 2002 The European Commission DREAM Project IST-1999-12679

This file is part of DRM.

DRM is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

DRM is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with DRM; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

contact: http://www.dr-ea-m.org, http://www.sourceforge.net/projects/dr-ea-m
*/


package drm.server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Properties;
import javax.swing.*;
import javax.swing.event.*;

import drm.agentbase.*;
import drm.core.*;
import drm.agents.*;
import drm.util.ConfigProperties;

/**
* A node with a graphical user interface, primarily for testing and
* debugging the node itself, and also the experiments.
* For details on configuration
* please refer to the <a href="doc-files/config.html">configuration manual</a>
* and also to the class {@link ConsoleLogger} which this application uses.
*/
public class TestGUINode extends JFrame
implements ActionListener, ListSelectionListener, IBaseListener {


// =========== Private Fields ========================================
// ===================================================================


private JList		list;

private DefaultListModel listModel;

private JScrollPane	listScroller;

/**
 * Name of the agent that was selected in the list.
 */
private String selectedAgent = null;

private Node node;

/**
* The configuration parameters.
*/
private Properties cfg;


// =========== Private Methods =======================================
// ===================================================================


private boolean selectedOK() {

	if( selectedAgent != null ) return true;
	
	(new JOptionPane()).showMessageDialog( this,
			"No agent is selected!", null,
			JOptionPane.ERROR_MESSAGE );
	return false;
}

// -------------------------------------------------------------------

private void showError( String s ) {

	String[] m = new String[ (s.length()-1)/70 + 1 ];
	for( int i=0; i<m.length; i++ )
	{
		m[i] = s.substring( i*70, Math.min( (i+1)*70, s.length() ) );
	}
	(new JOptionPane()).showMessageDialog( this,
			m, null, JOptionPane.ERROR_MESSAGE );
}

// -------------------------------------------------------------------

private Address getLivingAddress() {
	
	InetAddress host = null;
	int port = Integer.parseInt(cfg.getProperty("port","10101"));
	String name = null;

	try
	{
		// ---- open dialog for the input of the server address
		
		JOptionPane dialog = new JOptionPane();
		String server = dialog.showInputDialog( this,
					"Give address as host[:port[:name]]" );
		if( server == null || server.length()==0 ) return null;
		StringTokenizer st = new StringTokenizer(server," :");
		if( st.countTokens()==0 ) return null;
		
		host = InetAddress.getByName( st.nextToken() );
		if( st.hasMoreTokens() )
			port = Integer.parseInt(st.nextToken());
		if( st.hasMoreTokens() )
			name = st.nextToken();
		else
		{
			name = Base.getBaseName(host,port,node.group,10000);
			if(name == null)
			{
				showError( ""+host+":"+port+" is not alive!" );
				return null;
			}
		}
		return new Address(host, port, name);	
	}
	catch( IndexOutOfBoundsException e )
	{
		showError( "Use format host[:port]");
	}
	catch( NumberFormatException e )
	{
		showError( "Wrong port: " + e );
	}
	catch( Exception e ) { showError( e.toString() ); }

	return null;
}


// =========== Package Methods =======================================
// ===================================================================


void shutdown( int exitcode ) {
	
	node.close();
	System.exit( exitcode );
}

// -------------------------------------------------------------------


void showConfig() {

	try
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		cfg.store( bos, null );
		LineNumberReader lnr = new LineNumberReader(
			new StringReader( new String(bos.toByteArray()) ) );
		LinkedList ll = new LinkedList();
		String tmp = lnr.readLine();
		if( tmp != null ) tmp = lnr.readLine(); //ignore comment
		while( tmp != null ) 
		{
			ll.add(tmp);
			tmp = lnr.readLine();
		}
		Object[] lines = ll.toArray();
		(new JOptionPane()).showMessageDialog( this,
			lines, "Effective settings",
			JOptionPane.PLAIN_MESSAGE );
	}
	catch( IOException e ) { showError(e.toString()); }
}

// -------------------------------------------------------------------


void showThreads() {

	Thread[] threads = new Thread[100];
	int tnum = Thread.currentThread().getThreadGroup().enumerate(threads);
	(new JOptionPane()).showMessageDialog( this,
		threads, "Threads",
		JOptionPane.PLAIN_MESSAGE );
}


// -------------------------------------------------------------------

/** Loads a script and runs it as a thread.*/
void runScript() {

	String scr = null;
	JOptionPane dialog = new JOptionPane();
	scr = dialog.showInputDialog( this,
		"Give class to run as\nfile[!packagename.classname]"+
		"\nwhere file is a jar or a directory.");
	if( scr == null || scr.length()==0 ) return;
	
	try
	{
		ScriptUtils.startScript( scr, node );
	}
	catch( NoSuchMethodException e )
	{
		showError(
		"The class does not conform with the interface specification.");
	}
	catch( Exception e ) { showError( e.toString() ); }
}

// -------------------------------------------------------------------

/**
 * Sends the selected agent a message.
 * If no agent is selected asks for a (possibly remote) address.
 * The content is of type String at the moment. The format to be typed in is
 * messagetype[,messagecontent]. The sender is always the node at the
 * moment.
 */
void editMessage() {

	String mess = null;
	String type = null;
	String content = null;

	try
	{
		// ---- open dialog for the input of message
		
		JOptionPane dialog = new JOptionPane();
		mess = dialog.showInputDialog( this, "Message");
		if( mess == null || mess.length()==0 ) return;
		
		// ---- split mess into type and content
		
		mess = mess.trim();
		int delimiter = mess.indexOf(',');
		if( delimiter != -1)
		{
			type = mess.substring( 0, delimiter );
			content = mess.substring( delimiter + 1 );
		}
		else
		{
			type = mess;
		}
	
		Address to = null;
		if(selectedAgent==null)
		{
			to = getLivingAddress();
			if( to==null ) return;
		}
		else to = new Address(selectedAgent);
		
		IRequest req = node.fireMessage( new Message(
			new Address(node.getName()),to,type,content));
		
		if( req instanceof Thread )
			try{ ((Thread)req).join(10000); } catch(Exception e) {}
		
		if( req.getThrowable() != null )
		{
			req.getThrowable().printStackTrace();
			throw req.getThrowable();
		}

		(new JOptionPane()).showMessageDialog( this,
			""+req.getInfo("reply"),
			null, JOptionPane.INFORMATION_MESSAGE );
	}
	catch( IndexOutOfBoundsException e )
	{
		showError( "Use format messagetype[,messagecontent]" );
	}
	catch( Throwable e )
	{
		showError( ""+e );
	}
}

// -------------------------------------------------------------------
		
/**
* Adds the given address to the list of know other bases.
* The format of the address must be hostname[:port],
* e.g. localhost:10101 or just localhost
*/
void editAddToNodeList() {

	Address a = getLivingAddress();
	if( a != null )
	{
		Properties p = new Properties();
		p.setProperty("node",a.getHost().getHostName()+":"+a.port);
		node.addNodes(p);
	}
}

// -------------------------------------------------------------------

/**
 * Sends the selected agent to another base.
 */
void editSendTo() {

	if( !selectedOK() ) return;
	if( selectedAgent.startsWith("Node.") )
	{
		showError( "Node cannot be deleted." );
		return;
	}
	
	try
	{
		Address a = getLivingAddress();
		if( a == null ) return;
		
		IRequest req = node.dispatchAgent( selectedAgent, a );
		
		if( req instanceof Thread )
			try{ ((Thread)req).join(10000); } catch(Exception e) {}
		
		if( req.getThrowable() != null )
		{
			req.getThrowable().printStackTrace();
			throw req.getThrowable();
		}
		
		if( req.getStatus() != IRequest.DONE )
			(new JOptionPane()).showMessageDialog( this,
			"Sending was not succesful.\nAgent stays here.",
			null, JOptionPane.ERROR_MESSAGE );
	}
	catch( Throwable e ) { showError( e.toString() ); }
}

// -------------------------------------------------------------------

/**
 * Destroys the selected agent.
 */
void editDestroy() {
	
	if( !selectedOK() ) return;
	if( selectedAgent.startsWith("Node.") )
	{
		showError( "Node cannot be deleted." );
		return;
	}

	node.destroyAgent( selectedAgent );
}

// -------------------------------------------------------------------

/**
 * Generates an Island and puts it to the base.
 */
void createIsland() {

/*	String exper = null;
	String name = ""+System.currentTimeMillis()+"-"+Math.random();
	JOptionPane dialog = new JOptionPane();
	exper = dialog.showInputDialog( this,
		"Give name as experiment name or\nexperiment-name.name" );
	if( exper == null || exper.trim().length()==0 ) return;
	if( exper.indexOf('.') > 0 )
	{
		name = exper.substring(exper.indexOf('.')+1);
		exper = exper.substring(0,exper.indexOf('.'));
	}

	IRequest r = node.launch(
		"DIRECT", new TestIsland(exper, name, null, null), null );
	
	// static request, we don't have to wait
	if( r.getStatus() != IRequest.DONE )
	{
		showError(""+r.getThrowable());
	}
*/
	
	showError("Functionality temporarily disabled");
}

// -------------------------------------------------------------------

void editCleanLocal() {
	
	node.invokeCommandLocally(new NodeCommand("cleanall"));
}

// -------------------------------------------------------------------

void editClean() {
	
	node.invokeCommand(new NodeCommand("cleanall"));
}

// =========== Public Constructors ===================================
// ===================================================================


public TestGUINode( Properties cf ) {

	super("TestBAK GUI Node");	
	cfg = cf;
	
	node = new Node( cfg );
	node.addListener(this);

	setVisible(false);
	
	JMenuItem	menuItem;
	JMenu		menu;
	
	// create main menu
	JMenuBar menuBar = new JMenuBar();
	setJMenuBar( menuBar );

	// File menu
	menu = new JMenu( "File" );
	menuItem = new JMenuItem( "Run...", 'r' );
	menuItem.setActionCommand( "runScript" );
	menuItem.addActionListener( this );
	menuItem.registerKeyboardAction( this, "runScript",
		KeyStroke.getKeyStroke('r'),JComponent.WHEN_IN_FOCUSED_WINDOW);
	menu.add( menuItem );
	menuItem = new JMenuItem( "Show config", 'c' );
	menuItem.setActionCommand( "showConfig" );
	menuItem.addActionListener( this );
	menuItem.registerKeyboardAction( this, "showConfig",
		KeyStroke.getKeyStroke('c'),JComponent.WHEN_IN_FOCUSED_WINDOW);
	menu.add( menuItem );
	menuItem = new JMenuItem( "Show threads", 'T' );
	menuItem.setActionCommand( "showThreads" );
	menuItem.addActionListener( this );
	menuItem.registerKeyboardAction( this, "showThreads",
		KeyStroke.getKeyStroke('T'),JComponent.WHEN_IN_FOCUSED_WINDOW);
	menu.add( menuItem );
	menuItem = new JMenuItem( "Exit", 'x' );
	menuItem.setActionCommand( "shutdown" );
	menuItem.addActionListener( this );
	menuItem.registerKeyboardAction( this, "shutdown",
		KeyStroke.getKeyStroke('q'),JComponent.WHEN_IN_FOCUSED_WINDOW);
	menuItem.registerKeyboardAction( this, "shutdown",
		KeyStroke.getKeyStroke('x'),JComponent.WHEN_IN_FOCUSED_WINDOW);
	menu.add( menuItem );
	menuBar.add( menu );

	// Edit menu
	menu = new JMenu ("Edit");
	menuItem = new JMenuItem ("Send To...", 't');
	menuItem.setActionCommand ("editSendTo");
	menuItem.addActionListener (this);
	menuItem.registerKeyboardAction( this, "editSendTo",
		KeyStroke.getKeyStroke('t'),JComponent.WHEN_IN_FOCUSED_WINDOW);
	menu.add (menuItem);
	menuItem = new JMenuItem ("Destroy", 'd');
	menuItem.setActionCommand ("editDestroy");
	menuItem.addActionListener (this);
	menuItem.registerKeyboardAction( this, "editDestroy",
		KeyStroke.getKeyStroke('d'),JComponent.WHEN_IN_FOCUSED_WINDOW);
	menu.add (menuItem);
	menuItem = new JMenuItem( "Message...", 'm' );
	menuItem.setActionCommand( "editMessage" );
	menuItem.addActionListener( this );
	menuItem.registerKeyboardAction( this, "editMessage",
		KeyStroke.getKeyStroke('m'),JComponent.WHEN_IN_FOCUSED_WINDOW);
	menu.add( menuItem );
	menuItem = new JMenuItem( "Add to Node List...", 'a' );
	menuItem.setActionCommand( "editAddToNodeList" );
	menuItem.addActionListener( this );
	menuItem.registerKeyboardAction( this, "editAddToNodeList",
		KeyStroke.getKeyStroke('a'),JComponent.WHEN_IN_FOCUSED_WINDOW);
	menu.add( menuItem );
	menuItem = new JMenuItem( "Clean local node" );
	menuItem.setActionCommand( "editCleanLocal" );
	menuItem.addActionListener( this );
	menu.add( menuItem );
	menuItem = new JMenuItem( "Clean DRM" );
	menuItem.setActionCommand( "editClean" );
	menuItem.addActionListener( this );
	menu.add( menuItem );
	menuBar.add (menu);

	// Create Menu
	menu = new JMenu ("Create");
	menuItem = new JMenuItem ("Island...", 'i' );
	menuItem.setActionCommand ("createIsland");
	menuItem.addActionListener (this);
	menuItem.registerKeyboardAction( this, "createIsland",
		KeyStroke.getKeyStroke('i'),JComponent.WHEN_IN_FOCUSED_WINDOW);
	menu.add (menuItem);

	menuBar.add (menu);

	// Frame contents
	JPanel panel = new JPanel();
	getContentPane().add( panel );
	panel.setLayout( new GridLayout(1,1) );
	panel.setPreferredSize( new java.awt.Dimension(500, 300) );

	listModel = new DefaultListModel();
	list = new JList (listModel);
	list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	list.addListSelectionListener(this);
	listScroller = new JScrollPane (list);

	panel.add( listScroller );
	
	int port = Integer.parseInt(cfg.getProperty("port","10101"));
	node.goOnline( port, port+10 );
}


// =========== Public Methods ========================================
// ===================================================================


public static void main( String[] args ){

	try
	{
		UIManager.setLookAndFeel(
			UIManager.getSystemLookAndFeelClassName());
	}
	catch( Exception e )
	{
		System.err.println(e);
	}

	ConsoleLogger cl;
	Logger.addListener( cl = new ConsoleLogger() );
	ConfigProperties cfg = new ConfigProperties( args, null );
	
	cl.verbosity = Integer.parseInt(cfg.getProperty("verbosity","10"));
	
	TestGUINode frame = new TestGUINode(cfg);
	
	WindowListener l = new WindowAdapter() {
		
		public void windowClosing( WindowEvent e ) {
		
			((TestGUINode)e.getWindow()).shutdown(0);
		}
	};

	frame.addWindowListener(l);
	
	frame.pack();
	frame.setLocation(10,10);
	frame.setVisible(true);

	try
	{ 
		// first wait 30 secs for connection to the network
		Thread.currentThread().sleep(30000);
		ScriptUtils.startAllScripts( cfg, "runClass", frame.node );
	}
	catch( Exception e )
	{
		frame.showError(
			"While running configured experiments: "+e.toString());
	}
}


// =========== Public ActionListener Implementations =================
// ===================================================================


public void actionPerformed (ActionEvent e){
	
	if ( e.getActionCommand().equals ("editSendTo") )
	{
		editSendTo();
	}
	else if ( e.getActionCommand().equals ( "editAddToNodeList"  ) )
	{
		editAddToNodeList();
	}
	else if ( e.getActionCommand().equals ("editMessage") )
	{
		editMessage();
	}
	else if ( e.getActionCommand().equals ("runScript") )
	{
		runScript();
	}
	else if ( e.getActionCommand().equals ("showConfig") )
	{
		showConfig();
	}
	else if ( e.getActionCommand().equals ("showThreads") )
	{
		showThreads();
	}
	else if ( e.getActionCommand().equals ("shutdown") )
	{
		shutdown(0);
	}
	else if ( e.getActionCommand().equals ("editDestroy") )
	{
		editDestroy();
	}
	else if ( e.getActionCommand().equals ("createIsland") )
	{
		createIsland();
	}
	else if ( e.getActionCommand().equals ("editCleanLocal") )
	{
		editCleanLocal();
	}
	else if ( e.getActionCommand().equals ("editClean") )
	{
		editClean();
	}
}


// =========== Public ListSelectionListener Implementations ==========
// ===================================================================


public synchronized void valueChanged(ListSelectionEvent e) {

	int pos = list.getSelectedIndex();

	if( pos != -1 ) selectedAgent = (String) listModel.elementAt (pos);
	else selectedAgent = null;
}


// =========== Public IBaseListener Implementations ==================
// ===================================================================


public synchronized void agentArrived( String s ) {

	synchronized( listModel ) { listModel.addElement(s); }
}

// -------------------------------------------------------------------

public synchronized void agentDestroyed( String s ) {

	synchronized( listModel )
	{ 
		
		int pos = list.getSelectedIndex();
	
		listModel.removeElement(s);
	
		if( pos == -1 ) selectedAgent = null;
		else if( pos < listModel.size() ) list.setSelectedIndex(pos);
		else if( pos > 0 ) list.setSelectedIndex( pos-1 );
		else selectedAgent = null;
		try{ Thread.currentThread().sleep(20); } catch(Exception e){}
	}
}

}


