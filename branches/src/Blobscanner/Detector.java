/**
 *
 * This library is for the Processing programming environment
 * and can be used for blob detection and analysis in
 * any type of image and video (included live stream
 * from web cam or other video sources). It's useful, also
 * for image labelling and segmentation.
 * This is the first public release of the library source code
 * which is still under development,
 * so please if you find a bug or if you have a suggestion
 * drop me a line at blobdetector.info@gmail.com or visit the
 * project site http://sites.google.com/site/blobscanner/home.
 * 
 * @version 0.1
 * @author Antonio Molinaro
 * @date 30-12-2010
 *
 */
package Blobscanner;

import processing.core.*;



public class Detector {

private PApplet p5;

/** The brightness value ( 0 - 255 ) of the blobs searched . */
private int threshold;

/** The image or video frame labels buffer. */
private int[][] MyLabels;

/** The pixel group buffer (blob or not-blob pixels). */
private int[][] MyGroup;
/** Holds the total blob pixels. */
private int countBlobPixel;

/** Neighbours x coordinates of labelling kernel.*/
private int[] dx = {-1, 0, 1,-1};

/** Neighbours y coordinates of labelling kernel.*/
private int[] dy = {-1,-1,-1, 0};

/**Kernel of neighbours x coordinates for contours detection.*/
private int xkern[] = {1, 0, -1, 0, 1, -1, -1, 1};

/** Kernel of neighbours y coordinates for contours detection.*/
private int ykern[] = {0, -1, 0, 1, -1, -1, 1, 1};

/** Holds the blob's labels, one for each label. */
private int labelTab2[];

/** The label */
private int Label;

/** Holds the x coordinates of the blob edge pixels and the relative blob number*/
private int[][] plotX;

/** Holds the y coordinates of the blob edge pixels and the relative blob number*/
private int[][] plotY;

/** Holds the x coordinates of the blob edge pixels */
private int[] edgeX;

/** Holds the y coordinates of the blob edge pixels */
private int[] edgeY;

/** The width of the rectangular area scanned for blobs. */
private int w;

/** The height of the rectangular area scanned for blobs. */
private int h;

/** The x coordinate of the top left corner of the rectangular area scanned for blobs.*/
private int sx;

/** The y coordinate of the top left corner of the rectangular area scanned for blobs.*/
private int sy;

/** The total blobs number for the current frame. */
private int blobNumber;

/** The total number of pixels belonging to blobs in the current frame. */
private int totalBlobsWeight;

/** Holds the list of the weight of each blob in the current frame. */
private int blobWeightList[];

/** The the coordinates of the four corners of the blob's bounding box.
 *
 *         A---B
 *         |   |
 *         C---D
 */
private PVector[] A;
private PVector[] B;
private PVector[] C;
private PVector[] D;

/** The list of x coordinates of the blobs center of mass. */
private float []CenterOfMX ;

/** The list of y coordinates of the blobs center of mass. */
private float []CenterOfMY ;

/** The list of coordinates  of the the center point on the blob's bounding box side. */
private PVector[][] crosspoints ;


private int Ax ;
private int Ay ;
private int Bx ;
private int By ;
private int Cx ;
private int Cy ;
private int Dx ;
private int Dy ;


/**
 * <code> Detector </code> class constructor.
 * @param p5 The PApplet instance.
 * @param sx The x coordinate of the top left corner of the rectangular area scanned for blobs.
 * @param sy The y coordinate of the top left corner of the rectangular area scanned for blobs.
 * @param w  The width of the rectangular area scanned for blobs.
 * @param h  The height of the rectangular area scanned for blobs.
 * @param threshold Blobscanner will search for blobs which brightness is bigger
 * than threshold and smaller than threshold + 8.
 */

public Detector(PApplet p5, int sx, int sy, int w, int h, int threshold) {

MyLabels = new int[h][w];
MyGroup = new int[h][w];

this.threshold = threshold;
this.p5 = p5;
this.sx = sx;
this.sy = sy;
this.w = w;
this.h = h;

}

/**
 * Finds all the blobs pixels and save them into the
 * pixel group buffer <code>MyGroup</code>.
 * @param pix_array The screen buffer to scan for blobs.
 * @param trueWidth The width of the image or video to search for blobs.
 * @param trueHeight The height of the image or video to search for blobs.
 *
 *
 */
    public void findBlobs(int[] pix_array, int trueWidth, int  trueHeight ) {

       countBlobPixel = 0;
       if(threshold > 255)threshold = 255;
       if(threshold < 0)threshold = 0;

        for (int y = sy; y < h; y++) {
            for (int x = sx; x < w; x++) {
                //if is blob label with 1
                if (PApplet.abs(p5.brightness(pix_array[x + y * trueWidth])) > threshold
                        && PApplet.abs(p5.brightness(pix_array[x + y * trueWidth])) < threshold + 2 ) {
                    MyGroup[y][x] = 1;
                    countBlobPixel++;//total blob's pixels
                } else {
                    MyGroup[y][x] = 0;
                }
            }
        }

        doLabel();
        createLabelTab();

    }


    
/**
 * Fills the pixels group buffer in case you
 * need work on a PImage object.
 * @param img PImage to scan for blobs.
 */
    public void imageFindBlobs(PImage img ) {

        countBlobPixel = 0;
        if(threshold > 255)threshold = 255;
        if(threshold < 0)threshold = 0;

        for (int y = sy; y < h; y++) {
            for (int x = sx; x < w; x++) {
                //if is blob label with 1
                if (PApplet.abs(p5.brightness(img.get(x, y))) > threshold
                        && PApplet.abs(p5.brightness(img.get(x, y))) < threshold + 2 ) {
                    MyGroup[y][x] = 1;
                    countBlobPixel++;//increment total blob's pixels
                } else {
                    MyGroup[y][x] = 0;
                }
            }
        }

        doLabel();
        createLabelTab();

    }


/**
 * Initializes the labels buffer.
 */
        private void initMyLabels() {

            for (int y = sy; y < h; y++) {
                for (int x = sx; x < w; x++) {
                    MyLabels[y][x] = 0;
                }
            }
        }



/**
 *  Compute the connected components and
 *  fills the labels buffer.
 */
    private void doLabel() {

    initMyLabels();
    Label = 1;//start labelling from 1
    int sumTest = 0;



/**
 * The smallest label found locally
 *  when <code>dx[]</code> and <code>dy[]</code>
 *  kernels  are applied on the pixel.
 */
        int smallLabel = 0;

        for (int y = sy  ; y < h  ; y++) {
            for (int x = sx  ; x < w  ; x++) {

                if(x < w-1 && x >0 && y <=  h-1  && y > 0){
                //if is a blob pixel apply kernel
                if (MyGroup[y][x] == 1)
                {
                    for (int i = 0; i < 4; i++)
                    {
                        sumTest += MyLabels[y + dy[i]][x + dx[i]];
                    }
                    //if the local sum is 0
                    if (sumTest == 0) {
                        //assign new label
                        MyLabels[y][x] = Label;
                        //increment label
                        Label++;
                    } else {
                        //reset for next pixel
                        sumTest = 0;
                        //find the smollest label...
                        int labelLocalList[] = new int[4];

                        for (int i = 0; i < 4; i++)
                        {
                            labelLocalList[i] = MyLabels[y + dy[i]][x + dx[i]];
                        }
                        labelLocalList = PApplet.sort(labelLocalList);
                        labelLocalList = res3(labelLocalList);
                        smallLabel = labelLocalList[0];

                        //label the matrix space...
                        for (int i = 0; i < 4; i++) {
                            if (MyGroup[y + dy[i]][x + dx[i]] == 1) {
                                MyLabels[y + dy[i]][x + dx[i]] = smallLabel;
                            }
                        }
                        //..and finally assign the the labe to the pixel @ (x,y) position
                        MyLabels[y][x] = smallLabel;
                    }
                }
            }
        }
     }
        //repeat in inverse direction
        for (int y = h  ; y > sy  ; y--) {
            for (int x = w  ; x > sx  ; x--) {

                if(x < w-1 && x >0 && y <=  h-1  && y > 0){

                if (MyLabels[y][x] > 0) {
                    int labelLocallList_2[] = new int[4];
                    for (int i = 0; i < 4; i++) {
                        labelLocallList_2[i] = MyLabels[y + dy[i]][x + dx[i]];
                    }
                    labelLocallList_2 = PApplet.sort(labelLocallList_2);
                    labelLocallList_2 = res3(labelLocallList_2);
                    smallLabel = labelLocallList_2[0];

                    for (int i = 0; i < 4; i++) {
                        if (MyLabels[y + dy[i]][x + dx[i]] != 0 && MyGroup[y][x] == 1) {
                            MyLabels[y + dy[i]][x + dx[i]] = smallLabel;
                        }
                    }
                }
            }
        }
     }
  }



/**
 * Creates a buffer to hold a label for each blob.
 */

