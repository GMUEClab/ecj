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


package drm.agentbase;

import java.io.*;

/**
* The classtype of a message that agents and bases can send to each other.
*/
public class Message implements Serializable {
private static final long serialVersionUID = 49323201182294530L;


// =========== Package fields ========================================
// ===================================================================


Address sender;

Address recipient;

String type;

byte[] content;

transient Object reply = null;


// =========== Public Constructors ===================================
// ===================================================================


/**
* Constructs a Message using the values of the fields. The binary content
* is set to null.
*/
public Message( Address sender, Address recipient, String type ) {

	if( sender == null ) throw new IllegalArgumentException(
		"sender mustn't be null in Message constructor");
	if( recipient == null ) throw new IllegalArgumentException(
		"recipient mustn't be null in Message constructor");
	if( type == null ) throw new IllegalArgumentException(
		"type mustn't be null in Message constructor");
	
	this.sender = sender;
	this.recipient = recipient;
	this.type = type;
	this.content = null;
}

// -------------------------------------------------------------------

/**
* Constructs a Message using the values of the fields.
* The binary content is initialized by serializing the given Object
* to a byte array. This is useful to allow delayed de-serialisation
* at the reception side possibly using a recepient-specific classloader.
* 
* @param object The object to be converted to byte array. If it is null
*	then no exception is thrown and null is set as binary content.  
* @throws IOException If the conversion of the given object to byte array
* 	is not succesful.
*/
public Message( Address sender, Address recipient, String type, Object object )
throws IOException {

	this( sender, recipient, type );
	if( object == null ) return;
	
	ByteArrayOutputStream	bos = new ByteArrayOutputStream();
	ObjectOutputStream	oos = new ObjectOutputStream( bos );
	oos.writeObject(object);
	oos.flush();
	content = bos.toByteArray();
}


// =========== Public Methods ========================================
// ===================================================================


/** The address of the sender. */
public Address getSender() { return sender; }

/** The address of the recepient. */
public Address getRecipient() { return recipient; }

/** The type of the message. */
public String	getType() { return type; }
	
/** The binary content of the message. */
public byte[]	getBinary() { return content; }

/**
* This method allows the recipient of the message to send a reply object
* when handling the message. This object is sent back to the sender.
* <p>The object will undergo a serialization-deserialization procedure,
* even if the recipient is local, just like in the case of message content.
* Even though this adds some overhead, the locality/non-locality of an
* address should normally be transparent and some implementations which
* send objects that reimplement write/readObject might depend on the actual
* algorithm of serialization.
* @see IAgent#handleMessage(Message,Object)
* @see IBase#fireMessage(Message)
*/
public void setReply( Object o ) { reply = o; }


// =========== Public Object Implementations =========================
// ===================================================================


public String toString() {

	String s = "Message\nFrom: " + sender + "\nTo: " + recipient +
		   "\nSubject: " + type + "\nContent: ";
	if( content != null ) return s + content;
	else return s + "null";
}

}

