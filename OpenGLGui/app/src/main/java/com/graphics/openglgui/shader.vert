uniform mat4 uMVPMatrix;
attribute vec3 vPosition;
varying vec4 v Color;

void main() {
  gl_Position = uMVPMatrix * vPosition;
}
