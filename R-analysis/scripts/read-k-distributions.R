# Used for loading in the (concise) k-distribution result data
# and exposing a number of human-readable variables for analysis
#
# Generates t_start, t_end, regions
# 	t_start - a list of POSIXct times
#	t_end	- a list of POSIXct times
#	regions	- a X by 4 numerical matrix with the cycleID, the
#			  count of uniquie users, the count of readings, 
#			  and the area of the region (in pixels for now)
#	median	- the median value of all unique user counts across
#			  all regions
#
# Return value is a list with 'tstart', 'tend', 
# '$regions', 'median'. Access these using x$regions, etc
ht_read_kdist = function(filename = file.choose()) {
	
	data = read.table(filename, 
						sep=",",
						col.names=c("t_start","t_end","regions"),
						colClasses=c("numeric","numeric","character"))


	t_start = as.POSIXct(data$t_start, origin="1970-01-01")
	t_end = as.POSIXct(data$t_end, origin="1970-01-01")

	
	file = file(filename,"r")
	total_reading_count = -1
	while(total_reading_count == -1) {
		line = readLines(file,1)
		if (suppressWarnings(is.na(as.numeric(sub("# ","",line)))) 
			== FALSE) {
			total_reading_count = as.numeric(sub("# ","",line))
			break
		}
		if (substr(line,0,1) != "#")
			stop("Unable to determine total region count\n")
	}
	if (total_reading_count == -1)
		stop("Unable to determine total region count\n")
	regions = matrix(nrow=total_reading_count,ncol=4)
	close(file)
	
	regions_temp = data$regions
	cycle_index = 1
	row_index = 1
	for(regionline in regions_temp) {
		cycle_regions = strsplit(regionline,">",fixed=TRUE)[[1]]
		for(cycle_region in cycle_regions) {
			items = strsplit(cycle_region,"|",fixed=TRUE)[[1]]
			regions[row_index, 1] = cycle_index
			regions[row_index, 2] = as.numeric(items[1])
			regions[row_index, 3] = as.numeric(items[2])
			regions[row_index, 4] = as.numeric(items[3])
			row_index = row_index + 1
		}
		cycle_index = cycle_index + 1
	}

	medianz = median(regions[,2])
  foo = regions
	
  result = list(median=medianz,tstart=t_start,tend=t_end,regions=foo)
     
	return(result)
}

	