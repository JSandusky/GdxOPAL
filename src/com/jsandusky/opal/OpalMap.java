package com.jsandusky.opal;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.input.SwappedDataInputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class OpalMap implements Disposable {
	transient Vector3 lastCamPos = new Vector3();
	transient float lastCamHeight;
	transient float lastCamWidth;
	Vector2 map_size = new Vector2();
	Vector2 map_size2 = new Vector2();
	Array<Layer> Layers = new Array<Layer>();
	transient ArrayList<String> Entities = new ArrayList<String>();
	transient Array<OpalShape> drawSetBG = new Array<OpalShape>(16);
	transient Array<OpalShape> drawSetMID = new Array<OpalShape>(16);
	transient Array<OpalShape> drawSetFG = new Array<OpalShape>(16);
	
	public interface TextureHandler {
		public Texture getTexture(String name);
	}
	public interface EntityHandler {
		//return null if managing the entity yourself
		public OpalEntity onEntity(String classname, OpalShape data);
	}
	transient EntityHandler entHandler;
	
	public OpalMap(EntityHandler ent) {entHandler = ent;}
	
	private OpalMap() {//serial
		
	}
	
	public void construct(TextureHandler handler) {
		for (int i = 0; i < Layers.size; ++i) {
			for (int p = 0; p < Layers.get(i).shapes.size; ++p) {
				OpalShape s = Layers.get(i).shapes.get(p);				
				s.loadTextures(handler);
				s.generateMesh();
			}
		}
	}
	
	public void construct(TextureHandler handler, World w, float scale) {
		construct(handler);
		constructBox2D(w,scale);
	}
	
	//build static box2d bodies
	public void constructBox2D(World w, float scale) {
		for (int i = 0; i < Layers.size; ++i) {
			if (i != 1) //only the middle layer gets physics
				continue;
			for (OpalShape s : Layers.get(i).shapes) {
				if (s.entityData.ClassName.toLowerCase().contains("solid"))
					s.buildColData(w,scale);
				else if (s.entityData.ClassName.toLowerCase().contains("platform"))
					s.buildColData(w,scale);
			}
			for (OpalEntity e : Layers.get(i).entities)
				e.buildColData(w,scale);
		}
	}
	
	public void registerEntity(String name) {
		Entities.add(name);
	}
	
	public void load(FileHandle handle) throws IOException {
		
		SwappedDataInputStream str = new SwappedDataInputStream(handle.read());
		
		
		int fmt = (int) str.readInt();
		if (!(fmt >= 314 && fmt <= 315))
			return; //wrong file format
		
		if (fmt >= 315) {
			map_size.x = str.readFloat();
			map_size.y = str.readFloat();
			map_size2.x = str.readFloat();
			map_size2.y = str.readFloat();
		}
		
		int layerCount = str.readInt();
		for (int l = 0; l < layerCount; ++l) {

			Layer currentLayer = new Layer();
			Layers.add(currentLayer);
			int polyCount = str.readInt();
			
			for (int i = 0; i < polyCount; ++i) {
				boolean isEnt = false;
				
				OpalShape shape = new OpalShape();
				shape.load(str,fmt);
				
				for (String s : Entities) {
					if (s.equals(shape.entityData.ClassName)) {						
						isEnt = true;
						break;
					}
				}
				
				if (!isEnt) { //dynamic shape
					addPolygon(shape,l);
				} else if (shape.isStatic()) { //static shape?
					addPolygon(shape,l);
				} else {
					//entity
					addDynamic(shape,currentLayer);
				}
			}
		}
	}
	
	void addPolygon(OpalShape shape, int layer) {
		if (Layers.size < layer-1) {
			Layer l = new Layer();
			Layers.add(l);
			l.shapes.add(shape);
		} else {
			Layers.get(layer).shapes.add(shape);
		}
	}
	
	void addDynamic(OpalShape ent, Layer l) {
		OpalEntity e = entHandler.onEntity(ent.entityData.ClassName, ent);
		e.copy(ent);
		if (e != null) {
			l.entities.add(e);
		}
	}
	
	//NAIVE DRAW! Probably shouldn't use in practice other than just viewing a level
	public void draw(SpriteBatch batch) {
		for (int i = 0; i < Layers.size; ++i) {
			for (int p = 0; p < Layers.get(i).shapes.size; ++p) {
				OpalShape s = Layers.get(i).shapes.get(p);				
				s.draw(batch);
			}
		}
	}
	
	//YOU SHOULD BE USING THIS ONE which lets your draw specific layers
	//so that your entities appear in the CORRECT places
	public void draw(SpriteBatch batch, Integer... layers) {
		for (int i = 0; i < layers.length; ++i) {
			for (int p = 0; p < Layers.get(layers[i]).shapes.size; ++p) {
				OpalShape s = Layers.get(i).shapes.get(p);
				s.draw(batch);
			}
		}
	}
	
	//OR THIS ONE which calculates culling whenever the camera moves or changes size
	public void draw(OrthographicCamera cam, SpriteBatch batch, Integer... layers) {
		if (this.lastCamPos.equals(cam.position) && lastCamWidth == cam.viewportWidth && lastCamHeight == cam.viewportHeight) {
			for (Integer i : layers) {
				Array<OpalShape> set = null;
				if (i == 0)
					set = drawSetBG;
				else if (i == 1)
					set = drawSetMID;
				else if (i == 2)
					set = drawSetFG;
				for (OpalShape s : set) {
					s.draw(batch);
				}
			}
		} else {
			Rectangle camArea = new Rectangle();
			camArea.x = cam.position.x - cam.viewportWidth/2;
			camArea.y = cam.position.y - cam.viewportHeight/2;
			camArea.height = cam.viewportHeight;
			camArea.width = cam.viewportWidth;
			lastCamPos.set(cam.position);
			
			for (int i = 0; i < layers.length; ++i) {
				Array<OpalShape> set = null;
				if (i == 0)
					set = drawSetBG;
				else if (i == 1)
					set = drawSetMID;
				else if (i == 2)
					set = drawSetFG;
				set.clear();
				for (int p = 0; p < Layers.get(layers[i]).shapes.size; ++p) {
					OpalShape s = Layers.get(i).shapes.get(p);
					Rectangle r = s.getPoly().getBoundingRectangle();
					if (camArea.overlaps(r)) {
						s.draw(batch);
						set.add(s);
					}
				}
			}
		}
	}
	
	public void update(float delta) {
		for (Layer l : Layers) {
			for (OpalEntity e : l.entities) {
				e.update(delta);
			}
		}
	}
	
	public static class Layer {
		Array<OpalShape> shapes = new Array<OpalShape>();
		Array<OpalEntity> entities = new Array<OpalEntity>();
	
		void dispose() {
			for (OpalShape s : shapes)
				s.dispose();
			for (OpalEntity e : entities)
				e.dispose();
		}
	}

	@Override
	public void dispose() {
		for (Layer l : Layers)
			l.dispose();
	}
}
