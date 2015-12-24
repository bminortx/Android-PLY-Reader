# Android PLY Reader #

This program allows the user to view ASCII .ply files on their phone
using OpenGL ES.

## How to Use ##

This project is written with Gradle support, and does not use
JNI. This should make it a breeze to compile in Android Studio. There
is only one .ply located in memory, but the parser is not unique to
this file; the only requirement is that it be in ASCII format.

- Drag to rotate the object around the screen.
- Move two fingers towards/away each other for a scaling effect. It's
  recommended that your first finger stay in one place on the screen,
  to avoid simultaneous rotation of the object. 

And that's it! Since there are so many vertices, it takes a couple of
seconds for anything to show on the screen (~7 sec on my HTC One
M8). There are myriad improvements to be made to the app, but it's
complete enough for a proof-of-concept.
