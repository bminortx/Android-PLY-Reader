/*
 * Code to build a cube. Lots of help here from
 * http://bit.ly/1QPBhqK
*/
package com.graphics.openglgui;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Cube {
  private FloatBuffer cubeBuffer;
  private int mMVPMatrixHandle;
  private int mPositionHandle;
  private int mColorHandle;
  private int[] bufferIdx;
  // number of coordinates per vertex in this array
  static final int BYTES_PER_FLOAT = 4;
  static final int COORDS_PER_VERTEX = 3;
  static final int COORDS_PER_COLOR = 4;
  private final int stride = (COORDS_PER_COLOR + COORDS_PER_VERTEX) * BYTES_PER_FLOAT;
  static final int CUBE_VERTICES = 36;
  static float cubeCoords[] = {
          -0.5f,-0.5f, 0.5f, 1,0,0,1,
          +0.5f,-0.5f, 0.5f, 1,0,0,1,
          -0.5f,+0.5f, 0.5f, 1,0,0,1,
          -0.5f,+0.5f, 0.5f, 1,0,0,1,
          +0.5f,-0.5f, 0.5f, 1,0,0,1,
          +0.5f,+0.5f, 0.5f, 1,0,0,1,
          //  Back
          +0.5f,-0.5f,-0.5f, 0,0,1,1,
          -0.5f,-0.5f,-0.5f, 0,0,1,1,
          +0.5f,+0.5f,-0.5f, 0,0,1,1,
          +0.5f,+0.5f,-0.5f, 0,0,1,1,
          -0.5f,-0.5f,-0.5f, 0,0,1,1,
          -0.5f,+0.5f,-0.5f, 0,0,1,1,
          //  Right
          +0.5f,-0.5f,+0.5f, 1,1,0,1,
          +0.5f,-0.5f,-0.5f, 1,1,0,1,
          +0.5f,+0.5f,+0.5f, 1,1,0,1,
          +0.5f,+0.5f,+0.5f, 1,1,0,1,
          +0.5f,-0.5f,-0.5f, 1,1,0,1,
          +0.5f,+0.5f,-0.5f, 1,1,0,1,
          //  Left
          -0.5f,-0.5f,-0.5f, 0,1,0,1,
          -0.5f,-0.5f,+0.5f, 0,1,0,1,
          -0.5f,+0.5f,-0.5f, 0,1,0,1,
          -0.5f,+0.5f,-0.5f, 0,1,0,1,
          -0.5f,-0.5f,+0.5f, 0,1,0,1,
          -0.5f,+0.5f,+0.5f, 0,1,0,1,
          //  Top
          -0.5f,+0.5f,+0.5f, 0,1,1,1,
          +0.5f,+0.5f,+0.5f, 0,1,1,1,
          -0.5f,+0.5f,-0.5f, 0,1,1,1,
          -0.5f,+0.5f,-0.5f, 0,1,1,1,
          +0.5f,+0.5f,+0.5f, 0,1,1,1,
          +0.5f,+0.5f,-0.5f, 0,1,1,1,
          //  Bottom
          -0.5f,-0.5f,-0.5f, 1,0,1,1,
          +0.5f,-0.5f,-0.5f, 1,0,1,1,
          -0.5f,-0.5f,+0.5f, 1,0,1,1,
          -0.5f,-0.5f,+0.5f, 1,0,1,1,
          +0.5f,-0.5f,-0.5f, 1,0,1,1,
          +0.5f,-0.5f,+0.5f, 1,0,1,1,
  };

  public int loadShader(int type, final String shaderCode) {
    int shader = GLES20.glCreateShader(type);
    // add the source code to the shader and compile it
    GLES20.glShaderSource(shader, shaderCode);
    GLES20.glCompileShader(shader);
    int[] compiled = new int[1];
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
    if (compiled[0] == 0) {
      Log.e("Shader", "Could not compile shader " + type + ":");
      Log.e("Shader", GLES20.glGetShaderInfoLog(shader));
      GLES20.glDeleteShader(shader);
      shader = 0;
    }
    return shader;
  }

  protected String getVertexShaderCode() {
    final String code =
            "uniform mat4 uMVPMatrix;\n" +
            "attribute vec3 vPosition;\n" +
            "attribute vec4 vColor;\n" +
            "varying vec4 fColor;\n" +
            "void main() {\n" +
            "  gl_Position = uMVPMatrix * vec4(vPosition, 1);\n" +
            "  fColor = vColor;\n" +
            "}\n";
    return code;
  }

  protected String getFragmentShaderCode() {
    final String code =
            "precision mediump float;\n" +
            "varying vec4 fColor;\n" +
            "void main() {\n" +
            "  gl_FragColor = fColor;\n" +
            "}\n";
    return code;
  }

  protected void checkGlError(String TAG, String op) {
    int error;
    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
      Log.e(TAG, op + ": glError " + error);
      throw new RuntimeException(op + ": glError " + error);
    }
  }

  private final int mProgram;
  // Riffed off of the Android OpenGL ES 2.0 How-to page
  // http://bit.ly/1KVYlAx
  public Cube() {
    bufferIdx = new int[1];
    GLES20.glGenBuffers(1, bufferIdx, 0);
    cubeBuffer = ByteBuffer.allocateDirect(cubeCoords.length * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
    cubeBuffer.put(cubeCoords).position(0);
    Log.v("Cube Init", "I'm a warning!");

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferIdx[0]);
    Log.v("Cube Init", "I'm a warning! 2");
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,
            cubeBuffer.capacity() * BYTES_PER_FLOAT,
            cubeBuffer,
            GLES20.GL_STATIC_DRAW);
    Log.v("Cube Init", "I'm a warning! 3");
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    Log.v("Cube Init", "Cube bound to buffer");

    // Get our shaders ready
    int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, getVertexShaderCode());
    int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShaderCode());
    mProgram = GLES20.glCreateProgram();
    // add the vertex shader to program
    GLES20.glAttachShader(mProgram, vertexShader);
    checkGlError("Cube", "glAttachShader");

    // add the fragment shader to program
    GLES20.glAttachShader(mProgram, fragmentShader);
    checkGlError("Cube", "glAttachShader");

    GLES20.glLinkProgram(mProgram);
  }

  private final int vertexCount = 36;
  public void draw(float[] mvpMatrix) {
    // Add program to OpenGL ES environment
    GLES20.glUseProgram(mProgram);
    // Position Buffer, passed into shader
    mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
    Log.i("Draw function: ", Integer.toString(mPositionHandle));
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferIdx[0]);
    GLES20.glEnableVertexAttribArray(mPositionHandle);
    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
            GLES20.GL_FLOAT, false, stride, 0);
    // Color Buffer, passed into shader
    mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferIdx[0]);
    GLES20.glEnableVertexAttribArray(mColorHandle);
    GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_COLOR,
            GLES20.GL_FLOAT, false, stride, COORDS_PER_VERTEX * BYTES_PER_FLOAT);
    // get handle to shape's transformation matrix
    mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    // Pass the projection and view transformation to the shader
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
    // Draw the triangle
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
    // Disable vertex array
    GLES20.glDisableVertexAttribArray(mPositionHandle);
    GLES20.glDisableVertexAttribArray(mColorHandle);
    GLES20.glFlush();
  }
}
