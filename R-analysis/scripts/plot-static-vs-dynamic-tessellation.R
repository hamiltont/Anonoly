# Read in the data (do this once for static, once for dynamic)
static_data = ht_read_kdist()

sregions = static_data$regions
tstart = static_data$tstart

static_k_median = aggregate(sregions[,2],list(sregions[,1]), median)[,2]
static_k_25 = aggregate(x=sregions[,2],
                        by=list(sregions[,1]),
                        FUN=quantile, 
                        probs=0.25)$x
static_k_75 = aggregate(x=sregions[,2],
                        by=list(sregions[,1]), 
                        FUN=quantile, 
                        probs=0.75)$x


dynamic_data = ht_read_kdist()

dregions = dynamic_data$regions

dynamic_k_median = aggregate(dregions[,2],
                             list(dregions[,1]),
                             median)[,2]
dynamic_k_25 = aggregate(x=dregions[,2],
                        by=list(dregions[,1]),
                        FUN=quantile, 
                        probs=0.25)$x
dynamic_k_75 = aggregate(x=dregions[,2],
                        by=list(dregions[,1]), 
                        FUN=quantile, 
                        probs=0.75)$x


plot(smooth(static_k_median),
     type="l",
     xlab="Time", 
     ylab="Median K-Anonymity Of Regions", 
     xaxt="n",
     lwd=2)
lines(smooth(dynamic_k_median), 
      lwd=2,
      lty=3)

# Draw the safety vs privacy lines
abline(h=c(15,45), lty=3)

# find fridays
fri = format(time, format="%w")==5

# manually grab the index of each friday (could be automated, it's not my priority)
which(fri, arr.ind=TRUE)
fris = c(1,88,176,264,352,440)

# Draw friday lines
abline(v=fris, lty=3)

# Build the axis
time_axis = time[fris]
axis(1, at=fris, labels=format(time_axis, format="%a %d-%m"))

# Add the legend
legend("bottom", 
       legend=c("Static Tessellation", "Dynamic Tessellation"), 
       lty=c(1, 3), 
       lwd=2)


# Cut the first week out
w1_dynamic = dynamic_k_median[1:87]
w1_static = static_k_median[1:87]
w1_time = time[1:87]

plot(smooth(w1_static),
     type="l",
     xlab="Time", 
     ylab="Median K-Anonymity Of Regions", 
     xaxt="n",
     lwd=2, 
     ylim = range(smooth(w1_static), smooth(w1_dynamic)))
lines(smooth(w1_dynamic), 
      lwd=2,
      lty=3)

# find days
dys = as.numeric(format(w1_time, format="%d"))

# Grab the index of each weekday
days = c(1, which(dys != dys[-1]) + 1)


# Draw weekday lines
abline(v=days, lty=3)

# Build the axis
time_axis = time[days]
axis(1, at=days, labels=format(time_axis, format="%m-%d"))

# Draw the safety vs privacy lines
abline(h=c(15, 45), lty = c(4, 5), lwd = 1)

# Add the legend
legend("bottom", 
       legend=c("Static Tessellation", "Dynamic Tessellation"), 
       lty=c(1, 3),
       lwd=2)


# Determine the data fidelity for the static vs dynamic
sw1_static = smooth(w1_static)
fidelity = sum(sw1_static[which((sw1_static - 45) > 0)] - 45)
sw1_dynamic = smooth(w1_dynamic)
fidelity2 = sum(sw1_dynamic[which((sw1_dynamic - 45) > 0)] - 45)

# Visually indicate the data fidelity
y = pmax(sw1_static,45)
y = append(y, c(45, 45))
x = seq(1,87,1)
x = append(x, c(88, 1))
polygon(x,y=y,density=20,angle=45, lty=3)
y = pmax(sw1_dynamic, 45)
y = append(y, 45)
polygon(x,y=y,density=20,angle=-45)