    private void createLabelTab() {
        int index = 0;
        if (countBlobPixel > 0) {
            int labelTab[] = new int[countBlobPixel];
            totalBlobsWeight = 0;

            for (int y = sy; y < h; y++) {
                for (int x = sx; x < w; x++) {
                    if (MyGroup[y][x] == 1 && MyLabels[y][x] > 0) {

                        labelTab[index] = MyLabels[y][x];
                        index++;

                    }

                }
            }
            totalBlobsWeight = index;
             index = 0;


            labelTab = PApplet.sort(labelTab);
            labelTab2 = res3(labelTab);
            blobNumber = labelTab2.length;

        }
    }



/**
 * Calculates the total blobs weight (mass)
 * for each frame or image. This method must be called
 * before calling <code>getBlobWeight(int blobNum)</code> ,
 * <code>getBlobWeightLabel(int label)</code>  ,
 * <code>drawSelectBox(int minimumWeight,int boxColor,float thickness) </code>
 * and <code>drawSelectContours(int minimumWeight,int contourColor,float thickness)</code> methods.
 * @param printsConsoleIfZero
 * If <code> true,</code> the methods will
 * print a message to the console if
 * zero blobs have been found.
 */
  public void  weightBlobs(boolean printsConsoleIfZero)
   {


     int  bPix = 0;




     if(totalBlobsWeight > 0){

     blobWeightList = new int[blobNumber];

     boolean cerca = true;

    for(int i = 0; i < blobNumber; i++)
    {

     for(int y = sy + 2; y < h - 2; y++)
     {
      for(int x = sx + 2; x < w - 2; x++)
       {

           if(labelTab2[i] == MyLabels[y][x]){
           bPix++;


           cerca = true;
           }else if(cerca){


           blobWeightList[i] += bPix;


           cerca = false;

           bPix=0;


                 }
             }
         }
     }
 }
     else
     {
       if( printsConsoleIfZero)
       PApplet.println(" *** zero blobs found ***");
     }
  }



/**
 * Returns true if the pixel at x y coordinates is a blob's edge pixel.
 * @param x The screen x coordinate to check.
 * @param y The screen y coordinate to check.
 */

