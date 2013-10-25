package com.jsandusky.opal;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.input.SwappedDataInputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ShortArray;


public class OpalShape implements Disposable, Serializable {
	transient Mesh mesh; //For rendering
	transient ColData colData;
	static ShaderProgram shader;
	Polygon poly;
	Vector2 point;
	transient Array<BorderQuad> Borders = new Array<BorderQuad>();
	transient Array<BorderQuad> Shadows = new Array<BorderQuad>();
	
	float[] normals;
	public EntityData entityData;
	public ShapeTextureData textureData = new ShapeTextureData();
	
	transient Texture texture;
	
	public boolean isPoint() {return point != null;}
	public Polygon getPoly() {return poly;}
	public Vector2 getPoint() {return point;}
	
	public static class ShapeTextureData {
		public int BlendMode;
		public int color;
		public float XOffset;
		public float YOffset;
		public boolean Tiled;
		public float Scale;
		public boolean WorldCoords;
		
		//Border
		public boolean Bordered;
		public float BorderMax;
		public float BorderMin;
		public float BorderHeight;
		public float BorderScale;
		
		//Textures
		public String TextureName;
		public String BorderTextureName;
		public String ShadowTextureName;
		transient Texture MainTexture;
		transient Texture BorderTexture;
		transient Texture ShadowTexture;
		
		//Shadow
		public boolean Shadowed;
		public boolean ShadowFollowBorder;
		public int ShadowColor;
		public float ShadowHeight;
		public float ShadeScale;
	}
	
	public OpalShape() {
		
	}
	
	public void load(SwappedDataInputStream str, int fmt) throws IOException {
		int vertCt = str.readInt();
		
		if (vertCt > 0) {
			float[] verts = new float[vertCt*2];
			normals = new float[vertCt];
			int vertSub = 0;
			for (int i = 0; i < vertCt; ++i) {
				float px = str.readFloat();
				float py = str.readFloat();
				float norm = str.readFloat();
				
				//add vertex
				verts[vertSub] = px; ++vertSub;
				verts[vertSub] = py * -1f; ++vertSub;
				normals[i] = norm;
			}
			
			if (vertCt > 1) {
				poly = new Polygon();
				poly.setVertices(verts);
			} else {
				point = new Vector2(verts[0],verts[1]);
			}
		}
		
		//not doing anything with these?
		int triCount = str.readInt();
		for (int i = 0; i < triCount; ++i) {
			float px[] = new float[3];
			float py[] = new float[3];
			float tx[] = new float[3];
			float ty[] = new float[3];
			for (int j = 0; j < 3; ++j) {
				px[j] = str.readFloat();
				py[j] = str.readFloat();
				
				if (fmt >= 315) {
					tx[j] =	str.readFloat();
					ty[j] =	str.readFloat();
				}
			}
			
			//create a triangle
			if (fmt >= 315) {
				for (int j = 0; j < 3; ++j) {
					//??set tx ty
				}
			}
			//add the triangle
		}
		
		textureData.BlendMode = str.readInt();
		textureData.color = (int) str.readInt();
		textureData.XOffset = str.readFloat();
		textureData.YOffset = str.readFloat();
		textureData.Tiled = str.readBoolean();
		textureData.Scale = str.readFloat();
		
		if (fmt >= 315) {
			textureData.WorldCoords = str.readBoolean();
		} else {
			textureData.WorldCoords = false;
		}
		
		textureData.Bordered = str.readBoolean();
		textureData.BorderMax = str.readFloat();
		textureData.BorderMin = str.readFloat();
		textureData.BorderHeight = str.readFloat();
		textureData.BorderScale = str.readFloat();
		
		textureData.Shadowed = str.readBoolean();
		textureData.ShadowFollowBorder = str.readBoolean();
		textureData.ShadowColor = (int) str.readInt();
		textureData.ShadowHeight = str.readFloat();
		textureData.ShadeScale = str.readFloat();
		
	//textures
		//border texture
		int len = str.readInt();
		textureData.BorderTextureName = "";
		for (int i = 0; i < len; ++i) {
			textureData.BorderTextureName += ((char)str.readByte());
		}
		textureData.BorderTextureName = textureData.BorderTextureName.substring(0,textureData.BorderTextureName.length()-1);
		//main texture
		len = str.readInt();
		textureData.TextureName = "";
		for (int i = 0; i < len; ++i) {
			textureData.TextureName += ((char)str.readByte());
		}
		textureData.TextureName = textureData.TextureName.substring(0,textureData.TextureName.length()-1);
		//Shadow texture
		len = str.readInt();
		textureData.ShadowTextureName = "";
		for (int i = 0; i < len; ++i) {
			textureData.ShadowTextureName += ((char)str.readByte());
		}
		textureData.ShadowTextureName = textureData.ShadowTextureName.substring(0,textureData.ShadowTextureName.length()-1);
		
		entityData = new EntityData();
		entityData.load(str);
		
		calcAABB();
		
		if (fmt < 315)
			calcTexCoords(true);
		else
			calcTexCoords(false);
	}
	
