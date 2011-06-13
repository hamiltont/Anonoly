# Builds a plot of the median versus the desired K-anonymity 
# values

# Assumes that there are variables for desired_k_anon_filtered, 
# and median_k_anon_*_***_filtered available

# Ensure the read_kdist() function is available
setwd("~/Documents/Programming/eclipse-workspace/Anonoly")
source(paste(getwd(),"/R-analysis/scripts/read-k-distributions.R",sep=""))

# Build the needed metrics
base = paste(getwd(),"/k-distributions/",sep="")
get_dist = function(name) {return(paste(base,name,sep=""))}
read_kdist(get_dist("10in15min-100x100.csv"))
read_kdist(get_dist("10in15min-100x100.csv"))
read_kdist(get_dist("10in15min-100x100.csv"))
read_kdist(get_dist("10in15min-100x100.csv"))
... (and so on)

plot(desired_k_anon_filtered,median_k_anon_1_day_filtered,type="o",col="yellow",ylim=c(0,260),xlim=c(0,100),main="Median Achieved K-anonymity vs. Desired K-anonymity",xlab="Desired K-anonymity",ylab="Median Actual K-anonymity")
points(desired_k_anon_filtered,median_k_anon_60_min_filtered,type="o",col="green")
points(desired_k_anon_filtered,median_k_anon_30_min_filtered,type="o",col="blue")
points(desired_k_anon_filtered,median_k_anon_15_min_filtered,type="o",col="purple")

# Below this line is privacy invasion
abline(a=0,b=1,col="red")

# This is the safety margin line
abline(a=0,b=3,col="black")

legend("topleft",c("Safety Margin","Privacy Margin","15 min timeslice","30 min timeslice","60 min timeslice","1 day timeslice"),col=c("black","red","purple","blue","green","yellow"),bty="n",lty=1)