  public boolean isEdge(int x, int y){

  int count = 0;

          if(MyLabels[y][x]>0 )

                for(int s = 0; s < 8; s++)
                {
                 if( MyLabels[ y + ykern[s] ][ x + xkern[s] ]==0)
                {
                     count++;
                }
              }
          if(count>0) return true;
          else
          return false;
         }



/**
 * Draws the edges of each blob.
 * @param contoursColor The edges color. Here it can be passed color( r, g, b)
 * Processing function.
 * This parameter is passed to <code>stroke</code> Processing function.
 * @param thickness The edges thickness. This parameter is passed
 * to <code>strokeWeight</code> Processing function.
 */

  public void drawContours(int contoursColor, float thickness) {
        int i = 0;


        for (int y = sy + 2; y < h - 2; y++) {
            for (int x = sx + 2; x < w - 2; x++) {

                if (MyLabels[y][x] > 0 && isEdge(x, y)) {

                    p5.stroke(contoursColor);
                    p5.strokeWeight(thickness);

                    p5.point(x, y);

                    i++;
                }
             }
          }

        //reset to default strokeWeight and stroke
        p5.stroke(0);
        p5.strokeWeight(1);
    }



/**
 * Draws the blob's contours only for blob
 * which weight is bigger than or equals to
 * <code> minimumWeight</code> parameter value.
 * Before call this method <code> weightBlobs(boolean) </code>
 * and <code>loadBlobsFeatures() </code> methods must be called.
 * @param  minimumWeight The minimum weight of the blob which contours are drawn
 * @param  contoursColor The edges color. Here it can be passed color( r, g, b)
 * Processing function.
 * @param  thickness The edges thickness. This parameter is passed
 * to <code>strokeWeight</code> Processing function.
 * @see #loadBlobsFeatures()
 * @see #weightBlobs(boolean)
 **/
  public void drawSelectContours(int minimumWeight,int contoursColor,float thickness)////draw contours of blobs by weight
  {
     int i = 0;

        for (int y = sy + 2; y < h - 2; y++) {
            for (int x = sx + 2; x < w - 2; x++) {

                if (MyLabels[y][x] > 0 && isEdge(x, y) && getBlobWeightLabel(getLabel(x, y))>= minimumWeight) {

                    p5.stroke(contoursColor);
                    p5.strokeWeight(thickness);

                    p5.point(x, y);

                    i++;
                }
             }
          }


   
       //reset to default strokeWeight and stroke
        p5.stroke(0);
        p5.strokeWeight(1);
}



/**
 * Draws the blobs bounding box.
 * @param boxColor The bounding box color. Here it can be passed color( r, g, b)
 * Processing function.
 * This parameter is passed to <code>stroke</code> Processing function.
 * @param thickness The bounding box thickness. This parameter is passed
 * to <code>strokeWeight</code> Processing function.
 */

