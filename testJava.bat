javac -d . Geohexa.java
IF %ERRORLEVEL% == 0 javac -d . Run.java
IF %ERRORLEVEL% == 0 javac -d . TestGeohexa.java
IF %ERRORLEVEL% == 0 javac -d . TestRunner.java

IF %ERRORLEVEL% == 0 java geohexa.TestRunner