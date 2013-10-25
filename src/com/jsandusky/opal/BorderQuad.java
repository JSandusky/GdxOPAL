package com.jsandusky.opal;

import java.io.Serializable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Polygon;

//used to render the borders and shadows
public class BorderQuad implements Serializable {
	float[] vertexData;
	
	public BorderQuad(Polygon poly, Color color, float startU, float endU) {
		build(poly,color,startU,endU);
	}
	private BorderQuad() {
		//serial
	}
	void build(Polygon poly,Color color, float startU, float endU) {
		vertexData = new float[4*5];
		float[] verts = poly.getVertices();
		vertexData[0] = verts[0];
		vertexData[1] = verts[1];
		vertexData[2] = color.toFloatBits();
		vertexData[3] = startU;
		vertexData[4] = 1f;
		
		vertexData[5] = verts[2];
		vertexData[6] = verts[3];
		vertexData[7] = color.toFloatBits();
		vertexData[8] = endU; //right
		vertexData[9] = 1f;
		
		vertexData[10] = verts[4];
		vertexData[11] = verts[5];
		vertexData[12] = color.toFloatBits();
		vertexData[13] = endU; //right
		vertexData[14] = 0f;
		
		vertexData[15] = verts[6];
		vertexData[16] = verts[7];
		vertexData[17] = color.toFloatBits();
		vertexData[18] = startU;
		vertexData[19] = 0f;
	}
	
	public void render(Texture tex, SpriteBatch batch) {
		batch.draw(tex,vertexData,0,vertexData.length);
	}
}