    public void drawBox(int boxColor,float thickness)
     {
        p5.stroke(boxColor);
        p5.strokeWeight(thickness);
        if(blobNumber>0){
          for(int i = 0; i < blobNumber; i++){
           p5.line(getA()[i].x,getA()[i].y,getB()[i].x,getB()[i].y);
           p5.line(getB()[i].x,getB()[i].y,getD()[i].x,getD()[i].y);
           p5.line(getA()[i].x,getA()[i].y,getC()[i].x,getC()[i].y);
           p5.line(getC()[i].x,getC()[i].y,getD()[i].x,getD()[i].y);
        }
      }
         //reset to default strokeWeight and stroke
        p5.stroke(0);
        p5.strokeWeight(1);
     }



/**
 * Draws the blob's bounding box only for blob
 * which weight is bigger than or equals to
 * <code> minimumWeight</code> parameter value.
 * Before to call this method <code> weightBlobs()
 * </code> and <code>loadBlobsFeatures() </code> methods must be called.
 *
 * @param  minimumWeight The minimum weight of the blob which bounding box is drawn
 * @param  boxColor The bounding box color. Here it can be passed color( r, g, b)
 * Processing function.
 * @param  thickness The edges thickness. This parameter is passed
 * to <code>strokeWeight</code> Processing function.
 * @see #loadBlobsFeatures()
 * @see #weightBlobs(boolean)
 */

  public void drawSelectBox(int minimumWeight,int boxColor,float thickness)//new drawBox(color,int) 7/12/2010
     {
        p5.stroke(boxColor);
        p5.strokeWeight(thickness);
        if(blobNumber>0){
          for(int i = 0; i < blobNumber; i++){
           if(getBlobWeight(i)>=minimumWeight){
           p5.line(getA()[i].x,getA()[i].y,getB()[i].x,getB()[i].y);
           p5.line(getB()[i].x,getB()[i].y,getD()[i].x,getD()[i].y);
           p5.line(getA()[i].x,getA()[i].y,getC()[i].x,getC()[i].y);
           p5.line(getC()[i].x,getC()[i].y,getD()[i].x,getD()[i].y);
        }
      }
     }
         //reset to default strokeWeight and stroke
        p5.stroke(0);
        p5.strokeWeight(1);
   }




/**
 * Removes all 0s values from <code> arra</code>
 * condensing the rest of the values.
 * @param arra The array to process.
 * @return a The new array with the condensed values.
 */

    private static int[] res3(int[] arra) {
        int[] ar = new int[arra.length];
        int index = 0;

        for (int i = 0; i < arra.length; i++) {

            int t = arra[i];
            if (i != arra.length - 1) {
                if (arra[i] != 0 && arra[i + 1] != t) {
                    ar[index] = arra[i];
                    index++;
                }
            }
            ar[index] = arra[arra.length - 1];
        }

        int[] a = new int[1];
        for (int i = 0; i < ar.length; i++) {
            if (ar[i] != 0) {
                a = new int[i + 1];
                //Need to copy manually.
                for (int j = 0; j < a.length; j++) {
                    a[j] = ar[j];
                }

            }
        }
        return a;
    }
/**
 * Returns true if at x y location finds a blob pixels.
 * @param x The x location of the pixel to check.
 * @param y The y location of the pixel to check.
 * @return Returns true if at x y location finds a blob pixels.
 */
    public boolean isBlob(int x, int y) {
        if (MyLabels[y][x] > 0) {
            return true;
        } else {
            return false;
        }
    }


/**
 * Inizializes four PVector arrays of four elements each
 * to hold the blob's bounding box corners coordinates vectors.
 */

