


#################################################################################
#                         W H A T   I S   T H I S ?
#
# Hackerrank runs this when you submit your code. Safe to ignore.
# 
# To use a something other than Maven (example: Gradle) customize the line below.
#################################################################################


# generate a score even for an empty project, inspired by https://stackoverflow.com/a/3856879/152061
num_java_files=`find src/main/java -name "*.java" 2>/dev/null | wc -l`
if [[ $num_java_files -eq 0 ]]; then
  echo "FS_SCORE:0%"
else


  ##################################################
  # To use a tool other than Maven, change this line
  ##################################################
  mvn clean verify


  if [[ $? -eq 0 ]]; then
    echo "FS_SCORE:100%"
  else
    echo "FS_SCORE:0%"
  fi
fi
