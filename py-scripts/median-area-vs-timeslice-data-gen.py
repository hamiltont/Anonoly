
import os

k = [5,15,30,55,80,115,150]
ts = [10,30]

for i in range(100,2500,100):
	ts.append(i)

# /System/Library/Frameworks/JavaVM.framework/Versions/1.4/Home/bin/java -Djava.util.logging.config.file=/Users/hamiltont/Documents/Programming/eclipse-workspace/Anonoly/logging.properties -Danonoly.K=10 -Danonoly.DatasetRange=kapadia -Danonoly.Timeslice=1200 -Dfile.encoding=MacRoman -classpath /Users/hamiltont/Documents/Programming/eclipse-workspace/Anonoly/bin turnerha.Main

for kval in k:
	for time in ts:
		command = '/System/Library/Frameworks/JavaVM.framework/Versions/1.4/Home/bin/java -Djava.util.logging.config.file=/Users/hamiltont/Documents/Programming/eclipse-workspace/Anonoly/logging.properties'
		command += ' -Danonoly.K='
		command += str(kval)
		command += ' -Danonoly.DatasetRange=kapadia'
		command += ' -Danonoly.Timeslice='
		command += str(time * 60)
		command += ' -Dfile.encoding=MacRoman -classpath /Users/hamiltont/Documents/Programming/eclipse-workspace/Anonoly/bin turnerha.Main'
		print command
		os.system(command)

