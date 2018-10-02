#!/bin/bash

java -Djava.library.path=../../lib/lwjgl-2.9.1/native/linux -cp ../../lib/lwjgl-2.9.1/jar/lwjgl.jar:../../lib/lwjgl-2.9.1/jar/lwjgl_util.jar:../../lib/jpct/jpct.jar:fps.jar -Xmx128m JPCTDemo width=640 height=480 mipmap zbuffer=16 refresh=60

