javac ../1.1.0/ThreadTest.java -d .
jar cmf manifest.txt ThreadTest.jar *.class
rm *.class
