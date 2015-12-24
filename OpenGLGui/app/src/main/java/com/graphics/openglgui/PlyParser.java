/*
  PLYParser: A reader for .ply files
  See the below links for info:
  http://paulbourke.net/dataformats/ply/
  http://stackoverflow.com/questions/6420293/reading-android-raw-text-file
 */

package com.graphics.openglgui;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PlyParser {
  // Parser mechanisms
  private BufferedReader bufferedReader;
  private final int NO_INDEX = 100;
  private int vertexIndex = NO_INDEX;
  private int colorIndex = NO_INDEX;
  private int normalIndex = NO_INDEX;
  private boolean inHeader = true;
  public int currentElement = 0;
  public int currentFace = 0;
  /* data fields to store points, colors, faces information read from PLY file */
  public float[] vertices = null;
  public float[] colors = null;
  public float[] normals = null;
  public int[] faces = null;
  // Size of an individual element, in floats
  public int vertexSize = 0;
  public int colorSize = 0;
  public int normalSize = 0;
  public int faceSize = 3;
  // Normalizing constants
  public float vertexMax = 0;
  public float colorMax = 0;
  // Number of elements in the entire PLY
  public int vertexCount = 0;
  public int faceCount = 0;
  // Counter for header
  private int elementCount = 0;

  public PlyParser(InputStream plyFile) {
    bufferedReader = new BufferedReader(new InputStreamReader(plyFile));
  }

  boolean ParsePly() throws IOException {
    // Check if this is even a PLY file.
    String line = bufferedReader.readLine();
    if(!line.equals("ply")) {
      Log.e("ReadHeader", "File is not a PLY! Leave us.");
      return false;
    }

    // Check for ASCII format
    line = bufferedReader.readLine();
    String words[] = line.split(" ");
    if(!words[1].equals("ascii")) {
      Log.e("ReadHeader", "File is not ASCII format! Cannot read.");
      return false;
    }

    // Read the header
    line = bufferedReader.readLine();
    while (line != null && inHeader) {
      ReadHeader(line);
      line = bufferedReader.readLine();
    }

    // Populate the data
    if (vertexSize != 3) {
      Log.e("ParsePly", "Incorrect count of vertices! Expected 3.");
      return false;
    }
    vertices = new float[vertexCount * vertexSize];
    faces = new int[faceCount * faceSize];
    if (colorSize != 0) { colors = new float [vertexCount * colorSize]; }
    if (normalSize != 0) { normals = new float [vertexCount * normalSize]; }
    line = bufferedReader.readLine();
    while (line != null) {
      ReadData(line);
      line = bufferedReader.readLine();
    }
    ScaleData();
    return true;
  }

  void ReadHeader(String line) {
    // Make into a list of words, yo.
    String words[] = line.split(" ");
    if(words[0].equals("comment")) { return; }
    // Check if element or property
    if (words[0].equals("element")) {
      if (words[1].equals("vertex")) {
        vertexCount = Integer.parseInt(words[2]);
      } else if (words[1].equals("face")) {
        faceCount = Integer.parseInt(words[2]);
      }
    }
    if (words[0].equals("property")) {
      if (words[2].equals("x") ||
              words[2].equals("y") ||
              words[2].equals("z")) {
        if (vertexIndex > elementCount) { vertexIndex = elementCount; }
        vertexSize++;
      } else if (words[2].equals("nx") ||
              words[2].equals("ny")||
              words[2].equals("nz")) {
        if (normalIndex > elementCount)  { normalIndex = elementCount; }
        normalSize++;
      } else if (words[2].equals("red") ||
              words[2].equals("green") ||
              words[2].equals("blue") ||
              words[2].equals("alpha")) {
        if (colorIndex > elementCount) { colorIndex = elementCount; }
        colorSize++;
      }
      elementCount++;
    }

    if (words[0].equals("end_header")) {
      inHeader = false;
      return;
    }
  }

  void ReadData(String line) {
    String words[] = line.split(" ");
    // Compensate for extra line read with (vertexCount - 1)
    if (currentElement < vertexCount - 1) {
      for (int i = 0; i < vertexSize; i++) {
        vertices[currentElement * vertexSize + i] = Float.parseFloat(words[vertexIndex + i]);
        if (vertexMax < Math.abs(vertices[currentElement * vertexSize + i])) {
          vertexMax = Math.abs(vertices[currentElement * vertexSize + i]);
        }
      }
      for (int i = 0; i < colorSize; i++) {
        colors[currentElement * colorSize + i] = Float.parseFloat(words[colorIndex + i]);
        if (colorMax < colors[currentElement * colorSize + i]) {
          colorMax = colors[currentElement * colorSize + i];
        }
      }
      for (int i = 0; i < normalSize; i++) {
        normals[currentElement * normalSize + i] = Float.parseFloat(words[normalIndex + i]);
      }
      currentElement++;
    } else if (currentFace < faceCount) {
      for (int i = 0; i < 3; i++) {
        faces[currentFace * faceSize + i] = Integer.parseInt(words[i + 1]);
      }
      currentFace++;
    }
  }

  void ScaleData() {
    for (int i = 0; i < vertexCount * vertexSize; i++) {
      vertices[i] /= vertexMax;
    }
    for (int i = 0; i < vertexCount * colorSize; i++) {
      colors[i] /= colorMax;
    }
  }

  // Getters
  public float[] getVertices() { return vertices; }

  public int getVertexCount() {
    return vertexCount;
  }

  public float[] getColors() {
    return colors;
  }

  public float[] getNormals() {
    return normals;
  }

  public int getFaceCount() {
    return faceCount;
  }

  public int[] getFaces() {
    return faces;
  }
}
