# wd == path to the Anonoly folder (without the trailing backslash), which allows the 
#		script to locate the k-distribution output files
# k == the desired k-anonymity values. These will be used to define the series, e.g. 
#		there will be a K=5 series, a K=10, etc, with the axis being time interval and
#		median area
# times == the time regions in minutes
# suffix == an suffix on the filenames (if there is anything between the hyphen and the 
# 			dot)
#
# Returns a matrix of "matrix(nrow=length(k),ncol=length(times),dimnames=list(k,times))"
# that contains the median region size of all regions created for that K at that time slice
# size. Also plots the data
ht_plot_region_size_vs_time = function(wd = "~/Documents/Programming/eclipse-workspace/Anonoly", k= c(5,10,15,20,30,40,55,80,115,150), times=c(15,30,60,60*24), suffix = "100x100") {
	
	# Ensure the read_kdist() function is available
	source(paste(wd,"/R-analysis/scripts/read-k-distributions.R",sep=""))
	
	# We want to have multiple series, series are separated by their k-anon value
	# The x's of each series should be the time intervals, the y's of each series 
	# should be the median areas squared
	
	# The data struct we are using is a 2D matrix where the columns indicate the 
	# time, the rows indicate the k-value, and the value indicates the median
	# size
	data = matrix(nrow=length(k),ncol=length(times),dimnames=list(k,times))

	
	for (kval in k) {
		for (timeval in times) {
			if (timeval == 60*24)
				filename = paste(wd,"/k-distributions/",kval,"in1day-",suffix,".csv",sep="")
			else
				filename = paste(wd,"/k-distributions/",
								kval,"in",timeval,"min-",suffix,".csv",sep="")
			cat("Reading ", filename, "\n")
			
			regions = read_kdist(filename)
			
			data[as.character(kval),as.character(timeval)] = median(regions[,4])
		}	
	}
	
	# Setup plot parameters
	y_lim = c(min(data) * 0.95, max(data) * 1.05)
	colors = rainbow(length(k))
	plot(times,data[1,],xlab="Time Slice Length (minutes)",
		ylab="Median Area (sq. pixels)",type="o",ylim=y_lim,
		col=colors[1],main="Median Area vs Time Interval")
	
	pos = 1
	for (kval in k) {
		points(times,data[as.character(kval),],type="o",col=colors[pos])
		pos=pos+1
	}
	
	
	legend("topright",lty=1,lwd=3,legend=
			paste(k,"Anonymity",sep="-"),col=colors,bty="n")
	
	return(output)
	
}
