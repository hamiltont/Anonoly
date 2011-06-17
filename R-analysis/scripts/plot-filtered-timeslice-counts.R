
# slice 	==	timeslice width (in any format accepted by seq.POSIXt(by=...) )
# wd == path to the Anonoly folder (without the trailing backslash), which allows the 
#		script to locate the input file
# plot	== 	should the plot be shown
# preread_data == the data read in describing the time and location of each AP association. Useful if you want to run these plots multiple times in a row
# 
# Plots the data, and returns the counts for all timeslices
#
ht_plot_filtered_timeslice_counts = function(slice = "15 min", wd = "~/Documents/Programming/eclipse-workspace/Anonoly", plot=TRUE, preread_data = NA) {
	filename = paste(wd,"/sorted_data_by_mysql.tsv",sep="")
	
	cat("Reading data\n")
	if (is.na(preread_data))
		data = read.table(file=filename,
						sep="\t",
						col.names=c("time","id","x","y"),
						colClasses=c("integer","NULL","NULL","NULL"))
	else
		data = preread_data

	cat("Analyzing data\n")
	
	times = data$time
	
	from_time = as.POSIXct(min(times)*0.99,origin="1970-01-01")
	to_time = as.POSIXct(max(times)*1.01,origin="1970-01-01")
	bins = seq.POSIXt(from=from_time,to=to_time,by=slice)
	
	# Remove the dates that are not in the filtered timespan
	# Explanation in R-analysis/methods
	index = as.POSIXlt(bins)$hour>=12 & 
			(as.POSIXlt(bins)$hour<18 | 
				(as.POSIXlt(bins)$hour==18 & as.POSIXlt(bins)$min==0))
	bins = bins[index]
	index = as.POSIXlt(bins)$hour!=18
	bins = bins[index]
	bins = append(bins,as.POSIXct("2003-12-09 18:00:00 EST"))
	
	should_plot = plot
	counts = hist(as.POSIXct(times,origin="1970-01-01"),breaks=bins,main="Readings Per Timeslice",xlab="Time",ylab="Number of Incoming Data Readings", freq=TRUE, plot=should_plot)$counts
	
	return(counts)
}