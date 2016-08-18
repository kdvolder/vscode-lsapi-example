# Installs locally
# You will need java, maven, vsce, and visual studio code to run this script

# Build fat jar
mvn package 

# Build vsix
vsce package -o build.vsix

# Install vsix
code build.vsix