     private void initCornersVectors() {
        A = new PVector[getBlobsNumber()];
        B = new PVector[getBlobsNumber()];
        C = new PVector[getBlobsNumber()];
        D = new PVector[getBlobsNumber()];
    }



/**
 * Computes the blob's features.
 */
      private void computeBlobsFeatures()
      {

        initCornersVectors();
        crosspoints = new PVector[getBlobsNumber()][4];

        //If we have blobs...
        if(getBlobsNumber() > 0){
        plotX = new int[blobNumber][totalBlobsWeight];
        plotY = new int[blobNumber][totalBlobsWeight];
        //For each blob...
        for(int k=0;  k <labelTab2.length; k++){
         int i=0;
        //Inizialize two arrays to hold the blob's edges point coordinates.
        edgeX = new int[totalBlobsWeight];
        edgeY = new int[ totalBlobsWeight];


      //For each pixel
      for(int y= sy+2; y<h-2 ; y++){

         for(int x= sx+2; x<w-2 ; x++){

            //If a pixel is labelled...
            if(MyLabels[y][x]>0)
            {
            //If the labe is the same as the current blob's label..
            if( MyLabels[y][x]==labelTab2[k] && isEdge(x, y)){

                 //Save its coordinates

                 edgeY[i]=y ;
                 edgeX[i]=x ;
                 plotX[k][i] = x;
                 plotY[k][i] = y;

             // ..and increment for the next edge point..
               i++;
            }
          }
        }
      }

    edgeX =  PApplet.sort(edgeX);
    edgeY =  PApplet.sort(edgeY);

    edgeX = res3(edgeX);
    edgeY = res3(edgeY);

    int minimx = edgeX[0];
    int maximx = edgeX[edgeX.length-1];
    int minimy = edgeY[0];
    int maximy = edgeY[edgeY.length-1];


    Ax = minimx;   Ay = minimy;
    Bx = maximx;   By = minimy;
    Cx = minimx;   Cy = maximy;
    Dx = maximx;   Dy = maximy;


    A[k] = new PVector(Ax, Ay);
    B[k] = new PVector(Bx, By);
    C[k] = new PVector(Cx, Cy);
    D[k] = new PVector(Dx, Dy);
 

    crosspoints[k][0] = new PVector(((Bx-Ax)/2)+Ax,Ay);
    crosspoints[k][1] = new PVector( ((Bx-Ax)/2)+Ax,Cy);
    crosspoints[k][2] = new PVector(Ax, ((Cy-Ay)/2)+Ay);
    crosspoints[k][3] = new PVector(Bx,((Cy-Ay)/2)+Ay);
     }
  }
}


/**
 * This method must be called before any call to <code>getA,
 * getB, getC, getD, getBlobWidth, getBlobHeight, getBoxCentX, getBoxCentY,
 * getCentroidX, getCentroidY, getcrossPoints, getEdgeX, getEdgeY, getEdgeXY,
 * findCentroids.</code>
 * @see #getA()
 * @see #getB()
 * @see #getC()
 * @see #getD()
 * @see #getBoxCentX(int)
 * @see #getBoxCentY(int)
 * @see #getBlobWidth(int)
 * @see #getBlobHeight(int)
 * @see #findCentroids(boolean, boolean)
 * @see #getCentroidX(int)
 * @see #getCentroidY(int)
 * @see #getEdgeX(int, int)
 * @see #getEdgeY(int, int)
 * @see #getEdgeXY(int)
 * @see #getCrossPoints(int, int, boolean)
 *
 */

    public void loadBlobsFeatures() {

     computeBlobsFeatures();

    }



/**
 * Returns the blob width.
 * To call this method,
 * the <code>loadBlobsFeatures()
 * </code> method must be called first.
 * @param blobnumber
 * @return
 * @see #loadBlobsFeatures()
 */
   public int getBlobWidth(int blobnumber)
   {
     if(blobNumber> 0)
     return (int)( B[ blobnumber].x - A [blobnumber].x)+1 ;
     else return  0;

   }



/**
 * Returns the blob height.
 * To call this method,
 * the <code>loadBlobsFeatures()
 * </code> method must be called first.
 * @param blobnumber
 * @return
 * @see #loadBlobsFeatures()
 */

