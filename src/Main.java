/**
 * @author ewenw
 * May 18, 2016
 */

import processing.core.*; 
import processing.event.*; 
import java.util.Collections; 
import java.util.Comparator; 
import java.util.ArrayList; 

public class Main extends PApplet {

	boolean display = false;
	boolean x = false;
	ArrayList<PVector> shape = new ArrayList<PVector>();
	int oldX, oldY;
	int slices = 100, rotx=0, roty=0, rotFX = 0, rotFY = 0, anchorx=-1, anchory=-1, camZ = 0, interval = 50;
	int canDraw = 0;
	float shapeVolume;
	
	public void settings() {
		size(800, 800, "processing.opengl.PGraphics3D");
	}
	
	public void setup() { 
		surface.setResizable(true);
		surface.setTitle("Visualizer");
		reset();
	}
	
	public void reset() {
		rotFX=0;
		rotFY=0;
		slices = 100;
		display = false;
		fill(20);
		canDraw = 0;
		scrX = width/2;
		shape = new ArrayList<PVector>();
		beginShape();
	}
	
	public void draw() {
		if(slices<1)slices=1;
		if(camZ>600)camZ=600;
		if(interval<10)interval = 10;
		canDraw++;
		strokeWeight(4);
		int camX = (mouseX+oldX)/2, camY = (mouseY+oldY)/2;
		if (mousePressed && !display&&canDraw>10 && (camX!=oldX || camY!=oldY))
		{
			stroke(0);
			line(camX,camY, oldX, oldY);
			shape.add(new PVector(camX, camY));

		}
		if (mousePressed && display)
		{
			if (anchorx==-1)
			{
				anchorx = mouseX;
				anchory = mouseY;
			}
			rotx = (mouseX - anchorx)/5;
			roty = (mouseY - anchory)/5;
		} else if (!mousePressed && display)
		{
			if(anchorx!=-1){
				rotFX = rotFX + rotx;
				rotFY = rotFY - roty;
				rotx = 0;
				roty = 0;
			}
			anchorx = -1;
			anchory = -1;
		}
		oldX = camX;
		oldY = camY;
		if (!display)
		{
			background(255);
			pushMatrix();
			translate(width/2,height/2);
			drawAxis();
			drawMarkers();
			popMatrix();
			drawCursor();
			fill(0, 102, 153);
			textSize(20);
			text(frameRate, 30, height-200);
			text("X or Y to start simulation", 30, height-120);
			text("SPACE to reset", 30, height-160);
			text("App by Ewen", 50, height-20);
			renderShape();
			drawButton();
		}
		if (display) {
			background(255);
			hint(ENABLE_DEPTH_TEST); 
			pushMatrix();
			translate(width/2,height/2,camZ);
			//rotate shape
			rotateY(radians(rotFX+rotx));
			rotateX(radians(rotFY-roty));
			drawAxis();
			drawMarkers();
			renderModel();
			colorMode(RGB);
			popMatrix();
			//cam.beginHUD();
			textSize(20);
			fill(0, 102, 153);
			text("Slices: " + slices, 30, height-36);
			text("Estimated Volume: " + shapeVolume, 30, height-18);
			drawScrollBar();
			drawButton();
			//cam.endHUD();
		}
		if (keyPressed)
		{
			if (key == ' ') {
				reset();
			} else if (key == 'd') {
				slices+=4;
			} else if (key == 'a') {
				slices-=4;
			} else {
				if (!display) {
					if (key == 'x' || key == 'X')
					{
						x = true;
						display = true;
						calculate(true);
					}
					if (key == 'y' || key == 'Y')
					{
						x = false;
						display = true;
						calculate(false);
					}
					endShape(CLOSE);
				}
			}
		}
	}

	public void mouseWheel(MouseEvent event) {
		float e = event.getCount();
		if(display)camZ-=e*8;
		else interval+=e;
	}

	public void drawMarkers(){
		strokeWeight(3);
		stroke(0);
		noFill();
		for(int x1=interval;x1<width;x1+=interval){
			line(x1,-5,x1,5);
			line(-x1,-5,-x1,5);
		}
		for(int y1=interval;y1<height;y1+=interval){
			line(-5,y1,5,y1);
			line(-5,-y1,5,-y1);
		}
	}
	public void renderModel(){
		float dAngle = 2.0f * PI / slices;
		float angle = 0;
		colorMode(HSB);
		for (int i=0; i < slices; i++) {
			strokeWeight(2);
			stroke(255*i/slices, 255, 255);
			noFill();
			angle += dAngle;
			pushMatrix();
			if (x) rotateX(angle);
			if (!x) rotateY(angle);
			// draw 2D shape
			beginShape();
			for (PVector v:shape) {  

				vertex(v.x-width/2, v.y-height/2, 0);
			}
			if(shape.size()>0)vertex(shape.get(0).x-width/2,shape.get(0).y-height/2,0);
			endShape();
			popMatrix();
		}
	}
	
	public void drawAxis() {
		strokeWeight(2);
		stroke(255, 0, 0);
		line(-width, 0, 0, width, 0, 0);
		stroke(0, 255, 0);
		line(0, -height, 0, 0, height, 0);
		stroke(0, 0, 255);
		line(0, 0, -width, 0, 0, width);
	}
	
	public void renderShape(){
		beginShape();
		stroke(0);
		noFill();
		for(PVector vu:shape)
		{
			vertex(vu.x,vu.y);
		}
		if(shape.size()>0)vertex(shape.get(0).x,shape.get(0).y);
		endShape();
	}

