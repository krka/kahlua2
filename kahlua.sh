#!/bin/bash
java -cp bin/kahlua-5.1_2.0.0-core.jar:bin/kahlua-5.1_2.0.0-j2se.jar:bin/kahlua-5.1_2.0.0-interpreter.jar:interpreter/lib/jsyntaxpane-0.9.5.jar se.krka.kahlua.j2se.Kahlua "$@"

