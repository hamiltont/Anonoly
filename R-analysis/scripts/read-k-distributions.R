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

cat("Please choose a file from the k-distributions directory\n")

filename = file.choose()
cat("Reading data\n")
data = read.table(filename, sep=",",col.names=c("t_start","t_end","regions"),colClasses=c("numeric","numeric","character"))


cat("Parsing Date\n")
t_start = as.POSIXct(data$t_start, origin="1970-01-01")
t_end = as.POSIXct(data$t_end, origin="1970-01-01")

cat("Allocating space for Regions\n")
file = file(filename,"r")
rm(filename)
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
rm(line)
rm(total_reading_count)

cat("Parsing Regions\n")
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
rm(regions_temp)
rm(cycle_index)
rm(row_index)
rm(regionline)
rm(items)
rm(cycle_regions)
rm(cycle_region)
rm(file)
rm(data)

median = median(regions[,2])


	