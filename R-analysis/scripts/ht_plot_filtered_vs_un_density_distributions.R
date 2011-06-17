# slice 	==	timeslice width (in any format accepted by seq.POSIXt(by=...) )
# Returns the data (that can later be passed to preread_data)
ht_plot_filtered_vs_un_density_distributions = function(wd = "~/Documents/Programming/eclipse-workspace/Anonoly", preread_data = NA, slice = "15 min") {
	
	# Ensure the plot_unfiltered and plot_filtered functions are available
	source(paste(wd,"/R-analysis/scripts/plot-unfiltered-timeslice-counts.R",sep=""))
	source(paste(wd,"/R-analysis/scripts/plot-filtered-timeslice-counts.R",sep=""))
	
	filename = paste(wd,"/sorted_data_by_mysql.tsv",sep="")
	if (is.na(preread_data))
		data = read.table(file=filename,
						sep="\t",
						col.names=c("time","id","x","y"),
						colClasses=c("integer","NULL","NULL","NULL"))
	else
		data = preread_data

	
	filt = ht_plot_filtered_timeslice_counts(plot=FALSE,preread_data = data, slice=slice)
	filt = filt[filt!=0]
	
	un = ht_plot_unfiltered_timeslice_counts(plot=FALSE,preread_data = data, slice=slice)
	un = un[un!=0]
	
	plot(density(un),main="Distribution of Rate of Incoming Data Readings",xlab="Number of Readings Entered In One Timeslice",ylab="Frequency of Number occurance")
	lines(density(filt), col="red")
	legend("topright", c("Unfiltered Data", "Filtered Data"), lty=1, col=c("black","red"),)
	
	return(data)
}

