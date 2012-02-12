# Copyright 2000-2001 by Michael Alyn Miller
# Post Office Box 2304, Cupertino, California, 95015-2304, U.S.A.
# All rights reserved.

JAR = jar
JAVAC = javac -source 1.2 -target 1.1 -d ./classes
JAVADOC = javadoc

VERSION = 1.0.4

cdb:
	@if [ ! -d ./classes ]; then mkdir ./classes; fi
	$(JAVAC) -classpath ./java \
		./java/com/strangegizmo/cdb/Cdb.java \
		./java/com/strangegizmo/cdb/CdbMake.java \
		./java/cdb/dump.java \
		./java/cdb/get.java \
		./java/cdb/make.java

.PHONY: jar
jar: cdb
	$(JAR) cvfM0 sg-cdb-$(VERSION).jar -C classes .

.PHONY: doc
doc:
	@if [ ! -d ./doc ]; then mkdir ./doc; fi
	$(JAVADOC) -d doc -sourcepath java -classpath classes cdb com.strangegizmo.cdb

.PHONY: zip
zip: jar doc
	rm -rf releases/sg-cdb-$(VERSION)
	rm -f releases/sg-cdb-$(VERSION).zip
	mkdir releases/sg-cdb-$(VERSION)

	cp -R ChangeLog.txt releases/sg-cdb-$(VERSION)
	cp -R LICENSE releases/sg-cdb-$(VERSION)
	cp -R sg-cdb-$(VERSION).jar releases/sg-cdb-$(VERSION)
	cp -R java releases/sg-cdb-$(VERSION)
	cp -R doc releases/sg-cdb-$(VERSION)

	jar cvfM releases/sg-cdb-$(VERSION).zip -C releases sg-cdb-$(VERSION)

	rm -rf releases/sg-cdb-$(VERSION)

.PHONY: release
release: clean cdb jar doc zip

clean:
	rm -rf ./classes
	rm -rf ./doc
	rm -f sg-cdb-$(VERSION).jar