	void copy(OpalShape shape) {
		shape.Borders = Borders;
		shape.Shadows = Shadows;
		shape.poly = poly;
		shape.normals = normals;
	}
	
	public boolean isStatic() {
		return false;
	}
	
	public void dispose() {
		if (mesh != null) {
			mesh.dispose();
			Borders.clear();
			Shadows.clear();
			if (textureData.MainTexture != null)
				textureData.MainTexture.dispose();
			if (textureData.BorderTexture != null)
				textureData.BorderTexture.dispose();
			if (textureData.ShadowTexture != null)
				textureData.ShadowTexture.dispose();
		}
	}
	
	void generateBorder(Array<BorderQuad> target, boolean shadow) {
		if (poly == null)
			return;
		if (!textureData.Bordered && !textureData.Shadowed)
			return;
		
		
		int vertSub = 0;
		int firstVert = 0;
		int lastVertSub = poly.getVertices().length / 2 - 1;
		int prevVertSub = lastVertSub;
		
		float perim_outer = 0;
		float perim = 0;
		float perim_inner = 0;
		float ang_diff = 0;
		
		for (;;) {
			int nextVertSub = vertSub + 1;
			if (nextVertSub > lastVertSub)
				nextVertSub = firstVert;
			
			Vector2 norm = new Vector2(0, shadow ? textureData.ShadowHeight*120 : textureData.BorderHeight*200);
			norm.rotate((normals[vertSub]*-1f - MathUtils.PI2) * MathUtils.radiansToDegrees + 90);
			
			//prevnorm
			Vector2 prevNorm = new Vector2(0, shadow ? textureData.ShadowHeight*120 : textureData.BorderHeight * 200);
			prevNorm.rotate((normals[prevVertSub]*-1f - MathUtils.PI2) * MathUtils.radiansToDegrees + 90);
			
			//next
			Vector2 nextNorm = new Vector2(0, shadow ? textureData.ShadowHeight*120 : textureData.BorderHeight * 200);
			nextNorm.rotate((normals[nextVertSub]*-1f - MathUtils.PI2) * MathUtils.radiansToDegrees + 90);
			
			Vector2 triLine = norm.cpy().sub(prevNorm);
			Vector2 triLine2 = nextNorm.cpy().sub(norm);
			
			boolean rend = false;
			if (!shadow || textureData.ShadowFollowBorder) {
				float n = normals[vertSub];
				if (textureData.BorderMin > textureData.BorderMax){ 
					if (n >= textureData.BorderMin || n <= textureData.BorderMax)
						rend = true;
				} else {
					if (n >= textureData.BorderMin && n <= textureData.BorderMax)
						rend = true;
				}
			} else {
				rend = true;
			}
			
			Polygon p = new Polygon();
			float[] verts = new float[4*2];
			
			Vector2 norNorm = norm.cpy().nor();
			verts[0] = poly.getVertices()[(2*vertSub)];
			verts[1] = poly.getVertices()[(2*vertSub)+1];
			Vector2 vertA = new Vector2(poly.getVertices()[(2*vertSub)],poly.getVertices()[(2*vertSub)+1]);
			
			verts[2] = poly.getVertices()[(nextVertSub*2)];
			verts[3] = poly.getVertices()[(nextVertSub*2)+1];
			Vector2 vertB = new Vector2(poly.getVertices()[(nextVertSub*2)],poly.getVertices()[(nextVertSub*2)+1]);
			
			
			if (!shadow) {
				verts[4] = poly.getVertices()[(nextVertSub*2)] - norm.x;
				verts[5] = poly.getVertices()[(nextVertSub*2)+1] - norm.y;
			
				verts[6] = poly.getVertices()[(vertSub*2)] - norm.x;
				verts[7] = poly.getVertices()[(vertSub*2)+1] - norm.y;
			} else {
				verts[2] -= (norm.x + triLine2.x/2)/20;
				verts[3] -= (norm.y + triLine2.y/2)/20;
				
				verts[0] -= (prevNorm.x + triLine.x/2)/20;
				verts[1] -= (prevNorm.y + triLine.y/2)/20;
				
				verts[4] = poly.getVertices()[nextVertSub*2] + norm.x + triLine2.x/2;
				verts[5] = poly.getVertices()[nextVertSub*2+1] + norm.y + triLine2.y/2;
			
				verts[6] = poly.getVertices()[vertSub*2] + prevNorm.x + triLine.x/2;
				verts[7] = poly.getVertices()[vertSub*2+1] + prevNorm.y + triLine.y/2;	
			}
			p.setVertices(verts);
			
			// add to perimeter count\
			float len = vertA.dst(vertB);
			final float oldPerim = perim;
			if (shadow && textureData.ShadowTexture != null) {
				float ratio = 0;
				if (len < textureData.ShadowTexture.getWidth()) {
					ratio = len / textureData.ShadowTexture.getWidth();
				} else {
					ratio = textureData.ShadowTexture.getWidth() / len;
				}
				perim += ratio * (textureData.ShadeScale*3.5f);//(textureData.BorderTexture.getWidth()) / (textureData.BorderScale * 3) / len;
				perim_outer += (len / textureData.ShadowTexture.getWidth());// * textureData.BorderScale;
			} else if (textureData.BorderTexture != null) {
				float ratio = 0;
				if (len < textureData.BorderTexture.getWidth()) {
					ratio = len / textureData.BorderTexture.getWidth();
				} else {
					ratio = textureData.BorderTexture.getWidth() / len;
				}
				perim += ratio * (textureData.BorderScale*3.5f);//(textureData.BorderTexture.getWidth()) / (textureData.BorderScale * 3) / len;
				perim_outer += (len / textureData.BorderTexture.getWidth());// * textureData.BorderScale;
			}
			
			if (rend) {
				Color c = shadow ? new Color(textureData.ShadowColor) : Color.WHITE.cpy();
				float al = c.a;
				float r = c.r;
				float g = c.g;
				float b = c.b;
				//RGBA
				//ABGR
				c.r = al;
				c.a = r;
				c.b = g; 
				c.g = b;
				target.add(new BorderQuad(p, c,oldPerim,perim));
			}
			
			++vertSub;
			++nextVertSub;
			++prevVertSub;
			if (prevVertSub > lastVertSub)
				prevVertSub = 0;
			if (nextVertSub == 0) //made a full cycle over the edges
				break;
			if(vertSub > lastVertSub) {
				break;
			}
		}
	}
	
