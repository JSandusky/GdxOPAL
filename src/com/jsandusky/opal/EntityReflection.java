package com.jsandusky.opal;

import java.lang.reflect.Field;

import com.badlogic.gdx.Gdx;

//uses reflection to populate something from the names of it's fields in the entity data
public class EntityReflection {
	public static void set(Object obj, OpalShape entity) {
		Class t = obj.getClass();
		EntityData data = entity.entityData;
		Field fld[] = t.getDeclaredFields();
		
		for (String datum : data.getProperties()) {
			for (Field f : fld) {
				try {
					if (f.getName().toLowerCase().equals(datum.toLowerCase())) {
						if (f.getType() == int.class) {
							f.set(obj, data.getIntValue(datum,0));
						} else if (f.getType() == boolean.class) {
							f.set(obj, data.getValue(datum, "false").toLowerCase().equals("true"));
						} else if (f.getType() == String.class) {
							f.set(obj, data.getValue(datum,""));
						} else if (f.getType() == float.class) {
							f.set(obj,data.getFloatValue(datum,0));
						} else if (f.getType().isEnum()) {
							f.set(obj, Enum.valueOf((Class<Enum>)f.getType(), data.getValue(datum,"")));
						}
					}
				} catch (Exception e) {
					Gdx.app.log("Opal","EntityReflection",e);
				}
			}
		}
	}
}
