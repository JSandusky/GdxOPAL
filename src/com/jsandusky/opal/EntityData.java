package com.jsandusky.opal;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.io.Serializable;
import org.apache.commons.io.input.SwappedDataInputStream;

public class EntityData implements Serializable {
	public String ClassName;
	HashMap<String,Property> property = new HashMap<String,Property>();
	
	public Property getProperty(String name) {
		if (property.containsKey(name))
			return property.get(name);
		return null;
	}
	public static class Property implements Serializable {
		private Property() {
			//serial
		}
		public Property(String t, String v) {
			Type = t; Value = v;
		}
		public String Type;
		public String Value;
	}
	public EntityData(SwappedDataInputStream str) throws IOException {
		load(str);
	}
	public EntityData() {
		//serial mostly
	}

	public Collection<String> getProperties() {
		return property.keySet();
	}
	
	public void load(SwappedDataInputStream str) throws IOException {
		ClassName = readString(str);
		
		int ct = str.readInt();
		for (int i = 0; i < ct; ++i) {
			String type = readString(str);
			String keyName = readString(str);
			String value = readString(str);
			property.put(keyName, new Property(type,value));
		}
	}
	
	String readString(SwappedDataInputStream str) throws IOException {
		int len = str.readInt();
		String ret = "";
		for (int i = 0; i < len; ++i) {
			ret += ((char)str.readByte());
		}
		ret = ret.substring(0,ret.length()-1);
		return ret;
	}
	
	public float getFloatValue(String prop, float def) {
		if (property.containsKey(prop))
			return Float.parseFloat(this.getProperty(prop).Value);
		return def;
	}
	
	public int getIntValue(String prop, int def) {
		if (property.containsKey(prop))
			return Integer.parseInt(this.getProperty(prop).Value);
		return def;
	}
	
	public String getValue(String prop, String def) {
		if (this.property.containsKey(prop))
			return getProperty(prop).Value;
		return def;
	}
}
