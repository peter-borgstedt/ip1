LIBRARIES=lib
OUTPUT=bin

clean:
	@rm -rf ${OUTPUT}

all: clean
	@mkdir ${OUTPUT}
	@echo "Compiling src/**/*.java ..."
	@javac -g src/**/*.java -cp :./${LIBRARIES}/* -d ${OUTPUT};
	@echo "Done! Run using command: java -cp lib/*:bin <class-name>"
