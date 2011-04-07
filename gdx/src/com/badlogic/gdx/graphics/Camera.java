package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Base class for {@link OrthographicCamera} and {@link PerspectiveCamera}. 
 * @author mzechner
 *
 */
public abstract class Camera {				
	/** the position of the camera **/
	public final Vector3 position = new Vector3();
	/** the unit length direction vector of the camera **/
	public final Vector3 direction = new Vector3(0, 0, -1);
	/** the unit length up vector of the camera **/
	public final Vector3 up = new Vector3(0, 1, 0);
	
	/** the projection matrix **/
	public final Matrix4 projection = new Matrix4();
	/** the view matrix **/
	public final Matrix4 view = new Matrix4();
	/** the combined projection and view matrix **/
	public final Matrix4 combined = new Matrix4();
	/** the inverse combined projection and view matrix **/
	public final Matrix4 invProjectionView = new Matrix4();
	
	/** the near clipping plane distance, has to be positive **/
	public float near = 1;	
	/** the far clipping plane distance, has to be positive **/
	public float far = 100;
	
	/** the viewport width **/
	public float viewportWidth = 0;
	/** the viewport height **/
	public float viewportHeight = 0;
	
	/** the frustum **/
	public final Frustum frustum = new Frustum();
	
	private final Matrix4 tmpMat = new Matrix4();
	private final Vector3 tmpVec = new Vector3();
	
	/**
	 * Recalculates the projection and view matrix of this
	 * camera and the {@link Frustum} planes. Use this after you've manipulated
	 * any of the attributes of the camera.
	 */
	public abstract void update();
	
	/**
	 * Sets the current projection and model-view matrix of this camera.
	 * Only works with {@link GL10} and {@link GL11} of course. The parameter is there 
	 * to remind you that it does not work with GL20. Make sure to call 
	 * {@link #update()} before calling this method so all matrices are up to date.
	 * 
	 * @param gl the GL10 or GL11 instance.
	 */
	public void apply(GL10 gl) {
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadMatrixf(projection.val, 0);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadMatrixf(view.val, 0);
	}
	
	/**
	 * Recalculates the direction of the camera to look at the point
	 * (x, y, z).
	 * @param x the x-coordinate of the point to look at
	 * @param y the x-coordinate of the point to look at
	 * @param z the x-coordinate of the point to look at
	 */
	public void lookAt(float x, float y, float z) {	
		direction.set(x, y, z).sub(position).nor();
	}
	
	/**
	 * Rotates the direction and up vector of this camera by the
	 * given angle around the given axis. The direction and up
	 * vector will not be orthogonalized.
	 * 
	 * @param angle the angle
	 * @param axisX the x-component of the axis
	 * @param axisY the y-component of the axis
	 * @param axisZ the z-component of the axis
	 */
	public void rotate(float angle, float axisX, float axisY, float axisZ) {		
		tmpMat.setToRotation(tmpVec.set(axisX, axisY, axisZ), angle);
		direction.mul(tmpMat).nor();
		up.mul(tmpMat).nor();
	}
	
	/**
	 * Moves the camera by the given amount on each axis.
	 * @param x the displacement on the x-axis
	 * @param y the displacement on the y-axis
	 * @param z the displacement on the z-axis
	 */
	public void translate(float x, float y, float z) {
		position.add(x, y, z);
	}	
	
	/**
	 * Function to translate a point given in window (or window)
	 * coordinates to world space. It's the same as {@link GLU#gluUnProject(float, float, float, float[], int, float[], int, int[], int, float[], int)}
	 * but does not rely on OpenGL. The viewport is assuemd to span the whole screen
	 * and is fetched from {@link Graphics#getWidth()} and {@link Graphics#getHeight()}. The
	 * x- and y-coordinate of vec are assumed to be in window coordinates (origin is the 
	 * top left corner, y pointing down, x pointing to the right) as reported by the
	 * touch methods in {@link Input}. A z-coordinate of 0 will return a point on the
	 * near plane, a z-coordinate of 1 will return a point on the far plane. 
	 * 
	 * @param vec the point in window coordinates
	 */
	public void unproject(Vector3 vec) {
		vec.x = (2 * vec.x) / Gdx.graphics.getWidth() - 1;
		vec.y = (2 * (Gdx.graphics.getHeight() - vec.y - 1)) / Gdx.graphics.getHeight() - 1;
		vec.z = 2 * vec.z - 1;
		vec.prj(invProjectionView);
	}
	
	/**
	 * Projects the {@link Vector3} given in object/world space to window coordinates. It's the
	 * same as {@link GLU#gluProject(float, float, float, float[], int, float[], int, int[], int, float[], int)} with
	 * one small deviation:
	 * The viewport is assumed to span the whole screen. The window coordinate
	 * system has its origin in the <b>bottom</b> left, with the y-axis pointing <b>upwards</b>
	 * and the x-axis pointing to the right. This makes it easily useable in conjunction with
	 * {@link SpriteBatch} and similar classes.
	 * @param vec the position in object/world space.
	 */
	public void project(Vector3 vec) {
		vec.prj(combined);
		vec.x = Gdx.graphics.getWidth() * (vec.x + 1) / 2;
		vec.y = Gdx.graphics.getHeight() * (vec.y + 1) / 2;
		vec.z = (vec.z + 1) / 2;
	}
	
	final Ray ray = new Ray(new Vector3(), new Vector3());
	/**
	 * Creates a picking {@link Ray} from the coordinates given in window 
	 * coordinates. It is assumed that the viewport spans the whole screen.
	 * The window coordinates origin is assumed to be in the top left corner,
	 * its y-axis pointing down, the x-axis pointing to the right. The
	 * returned instance is not a new instance but an internal member only
	 * accessible via this function.
	 * 
	 * @param x the x-coordinate in window coordinates.
	 * @param y the y-coordinate in window coordinates.
	 * @return the picking Ray.
	 */
	public Ray getPickRay(float x, float y) {
		unproject(ray.origin.set(x, y, 0));
		unproject(ray.direction.set(x, y, 1));
		ray.direction.sub(ray.origin).nor();
		return ray;
	}	
}