   public int getBlobHeight(int blobnumber)
   {
     if(blobNumber> 0)
     return (int)( getC()[blobnumber].y -  getA()[blobnumber].y) +1;
     else return  0;
   }


/**
 * Calculates the blob centroids.
 * Before calling this method <code>weightBlobs(boolean)</code>
 * and <code>loadBlobsFeatures()</code> methods
 * must be called first. Also this method must be called
 * always before calling<code>getCentroidX(int)<,/code> and
 * <code>getCentroidY(int)</code> methods.
 * @param printMessage If true prints to the console
 * the blob's centroid coordinate x y.
 * @param drawCentroids If true draws a point at the centroid
 * coordinates.
 * @see #getCentroidX(int)
 * @see #getCentroidY(int)
 * @see #weightBlobs(boolean)
 * @see #loadBlobsFeatures()
 */

   public void findCentroids(boolean printMessage, boolean drawCentroids){


    int countX = 0;
    int countY = 0;
    float [] X ;     //coordinate along bounding box width
    float [] Y ;     //coordinate along bounding box height
    float [] lx;     //number of blob pixel per row
    float [] ly;     //number of blob pixel per colon
    float [] sumX  ; // all the products of x times blob pixel per row
    float [] sumY  ;
    float centx = 0;
    float centy = 0;
    CenterOfMX = new float[blobNumber];
    CenterOfMY = new float[blobNumber];


    for(int k=0;  k < blobNumber; k++){

      countX = 0;
      countY = 0;
    //if(getBlobWeight(k)>=1000){//noise filter da implementare



    X = new float   [getBlobWidth(k)];//coordinate along bounding box width
    Y = new float   [getBlobHeight(k)];//coordinate along bounding box height
    lx = new float  [getBlobHeight(k)] ;//number of blob pixel per row
    ly = new float  [getBlobWidth(k)] ;//number of blob pixel per colon
    sumX = new float[getBlobWidth(k)]; //all the products of x and y coordinates
    sumY = new float[getBlobHeight(k)];//for the number of pixel for each row and colon
    
     for(float x =  getA()[k].x  ; x <= getB()[k].x  ; x++){




    X[countX] = x;
    countX++;





    }

  for(float y =  getA()[k].y   ; y <=  getC()[k].y   ; y++){



       Y[countY] = y;
       countY++;


  }
   int cx = 0;

  for(float y = getA()[k].y ; y <=  getC()[k].y ; y++){

       countX=0;

        for(float x = getA()[k].x ; x <=  getB()[k].x ; x++){
              if(isBlob((int)x,(int)y)){

              countX++;


              }
        }
        lx[cx]=countX;
       cx++;

  }

   int cy =0;

   for(float x = getA()[k].x  ; x <=  getB()[k].x  ;x++){

      countY=0;

       for(float y =  getA()[k].y  ; y <=  getC()[k].y  ; y++){
        if(isBlob((int)x,(int)y)){

           countY++;

         }
       }
       ly[cy]=countY;
       cy ++;
   }





    float sumx = 0;
    float sumy = 0;
    centx = centy = 0;

 for(int i = 0; i < getBlobWidth(k); i++){
   sumX[i] = X[i]*ly[i];
   sumx +=  sumX[i];
 }
 for(int i = 0; i < getBlobHeight(k);i++){
   sumY[i] = Y[i] *lx[i];
   sumy +=   sumY[i];
 }


  centx = sumx/getBlobWeight(k);
  centy = sumy/getBlobWeight(k);

  if(drawCentroids)
  p5.point(centx, centy);

  if(printMessage)
  PApplet.print("blob " +(k+1) +" centroid coord. are :cent x = " + centx + " cent y = " + centy + " \n");

  CenterOfMX[k] = centx;
  CenterOfMY[k] = centy;

     }
    }



/**
 *
 *
 * Returns the blob centroid x coordinate.
 * This method must be call after the <code>
 * findCentroids(boolean printMessage)</code>
 * methods has been called.
 *
 *
 * @param blobnumber The blob for which the centroid is returned.
 * @return the blob centroid x coordinates.
 * @see findCentroids(boolean printMessage)
 */

