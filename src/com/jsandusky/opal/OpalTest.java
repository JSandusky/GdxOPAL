package com.jsandusky.opal;

import java.util.HashMap;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.jsandusky.opal.OpalMap.TextureHandler;

public class OpalTest implements ApplicationListener {
	OrthographicCamera camera;
	SpriteBatch sb;
	OpalMap map;
	
	@Override
	public void create() {
		map = new OpalMap(null);
		try {
			map.load(Gdx.files.internal("data/test.map"));
			
			map.construct(new TextureHandler() {
				HashMap<String,Texture> cache = new HashMap<String,Texture>();
				@Override
				public Texture getTexture(String name) {
					if (cache.containsKey(name))
						return cache.get(name);
					Texture tex = new Texture(Gdx.files.internal(name));
					tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
					tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
					cache.put(name, tex);
					return tex;
				}
				
			});
		} catch (Exception e) {
			Gdx.app.log("Opal", "MapLoad",e);
		}
		// TODO Auto-generated method stub
		camera = new OrthographicCamera();
		camera.viewportHeight = Gdx.graphics.getWidth();
		camera.viewportWidth = Gdx.graphics.getHeight();
		camera.update();
		
		Gdx.input.setInputProcessor(new InputProcessor() {

			@Override
			public boolean keyDown(int arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean keyTyped(char arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean keyUp(int arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean mouseMoved(int arg0, int arg1) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean scrolled(int arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean touchDown(int arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean touchDragged(int x, int y, int ptr) {
				OpalTest.this.camera.position.x = x;
				OpalTest.this.camera.position.y = y;
				OpalTest.this.camera.update();
				return false;
			}

			@Override
			public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {
				Vector3 pt = new Vector3(arg0,arg1,0);
				OpalTest.this.camera.unproject(pt);
				OpalTest.this.camera.position.x = pt.x;
				OpalTest.this.camera.position.y = pt.y;
				OpalTest.this.camera.update();
				return false;
			}
			
		});
		
		sb = new SpriteBatch();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClearColor(0, 0, 0, 0);
		gl.glClear(GL10.GL_DEPTH_BUFFER_BIT | GL10.GL_COLOR_BUFFER_BIT);
		gl.glLineWidth(10);
		
		sb.setProjectionMatrix(camera.combined);
		sb.begin();
			map.draw(sb);
		sb.end();
	}

	@Override
	public void resize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

}