	void calcAABB() {
		
	}
	
	void calcTexCoords(boolean newFormat) {
		
	}
	
	void generateMesh() {
		Borders.clear();
		Shadows.clear();
		generateBorder(Borders,false);
		generateBorder(Shadows,true);
		
		EarClippingTriangulator tri = new EarClippingTriangulator();
		ShortArray indices = tri.computeTriangles(poly.getVertices());
		
		Rectangle bnds = poly.getBoundingRectangle();
		
		Color col = new Color(textureData.color);
		float r = col.r;
		float g = col.g;
		float b = col.b;
		float a = col.a;
		col.a = r;
		col.r = a;
		col.b = g;
		col.g = b;
		if (col.r < 0.05f) col.r = 1;
		if (col.b < 0.05f) col.b = 1;
		if (col.a < 0.05f) col.a = 1;
		if (col.g < 0.05f) col.g = 1;
		mesh = new Mesh(isStatic(), isStatic(), (poly.getVertices().length/2), indices.size, 
				new VertexAttributes(
						new VertexAttribute(Usage.Position,3,"a_position"),
						new VertexAttribute(Usage.ColorPacked,4,"a_color"),
						new VertexAttribute(Usage.TextureCoordinates,2,"a_texCoords")
						));
		
		//build vertex data array
		float[] verts = new float[((poly.getVertices().length)/2) * 6];
		int vert = 0;
		for (int i = 0; i < (poly.getVertices().length/2)*6; ) {
			verts[i] = poly.getVertices()[vert];
			verts[i+1] = poly.getVertices()[vert+1];
			verts[i+2] = 0f; //Z position
			
			verts[i+3] = col.toFloatBits();
			//verts[i+3] = Color.toFloatBits(1,1,1,1);
			if (textureData.WorldCoords) {
				float factor = textureData.MainTexture.getWidth()/2;
				verts[i+4] = poly.getVertices()[vert]/factor * textureData.Scale;
				verts[i+5] = poly.getVertices()[vert+1]/factor * textureData.Scale;
			} else {
				float factor = textureData.MainTexture.getWidth()/2;
				verts[i+4] = (poly.getVertices()[vert] - bnds.x)/factor * textureData.Scale;
				verts[i+5] = (poly.getVertices()[vert+1] - bnds.y)/factor * textureData.Scale; 
			}
			
			i += 6;
			vert += 2;
		}
		
		mesh.setVertices(verts);
		mesh.setIndices(indices.toArray());
	}
	