   public float getCentroidX(int blobnumber)
   {
     if(blobnumber <= CenterOfMX.length-1)//work around : check why out of boun 1
     return CenterOfMX[blobnumber];
     else return -10f;

   }



/**
 *
 *
 * Returns the blob  centroid y coordinate.
 * This method must be call after the <code>
 * findCentroids(boolean printMessage)</code>
 * methods has been called.
 *
 *
 * @param blobnumber The blob for which the centroid is returned.
 * @return the blob centroid y coordinates.
 * @see findCentroids(boolean printMessage)
 */

   public float getCentroidY(int blobnumber)
   {
     if(blobnumber <= CenterOfMY.length-1)//work around : check why out of boun 1
     return CenterOfMY[blobnumber];
     else return -10f;
   }



/**
 * The total number of blobs in the current
 * frame or image.
 * @return The total number of blobs in the current
 * frame or image.
 */

    public int getBlobsNumber() {
        return blobNumber;
    }



/**
 * Get the total weight for all  the blobs in the current
 * frame or image.
 * @return The total weight for all  the blobs in the current
 * frame or image.
 */

    public int getGlobalWeight() {
        return totalBlobsWeight;
    }


/**
 * Returns the weight
 * of the blob
 * specified. Must be called after
 * <code>weightBlob(boolean printsConsoleIfZero)</code>.
 * @param  blobNum The number of the blob which you need
 * to get the weight for. The first blob is numbered with 0.
 */
    public int getBlobWeight(int blobNum) {
        return blobWeightList[blobNum];
    }

/**
 * Returns the weight
 * of the blob with the
 * specified label.
 *
 *
 * The first label is numbered with 1.
 * The labels are not consecutive. You can use
 * <code>getLabel(int blobnumber) </code> or
 * <code> getLabel(int x, int y)</code>
 * to find the blob's label number.
 * This method must be called after <code>
 * weightBlob(boolean)
 * </code> method has been called.
 * @param  label The label of the blob which
 * you need to get the weight for.
 * The labels start from 1 not 0.
 * @return The  weight values of the blob
 * labelled with  <code>label</code>.
 * @see #weightBlobs(boolean)
 */
    public int getBlobWeightLabel(int label) {
        int cnt = 0;
        while (labelTab2[cnt] != label) {
            cnt++;
        }
        return blobWeightList[cnt];
    }


/**
 * Returns an array of PVectors
 * which contains all the left upper corners
 * of the blobs bounding boxes.
 * To call this method,
 * the <code>loadBlobsFeatures()
 * </code> method must be called first.
 * @return A An array of PVectors
 * which contains all the left upper corners
 * of the blobs bounding boxes.
 *
 */
public PVector[] getA() {
    return A;
}

/**
 * Returns an array of PVectors
 * which contains all the right upper corners
 * of the blobs bounding boxes.
 * To call this method,
 * the <code>loadBlobsFeatures()
 * </code> method must be called first.
 * @return B An array of PVectors
 * which contains all the right upper corners
 * of the blobs bounding boxes.
 */
public PVector[] getB() {
    return B;
}

/**
 * Returns an array of PVectors
 * which contains all the left lower corners
 * of the blobs bounding boxes.
 * To call this method,
 * the <code>loadBlobsFeatures()
 * </code> method must be called first.
 * @return C  An array of PVectors
 * which contains all the left lower corners
 * of the blobs bounding boxes.
 */
public PVector[] getC() {
    return C;
}

/**
 * Returns an array of PVectors
 * which contains all the right lower corners
 * of the blobs bounding boxes.
 * To call this method,
 * the <code>loadBlobsFeatures()
 * </code> method must be called first.
 * @return D An array of PVectors
 * which contains all the right lower corners
 * of the blobs bounding boxes.
 */
public PVector[] getD() {
    return D;
}


/**
 *
 * Returns the blob's label
 * at  <code> x, y </code>screen  coordinates.
 * If at that location there is no blob
 * returns 0.
 * @param x The x screen coordinates.
 * @param y The y screen coordinates.
 * @return The value of the label at x y;
 *
 */

public int getLabel(int x, int y) {
return MyLabels[y][x];
}



/**
 *
 * Returns the blob's label
 * of the blob indicated by the <code>blobnumber</code> parameter.
 * <code>blobnumber</code> can be from 0 to <code>blobNumbers</code>
 * or from 0 to <code>computeBlobsFeatures()</code>.
 * If you have 5 blobs, <code>blobnumber</code> = 4 returns
 * the label of the 5th blob.
 * @param blobnumber The blob which label is returned.
 * @return Blob's label.
 *
 */
public int getLabel(int blobnumber) {
    if (blobNumber > 0) {
        return labelTab2[blobnumber];
    } else {
        return 0;
    }
}


/**
 *
 * Returns edge x coordinate
 * of the current blob.
 * To call this method,
 * the <code>loadBlobsFeatures()
 * </code> method must be called first.
 * @param blobnumber The blob which edges are computed.
 * @param index The edge point ( starts from 0 ).
 * @return  x coordinates
 * of the edge pixels.
 */
public int getEdgeX(int blobnumber, int index) {
    return plotX[blobnumber][index];
}

/**
 *
 * Returns edge y coordinate
 * of the current blob.
 * To call this method,
 * the <code>loadBlobsFeatures()
 * </code> method must be called first.
 *
 * @param blobnumber The blob which edges are computed.
 * @param index The edge point ( starts from 0 ).
 * @return  y coordinates
 * of the edge pixels.
 */
public int getEdgeY(int blobnumber, int index) {
    return plotY[blobnumber][index];
}

/**
 *
 * Returns a PVector object representing the
 * coordinates x y of a blob edge pixel.
 * To call this method, the <code>loadBlobsFeatures()
 * </code> method must be called first.
 * @param index The edge pixel number,
 * in reference to the total number edges pixel.
 * @return  A PVector containing the coordinates
 * x y of a blob edge pixel.
 */
public PVector getEdgeXY(int index) {
    PVector[] edgeXY = new PVector[totalBlobsWeight];

    int count = 0;

    for (int i = 0; i < blobNumber; i++) {
        for (int j = 0; j < totalBlobsWeight; j++) {

            if (getEdgeX(i, j) != 0 && getEdgeY(i, j) != 0) {


                edgeXY[count] = new PVector(getEdgeX(i, j), getEdgeY(i, j));
                count++;
            }
        }
    }
    return edgeXY[index];
}


/**
 * Returns the x coordinates of the
 * bounding box center.To call this method,
 * the <code>loadBlobsFeatures()
 * </code> method must be called first.
 * @param blobnumber The blob number ( starts from 0 ).
 * @return x the x coordinates of the
 * bounding box center.
 * @see  #loadBlobsFeatures()
 */

public float getBoxCentX(int blobnumber){
float w_ =  getB()[blobnumber].x -  getA()[blobnumber].x;
float x_ = w_/2  +  getA()[blobnumber].x ;
return x_;
}


/**
 * Returns the y coordinates of the
 * bounding box center.To call this method,
 * the <code>loadBlobsFeatures()
 * </code> method must be called first.
 * @param blobnumber The blob number ( starts from 0 ).
 * @return y the y coordinates of the
 * bounding box center.
 *
 * @see  #loadBlobsFeatures()
 *
 */

public float getBoxCentY(int blobnumber){
float h_ =  getC()[blobnumber].y -  getA()[blobnumber].y;
float y_ = h_/2 +  getA()[blobnumber].y;
return  y_;
}

/**
 * Finds the coordinates of the center point of the
 * blob's bounding box side. To call this method, the <code>loadBlobsFeatures()
 * </code> and <code>weightBlobs(boolean)</code> methods must be called first.
 * @param blobnumber The blob you need to compute the points for.
 * @param pointVector The point you need extract( 0 | 1 | 2 | 3 ).
 * The order of the vectors in the array is : 0 = top, 1 = down, 2 = left, 3 = right.
 * @param draw_them If true draws the four points.
 * @return The PVector containing  the  point coordinates (x, y).
 * @see #loadBlobsFeatures()
 * @see #weightBlobs(boolean)
 */

public PVector getCrossPoints(int blobnumber, int pointVector,boolean draw_them){



  if(blobNumber > 0 && draw_them){

     for(int k=0;  k < blobNumber; k++){


     p5.point(((B[k].x-A[k].x)/2)+A[k].x,A[k].y);// top
     p5.point(((B[k].x-A[k].x)/2)+A[k].x,C[k].y);// down
     p5.point(A[k].x,((C[k].y-A[k].y)/2)+A[k].y);// left
     p5.point(B[k].x,((D[k].y-B[k].y)/2)+B[k].y);// right
     }
  }


   return crosspoints[blobnumber][pointVector];
 }
    }




