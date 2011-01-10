/* 
 * Blobscanner Processing library 
 * by Antonio Molinaro - 01/01/2011.
 * The screen pixel is set after  
 * cheking if it is a blobs pixel or not.
 *
 */
import Blobscanner.*;
import processing.video.*;

Detector bd;
Capture frame;
 
 

void setup(){
  size(320, 240);
  frame = new Capture(this, width, height);
  bd = new Detector( this, 0, 0, frame.width, frame.height, 254 );
 
 
}

void draw(){
  if(frame.available()){
    frame.read();
 }
 
  frame.filter(BLUR);
  frame.filter(THRESHOLD);
  
  bd.imageFindBlobs(frame);
  bd.loadBlobsFeatures();
  
 
  for(int x = 0; x < width; x++){
    for(int y = 0; y < height; y++){  
       
      if(bd.isBlob(x, y)){
        set(x, y, color(y));
      } 
      else { 
        set(x, y,color(255-y));
      }
    }
  }
} 