	public void drawCursor(){
		pushMatrix();
		translate(mouseX,mouseY);
		stroke(0,0,0,150);
		fill(0);
		ellipse(0,0,4,4);
		float mx = (float)(mouseX-width/2)/interval, my = (float)(-mouseY+height/2)/interval;
		text("("+String.format("%.2f", mx) + ", "+String.format("%.2f", my)+")",0,50);
		popMatrix();
	}
	
	public void drawButton(){
		int btnX = 100, btnY = 100, btnW = 120, btnH = 80;
		fill(25,255,255);
		rect(btnX,btnY,btnW,btnH);
		fill(0);
		text("Reset",btnX+btnW/2-5,btnY+btnH/2);
		fill(255);
		if(mousePressed && !drag){
			if(mouseX>btnX && mouseX <btnX+btnW && mouseY>btnY && mouseY <btnY+btnH){
				reset();
			}
		} 
	}
	
	int scrX = width/2, scrY = height/2, scrW = 180, scrH = 30;
	boolean drag = false;
	public void drawScrollBar(){
		rectMode(CENTER);
		rect(scrX,scrY,scrW,scrH);
		fill(255);
		if(mousePressed){
			if(mouseX>scrX && mouseX <scrX+scrW && mouseY>scrY && mouseY <scrY+scrH){

				drag = true;
			}
		} 
		else if(drag==true) drag = false;
		if(drag){
			scrX = mouseX;
			slices = (int)scrX/2;
		}
	}
	public void calculate(boolean x){
		if(x) mathXRot();
		else mathYRot();
	}
	public void mathXRot(){
		float totalVolume = 0;
		ArrayList<PVector> dup = new ArrayList<PVector>();
		for(PVector d:shape) dup.add(new PVector(d.x-width/2,height/2-d.y));
		Collections.sort(dup, new CompareXRot());
		for(PVector ve:dup)
			println(ve);
		int dY = 10;
		int index=0;

		for(float i = dup.get(0).y + dY; i < dup.get(dup.size()-1).y; i+=dY){
			ArrayList<Float> xValues = new ArrayList<Float>();
			for(int j= index; dup.get(j).y<i;j++)
				xValues.add(dup.get(j).x);
			Collections.sort(xValues, new CompareFloat());
			float maxX = xValues.get(xValues.size()-1), minX= xValues.get(0);
			float maxX2 = xValues.get(xValues.size()-2), minX2 = xValues.get(1);
			float volume = getCylindricalVolume(i-dY, i,(maxX+maxX2)/2, (minX+minX2)/2);
			println(i-dY + " " + i+ " " +minX+ " " + minX2 + " " + maxX + " " + maxX2 +" "+ volume);
			totalVolume += volume;
		}
		println("Volume Estimate: " + totalVolume);
	}

	public void mathYRot(){
		float totalVolume = 0;
		ArrayList<PVector> dup = new ArrayList<PVector>();
		for(PVector d:shape) 
			dup.add(new PVector(d.x-width/2,height/2-d.y));
		Collections.sort(dup, new CompareYRot());
		for(PVector ve:dup)
			println(ve);
		int dX = 10;
		int index=0;
		ArrayList<Float> yValues = new ArrayList<Float>();
		for(float i=dup.get(0).x+dX;i<dup.get(dup.size()-1).x;i+=dX){
			for(int j= index; dup.get(j).x<i;j++){
				yValues.add(dup.get(j).y);
				//if(dup.get(j).y>maxY) maxY = dup.get(j).y;
				//if(dup.get(j).y<minY) minY = dup.get(j).y;   
			}
			Collections.sort(yValues, new CompareFloat());
			if(yValues.size()>=4)
			{
				float maxY = yValues.get(yValues.size()-1), minY = yValues.get(0);
				float maxY2 = yValues.get(yValues.size()-2), minY2 = yValues.get(1);
				float volume = getCylindricalVolume(i-dX, i,(maxY+maxY2)/2, (minY+minY2)/2);
				println(i-dX + " " + i+ " " +minY + " " + minY2 + " " + maxY + " " + maxY2 +" "+ volume);
				totalVolume += volume;
				yValues = new ArrayList<Float>();
			}
		}
		shapeVolume = totalVolume;
		println("Volume Estimate: " + totalVolume);
	}
	
	public float getCylindricalVolume(float radiusInner, float radiusOuter, float y1, float y2){
		float h = Math.abs(y1-y2);
		float volume = ((float)Math.PI * radiusOuter * radiusOuter * h) - ((float)Math.PI * radiusInner * radiusInner * h);

		// convert from pixels cubed to actual units cubed;
		return Math.abs(volume) / (float)Math.pow(interval,3);
	}

	class CompareYRot implements Comparator<PVector>
	{  //@Override
		public int compare(PVector v1, PVector v2)
		{
			return PApplet.parseInt(v1.x-v2.x);
		}
	}
	
	class CompareXRot implements Comparator<PVector>
	{  //@Override
		public int compare(PVector v1, PVector v2)
		{
			return PApplet.parseInt(v1.y-v2.y);
		}
	}
	
	class CompareFloat implements Comparator<Float>
	{  //@Override
		public int compare(Float v1, Float v2)
		{
			return PApplet.parseInt(v1-v2);
		}
	}

	public static void main(String[] args) {
		String[] a = {"Volumes of Rotation Visualizer"};
		PApplet.runSketch( a, new Main());
	}


}
