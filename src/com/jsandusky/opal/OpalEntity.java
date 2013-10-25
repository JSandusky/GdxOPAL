package com.jsandusky.opal;

//Implement one of these and return in order to use
//Opals rendering order and such for items
public abstract class OpalEntity extends OpalShape {
	public abstract void update(float delta);
}
