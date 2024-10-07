
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import Model.VboCube;
import obj.Cubo3D;
import obj.ObjModel;
import obj.Object3D;
import shaders.StaticShader;
import util.TextureLoader;

import java.awt.image.BufferedImage;

//import com.sun.org.apache.xerces.internal.dom.DeepNodeListImpl;

import java.nio.*;
import java.util.ArrayList;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main3D {

	// The window handle
	private long window;
	
	float angTankViewX = 0;
	float angTankViewY = 0;
	
	float angTowerViewY = 0;
	
	float angCanonViewZ = 0;
	
	public Random rnd = new Random();
	
	VboCube vboc;
	StaticShader shader;
	ArrayList<Object3D> listaObjetos = new ArrayList<>();
	

	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(800, 600, "Hello World!", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated
		// or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		
			if ( key == GLFW_KEY_W) {
				angTankViewX+=5;
			}
			if ( key == GLFW_KEY_S) {
				angTankViewX-=5;
			}
			if ( key == GLFW_KEY_A) {
				angTankViewY+=5;
			}
			if ( key == GLFW_KEY_D) {
				angTankViewY-=5;
			}
			
			if ( key == GLFW_KEY_N) {
				angTowerViewY+=5;
			}
			if ( key == GLFW_KEY_M) {
				angTowerViewY-=5;
			}
			
			if ( key == GLFW_KEY_G) {
				angCanonViewZ+=5;
			}
			if ( key == GLFW_KEY_B) {
				angCanonViewZ-=5;
			}
		
		});

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}

	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.

		GL.createCapabilities();

		
		
		BufferedImage imggato = TextureLoader.loadImage("texturaGato.jpeg");
		
		vboc = new VboCube();
		vboc.load();
		shader = new StaticShader();
		
		//Cubo3D cubo = new Cubo3D(0.0f, 0.0f, -1.0f, 0.2f);
		//cubo.vbocube = vboc;
		
		ObjModel x35 = new ObjModel();
		x35.loadObj("x-35_obj.obj");
		x35.load();

		for(int i = 0; i < 500; i++) {
			//Cubo3D cubo = new Cubo3D(rnd.nextFloat()*2-1,rnd.nextFloat()*2-1, rnd.nextFloat()*2-1, rnd.nextFloat()*0.005f+0.0001f);
			//cubo.model = x35;
			Cubo3D cubo = new Cubo3D(rnd.nextFloat()*2f-1,rnd.nextFloat()*2f-1, rnd.nextFloat()*1.1f-1, rnd.nextFloat()*0.1f+0.05f);
			cubo.model = vboc;
			cubo.vx = rnd.nextFloat()*0.4f-0.2f;
			cubo.vy = rnd.nextFloat()*0.4f-0.2f;
			cubo.vz = rnd.nextFloat()*0.4f-0.2f;
			cubo.rotvel = rnd.nextFloat()*9;
			cubo.mass = rnd.nextFloat()*0.9f+0.1f; // Random mass between 0.1 and 1.0
			listaObjetos.add(cubo);
		}

		BufferedImage gatorgba = new BufferedImage(imggato.getWidth(), imggato.getHeight(), BufferedImage.TYPE_INT_ARGB);
		gatorgba.getGraphics().drawImage(imggato, 0, 0, null);
		int tgato = TextureLoader.loadTexture(imggato);
		System.out.println("tgato "+tgato);
		
		/*BufferedImage imgx35 = TextureLoader.loadImage("x35text.jpg");
		int tx35 = TextureLoader.loadTexture(imgx35);
		System.out.println("tx35 "+tx35);

		// Set the clear color
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);



		ObjModel tankObj = new ObjModel();
		tankObj.loadObj("tank.obj");
		
		ObjModel mig29 = new ObjModel();
		mig29.loadObj("Mig_29_obj.obj");
		
		ObjModel x35 = new ObjModel();
		x35.loadObj("x-35_obj.obj");
		
		
		ArrayList<objeto3D> lista = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			objeto3D obj = new objeto3D(x35);
			obj.x = (rnd.nextFloat()*10)-5;
			obj.y = (rnd.nextFloat()*10)-5;
			obj.z = (-(rnd.nextFloat()*5))-4;
			lista.add(obj);
		}
		
		*/
		
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.

		int frame = 0;
		long lasttime = System.currentTimeMillis();

		float angle = 0;
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(45, 600f / 800f, 0.5f, 100);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		
		FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);
		long ultimoTempo = System.currentTimeMillis();
		while (!glfwWindowShouldClose(window)) {
			
			long diftime = System.currentTimeMillis()-ultimoTempo;
			ultimoTempo = System.currentTimeMillis();
			
			for(int i = 0; i < listaObjetos.size();i++) {
				listaObjetos.get(i).SimulaSe(diftime);
			}

			handleCollisions(listaObjetos);
			
			angle+=0.1;
			
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			glEnable(GL_LIGHTING);
			glShadeModel(GL_SMOOTH);

			//float senoang = (float) Math.sin(Math.toRadians(angle));
			//float cosang = (float) Math.cos(Math.toRadians(angle));
			glLoadIdentity();

			float[] lightAmbient = { 0.1f, 0.1f, 0.1f, 0.5f };
			float[] lightDiffuse = { 0.5f, 0.5f, 0.5f, 0.5f };
			float[] lightPosition = { 0.0f, 0.0f, 0.0f, 1.0f };
			//float[] lightPosition = { 0.0f, senoang * 600f, cosang * 600f, 1.0f };

			glLightfv(GL_LIGHT0, GL_AMBIENT, lightAmbient);
			glLightfv(GL_LIGHT0, GL_DIFFUSE, lightDiffuse);
			glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);

			float[] lightAmbient2 = { 0.0f, 0.0f, 0.0f, 1.0f };
			float[] lightDiffuse2 = { 1.0f, 1.0f, 1.0f, 1.0f };
			float[] lightPosition2 = { -5f, -5f, 0f, 1.0f };

			shader.start();
			
			glEnable(GL_DEPTH_TEST);
			
	        glActiveTexture(GL_TEXTURE0);
	        glBindTexture(GL_TEXTURE_2D, tgato);
	        
	        int loctexture = glGetUniformLocation(shader.programID, "tex");
	        glUniform1i(loctexture, 0);
			
			
			
			Matrix4f view = new Matrix4f();
			view.setIdentity();
			view.translate(new Vector3f(0,0,0));
			int viewlocation = glGetUniformLocation(shader.programID, "view");
			
			view.storeTranspose(matrixBuffer);
			matrixBuffer.flip();
			glUniformMatrix4fv(viewlocation, false, matrixBuffer);
			
			
			for(int i = 0; i < listaObjetos.size();i++) {
				listaObjetos.get(i).DesenhaSe(shader);
			}
			
			
			shader.stop();
			
			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
			frame++;
			long actualTime = System.currentTimeMillis();
			if ((lasttime / 1000) != (actualTime / 1000)) {
				System.out.println("FPS " + frame);
				frame = 0;
				lasttime = actualTime;
			}

		}
	}

	private void handleCollisions(ArrayList<Object3D> objects) {
		for (int i = 0; i < objects.size(); i++) {
			for (int j = i + 1; j < objects.size(); j++) {
				Object3D obj1 = objects.get(i);
				Object3D obj2 = objects.get(j);
				if (checkCollision(obj1, obj2)) {
					resolveCollision(obj1, obj2);
				}
			}
		}
	}

	private boolean checkCollision(Object3D obj1, Object3D obj2) {
		float dx = obj1.x - obj2.x;
		float dy = obj1.y - obj2.y;
		float dz = obj1.z - obj2.z;
		float DistanciaQuadrada = dx * dx + dy * dy + dz * dz;
		float SomaDosRaios = obj1.raio + obj2.raio; // Assuming each object has a radius attribute
		return DistanciaQuadrada < SomaDosRaios * SomaDosRaios;
	}

	private void resolveCollision(Object3D obj1, Object3D obj2) {
		Vector3f collisionNormal = new Vector3f(obj2.x - obj1.x, obj2.y - obj1.y, obj2.z - obj1.z);
		collisionNormal.normalise();

		Vector3f relativeVelocity = new Vector3f(obj2.vx - obj1.vx, obj2.vy - obj1.vy, obj2.vz - obj1.vz);
		float velocityAlongNormal = Vector3f.dot(relativeVelocity, collisionNormal);

		if (velocityAlongNormal > 0) {
			return; // The objects are moving away from each other
		}

		float restitution = 1.0f; // Coefficient of restitution (elastic collision)

		float impulseMagnitude = -(1 + restitution) * velocityAlongNormal;
		impulseMagnitude /= (1 / obj1.mass + 1 / obj2.mass);

		Vector3f impulse = new Vector3f(collisionNormal);
		impulse.scale(impulseMagnitude);

		obj1.vx -= (1 / obj1.mass) * impulse.x;
		obj1.vy -= (1 / obj1.mass) * impulse.y;
		obj1.vz -= (1 / obj1.mass) * impulse.z;

		obj2.vx += (1 / obj2.mass) * impulse.x;
		obj2.vy += (1 / obj2.mass) * impulse.y;
		obj2.vz += (1 / obj2.mass) * impulse.z;
	}

	public static void main(String[] args) {
		new Main3D().run();
	}

	public static void gluPerspective(float fovy, float aspect, float near, float far) {
		float bottom = -near * (float) Math.tan(fovy / 2);
		float top = -bottom;
		float left = aspect * bottom;
		float right = -left;
		glFrustum(left, right, bottom, top, near, far);
	}
}