	void loadTextures(OpalMap.TextureHandler handler) {
		if (handler != null) {
			if (textureData.TextureName != null && textureData.TextureName.length() > 0) {
				textureData.MainTexture = handler.getTexture(textureData.TextureName);
			}
			if (textureData.ShadowTextureName != null && textureData.ShadowTextureName.length() > 0 && textureData.Shadowed) {
				textureData.ShadowTexture = handler.getTexture(textureData.ShadowTextureName);
			}
			if (textureData.BorderTextureName != null && textureData.BorderTextureName.length() > 0 && textureData.Bordered) {
				textureData.BorderTexture = handler.getTexture(textureData.BorderTextureName);
			}
		}
	}
	
	public void draw(SpriteBatch batch) {
		if (mesh != null && textureData.MainTexture != null) {
			textureData.MainTexture.bind();
			if (Gdx.graphics.isGL20Available()) {
				mesh.bind(shader);
				mesh.render(GL20.GL_TRIANGLES);
			} else {
				mesh.bind();
				mesh.render(GL10.GL_TRIANGLES);
			}
		}
		
		if (Borders.size > 0 && textureData.BorderTexture != null) {
			for (int i = 0; i < Borders.size; ++i) {
				Borders.get(i).render(textureData.BorderTexture, batch);
			}
		}
		if (Shadows.size > 0 && textureData.ShadowTexture != null) {
			for (int i = 0; i < Shadows.size; ++i) {
				Shadows.get(i).render(textureData.ShadowTexture, batch);
			}
		}
	}
	
	//entities should probably override this
	public void buildColData(World world,float worldScale) {
		colData = new ColData();
		colData.bDef = new BodyDef();
		colData.fDef = new FixtureDef();
		colData.bDef.gravityScale = 0f;
		colData.bDef.type = BodyDef.BodyType.StaticBody;
		colData.fDef.isSensor = false;
		
		PolygonShape shp = new PolygonShape();
		Polygon p = new Polygon();
		p.setVertices(poly.getVertices().clone());
		p.setOrigin(0, 0);
		p.setScale(worldScale, worldScale);
		shp.set(p.getTransformedVertices().clone());
		colData.fDef.shape = shp;
		
		colData.body = world.createBody(colData.bDef);
		colData.fixture = colData.body.createFixture(colData.fDef);
		colData.fixture.setUserData(this);
	}
}
