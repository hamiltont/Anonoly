# Divides the entire sensing window into a number of sections
# of sizes equal to slice, an attempts to find a set of
# sections that are contigious and *most* different, where
# different is determined by counting the number of incoming
# data readings and finding contigious sections where this 
# number changes most drastically
#
# slice == the timeslice to use
# count == the number of contigious sections that we want to find 
# plotdc == plot the difference counts
# plotans == plot the 'answer' e.g. the max difference
ht_locate_volatile_data = function(slice = "30 min", 
                                   count = 3, 
                                   wd = "~/Documents/Programming/eclipse-workspace/Anonoly", 
                                   preread_data = NA,
                                   plotdc=FALSE,
                                   plotans=TRUE) {
	
  count = max(count,1)
  filename = paste(wd,"/sorted_data_by_mysql.tsv",sep="")
  
	if (is.na(preread_data)) {
		cat("Reading data\n")
		data = read.table(file=filename,
						sep="\t",
						col.names=c("time","id","x","y"),
						colClasses=c("integer","NULL","NULL","NULL"))
	}
	else
		data = preread_data

	times = data$time
	
	from_time = as.POSIXct(min(times)*0.995,origin="1970-01-01")
	to_time = as.POSIXct(max(times)*1.005,origin="1970-01-01")
	bins = seq.POSIXt(from=from_time,to=to_time,by=slice)
	
	counts = hist(as.POSIXct(times,origin="1970-01-01"),breaks=bins, freq=TRUE, plot=FALSE)$counts

  end = length(counts) - count
  current = 1
  diffcounts = vector(length=end)
	while (current != end)
	{
    
    lastval = -1
    diffsum = 0
    for (val in counts[current:(current + count)])
    {
      if (lastval == -1)
      {
        lastval = val
        next
      }
      
      diffsum = diffsum + abs(val - lastval)
      lastval = val
    }
    
    diffcounts[current] = diffsum
    current = current + 1
	}
  
  index = which.max(diffcounts)
  if (plotdc)
    plot(diffcounts,type="l")

  if (plotans)
    plot(counts[index:(index + count)],type="l")

  return(counts[index:(index + count)])
}