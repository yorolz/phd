#!/bin/bash

java -Djava.library.path=../../lib/lwjgl-2.9.1/native/linux -Xmx128m -classpath classes:../../lib/jpct/jpct.jar:../../lib/lwjgl-2.9.1/jar/lwjgl.jar:../../lib/lwjgl-2.9.1/jar/lwjgl_util.jar HelloWorldOGL

